package data_shift.controller;

import data_shift.dto.MainDataFrontendDTO;
import data_shift.entity.DataMainEntity;
import data_shift.helper.EvidenceCombiner;
import data_shift.service.MainDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
                        DataMainEntity::getControlId, // Key: controlId
                        EvidenceCombiner::new, // Value: new EvidenceCombiner
                        (existingCombiner, newCombiner) -> { // Merge function
                            existingCombiner.getPageNumbers().addAll(newCombiner.getPageNumbers());
                            existingCombiner.getKeywords().addAll(newCombiner.getKeywords());
                            existingCombiner.getDocumentNames().addAll(newCombiner.getDocumentNames());
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
            // Set documentName and pageNumber (take the first one from the sets)
            if (!combiner.getDocumentNames().isEmpty()) {
                dto.setDocumentName(combiner.getDocumentNames().iterator().next());
            }
            if (!combiner.getPageNumbers().isEmpty()) {
                dto.setPageNumber(combiner.getPageNumbers().iterator().next());
            }
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

    @GetMapping("/grouped") // New endpoint
    public ResponseEntity<List<GroupedDataDTO>> getGroupedData() {
        try {
            List<DataMainEntity> mainDataEntities = mainDataService.findAll();

            // Group by documentName, then keywords, then collect pageNumbers
            Map<String, Map<String, Set<String>>> groupedData = mainDataEntities.stream()
                    .collect(Collectors.groupingBy(
                            DataMainEntity::getDocumentName,
                            Collectors.groupingBy(
                                    DataMainEntity::getKeywords,
                                    Collectors.mapping(
                                            DataMainEntity::getPageNumber,
                                            Collectors.toCollection(TreeSet::new)))));

            List<GroupedDataDTO> result = new ArrayList<>();
            for (Map.Entry<String, Map<String, Set<String>>> documentEntry : groupedData.entrySet()) {
                String documentName = documentEntry.getKey();
                GroupedDataDTO documentDTO = new GroupedDataDTO(documentName);
                for (Map.Entry<String, Set<String>> keywordEntry : documentEntry.getValue().entrySet()) {
                    String keyword = keywordEntry.getKey();
                    Set<String> pages = keywordEntry.getValue();
                    Set<String> controlIds = mainDataEntities.stream()
                            .filter(entity -> entity.getDocumentName().equals(documentName)
                                    && entity.getKeywords().equals(keyword))
                            .map(DataMainEntity::getControlId)
                            .collect(Collectors.toSet());
                    documentDTO.addPageKeyword(pages, keyword, controlIds);
                }
                result.add(documentDTO);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // New DTO
    class GroupedDataDTO {
        private String documentName;
        private List<PageKeywordDTO> pageKeywords = new ArrayList<>();

        public GroupedDataDTO(String documentName) {
            this.documentName = documentName;
        }

        public void addPageKeyword(Set<String> pages, String keyword, Set<String> controlIds) {
            pageKeywords.add(new PageKeywordDTO(pages, keyword, controlIds));
        }

        // Getters and setters
        public String getDocumentName() {
            return documentName;
        }

        public void setDocumentName(String documentName) {
            this.documentName = documentName;
        }

        public List<PageKeywordDTO> getPageKeywords() {
            return pageKeywords;
        }

        public void setPageKeywords(List<PageKeywordDTO> pageKeywords) {
            this.pageKeywords = pageKeywords;
        }
    }

    class PageKeywordDTO {
        private Set<String> pages;
        private String keyword;
        private Set<String> controlIds;

        public PageKeywordDTO(Set<String> pages, String keyword, Set<String> controlIds) {
            this.pages = pages;
            this.keyword = keyword;
            this.controlIds = controlIds;
        }

        // Getters and setters
        public Set<String> getPages() {
            return pages;
        }

        public void setPages(Set<String> pages) {
            this.pages = pages;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public Set<String> getControlIds() {
            return controlIds;
        }

        public void setControlIds(Set<String> controlIds) {
            this.controlIds = controlIds;
        }
    }
}
