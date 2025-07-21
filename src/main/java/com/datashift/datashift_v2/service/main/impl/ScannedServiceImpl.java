package com.datashift.datashift_v2.service.main.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datashift.datashift_v2.dto.main.ScannedDTO;
import com.datashift.datashift_v2.entity.main.ScannedEntity;
import com.datashift.datashift_v2.exceptions.ResourceNotFoundException;
import com.datashift.datashift_v2.repository.main.ScannedRepository;
import com.datashift.datashift_v2.service.main.ScannedService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScannedServiceImpl implements ScannedService {

    private final ScannedRepository scannedRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ScannedDTO> getAllScannedData() {
        // Call the repository method
        List<ScannedEntity> list = scannedRepository.findAll();
        log.info("Printing the data from repository", list);
        // Convert the entity object to DTO object
        List<ScannedDTO> listOfDataScanned = list.stream().map(scannedEntity -> mapToScannedDTO(scannedEntity))
                .collect(Collectors.toList());
        // Return the list
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
        ScannedEntity scannedEntity = scannedRepository.findById(scannedDTO.getId())
                .orElse(new ScannedEntity());

        scannedEntity.setKeyword(scannedDTO.getKeyword());
        scannedEntity.setPage(scannedDTO.getPage());
        scannedEntity.setSentence(scannedDTO.getSentence());

        ScannedEntity savedEntity = scannedRepository.save(scannedEntity);
        return mapToScannedDTO(savedEntity);
    }

    @Override
    public ScannedDTO updateScannedDataDetails(ScannedDTO scannedDTO, Long scannedId) {
        ScannedEntity existingData = getScannedEntity(scannedId);
        ScannedEntity updateScannedEntity = mapToScannedEntity(scannedDTO);
        updateScannedEntity.setId(existingData.getId());
        updateScannedEntity = scannedRepository.save(updateScannedEntity);
        log.info("Printing the updated data details {}", scannedDTO);
        return mapToScannedDTO(updateScannedEntity);
    }

    private ScannedEntity mapToScannedEntity(ScannedDTO scannedDTO) {
        return modelMapper.map(scannedDTO, ScannedEntity.class);
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

                String[] sentences = pageText.split("(?<=[.!?])\\s+");
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
            List<ScannedEntity> entitiesToSave = foundData.stream()
                    .map(this::mapToScannedEntity)
                    .collect(Collectors.toList());

            List<ScannedEntity> savedEntities = scannedRepository.saveAll(entitiesToSave);

            return savedEntities.stream()
                    .map(this::mapToScannedDTO)
                    .collect(Collectors.toList());
        }
        return foundData;

    }

}
