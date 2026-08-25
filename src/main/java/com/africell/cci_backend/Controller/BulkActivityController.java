package com.africell.cci_backend.Controller;

import com.africell.cci_backend.Service.BulkActivityService;
import com.africell.cci_backend.dto.response.BulkPreviewResponse;
import com.africell.cci_backend.dto.response.BulkUploadResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.Map;
@RestController
@RequestMapping("/api/bulk-activity")
public class BulkActivityController {
    private final BulkActivityService bulkActivityService;
    public BulkActivityController(BulkActivityService bulkActivityService) {
        this.bulkActivityService = bulkActivityService;
    }
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            BulkUploadResponse response = bulkActivityService.uploadFile(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            "Unable to process uploaded file"
                    )
            );
        }
    }
    /*
     * Get uploaded record preview
     */
    @GetMapping("/preview/{previewId}")
    public ResponseEntity<?> getPreview(
            @PathVariable String previewId) {

        try {

            BulkPreviewResponse response =
                    bulkActivityService.getPreview(previewId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(404).body(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            "Unable to fetch preview"
                    )
            );
        }
    }

}
