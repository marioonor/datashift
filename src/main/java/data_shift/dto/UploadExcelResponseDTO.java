package data_shift.dto;

public class UploadExcelResponseDTO {
    private String message;

    public UploadExcelResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
