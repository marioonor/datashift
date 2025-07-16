package com.datashift.datashift_v2.service.main.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.datashift.datashift_v2.dto.main.ScannedDTO;
import com.datashift.datashift_v2.entity.main.ScannedEntity;
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

    private ScannedDTO mapToScannedDTO(ScannedEntity scannedEntity) {
        return modelMapper.map(scannedEntity, ScannedDTO.class);
    }
    
    
}
