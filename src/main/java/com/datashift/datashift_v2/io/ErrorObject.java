package com.datashift.datashift_v2.io;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorObject {
    
    private Integer statusCode;
    private String message;
    private Date timeimestamp;
    private String errorCode;

}
