package com.datashift.datashift_v2.service.main.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
        //Call the repository method 
        List<ScannedEntity> list = scannedRepository.findAll();
        log.info("Printing the data from repository", list);
        //Convert the entity object to DTO object
        List<ScannedDTO> listOfDataScanned = list.stream().map(scannedEntity -> mapToScannedDTO(scannedEntity)).collect(Collectors.toList());
        //Return the list
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
        return scannedRepository.findByScannedId(scannedId)
                .orElseThrow(() -> new ResourceNotFoundException("Data not found with scannedId: " + scannedId));
    }

    private ScannedDTO mapToScannedDTO(ScannedEntity scannedEntity) {
        return modelMapper.map(scannedEntity, ScannedDTO.class);
    }

    @Override
    public ScannedDTO saveScannedData(ScannedDTO scannedDTO) {
        ScannedEntity scannedEntity = scannedRepository.findByScannedId(scannedDTO.getScannedId())
                .orElse(new ScannedEntity());

        scannedEntity.setScannedId(scannedDTO.getScannedId());
        scannedEntity.setKeyword(scannedDTO.getKeyword());
        scannedEntity.setPage(scannedDTO.getPage());
        scannedEntity.setSentence(scannedDTO.getSentence());

        ScannedEntity savedEntity = scannedRepository.save(scannedEntity);
        return mapToScannedDTO(savedEntity);
    }

}
