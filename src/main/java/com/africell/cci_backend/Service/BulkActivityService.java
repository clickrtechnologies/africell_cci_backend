package com.africell.cci_backend.Service;

import com.africell.cci_backend.Entity.TblToneCatalogue;
import com.africell.cci_backend.Repository.TblToneCatalogueRepository;
import com.africell.cci_backend.dto.response.BulkPreviewResponse;
import com.africell.cci_backend.dto.response.BulkRecordResponse;
import com.africell.cci_backend.dto.response.BulkUploadResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    /*
     * Temporary storage between Upload and Process.
     *
     * This is only for the upload/preview stage.
     * tbl_bulk_history is NOT touched here.
     */
    private final Map<String, BulkPreviewResponse> previewStore =
            new ConcurrentHashMap<>();

    public BulkActivityService(
            TblToneCatalogueRepository toneCatalogueRepository) {

        this.toneCatalogueRepository = toneCatalogueRepository;
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

            BulkPreviewResponse preview = new BulkPreviewResponse(
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

        String lowerName = fileName.toLowerCase();

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

            String headerLine = reader.readLine();

            if (headerLine == null || headerLine.isBlank()) {

                throw new IllegalArgumentException(
                        "File is empty"
                );
            }

            List<String> headers =
                    Arrays.stream(headerLine.split(",", -1))
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

                String[] values = line.split(",", -1);

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

            workbook = new HSSFWorkbook(
                    file.getInputStream()
            );

        } else {

            workbook = new XSSFWorkbook(
                    file.getInputStream()
            );
        }

        try (workbook) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {

                throw new IllegalArgumentException(
                        "No worksheet found"
                );
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {

                throw new IllegalArgumentException(
                        "File is empty"
                );
            }

            List<String> headers = new ArrayList<>();

            for (int i = 0; i < REQUIRED_HEADERS.size(); i++) {

                Cell cell = headerRow.getCell(i);

                headers.add(
                        cell == null
                                ? ""
                                : getCellValue(cell).trim()
                );
            }

            validateHeaders(headers);

            List<BulkRecordResponse> records =
                    new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                records.add(
                        validateRecord(
                                getCellValue(row.getCell(0)),
                                getCellValue(row.getCell(1)),
                                getCellValue(row.getCell(2)),
                                getCellValue(row.getCell(3)),
                                getCellValue(row.getCell(4))
                        )
                );
            }

            return records;
        }
    }

    /*
     * Header validation
     */
    private void validateHeaders(List<String> headers) {

        if (headers.size() != REQUIRED_HEADERS.size()) {

            throw new IllegalArgumentException(
                    "File must contain exactly these columns: "
                            + String.join(", ", REQUIRED_HEADERS)
            );
        }

        for (int i = 0; i < REQUIRED_HEADERS.size(); i++) {

            if (!REQUIRED_HEADERS.get(i)
                    .equalsIgnoreCase(headers.get(i).trim())) {

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

        List<String> errors = new ArrayList<>();

        mobileNumber = clean(mobileNumber);
        toneId = clean(toneId);
        toneName = clean(toneName);
        packagePlan = clean(packagePlan);
        artistName = clean(artistName);

        /*
         * Mobile validation
         */
        if (mobileNumber == null) {

            errors.add("Mobile Number is required.");

        } else {

            try {
                Long.parseLong(mobileNumber);
            } catch (NumberFormatException e) {
                errors.add("Invalid Mobile Number.");
            }
        }

        /*
         * Tone ID validation
         */
        TblToneCatalogue tone = null;

        if (toneId == null) {

            errors.add("Tone ID is required.");

        } else {

            Optional<TblToneCatalogue> toneOptional =
                    toneCatalogueRepository.findByToneCode(toneId);

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
         * For this bulk flow, the operation is
         * Try N Buy.
         */
        if (packagePlan == null) {

            errors.add(
                    "Package Plan is required."
            );

        } else if (!normalizePackagePlan(packagePlan).equals("trybuy")) {

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

    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        DataFormatter formatter =
                new DataFormatter();

        return formatter.formatCellValue(cell).trim();
    }

    private boolean isEmptyRow(Row row) {

        for (int i = 0; i < REQUIRED_HEADERS.size(); i++) {

            Cell cell = row.getCell(i);

            if (cell != null
                    && !getCellValue(cell).isBlank()) {

                return false;
            }
        }

        return true;
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        value = value.trim();

        return value.isBlank()
                ? null
                : value;
    }

    private String generatePreviewId() {

        return "ID-"
                + System.currentTimeMillis()
                % 100000000;
    }
    private String normalizePackagePlan(String packagePlan) {

        if (packagePlan == null) {
            return "";
        }

        return packagePlan
                .trim()
                .replaceAll("[\\s_-]+", "")
                .toLowerCase();
    }
}
