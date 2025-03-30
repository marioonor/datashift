package data_shift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "data_shift_extracted_data")
public class DataShiftExtractedDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String controlId;

    @Column(name = "control_name", columnDefinition = "TEXT")
    private String controlName;

    @Column(name = "document_name", columnDefinition = "TEXT")
    private String documentName;
    private String pageNumber;
    private String keywords;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;
}
