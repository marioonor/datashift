package data_shift.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import data_shift.dataprocessing.KeywordsDataGenerator;
import data_shift.dto.ControlIdentifierDTO;
import data_shift.dto.ControlKeywordsDTO;
import data_shift.dto.UploadExcelResponseDTO;
import data_shift.service.DataShiftServiceImpl;

@RestController
@RequestMapping("data")
@CrossOrigin(origins = "http://localhost:4200")
public class DataShiftExcelController {

    @Autowired
    DataShiftServiceImpl dataShiftServiceImpl;

    @Autowired
    KeywordsDataGenerator keywordsDataGenerator;

    @PostMapping(value = "/path", consumes = "multipart/form-data")
    public ResponseEntity<UploadExcelResponseDTO> saveFileData(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            dataShiftServiceImpl.saveFileData(file.getInputStream());
            UploadExcelResponseDTO response = new UploadExcelResponseDTO("Data saved successfully!");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            UploadExcelResponseDTO response = new UploadExcelResponseDTO("Error saving data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response); 
        }
    }

    @GetMapping("/control-identifiers")
    public ResponseEntity<List<ControlIdentifierDTO>> getControlIdentifiers() {
        List<ControlIdentifierDTO> controlIdentifiers = keywordsDataGenerator.getControlIdentifiers();
        return ResponseEntity.ok(controlIdentifiers);
    }

    @GetMapping("/control-keywords/{controlIdentifier}")
    public ResponseEntity<List<ControlKeywordsDTO>> getControlKeywords(@PathVariable String controlIdentifier) {
        List<ControlKeywordsDTO> controlKeywordsList = keywordsDataGenerator
                .getKeywordsByControlIdentifier(controlIdentifier);
        return ResponseEntity.ok(controlKeywordsList);
    }

    @PostMapping(value = "/pdf", consumes = "multipart/form-data")
    public ResponseEntity<String> extractDataFromPdf(@RequestParam("file") MultipartFile file) throws IOException {
        dataShiftServiceImpl.extractDataFromPdf(file.getInputStream(), file.getOriginalFilename());
        return ResponseEntity.ok("Data extracted successfully");
    }
}
