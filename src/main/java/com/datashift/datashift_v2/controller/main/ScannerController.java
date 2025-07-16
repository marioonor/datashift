package com.datashift.datashift_v2.controller.main;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datashift.datashift_v2.dto.main.ScannedDTO;
import com.datashift.datashift_v2.io.ScannedResponse;
import com.datashift.datashift_v2.service.main.ScannedService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScannerController {

    private final ScannedService scannedService;
    private final ModelMapper modelMapper;

    @GetMapping("/scanneddata")
    public List<ScannedResponse> getAllDataScanned() {
        log.info("API GET /scanneddata called");
        //Call the service method
        List<ScannedDTO> list = scannedService.getAllScannedData();
        log.info("Printing the data from service {}", list);
        //Convert the Scanned DTO to Scanned Response
        List<ScannedResponse> response = list.stream().map(scannedDTO -> mapToScannedResponse(scannedDTO)).collect(Collectors.toList());
        //Return the list/response
        return response;
    }

    private ScannedResponse mapToScannedResponse(ScannedDTO scannedDTO) {
        return modelMapper.map(scannedDTO, ScannedResponse.class);
    }

}
