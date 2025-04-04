package data_shift.dataprocessing;

import data_shift.dto.ControlIdentifierDTO;
import data_shift.dto.ControlKeywordsDTO;
import data_shift.entity.DataShiftExcelEntity;
import data_shift.repository.DataShiftExcelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class KeywordsDataGenerator {

    private static final Logger logger = LoggerFactory.getLogger(KeywordsDataGenerator.class);

    @Autowired
    DataShiftExcelRepository dataShiftExcelRepository;

    public List<ControlKeywordsDTO> geneControlKeywordsForAllControls() {
        logger.info("geneControlKeywordsForAllControls() method called");
        List<DataShiftExcelEntity> files = dataShiftExcelRepository.findAll();

        Map<String, Map<String, List<DataShiftExcelEntity>>> groupedData = files.stream()
                .collect(Collectors.groupingBy(DataShiftExcelEntity::getControlId,
                        Collectors.groupingBy(DataShiftExcelEntity::getControlName)));

        List<ControlKeywordsDTO> controlKeywordsDTOList = new LinkedList<>();

        for (Map.Entry<String, Map<String, List<DataShiftExcelEntity>>> controlIdEntry : groupedData.entrySet()) {
            String controlIdentifier = controlIdEntry.getKey();
            Map<String, List<DataShiftExcelEntity>> controlNameMap = controlIdEntry.getValue();
            for (Map.Entry<String, List<DataShiftExcelEntity>> controlNameEntry : controlNameMap.entrySet()) {
                String controlName = controlNameEntry.getKey();
                List<DataShiftExcelEntity> controlData = controlNameEntry.getValue();
                Optional<String> keywordsOptional = controlData.stream().map(DataShiftExcelEntity::getKeywords)
                        .findFirst();
                List<String> keywords = new ArrayList<>();
                if (keywordsOptional.isPresent()) {
                    String[] keywordsArray = keywordsOptional.get().split("\n");
                    for (String keyword : keywordsArray) {
                        keywords.add(keyword);
                    }
                }

                ControlKeywordsDTO controlKeywordsDTO = new ControlKeywordsDTO(controlIdentifier, controlName,
                        keywords);
                controlKeywordsDTOList.add(controlKeywordsDTO);
            }
        }
        logger.info("geneControlKeywordsForAllControls() method finished");
        return controlKeywordsDTOList;
    }

    public List<ControlIdentifierDTO> getControlIdentifiers() {
        logger.info("getControlIdentifiers() method called");
        List<DataShiftExcelEntity> files = dataShiftExcelRepository.findAll();
        List<ControlIdentifierDTO> result = files.stream()
                .collect(Collectors.groupingBy(DataShiftExcelEntity::getControlId,
                        Collectors.groupingBy(DataShiftExcelEntity::getControlName)))
                .entrySet().stream()
                .flatMap(controlIdEntry -> controlIdEntry.getValue().entrySet().stream().map(
                        controlNameEntry -> new ControlIdentifierDTO(controlIdEntry.getKey(), controlNameEntry.getKey())))
                .distinct()
                .collect(Collectors.toList());
        logger.info("getControlIdentifiers() method finished");
        return result;
    }

    public List<String> extractKeywords(List<DataShiftExcelEntity> controlData) {
        logger.info("extractKeywords() method called");
        List<String> keywordsList = new ArrayList<>();

        String concatenatedKeywords = controlData.stream().map(DataShiftExcelEntity::getKeywords)
                .filter(keywords -> keywords != null && !keywords.isEmpty()).collect(Collectors.joining("\n"));

        String regex = "(?m)^.*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(concatenatedKeywords);

        while (matcher.find()) {
            String line = matcher.group();
            keywordsList.add(line);
        }
        logger.info("extractKeywords() method finished");
        return keywordsList;
    }

    public List<ControlKeywordsDTO> getKeywordsByControlIdentifier(String controlIdentifier) {
        logger.info("getKeywordsByControlIdentifier() method called for controlIdentifier: {}", controlIdentifier);
        List<DataShiftExcelEntity> files = dataShiftExcelRepository.findAll();

        Map<String, Map<String, List<DataShiftExcelEntity>>> groupedData = files.stream()
                .collect(Collectors.groupingBy(DataShiftExcelEntity::getControlId,
                        Collectors.groupingBy(DataShiftExcelEntity::getControlName)));

        List<ControlKeywordsDTO> controlKeywordsDTOList = new LinkedList<>();

        Map<String, List<DataShiftExcelEntity>> controlNameMap = groupedData.get(controlIdentifier);
        if (controlNameMap != null) {
            for (Map.Entry<String, List<DataShiftExcelEntity>> controlNameEntry : controlNameMap.entrySet()) {
                String controlName = controlNameEntry.getKey();
                List<DataShiftExcelEntity> controlData = controlNameEntry.getValue();
                Optional<String> keywordsOptional = controlData.stream().map(DataShiftExcelEntity::getKeywords)
                        .findFirst();
                List<String> keywords = new ArrayList<>();
                if (keywordsOptional.isPresent()) {
                    String[] keywordsArray = keywordsOptional.get().split("\n");
                    for (String keyword : keywordsArray) {
                        keywords.add(keyword);
                    }
                }

                ControlKeywordsDTO controlKeywordsDTO = new ControlKeywordsDTO(controlIdentifier, controlName,
                        keywords);
                controlKeywordsDTOList.add(controlKeywordsDTO);
            }
        }
        logger.info("getKeywordsByControlIdentifier() method finished for controlIdentifier: {}", controlIdentifier);
        return controlKeywordsDTOList;
    }

    public List<String> getAllUniqueKeywords() {
        logger.info("getAllUniqueKeywords() method called");
        List<DataShiftExcelEntity> files = dataShiftExcelRepository.findAll();
        List<String> result = files.stream().map(DataShiftExcelEntity::getKeywords)
                .filter(keywords -> keywords != null && !keywords.isEmpty())
                .flatMap(keywords -> java.util.Arrays.stream(keywords.split("\n"))).map(String::trim).distinct()
                .collect(Collectors.toList());
        logger.info("getAllUniqueKeywords() method finished");
        return result;
    }

    public Map<String, String> getControlIdentifierByKeyword(String keyword) {
        logger.info("getControlIdentifierByKeyword() method called with keyword: {}", keyword);
        List<DataShiftExcelEntity> files = dataShiftExcelRepository.findAll();
        Optional<DataShiftExcelEntity> foundEntity = files.stream()
                .filter(entity -> entity.getKeywords() != null && entity.getKeywords().contains(keyword))
                .findFirst();

        if (foundEntity.isPresent()) {
            Map<String, String> result = new HashMap<>();
            result.put("controlId", foundEntity.get().getControlId());
            result.put("controlName", foundEntity.get().getControlName());
            logger.info("getControlIdentifierByKeyword() method finished for keyword: {}", keyword);
            return result;
        } else {
            logger.warn("No control identifier found for keyword: {}", keyword);
            return null;
        }
    }

}
