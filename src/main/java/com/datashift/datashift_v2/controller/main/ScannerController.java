package com.datashift.datashift_v2.controller.main;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.datashift.datashift_v2.dto.main.ScannedDTO;
import com.datashift.datashift_v2.io.ScannedRequest;
import com.datashift.datashift_v2.io.ScannedResponse;
import com.datashift.datashift_v2.service.main.ScannedService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScannerController {

    private final ScannedService scannedService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @GetMapping("/scanneddata")
    public List<ScannedResponse> getAllDataScanned() {
        log.info("API GET /scanneddata called");
        List<ScannedDTO> list = scannedService.getAllScannedData();
        log.info("Printing the data from service {}", list);
        List<ScannedResponse> response = list.stream().map(scannedDTO -> mapToScannedResponse(scannedDTO))
                .collect(Collectors.toList());
        return response;
    }

    @GetMapping("/scanneddata/{id}")
    public ScannedResponse getScannedDataById(@PathVariable("id") Long id) {
        log.info("Printing the data using id {}", id);
        ScannedDTO scannedDTO = scannedService.getScannedDataById(id);
        return mapToScannedResponse(scannedDTO);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/scanneddata/{id}")
    public void deleteScannedDataById(@PathVariable Long id) {
        log.info("API DELETE /scanneddata/{id} called", id);
        scannedService.deleteScannedDataById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/scanneddata")
    public ScannedResponse saveScannedData(@Valid @RequestBody ScannedRequest scannedRequest) {
        log.info("API POST /scanneddata called {}", scannedRequest);
        ScannedDTO scannedDTO = mapToScannedDTO(scannedRequest);
        scannedDTO = scannedService.saveScannedData(scannedDTO);
        return mapToScannedResponse(scannedDTO);
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ScannedDTO>> extractFromPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("keywords") String keywordsJson) throws IOException {

        List<String> keywords = objectMapper.readValue(keywordsJson, new TypeReference<List<String>>() {});
        List<ScannedDTO> extractedData = scannedService.extract(file, keywords);
        return new ResponseEntity<>(extractedData, HttpStatus.CREATED);
    }

    @PutMapping("/scanneddata/{id}")
    public ScannedResponse updateScannedDataById(@PathVariable Long id,
            @Valid @RequestBody ScannedRequest scannedRequest) {
        log.info("API PUT /scanneddata/{id} called and request body {}", id, scannedRequest);
        ScannedDTO scannedDTO = mapToScannedDTO(scannedRequest);
        scannedDTO = scannedService.updateScannedDataDetails(scannedDTO, id);
        log.info("Printing the updated data {}", scannedDTO);
        return mapToScannedResponse(scannedDTO);
    }

    private ScannedDTO mapToScannedDTO(ScannedRequest scannedRequest) {
        return modelMapper.map(scannedRequest, ScannedDTO.class);
    }

    private ScannedResponse mapToScannedResponse(ScannedDTO scannedDTO) {
        return modelMapper.map(scannedDTO, ScannedResponse.class);
    }

}
