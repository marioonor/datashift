package data_shift.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainDataDTO {

    private String controlId;
    private String controlName;
    private String controlDescription;
    private String keywords;    
    private String evidence;
    private String documentName;
    private String pageNumber;
    private String remarks;
    private String status;

}
