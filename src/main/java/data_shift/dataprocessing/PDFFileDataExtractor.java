package data_shift.dataprocessing;

import data_shift.entity.DataShiftExtractedDataEntity;
import data_shift.repository.DataShiftExtractedDataRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PDFFileDataExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PDFFileDataExtractor.class);

    @Autowired
    private DataShiftExtractedDataRepository dataShiftExtractedDataRepository;

    @Autowired
    private KeywordsDataGenerator keywordsDataGenerator;

    @Autowired
    private JoinedTableDataForMain joinedTableDataForMain;

    @SuppressWarnings("unused")
    private String controlId;
    @SuppressWarnings("unused")
    private String controlName;

    public PDFFileDataExtractor() {
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }

    public void setControlIdentifier(String controlIdentifier) {
        if (controlIdentifier == null || controlIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Control Identifier cannot be null or empty");
        }
    }

    public void setControlName(String controlName) {
        this.controlName = controlName;
    }

    public void generateData(List<String> keywordLines, InputStream file, String fileName) throws IOException {
        logger.info("generateData() method called for file: {}", fileName);
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("pdf-", ".pdf");
            Files.copy(file, tempFile, StandardCopyOption.REPLACE_EXISTING);
            File pdfFile = tempFile.toFile();

            if (!pdfFile.exists()) {
                throw new IllegalArgumentException("File not found: " + pdfFile.getAbsolutePath());
            }

            if (keywordLines.isEmpty()) {
                throw new IllegalArgumentException("No keywords found. Please add keywords in the database.");
            }

            for (String keyword : keywordLines) {
                processKeyword(pdfFile, keyword, fileName);
            }
            logger.info("Finished processing all keywords for file: {}", fileName);
        } catch (IOException e) {
            logger.error("Error processing file: {}", fileName, e);
            throw e;
        } finally {
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
            joinedTableDataForMain.joinedTablesData();
        }
    }

    private void processKeyword(File pdfFile, String keyword, String fileName) {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            extractText(document, keyword, fileName);
        } catch (IOException e) {
            logger.error("Error processing PDF file for keyword '{}' in file '{}': {}", keyword, fileName, e.getMessage(), e);
            throw new RuntimeException("Error processing PDF file for keyword '" + keyword + "': " + e.getMessage(), e);
        }
    }

    public void extractText(PDDocument document, String keyword, String pdfFileName) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        Map<Integer, List<String>> pageParagraphs = new HashMap<>();
        Map<Integer, List<String>> pageTableRows = new HashMap<>();
        int startPage = 1;
        int endPage = document.getNumberOfPages();

        for (int pageNumber = startPage; pageNumber <= endPage; pageNumber++) {
            textStripper.setStartPage(pageNumber);
            textStripper.setEndPage(pageNumber);
            String pageText = textStripper.getText(document);

            List<String> tableRegions = detectTables(pageText);
            if (!tableRegions.isEmpty()) {
                List<String> allParagraphs = new ArrayList<>();
                List<String> allRows = new ArrayList<>();
                for (String tableRegion : tableRegions) {
                    allParagraphs.addAll(extractParagraphs(tableRegion));
                    allRows.addAll(extractTableRows(tableRegion));
                }
                pageParagraphs.put(pageNumber, allParagraphs);
                pageTableRows.put(pageNumber, allRows);
            } else {
                pageParagraphs.put(pageNumber, extractParagraphs(pageText));
            }
        }

        List<SentenceLocation> extractedSentences = extractSentencesWithLocation(document, keyword,
                pageParagraphs, pageTableRows);
        if (!extractedSentences.isEmpty()) {
            saveOutputToDatabase(extractedSentences, keyword, pdfFileName);
        }
    }

    public List<SentenceLocation> extractSentencesWithLocation(PDDocument document,
            String keyword, Map<Integer, List<String>> pageParagraphs,
            Map<Integer, List<String>> pageTableRows) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        List<SentenceLocation> result = new ArrayList<>();
        int startPage = 1;
        int endPage = document.getNumberOfPages();

        for (int pageNumber = startPage; pageNumber <= endPage; pageNumber++) {
            textStripper.setStartPage(pageNumber);
            textStripper.setEndPage(pageNumber);
            String pageText = textStripper.getText(document);
            String[] sentences = pageText.split("(?<=[.!?])\\s+");
            List<String> paragraphs = pageParagraphs.get(pageNumber);
            List<String> tableRows = pageTableRows.get(pageNumber);

            if (paragraphs == null)
                continue;

            for (String evidence : sentences) {
                if (evidence.toLowerCase().contains(keyword.toLowerCase())) {
                    String title = "N/A";
                    if (tableRows != null) {
                        title = findTableRowHeader(evidence, tableRows);
                    }
                    result.add(new SentenceLocation(evidence, pageNumber, title));
                }
            }
        }
        return result;
    }

    private List<String> extractParagraphs(String pageText) {
        List<String> paragraphs = new ArrayList<>();
        String[] potentialParagraphs = pageText.split("(\r?\n)+");

        for (String potentialParagraph : potentialParagraphs) {
            String trimmedParagraph = potentialParagraph.trim();
            if (!trimmedParagraph.isEmpty()) {
                String[] sentences = trimmedParagraph.split("(?<=[.!?])\\s+");
                for (String evidence : sentences) {
                    String trimmedSentence = evidence.trim();
                    if (!trimmedSentence.isEmpty()) {
                        paragraphs.add(trimmedSentence);
                    }
                }
            }
        }
        return paragraphs;
    }

    static class SentenceLocation {
        private String evidence;
        private int pageNumber;
        private String title;

        public SentenceLocation(String evidence, int pageNumber, String title) {
            this.evidence = evidence;
            this.pageNumber = pageNumber;
            this.title = title;
        }

        public String getSentence() {
            return evidence;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public String getTitle() {
            return title;
        }
    }

    private List<String> detectTables(String pageText) {
        List<String> tableRegions = new ArrayList<>();

        String[] lines = pageText.split("\r?\n");
        Pattern tablePattern = Pattern.compile("^((\\s*\\S+\\s+){2,})$");
        for (int i = 0; i < lines.length - 2; i++) {
            Matcher matcher1 = tablePattern.matcher(lines[i].trim());
            Matcher matcher2 = tablePattern.matcher(lines[i + 1].trim());
            Matcher matcher3 = tablePattern.matcher(lines[i + 2].trim());
            if (matcher1.matches() && matcher2.matches() && matcher3.matches()) {
                tableRegions.add(lines[i] + "\n" + lines[i + 1] + "\n" + lines[i + 2]);
            }
        }
        return tableRegions;
    }

    private List<String> extractTableRows(String tableRegion) {
        List<String> rows = new ArrayList<>();
        String[] lines = tableRegion.split("\r?\n");
        for (String line : lines) {
            rows.add(line.trim());
        }
        return rows;
    }

    private String findTableRowHeader(String evidence, List<String> tableRows) {
        for (String row : tableRows) {
            if (row.contains(evidence)) {
                String[] cells = row.split("\\s{2,}");
                if (cells.length > 0) {
                    return cells[0].trim();
                }
            }
        }
        return "N/A (Table Row)";
    }

    private void saveOutputToDatabase(List<SentenceLocation> extractedSentences, String keyword,
            String pdfFileName) {
        logger.info("saveOutputToDatabase() method called for keyword '{}' in file '{}'", keyword, pdfFileName);
        for (SentenceLocation sentenceLocation : extractedSentences) {
            DataShiftExtractedDataEntity extractedData = new DataShiftExtractedDataEntity();

            Map<String, String> controlIdentifier = keywordsDataGenerator.getControlIdentifierByKeyword(keyword);
            if (controlIdentifier != null) {
                extractedData.setControlId(controlIdentifier.get("controlId"));
                extractedData.setControlName(controlIdentifier.get("controlName"));
            } else {
                extractedData.setControlId("N/A");
                extractedData.setControlName("N/A");
            }
            extractedData.setDocumentName(pdfFileName);
            extractedData.setPageNumber(String.valueOf(sentenceLocation.getPageNumber()));
            extractedData.setKeywords(keyword);
            extractedData.setEvidence(sentenceLocation.getSentence());
            try {
                dataShiftExtractedDataRepository.save(extractedData);
            } catch (Exception e) {
                logger.error("Error saving extracted data to database for keyword '{}' in file '{}': {}", keyword, pdfFileName, e.getMessage(), e);
            }
        }
        logger.info("Data saved to the database successfully for keyword '{}' in file '{}'", keyword, pdfFileName);
    }
}
