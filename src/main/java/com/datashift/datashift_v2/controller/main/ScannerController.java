package com.datashift.datashift_v2.controller.main;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.datashift.datashift_v2.dto.main.ScannedDTO;
import com.datashift.datashift_v2.io.ScannedRequest;
import com.datashift.datashift_v2.io.ScannedResponse;
import com.datashift.datashift_v2.service.main.ScannedService;

import jakarta.validation.Valid;
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

    @GetMapping("/scanneddata/{scannedId}")
    public ScannedResponse getScannedDataById(@PathVariable Long scannedId) {
        log.info("Printing the data using id {}", scannedId);
        ScannedDTO scannedDTO = scannedService.getScannedDataById(scannedId);
        return mapToScannedResponse(scannedDTO);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/scanneddata/{scannedId}")
    public void deleteScannedDataById(@PathVariable Long scannedId) {
        log.info("API DELETE /scanneddata/{scannedId} called", scannedId);
        scannedService.deleteScannedDataById(scannedId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/scanneddata")
    public ScannedResponse saveScannedData(@Valid @RequestBody ScannedRequest scannedRequest) {
        log.info("API POST /scanneddata called {}",  scannedRequest);
        ScannedDTO scannedDTO = mapToScannedDTO(scannedRequest);
        scannedDTO = scannedService.saveScannedData(scannedDTO);
        return mapToScannedResponse(scannedDTO);
    }


    private ScannedDTO mapToScannedDTO(ScannedRequest scannedRequest) {
        return modelMapper.map(scannedRequest, ScannedDTO.class);  
    }

    private ScannedResponse mapToScannedResponse(ScannedDTO scannedDTO) {
        return modelMapper.map(scannedDTO, ScannedResponse.class);
    }

}
