package com.africell.cci_backend.Controller;

import com.africell.cci_backend.Service.BulkActivityService;
import com.africell.cci_backend.dto.response.BulkPreviewResponse;
import com.africell.cci_backend.dto.response.BulkUploadResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import com.africell.cci_backend.Entity.TblBulkHistory;
import com.africell.cci_backend.Entity.TblBulkHistoryDetail;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bulk-activity")
@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
public class BulkActivityController {

    private final BulkActivityService bulkActivityService;

    public BulkActivityController(
            BulkActivityService bulkActivityService) {

        this.bulkActivityService = bulkActivityService;
    }

    /*
     * Upload file
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {

            BulkUploadResponse response =
                    bulkActivityService.uploadFile(file);

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
                    bulkActivityService.getPreview(
                            previewId
                    );

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

    /*
     * Process bulk activity
     */
    @PostMapping("/process/{previewId}")
    public ResponseEntity<?> processBulk(
            @PathVariable String previewId) {

        try {

            Map<String, Object> response =
                    bulkActivityService.processBulk(
                            previewId
                    );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to process uploaded file"
                    )
            );
        }
    }
    /*
     * Get previous bulk transactions
     *
     * Data comes from tbl_bulk_history.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {

        try {

            List<TblBulkHistory> history =
                    bulkActivityService.getHistory();

            return ResponseEntity.ok(history);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            "Unable to fetch bulk history"
                    )
            );
        }
    }


    /*
     * Get transaction details for export
     *
     * Data comes from tbl_bulk_history_detail.
     */
    @GetMapping("/history/{previewId}/details")
    public ResponseEntity<?> getHistoryDetails(
            @PathVariable String previewId) {

        try {

            List<TblBulkHistoryDetail> details =
                    bulkActivityService.getHistoryDetails(
                            previewId
                    );

            return ResponseEntity.ok(details);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            "Unable to fetch transaction details"
                    )
            );
        }
    }
}