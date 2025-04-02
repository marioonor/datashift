package data_shift.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import data_shift.entity.DataMainEntity;
import data_shift.service.MainDataService;

@RestController
@RequestMapping("main-data")
@CrossOrigin(origins = "http://localhost:4200")
public class MainDataController {
    
    @Autowired
    private MainDataService mainDataService;

    @GetMapping
    public List<DataMainEntity> getAllMainData() {
        return mainDataService.findAll();
    }
    
}
