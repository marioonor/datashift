package data_shift.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("api/data-shift")
@CrossOrigin(origins = "http://localhost:4200")
public class DataShiftUploadPDFController {

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadPDF(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No file uploaded");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // String uploadDir = System.getProperty("user.dir") + "/uploads/";
            String uploadDir = System.getProperty("user.dir") + "/data_shift_web/src/assets/pdfs/";
            
            File directory = new File(uploadDir);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File uploadedPath = new File(uploadDir + file.getOriginalFilename());
            file.transferTo(uploadedPath);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("documentName", file.getOriginalFilename()); // Return the documentName in a separate field
            response.put("path", uploadedPath.getAbsolutePath());
            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error uploading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam("filename") String filename) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/data_shift_web/src/assets/pdfs/";
            File fileToDelete = new File(uploadDir + filename);

            if (!fileToDelete.exists()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "File not found: " + filename);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (fileToDelete.delete()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "File deleted successfully: " + filename);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Error deleting file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error deleting file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Server is up and running");
    }

}
