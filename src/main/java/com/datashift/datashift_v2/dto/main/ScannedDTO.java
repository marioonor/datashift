package com.datashift.datashift_v2.dto.main;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScannedDTO {
    
    private Long scannedId; 
    private String keyword;
    private Long page;
    private String sentence;
}
