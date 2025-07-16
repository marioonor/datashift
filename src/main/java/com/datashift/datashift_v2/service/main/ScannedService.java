package com.datashift.datashift_v2.service.main;

import java.util.List;

import com.datashift.datashift_v2.dto.main.ScannedDTO;

public interface ScannedService {
    
    List<ScannedDTO> getAllScannedData();

    ScannedDTO getScannedDataById(Long scannedId);

    void deleteScannedDataById(Long scannedId);

    ScannedDTO saveScannedData(ScannedDTO scannedDTO);

}
