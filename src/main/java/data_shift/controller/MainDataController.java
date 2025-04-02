package data_shift.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import data_shift.dto.MainDataFrontendDTO;
import data_shift.entity.DataMainEntity;
import data_shift.helper.EvidenceCombiner;
import data_shift.service.MainDataService;

@RestController
@RequestMapping("main-data")
@CrossOrigin(origins = "http://localhost:4200")
public class MainDataController {

    @Autowired
    private MainDataService mainDataService;

    @GetMapping
    public List<MainDataFrontendDTO> findAll() {
        List<DataMainEntity> mainDataEntities = mainDataService.findAll();

        // Group by controlId and combine evidence
        Map<String, EvidenceCombiner> combinerMap = mainDataEntities.stream()
                .collect(Collectors.toMap(
                        DataMainEntity::getControlId,
                        EvidenceCombiner::new,
                        (existingCombiner, newCombiner) -> {
                            existingCombiner.getPageNumbers().addAll(newCombiner.getPageNumbers());
                            newCombiner.getKeywords().forEach(existingCombiner::addKeyword);
                            newCombiner.getDocumentNames().forEach(existingCombiner::addDocumentName);
                            return existingCombiner;
                        }));

        // Convert to MainDataFrontendDTO
        List<MainDataFrontendDTO> frontendDTOs = new ArrayList<>();

        for (EvidenceCombiner combiner : combinerMap.values()) {
            MainDataFrontendDTO dto = new MainDataFrontendDTO();
            dto.setControlId(combiner.getControlId());
            dto.setControlName(combiner.getControlName());
            dto.setControlDescription(combiner.getControlDescription());
            dto.setEvidence(combiner.getCombinedEvidence());
            dto.setRemarks(combiner.getRemarks());
            dto.setStatus(combiner.getStatus());
            frontendDTOs.add(dto);
        }

        // Sort by controlId using a custom comparator
        frontendDTOs.sort(new ControlIdComparator());

        return frontendDTOs;
    }

    // Custom Comparator for controlId
    static class ControlIdComparator implements Comparator<MainDataFrontendDTO> {
        @Override
        public int compare(MainDataFrontendDTO dto1, MainDataFrontendDTO dto2) {
            return compareControlIds(dto1.getControlId(), dto2.getControlId());
        }

        private int compareControlIds(String controlId1, String controlId2) {
            String[] parts1 = controlId1.split("\\.");
            String[] parts2 = controlId2.split("\\.");

            int length = Math.min(parts1.length, parts2.length);
            for (int i = 0; i < length; i++) {
                int num1 = Integer.parseInt(parts1[i]);
                int num2 = Integer.parseInt(parts2[i]);
                int result = Integer.compare(num1, num2);
                if (result != 0) {
                    return result;
                }
            }

            return Integer.compare(parts1.length, parts2.length);
        }
    }
}
