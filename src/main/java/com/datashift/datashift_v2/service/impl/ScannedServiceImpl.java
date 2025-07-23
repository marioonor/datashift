package com.datashift.datashift_v2.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datashift.datashift_v2.dto.main.ScannedDTO;
import com.datashift.datashift_v2.entity.main.ScannedEntity;
import com.datashift.datashift_v2.entity.users.UserEntity;
import com.datashift.datashift_v2.exceptions.ResourceNotFoundException;
import com.datashift.datashift_v2.repository.main.ScannedRepository;
import com.datashift.datashift_v2.repository.users.UserRepository;
import com.datashift.datashift_v2.service.main.ScannedService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScannedServiceImpl implements ScannedService {

    private final ScannedRepository scannedRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ScannedDTO> getAllScannedData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("User not authenticated. Cannot fetch scanned data.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        UserEntity authenticatedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Authenticated user not found in database: " + email));

        List<ScannedEntity> userScannedData = scannedRepository.findByUserId(authenticatedUser.getId());
        log.info("Found {} scanned data entries for user {}", userScannedData.size(), email);

        List<ScannedDTO> listOfDataScanned = userScannedData.stream()
                .map(scannedEntity -> mapToScannedDTO(scannedEntity))
                .collect(Collectors.toList());
        return listOfDataScanned;

    }

    @Override
    public ScannedDTO getScannedDataById(Long scannedId) {
        ScannedEntity optionalScannedData = getScannedEntity(scannedId);
        return mapToScannedDTO(optionalScannedData);
    }

    @Override
    public void deleteScannedDataById(Long scannedId) {
        ScannedEntity scannedEntity = getScannedEntity(scannedId);
        log.info("Printing the  data to be deleted {}", scannedEntity);
        scannedRepository.delete(scannedEntity);
    }

    private ScannedEntity getScannedEntity(Long scannedId) {
        return scannedRepository.findById(scannedId)
                .orElseThrow(() -> new ResourceNotFoundException("Data not found with id: " + scannedId));
    }

    private ScannedDTO mapToScannedDTO(ScannedEntity scannedEntity) {
        return modelMapper.map(scannedEntity, ScannedDTO.class);
    }

    @Override
    public ScannedDTO saveScannedData(ScannedDTO scannedDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ScannedEntity scannedEntity = mapToScannedEntity(scannedDTO, user);
        scannedEntity.setId(null);

        ScannedEntity savedEntity = scannedRepository.save(scannedEntity);
        return mapToScannedDTO(savedEntity);
    }

    @Override
    public ScannedDTO updateScannedDataDetails(ScannedDTO scannedDTO, Long scannedId) {
        ScannedEntity existingData = getScannedEntity(scannedId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ScannedEntity updateScannedEntity = mapToScannedEntity(scannedDTO, user);
        updateScannedEntity.setId(existingData.getId());

        updateScannedEntity = scannedRepository.save(updateScannedEntity);

        log.info("Printing the updated data details {}", scannedDTO);
        return mapToScannedDTO(updateScannedEntity);
    }

    private ScannedEntity mapToScannedEntity(ScannedDTO scannedDTO, UserEntity user) {
        ScannedEntity entity = modelMapper.map(scannedDTO, ScannedEntity.class);
        entity.setUser(user);
        return entity;
    }

    @Override
    public List<ScannedDTO> extract(MultipartFile file, List<String> keywords) {

        List<ScannedDTO> foundData = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();

            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String pageText = stripper.getText(document);

                String cleanedText = pageText.replaceAll("(?m)^\\s*_+\\s*$\\r?\\n?", "");
                String[] sentences = cleanedText.split("(?<=[.!?])\\s+");

                for (String sentence : sentences) {
                    if (sentence.isBlank()) {
                        continue;
                    }
                    for (String keyword : keywords) {
                        if (sentence.toLowerCase().contains(keyword.toLowerCase())) {
                            ScannedDTO scannedDTO = new ScannedDTO();
                            scannedDTO.setKeyword(keyword);
                            scannedDTO.setPage(page);
                            scannedDTO.setSentence(sentence);
                            foundData.add(scannedDTO);
                        }
                    }

                }
            }

        } catch (IOException e) {
            log.error("Error extracting text from PDF: {}", e.getMessage());
            throw new RuntimeException("Error processing PDF file", e);
        }

        if (!foundData.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            List<ScannedEntity> entitiesToSave = foundData.stream()
                    .map(dto -> mapToScannedEntity(dto, user))
                    .collect(Collectors.toList());

            List<ScannedEntity> savedEntities = scannedRepository.saveAll(entitiesToSave);

            return savedEntities.stream()
                    .map(this::mapToScannedDTO)
                    .collect(Collectors.toList());
        }
        return foundData;

    }

}
