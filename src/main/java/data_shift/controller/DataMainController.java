package data_shift.controller;

import data_shift.entity.DataMainEntity;
import data_shift.repository.AllDataMainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/all-main-data") // Base path for this controller
public class DataMainController {

    @Autowired
    private AllDataMainRepository alldataMainRepository;

    @GetMapping
    public ResponseEntity<List<DataMainEntity>> getAllMainData() {
        List<DataMainEntity> allData = alldataMainRepository.findAll();
        return ResponseEntity.ok(allData);
    }

}