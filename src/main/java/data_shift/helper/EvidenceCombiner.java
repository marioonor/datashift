package data_shift.helper;

import java.util.HashSet;
import java.util.Set;

import data_shift.entity.DataMainEntity;

public class EvidenceCombiner {

    private String controlId;
    private String controlName;
    private String controlDescription;
    private String remarks;
    private String status;
    private Set<String> pageNumbers;
    private Set<String> keywords;
    private Set<String> documentNames; // Changed to Set<String>

    public EvidenceCombiner(DataMainEntity entity) {
        this.controlId = entity.getControlId();
        this.controlName = entity.getControlName();
        this.controlDescription = entity.getControlDescription();
        this.remarks = entity.getRemarks();
        this.status = entity.getStatus();
        this.pageNumbers = new HashSet<>();
        this.keywords = new HashSet<>();
        this.documentNames = new HashSet<>(); // Initialize documentNames
        addPageNumber(entity.getPageNumber());
        addKeyword(entity.getKeywords());
        addDocumentName(entity.getDocumentName()); // Add documentName
    }

    public void addPageNumber(String pageNumber) {
        this.pageNumbers.add(pageNumber);
    }

    public void addKeyword(String keyword) {
        this.keywords.add(keyword);
    }

    public void addDocumentName(String documentName) {
        this.documentNames.add(documentName); // Add documentName
    }

    public String getControlId() {
        return controlId;
    }

    public String getControlName() {
        return controlName;
    }

    public String getControlDescription() {
        return controlDescription;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getStatus() {
        return status;
    }

    public Set<String> getPageNumbers() {
        return pageNumbers;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public Set<String> getDocumentNames() {
        return documentNames;
    }

    public String getCombinedEvidence() {
        StringBuilder sb = new StringBuilder();
        sb.append("Documents: ");
        sb.append(String.join(", ", documentNames));
        sb.append(", Pages: ");
        sb.append(String.join(", ", pageNumbers));
        sb.append(", Keywords: ");
        sb.append(String.join(", ", keywords));
        return sb.toString();
    }
}
