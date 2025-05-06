package data_shift.controller;

import data_shift.dto.MainDataFrontendDTO;
import data_shift.entity.DataMainEntity;
import data_shift.helper.EvidenceCombiner; // Keep EvidenceCombiner
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
        // 1. Fetch all entities
        List<DataMainEntity> mainDataEntities = mainDataService.findAll();

        // 2. Group by controlId to calculate combined evidence using EvidenceCombiner
        // This map will store the combined state for each controlId.
        Map<String, EvidenceCombiner> combinerMap = mainDataEntities.stream()
                .collect(Collectors.toMap(
                        DataMainEntity::getControlId, // Key: controlId
                        EvidenceCombiner::new,        // Value: new EvidenceCombiner from the entity
                        (existingCombiner, newCombiner) -> { // Merge function for duplicate controlIds
                            // Combine the information into the existing combiner
                            existingCombiner.getPageNumbers().addAll(newCombiner.getPageNumbers());
                            existingCombiner.getKeywords().addAll(newCombiner.getKeywords());
                            existingCombiner.getDocumentNames().addAll(newCombiner.getDocumentNames());
                            // Add logic here if remarks/status need specific combination rules
                            // For example, maybe take the latest status or concatenate remarks.
                            // Assuming EvidenceCombiner handles the internal state needed for getCombinedEvidence()
                            return existingCombiner;
                        }));

        // 3. Create the final DTO list, mapping each original entity
        List<MainDataFrontendDTO> frontendDTOs = new ArrayList<>();
        for (DataMainEntity entity : mainDataEntities) {
            MainDataFrontendDTO dto = new MainDataFrontendDTO();

            // --- Map fields directly from the current entity ---
            dto.setId(entity.getId()); // Assuming DataMainEntity has getId()
            dto.setControlId(entity.getControlId());
            dto.setControlName(entity.getControlName());
            dto.setControlDescription(entity.getControlDescription());
            dto.setKeywords(entity.getKeywords()); // Use the specific keyword from this row
            dto.setRemarks(entity.getRemarks());   // Use the specific remarks from this row
            dto.setStatus(entity.getStatus());     // Use the specific status from this row
            dto.setDocumentName(entity.getDocumentName()); // Use the specific document name
            dto.setPageNumber(entity.getPageNumber());   // Use the specific page number

            // --- Get the COMBINED evidence from the pre-calculated map ---
            EvidenceCombiner combiner = combinerMap.get(entity.getControlId());
            if (combiner != null) {
                // Assuming EvidenceCombiner has a method to get the combined evidence string
                dto.setEvidence(combiner.getCombinedEvidence());
            } else {
                // Fallback: Should ideally not happen if the entity was part of the initial list
                dto.setEvidence("Error: Combined evidence not found for " + entity.getControlId());
                // Or use the entity's individual evidence as a fallback:
                // dto.setEvidence(entity.getEvidence());
            }

            frontendDTOs.add(dto);
        }

        // 4. Sort the resulting list (optional, but kept from original)
        frontendDTOs.sort(new ControlIdComparator());

        // 5. Return the list of DTOs
        return frontendDTOs;
    }

    // Custom Comparator for controlId (with added robustness)
    static class ControlIdComparator implements Comparator<MainDataFrontendDTO> {
        @Override
        public int compare(MainDataFrontendDTO dto1, MainDataFrontendDTO dto2) {
            // Handle potential nulls gracefully
            String controlId1 = dto1.getControlId() != null ? dto1.getControlId() : "";
            String controlId2 = dto2.getControlId() != null ? dto2.getControlId() : "";
            return compareControlIds(controlId1, controlId2);
        }

        private int compareControlIds(String controlId1, String controlId2) {
            // Basic null/empty checks
            if (controlId1.isEmpty() && controlId2.isEmpty()) return 0;
            if (controlId1.isEmpty()) return -1;
            if (controlId2.isEmpty()) return 1;

            String[] parts1 = controlId1.split("\\.");
            String[] parts2 = controlId2.split("\\.");

            int length = Math.min(parts1.length, parts2.length);
            for (int i = 0; i < length; i++) {
                try {
                    // Attempt to parse as numbers for numeric comparison
                    int num1 = Integer.parseInt(parts1[i]);
                    int num2 = Integer.parseInt(parts2[i]);
                    int result = Integer.compare(num1, num2);
                    if (result != 0) {
                        return result;
                    }
                } catch (NumberFormatException e) {
                    // Fallback to string comparison if parts are not numeric
                    int result = parts1[i].compareTo(parts2[i]);
                    if (result != 0) {
                        return result;
                    }
                }
            }

            // If prefixes match, compare by length (e.g., 1.1 vs 1.1.1)
            return Integer.compare(parts1.length, parts2.length);
        }
    }


    // The /grouped endpoint and its DTOs remain unchanged as they serve a different purpose
    @GetMapping("/grouped")
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
                    // Find control IDs associated with this specific document/keyword combination
                    Set<String> controlIds = mainDataEntities.stream()
                            .filter(entity -> documentName.equals(entity.getDocumentName())
                                    && keyword.equals(entity.getKeywords()))
                            .map(DataMainEntity::getControlId)
                            .collect(Collectors.toSet());
                    documentDTO.addPageKeyword(pages, keyword, controlIds);
                }
                result.add(documentDTO);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Consider logging the exception e.g., log.error("Error grouping data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- Inner DTO classes for /grouped endpoint (remain the same) ---
    // Using public static inner classes for better practice if they don't need instance members of MainDataController
    // Or move them to separate files if they grow complex. Added Lombok annotations for brevity.

    @lombok.Data // Added Lombok
    @lombok.NoArgsConstructor // Added Lombok
    @lombok.AllArgsConstructor // Added Lombok
    public static class GroupedDataDTO { // Made static
        private String documentName;
        private List<PageKeywordDTO> pageKeywords = new ArrayList<>();

        // Constructor needed if using Lombok's @AllArgsConstructor
        public GroupedDataDTO(String documentName) {
            this.documentName = documentName;
            this.pageKeywords = new ArrayList<>(); // Initialize list
        }

        public void addPageKeyword(Set<String> pages, String keyword, Set<String> controlIds) {
            pageKeywords.add(new PageKeywordDTO(pages, keyword, controlIds));
        }
        // Getters/Setters generated by Lombok
    }

    @lombok.Data // Added Lombok
    @lombok.NoArgsConstructor // Added Lombok
    @lombok.AllArgsConstructor // Added Lombok
    public static class PageKeywordDTO { // Made static
        private Set<String> pages;
        private String keyword;
        private Set<String> controlIds;
        // Getters/Setters generated by Lombok
    }
}
