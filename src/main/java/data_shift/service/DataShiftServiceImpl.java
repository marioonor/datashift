package data_shift.service;

import data_shift.dataprocessing.PDFFileDataExtractor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data_shift.dataprocessing.KeywordsDataGenerator;
import data_shift.entity.DataShiftExcelEntity;
import data_shift.repository.DataShiftExcelRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataShiftServiceImpl {

    @Autowired
    private DataShiftExcelRepository dataShiftExcelRepository;

    @Autowired
    private KeywordsDataGenerator keywordsDataGenerator;

    @Autowired
    private PDFFileDataExtractor pdfFileDataExtractor;

    public void processPdf(InputStream pdfFileStream, String fileName) throws IOException {
        List<String> allKeywords = keywordsDataGenerator.getAllUniqueKeywords();
        pdfFileDataExtractor.generateData(allKeywords, pdfFileStream, fileName);
    }

    public void saveFileData(InputStream file) throws IOException {
        System.out.println("saveFileData method called");
        List<DataShiftExcelEntity> dataShiftExcelEntities = new LinkedList<>();
        try (Workbook workbook = WorkbookFactory.create(file)) {
            System.out.println("Workbook created");
            Sheet sheet = workbook.getSheetAt(0);
            System.out.println("Sheet retrieved");
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                System.out.println("Processing row: " + row.getRowNum());
                DataShiftExcelEntity fileData = new DataShiftExcelEntity();

                fileData.setControlId(getCellValue(row.getCell(0)));
                fileData.setControlName(getCellValue(row.getCell(1)));
                fileData.setControlDescription(getCellValue(row.getCell(2)));
                fileData.setKeywords(getCellValue(row.getCell(3)));
                fileData.setStatus(getCellValue(row.getCell(4)));
                fileData.setEvidence(getCellValue(row.getCell(5)));
                fileData.setRemarks(getCellValue(row.getCell(6)));

                dataShiftExcelEntities.add(fileData);
            }
            System.out.println("Saving entities to database");
            dataShiftExcelRepository.saveAll(dataShiftExcelEntities);
            System.out.println("Entities saved to database");

            Map<String, Map<String, List<DataShiftExcelEntity>>> groupedData = dataShiftExcelEntities.stream()
                    .collect(Collectors.groupingBy(DataShiftExcelEntity::getControlId,
                            Collectors.groupingBy(DataShiftExcelEntity::getControlName)));

            for (Map.Entry<String, Map<String, List<DataShiftExcelEntity>>> controlIdEntry : groupedData.entrySet()) {
                Map<String, List<DataShiftExcelEntity>> controlNameMap = controlIdEntry.getValue();
                for (Map.Entry<String, List<DataShiftExcelEntity>> controlNameEntry : controlNameMap.entrySet()) {
                    List<DataShiftExcelEntity> controlData = controlNameEntry.getValue();

                    List<String> keywords = keywordsDataGenerator.extractKeywords(controlData);

                    for (DataShiftExcelEntity entity : controlData) {
                        entity.setKeywords(String.join("\n", keywords));
                    }
                }
            }
            System.out.println("Saving entities to database");
            dataShiftExcelRepository.saveAll(dataShiftExcelEntities);
            System.out.println("Entities saved to database");
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return String.valueOf(cell.getDateCellValue());
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    public List<DataShiftExcelEntity> findAll() {
        return dataShiftExcelRepository.findAll();
    }

    public void extractDataFromPdf(InputStream file, String fileName) throws IOException {
        List<String> keywordLines = keywordsDataGenerator.getAllUniqueKeywords();
        pdfFileDataExtractor.generateData(keywordLines, file, fileName);
    }
}
