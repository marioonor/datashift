package data_shift.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainDataFrontendDTO {
    private Long id;
    private String controlId;
    private String controlName;
    private String controlDescription;
    private String evidence; 
    private String remarks;
    private String status;

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    private String documentName;
    private String pageNumber;
}
