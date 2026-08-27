package com.africell.cci_backend.Service;

import com.africell.cci_backend.Entity.TblBulkHistory;
import com.africell.cci_backend.Entity.TblBulkHistoryDetail;
import com.africell.cci_backend.Entity.TblToneCatalogue;
import com.africell.cci_backend.Repository.TblBulkHistoryDetailRepository;
import com.africell.cci_backend.Repository.TblBulkHistoryRepository;
import com.africell.cci_backend.Repository.TblToneCatalogueRepository;
import com.africell.cci_backend.dto.response.BulkPreviewResponse;
import com.africell.cci_backend.dto.response.BulkRecordResponse;
import com.africell.cci_backend.dto.response.BulkUploadResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BulkActivityService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final List<String> REQUIRED_HEADERS = List.of(
            "Mobile Number",
            "Tone ID",
            "Tone Name",
            "Package Plan",
            "Artist Name"
    );

    private final TblToneCatalogueRepository toneCatalogueRepository;

    private final TblBulkHistoryRepository bulkHistoryRepository;

    private final TblBulkHistoryDetailRepository bulkHistoryDetailRepository;

    private final SubscriberService subscriberService;

    /*
     * Temporary storage between Upload and Process.
     *
     * This is only for the upload/preview stage.
     * tbl_bulk_history is NOT touched here.
     */
    private final Map<String, BulkPreviewResponse> previewStore =
            new ConcurrentHashMap<>();

    public BulkActivityService(
            TblToneCatalogueRepository toneCatalogueRepository,
            TblBulkHistoryRepository bulkHistoryRepository,
            TblBulkHistoryDetailRepository bulkHistoryDetailRepository,
            SubscriberService subscriberService) {

        this.toneCatalogueRepository = toneCatalogueRepository;
        this.bulkHistoryRepository = bulkHistoryRepository;
        this.bulkHistoryDetailRepository = bulkHistoryDetailRepository;
        this.subscriberService = subscriberService;
    }

    /*
     * Upload + validate file
     */
    public BulkUploadResponse uploadFile(MultipartFile file) {

        validateFile(file);

        try {

            List<BulkRecordResponse> records;

            String fileName = file.getOriginalFilename();

            if (fileName == null) {
                throw new IllegalArgumentException(
                        "File name is missing"
                );
            }

            String lowerName = fileName.toLowerCase();

            if (lowerName.endsWith(".csv")) {

                records = readCsvFile(file);

            } else if (lowerName.endsWith(".xlsx")) {

                records = readExcelFile(file, false);

            } else if (lowerName.endsWith(".xls")) {

                records = readExcelFile(file, true);

            } else {

                throw new IllegalArgumentException(
                        "Unsupported file type. Only CSV, XLSX and XLS are supported."
                );
            }

            int totalRecords = records.size();

            int validRecords = (int) records.stream()
                    .filter(BulkRecordResponse::isValid)
                    .count();

            int invalidRecords = totalRecords - validRecords;

            String previewId = generatePreviewId();

            BulkPreviewResponse preview =
                    new BulkPreviewResponse(
                            previewId,
                            fileName,
                            totalRecords,
                            validRecords,
                            invalidRecords,
                            records
                    );

            previewStore.put(previewId, preview);

            return new BulkUploadResponse(
                    previewId,
                    fileName,
                    totalRecords,
                    validRecords,
                    invalidRecords,
                    records
            );

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to process uploaded file",
                    e
            );
        }
    }

    /*
     * Get preview
     */
    public BulkPreviewResponse getPreview(String previewId) {

        BulkPreviewResponse preview =
                previewStore.get(previewId);

        if (preview == null) {

            throw new IllegalArgumentException(
                    "Preview not found: " + previewId
            );
        }

        return preview;
    }

    /*
     * Process bulk activity
     *
     * IMPORTANT:
     * If even one record is invalid,
     * nothing will be processed.
     */
    @Transactional
    public Map<String, Object> processBulk(String previewId) {

        BulkPreviewResponse preview =
                previewStore.get(previewId);

        if (preview == null) {

            throw new IllegalArgumentException(
                    "Preview not found: " + previewId
            );
        }

        /*
         * Do not process anything if there is
         * even one invalid record.
         */
        if (preview.getInvalidRecords() != null
                && preview.getInvalidRecords() > 0) {

            throw new IllegalArgumentException(
                    "Bulk process cannot continue because "
                            + preview.getInvalidRecords()
                            + " invalid record(s) were found."
            );
        }

        /*
         * No records to process.
         */
        if (preview.getRecords() == null
                || preview.getRecords().isEmpty()) {

            throw new IllegalArgumentException(
                    "No records found to process."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        /*
         * Process every valid record through the
         * existing SubscriberService Try N Buy logic.
         */
        for (BulkRecordResponse record : preview.getRecords()) {

            Long msisdn;

            try {

                msisdn = Long.parseLong(
                        record.getMobileNumber()
                );

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Invalid Mobile Number: "
                                + record.getMobileNumber()
                );
            }

            /*
             * Reuse existing Try N Buy business logic.
             *
             * This method handles:
             * - existing record → update
             * - new record → insert
             * - reqDate
             * - status = 2
             * - toneCode
             */
            subscriberService.processTryNBuy(
                    msisdn,
                    record.getToneId()
            );
        }

        /*
         * Save bulk history header.
         */
        TblBulkHistory history =
                new TblBulkHistory();

        history.setPreviewId(
                preview.getPreviewId()
        );

        history.setFileName(
                preview.getFileName()
        );

        history.setTotalRecords(
                preview.getTotalRecords()
        );

        history.setSuccessRecords(
                preview.getValidRecords()
        );

        history.setFailedRecords(
                preview.getInvalidRecords()
        );

        /*
         * Bulk process completed successfully.
         */
        history.setStatus((byte) 1);

        history.setTransactionDate(now);

        bulkHistoryRepository.save(history);

        /*
         * Save detail for every processed record.
         */
        for (BulkRecordResponse record : preview.getRecords()) {

            TblBulkHistoryDetail detail =
                    new TblBulkHistoryDetail();

            detail.setPreviewId(
                    preview.getPreviewId()
            );

            detail.setMobile(
                    Long.parseLong(
                            record.getMobileNumber()
                    )
            );

            detail.setToneId(
                    record.getToneId()
            );

            detail.setToneName(
                    record.getToneName()
            );

            detail.setArtistName(
                    record.getArtistName()
            );

            detail.setPackagePlan(
                    record.getPackagePlan()
            );

            detail.setStatus((byte) 1);

            detail.setMessage(
                    "Active"
            );

            detail.setTransactionDate(now);

            bulkHistoryDetailRepository.save(detail);
        }

        /*
         * Remove preview after successful processing.
         * This prevents the same preview from being
         * processed again.
         */
        previewStore.remove(previewId);

        return Map.of(
                "previewId", preview.getPreviewId(),
                "fileName", preview.getFileName(),
                "totalRecords", preview.getTotalRecords(),
                "successRecords", preview.getValidRecords(),
                "failedRecords", preview.getInvalidRecords(),
                "message", "Bulk activity processed successfully"
        );
    }
    /*
     * Get previous bulk transactions
     *
     * Data comes directly from tbl_bulk_history.
     */
    public List<TblBulkHistory> getHistory() {

        return bulkHistoryRepository.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC,
                        "transactionDate"
                )
        );
    }


    /*
     * Get transaction details for export
     *
     * Data comes directly from tbl_bulk_history_detail.
     */
    public List<TblBulkHistoryDetail> getHistoryDetails(
            String previewId) {

        return bulkHistoryDetailRepository.findByPreviewId(
                previewId
        );
    }

    /*
     * Validate file itself
     */
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "File is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "File size must not exceed 10 MB"
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {

            throw new IllegalArgumentException(
                    "File name is missing"
            );
        }

        String lowerName =
                fileName.toLowerCase();

        if (!(lowerName.endsWith(".csv")
                || lowerName.endsWith(".xlsx")
                || lowerName.endsWith(".xls"))) {

            throw new IllegalArgumentException(
                    "Unsupported file type. Only CSV, XLSX and XLS are supported."
            );
        }
    }

    /*
     * CSV
     */
    private List<BulkRecordResponse> readCsvFile(
            MultipartFile file) throws Exception {

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        file.getInputStream(),
                                        StandardCharsets.UTF_8))
        ) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null
                    || headerLine.isBlank()) {

                throw new IllegalArgumentException(
                        "File is empty"
                );
            }

            List<String> headers =
                    Arrays.stream(
                                    headerLine.split(",", -1))
                            .map(String::trim)
                            .toList();

            validateHeaders(headers);

            List<BulkRecordResponse> records =
                    new ArrayList<>();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] values =
                        line.split(",", -1);

                records.add(
                        validateRecord(
                                getValue(values, 0),
                                getValue(values, 1),
                                getValue(values, 2),
                                getValue(values, 3),
                                getValue(values, 4)
                        )
                );
            }

            return records;
        }
    }

    /*
     * XLS / XLSX
     */
    private List<BulkRecordResponse> readExcelFile(
            MultipartFile file,
            boolean oldExcelFormat) throws Exception {

        Workbook workbook;

        if (oldExcelFormat) {

            workbook =
                    new HSSFWorkbook(
                            file.getInputStream()
                    );

        } else {

            workbook =
                    new XSSFWorkbook(
                            file.getInputStream()
                    );
        }

        try (workbook) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            if (sheet == null) {

                throw new IllegalArgumentException(
                        "No worksheet found"
                );
            }

            Row headerRow =
                    sheet.getRow(0);

            if (headerRow == null) {

                throw new IllegalArgumentException(
                        "File is empty"
                );
            }

            List<String> headers =
                    new ArrayList<>();

            for (int i = 0;
                 i < REQUIRED_HEADERS.size();
                 i++) {

                Cell cell =
                        headerRow.getCell(i);

                headers.add(
                        cell == null
                                ? ""
                                : getCellValue(cell).trim()
                );
            }

            validateHeaders(headers);

            List<BulkRecordResponse> records =
                    new ArrayList<>();

            for (int i = 1;
                 i <= sheet.getLastRowNum();
                 i++) {

                Row row =
                        sheet.getRow(i);

                if (row == null
                        || isEmptyRow(row)) {

                    continue;
                }

                records.add(
                        validateRecord(
                                getCellValue(
                                        row.getCell(0)),
                                getCellValue(
                                        row.getCell(1)),
                                getCellValue(
                                        row.getCell(2)),
                                getCellValue(
                                        row.getCell(3)),
                                getCellValue(
                                        row.getCell(4))
                        )
                );
            }

            return records;
        }
    }

    /*
     * Header validation
     */
    private void validateHeaders(
            List<String> headers) {

        if (headers.size()
                != REQUIRED_HEADERS.size()) {

            throw new IllegalArgumentException(
                    "File must contain exactly these columns: "
                            + String.join(
                            ", ",
                            REQUIRED_HEADERS)
            );
        }

        for (int i = 0;
             i < REQUIRED_HEADERS.size();
             i++) {

            if (!REQUIRED_HEADERS.get(i)
                    .equalsIgnoreCase(
                            headers.get(i).trim())) {

                throw new IllegalArgumentException(
                        "Invalid column at position "
                                + (i + 1)
                                + ". Expected '"
                                + REQUIRED_HEADERS.get(i)
                                + "'."
                );
            }
        }
    }

    /*
     * Row validation
     */
    private BulkRecordResponse validateRecord(
            String mobileNumber,
            String toneId,
            String toneName,
            String packagePlan,
            String artistName) {

        List<String> errors =
                new ArrayList<>();

        mobileNumber = clean(mobileNumber);
        toneId = clean(toneId);
        toneName = clean(toneName);
        packagePlan = clean(packagePlan);
        artistName = clean(artistName);

        /*
         * Mobile validation
         */
        if (mobileNumber == null) {

            errors.add(
                    "Mobile Number is required."
            );

        } else {

            try {

                Long.parseLong(mobileNumber);

            } catch (NumberFormatException e) {

                errors.add(
                        "Invalid Mobile Number."
                );
            }
        }

        /*
         * Tone ID validation
         */
        TblToneCatalogue tone = null;

        if (toneId == null) {

            errors.add(
                    "Tone ID is required."
            );

        } else {

            Optional<TblToneCatalogue> toneOptional =
                    toneCatalogueRepository
                            .findByToneCode(toneId);

            if (toneOptional.isEmpty()) {

                errors.add(
                        "Tone ID not found."
                );

            } else {

                tone = toneOptional.get();
            }
        }

        /*
         * Tone Name validation
         */
        if (toneName == null) {

            errors.add(
                    "Tone Name is required."
            );

        } else if (tone != null
                && (tone.getToneName() == null
                || !tone.getToneName()
                .equalsIgnoreCase(toneName))) {

            errors.add(
                    "Tone Name does not match Tone ID."
            );
        }

        /*
         * Artist validation
         */
        if (artistName == null) {

            errors.add(
                    "Artist Name is required."
            );

        } else if (tone != null
                && (tone.getArtistName() == null
                || !tone.getArtistName()
                .equalsIgnoreCase(artistName))) {

            errors.add(
                    "Artist Name does not match Tone ID."
            );
        }

        /*
         * Package validation
         *
         * Bulk flow is Try N Buy.
         */
        if (packagePlan == null) {

            errors.add(
                    "Package Plan is required."
            );

        } else if (!normalizePackagePlan(
                packagePlan
        ).equals("trybuy")) {

            errors.add(
                    "Package Plan must be TryBuy."
            );
        }

        return new BulkRecordResponse(
                mobileNumber,
                toneId,
                toneName,
                packagePlan,
                artistName,
                errors.isEmpty(),
                errors
        );
    }

    private String getValue(
            String[] values,
            int index) {

        if (index >= values.length) {
            return null;
        }

        return values[index];
    }

    private String getCellValue(
            Cell cell) {

        if (cell == null) {
            return "";
        }

        DataFormatter formatter =
                new DataFormatter();

        return formatter
                .formatCellValue(cell)
                .trim();
    }

    private boolean isEmptyRow(
            Row row) {

        for (int i = 0;
             i < REQUIRED_HEADERS.size();
             i++) {

            Cell cell =
                    row.getCell(i);

            if (cell != null
                    && !getCellValue(cell)
                    .isBlank()) {

                return false;
            }
        }

        return true;
    }

    private String clean(
            String value) {

        if (value == null) {
            return null;
        }

        value = value.trim();

        return value.isBlank()
                ? null
                : value;
    }

    private String generatePreviewId() {
        int number = new Random().nextInt(10000);
        return String.format("ID-%04d", number);
    }

    private String normalizePackagePlan(
            String packagePlan) {

        if (packagePlan == null) {
            return "";
        }

        return packagePlan
                .trim()
                .replaceAll("[\\s_-]+", "")
                .toLowerCase();
    }
    public byte[] exportHistory() {

        List<TblBulkHistoryDetail> details =
                bulkHistoryDetailRepository.findAll();

        if (details == null || details.isEmpty()) {
            return new byte[0];
        }

        StringBuilder csv = new StringBuilder();

        csv.append(
                "ID,Preview ID,Mobile Number,Tone ID,Tone Name,Artist Name,Package Plan,Status,Message,Transaction Date\n"
        );

        for (TblBulkHistoryDetail detail : details) {

            csv.append(csvValue(detail.getId())).append(",");
            csv.append(csvValue(detail.getPreviewId())).append(",");
            csv.append(csvValue(detail.getMobile())).append(",");
            csv.append(csvValue(detail.getToneId())).append(",");
            csv.append(csvValue(detail.getToneName())).append(",");
            csv.append(csvValue(detail.getArtistName())).append(",");
            csv.append(csvValue(detail.getPackagePlan())).append(",");
            csv.append(
                    csvValue(
                            detail.getStatus() != null &&
                                    detail.getStatus() == 1
                                    ? "SUCCESS"
                                    : "FAILED"
                    )
            ).append(",");
            csv.append(csvValue(detail.getMessage())).append(",");
            csv.append(csvValue(detail.getTransactionDate())).append("\n");
        }

        return csv.toString()
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    private String csvValue(Object value) {

        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);

        if (text.contains(",")
                || text.contains("\"")
                || text.contains("\n")
                || text.contains("\r")) {

            text = text.replace("\"", "\"\"");

            return "\"" + text + "\"";
        }

        return text;
    }
}