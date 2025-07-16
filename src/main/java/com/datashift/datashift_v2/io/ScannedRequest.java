package com.datashift.datashift_v2.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScannedRequest {

    private Long scannedId;
    private String keyword;
    private Long page;
    private String sentence;

}
