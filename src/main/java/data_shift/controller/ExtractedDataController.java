package data_shift.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import data_shift.entity.DataShiftExtractedDataEntity;
import data_shift.service.ExtractedDataService;

@RestController
@RequestMapping("extracted-data")
@CrossOrigin(origins = "*")
public class ExtractedDataController {

    @Autowired
    private ExtractedDataService extractedDataService;

    @GetMapping
    public List<DataShiftExtractedDataEntity> getAllExtractedData() {
        return extractedDataService.findAll();
    }
}

