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

    private Long id;
    private String fileName;
    private String keyword;
    private int page;
    private String sentence;

}
