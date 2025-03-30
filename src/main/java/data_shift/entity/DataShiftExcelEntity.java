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
@Table(name = "data_shift_excel")

public class DataShiftExcelEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    
    private String controlId;

    @Column(name = "control_name", columnDefinition = "TEXT")
    private String controlName;

    @Column(name = "control_description", columnDefinition = "TEXT")
    private String controlDescription;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    private String status;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;
    private String remarks;
}
