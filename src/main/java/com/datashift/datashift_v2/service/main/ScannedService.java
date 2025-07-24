package com.datashift.datashift_v2.service.main;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.datashift.datashift_v2.dto.main.ScannedDTO;

public interface ScannedService {
    
    List<ScannedDTO> getAllScannedData();

    ScannedDTO getScannedDataById(Long scannedId);

    void deleteScannedDataById(Long scannedId);

    ScannedDTO saveScannedData(ScannedDTO scannedDTO);

    ScannedDTO updateScannedDataDetails(ScannedDTO scannedDTO, Long scannedId);

    List<ScannedDTO> extract(MultipartFile file, String fileName, List<String> keywords);

}
