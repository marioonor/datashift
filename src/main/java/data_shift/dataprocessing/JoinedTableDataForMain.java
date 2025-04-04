package data_shift.dataprocessing;

import data_shift.dto.MainDataDTO;
import data_shift.entity.DataMainEntity;
import data_shift.entity.DataShiftExcelEntity;
import data_shift.entity.DataShiftExtractedDataEntity;
import data_shift.repository.DataMainRepository;
import data_shift.repository.DataShiftExcelRepository;
import data_shift.repository.DataShiftExtractedDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class JoinedTableDataForMain {

    private static final Logger logger = LoggerFactory.getLogger(JoinedTableDataForMain.class);

    @Autowired
    DataShiftExtractedDataRepository dataShiftExtractedDataRepository;

    @Autowired
    DataShiftExcelRepository dataShiftExcelRepository;

    @Autowired
    DataMainRepository dataMainRepository;

    public List<MainDataDTO> joinedTablesData() {
        logger.info("joinedTablesData() method called!");

        List<DataShiftExtractedDataEntity> extractedData = dataShiftExtractedDataRepository.findAll();
        List<DataShiftExcelEntity> excelData = dataShiftExcelRepository.findAll();

        // Group extracted data by document name, then keywords, then collect unique page numbers
        Map<String, Map<String, Set<Integer>>> groupedExtractedData = extractedData.stream()
                .collect(Collectors.groupingBy(
                        DataShiftExtractedDataEntity::getDocumentName,
                        Collectors.groupingBy(
                                DataShiftExtractedDataEntity::getKeywords,
                                Collectors.mapping(
                                        entity -> Integer.parseInt(entity.getPageNumber()),
                                        Collectors.toCollection(TreeSet::new)
                                )
                        )
                ));

        List<MainDataDTO> mainDataDTOList = new ArrayList<>();
        for (DataShiftExcelEntity excelRow : excelData) {
            String controlId = excelRow.getControlId();
            String controlName = excelRow.getControlName();
            String controlDescription = excelRow.getControlDescription();
            String excelKeywords = excelRow.getKeywords();

            logger.info("Checking Excel Row: controlId={}, keywords={}", controlId, excelKeywords);

            for (Map.Entry<String, Map<String, Set<Integer>>> documentEntry : groupedExtractedData.entrySet()) {
                String documentName = documentEntry.getKey();
                Map<String, Set<Integer>> keywordPageMap = documentEntry.getValue();

                for (Map.Entry<String, Set<Integer>> keywordEntry : keywordPageMap.entrySet()) {
                    String keyword = keywordEntry.getKey();
                    if (excelKeywords != null && excelKeywords.contains(keyword)) {
                        Set<Integer> pages = keywordEntry.getValue();
                        MainDataDTO mainDataDTO = new MainDataDTO();
                        mainDataDTO.setControlId(controlId);
                        mainDataDTO.setControlName(controlName);
                        mainDataDTO.setControlDescription(controlDescription);
                        mainDataDTO.setKeywords(keyword);
                        // Add newlines for emphasis
                        String evidence = "Document: " + documentName + "\n" +
                                "Page: " + pages.stream().map(String::valueOf).collect(Collectors.joining(", ")) + "\n" +
                                "Keywords: " + keyword;
                        mainDataDTO.setEvidence(evidence);
                        mainDataDTO.setRemarks("");
                        mainDataDTO.setStatus("Pending");
                        mainDataDTO.setDocumentName(documentName);
                        mainDataDTO.setPageNumber(pages.stream().map(String::valueOf).collect(Collectors.joining(", ")));
                        mainDataDTOList.add(mainDataDTO);
                    }
                }
            }
        }

        List<DataMainEntity> dataMainEntities = mainDataDTOList.stream()
                .map(this::convertToDataMainEntity)
                .collect(Collectors.toList());

        try {
            dataMainRepository.saveAll(dataMainEntities);
            logger.info("Data saved successfully.");
        } catch (Exception e) {
            logger.error("Error saving to database: {}", e.getMessage(), e);
        }

        logger.info("joinedTablesData() method finished!");
        return mainDataDTOList;
    }

    private DataMainEntity convertToDataMainEntity(MainDataDTO mainDataDTO) {
        DataMainEntity dataMainEntity = new DataMainEntity();
        dataMainEntity.setControlId(mainDataDTO.getControlId());
        dataMainEntity.setControlName(mainDataDTO.getControlName());
        dataMainEntity.setControlDescription(mainDataDTO.getControlDescription());
        dataMainEntity.setKeywords(mainDataDTO.getKeywords());
        dataMainEntity.setEvidence(mainDataDTO.getEvidence());
        dataMainEntity.setRemarks(mainDataDTO.getRemarks());
        dataMainEntity.setStatus(mainDataDTO.getStatus());
        dataMainEntity.setDocumentName(mainDataDTO.getDocumentName());
        dataMainEntity.setPageNumber(mainDataDTO.getPageNumber());
        return dataMainEntity;
    }
}
