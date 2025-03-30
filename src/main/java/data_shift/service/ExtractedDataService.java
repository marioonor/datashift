package data_shift.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data_shift.entity.DataShiftExtractedDataEntity;
import data_shift.repository.DataShiftExtractedDataRepository;

@Service
public class ExtractedDataService {

    @Autowired
    private DataShiftExtractedDataRepository dataShiftExtractedDataRepository; // Corrected repository

    public List<DataShiftExtractedDataEntity> findAll() {
        return dataShiftExtractedDataRepository.findAll();
    }
}
