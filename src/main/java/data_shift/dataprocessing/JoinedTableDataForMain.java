package data_shift.dataprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data_shift.dto.MainDataDTO;
import data_shift.entity.DataMainEntity;
import data_shift.entity.DataShiftExcelEntity;
import data_shift.entity.DataShiftExtractedDataEntity;
import data_shift.repository.DataMainRepository;
import data_shift.repository.DataShiftExcelRepository;
import data_shift.repository.DataShiftExtractedDataRepository;

@Service
public class JoinedTableDataForMain {

    @Autowired
    DataShiftExtractedDataRepository dataShiftExtractedDataRepository;

    @Autowired
    DataShiftExcelRepository dataShiftExcelRepository;

    @Autowired
    DataMainRepository dataMainRepository;

    public List<MainDataDTO> joinedTablesData() {
        System.out.println("joinedTablesData() method called!");

        List<DataShiftExtractedDataEntity> extractedData = dataShiftExtractedDataRepository.findAll();
        List<DataShiftExcelEntity> excelData = dataShiftExcelRepository.findAll();

        // Group extracted data by controlId, documentName, pageNumber, keywords, and evidence
        Map<String, Map<String, Map<String, Map<String, Map<String, List<DataShiftExtractedDataEntity>>>>>> groupedExtractedData = extractedData
                .stream()
                .collect(Collectors.groupingBy(DataShiftExtractedDataEntity::getControlId,
                        Collectors.groupingBy(DataShiftExtractedDataEntity::getDocumentName,
                                Collectors.groupingBy(DataShiftExtractedDataEntity::getPageNumber,
                                        Collectors.groupingBy(DataShiftExtractedDataEntity::getKeywords,
                                                Collectors.groupingBy(DataShiftExtractedDataEntity::getEvidence))))));

        List<MainDataDTO> mainDataDTOList = new ArrayList<>();

        for (DataShiftExcelEntity excelRow : excelData) {
            String controlId = excelRow.getControlId();
            String controlName = excelRow.getControlName();
            String controlDescription = excelRow.getControlDescription();
            String excelKeywords = excelRow.getKeywords();

            System.out.println("Checking Excel Row: controlId=" + controlId + ", keywords=" + excelKeywords);

            if (groupedExtractedData.containsKey(controlId)) {
                Map<String, Map<String, Map<String, Map<String, List<DataShiftExtractedDataEntity>>>>> documentNameMap = groupedExtractedData
                        .get(controlId);
                for (Map.Entry<String, Map<String, Map<String, Map<String, List<DataShiftExtractedDataEntity>>>>> documentNameEntry : documentNameMap
                        .entrySet()) {
                    String documentName = documentNameEntry.getKey();
                    Map<String, Map<String, Map<String, List<DataShiftExtractedDataEntity>>>> pageNumberMap = documentNameEntry
                            .getValue();
                    for (Map.Entry<String, Map<String, Map<String, List<DataShiftExtractedDataEntity>>>> pageNumberEntry : pageNumberMap
                            .entrySet()) {
                        String pageNumber = pageNumberEntry.getKey();
                        Map<String, Map<String, List<DataShiftExtractedDataEntity>>> keywordsMap = pageNumberEntry
                                .getValue();

                        // Iterate through each keyword in the extracted data
                        for (Map.Entry<String, Map<String, List<DataShiftExtractedDataEntity>>> keywordEntry : keywordsMap
                                .entrySet()) {
                            String extractedKeyword = keywordEntry.getKey();
                            // Check if any of the excel keywords contains the extracted keyword
                            if (excelKeywords != null && excelKeywords.contains(extractedKeyword)) {
                                Map<String, List<DataShiftExtractedDataEntity>> evidenceMap = keywordEntry.getValue();
                                for (Map.Entry<String, List<DataShiftExtractedDataEntity>> evidenceEntry : evidenceMap
                                        .entrySet()) {
                                    String evidenceFromExtracted = evidenceEntry.getKey();
                                    // Create MainDataDTO and populate it
                                    MainDataDTO mainDataDTO = new MainDataDTO();
                                    mainDataDTO.setControlId(controlId);
                                    mainDataDTO.setControlName(controlName);
                                    mainDataDTO.setControlDescription(controlDescription);
                                    mainDataDTO.setKeywords(extractedKeyword); // Use the extracted keyword
                                    mainDataDTO.setEvidence(evidenceFromExtracted);
                                    mainDataDTO.setRemarks("");
                                    mainDataDTO.setStatus("Pending");
                                    mainDataDTO.setDocumentName(documentName);
                                    mainDataDTO.setPageNumber(pageNumber);
                                    mainDataDTOList.add(mainDataDTO);
                                }
                            }
                        }
                    }
                }
            }
        }

        List<DataMainEntity> dataMainEntities = mainDataDTOList.stream()
                .map(this::convertToDataMainEntity)
                .collect(Collectors.toList());

        try {
            dataMainRepository.saveAll(dataMainEntities);
            System.out.println("Data saved to data_shift_main_data successfully.");
        } catch (Exception e) {
            System.err.println("Error saving to data_shift_main_data: " + e.getMessage());
            e.printStackTrace();
        }

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
