package data_shift.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data_shift.entity.DataMainEntity;
import data_shift.repository.DataMainRepository;

@Service
public class MainDataService {
    
    @Autowired
    private DataMainRepository dataMainRepository;
    public List<DataMainEntity> findAll() {
        return dataMainRepository.findAll();
    }
}
