package data_shift.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControlKeywordsDTO {
    private String controlIdentifier;
    private String controlName;
    private List<String> keywords;
}
