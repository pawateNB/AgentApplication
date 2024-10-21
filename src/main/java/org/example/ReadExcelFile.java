package org.example;

import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadExcelFile {

    public static final String[] COLUMN_NAMES = { "CarrierName", "PortalType", "ActionType", "FirstName", "SecondName", "Email", "Username" };;
    private static final String DEFAULT_STRING = "string";
    private static final String EXCEL_FILE_PATH = "C:\\Users\\ParthAwate\\ExcelSheets\\CredCheck.xlsx";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter the DB type you are working with \n Enter E for Elevance \n Enter N for NationsHearing \n Enter your choice:");
        char val = scanner.next().charAt(0);
        DbConnector dbConnector = new DbConnector(val);

        try (Workbook workbook = loadExcelFile(EXCEL_FILE_PATH)) {
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, List<String>> extractedData = extractColumnsData(sheet, COLUMN_NAMES);

            displayExtractedData(extractedData);

            List<Integer> carrierIds = fetchCarrierIds(dbConnector, extractedData.get(COLUMN_NAMES[0]));

            ApiConnector apiConnector = new ApiConnector();
            apiConnector.connectToApi(carrierIds,
                    extractedData.get(COLUMN_NAMES[1]), // portalTypes
                    extractedData.get(COLUMN_NAMES[2]), // actionTypes
                    extractedData.get(COLUMN_NAMES[3]), // firstNames
                    extractedData.get(COLUMN_NAMES[4]), // secondNames
                    extractedData.get(COLUMN_NAMES[5]), // emails
                    extractedData.get(COLUMN_NAMES[6]), // userNames
                    val);

        } catch (FileNotFoundException e) {
            System.err.println("Excel file not found: " + EXCEL_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
        }
    }

    // Load Excel file
    static Workbook loadExcelFile(String excelFilePath) throws IOException {
        try (FileInputStream file = new FileInputStream(new File(excelFilePath))) {
            return new XSSFWorkbook(file);
        }
    }

    // Extract column data for all specified column names
    static Map<String, List<String>> extractColumnsData(Sheet sheet, String[] columnNames) {
        Map<String, List<String>> dataMap = new HashMap<>();
        for (String columnName : columnNames) {
            List<String> columnData = getColumnData(sheet, columnName);
            dataMap.put(columnName, columnData);
        }
        return dataMap;
    }

    // Display extracted data for each column
    private static void displayExtractedData(Map<String, List<String>> extractedData) {
        for (Map.Entry<String, List<String>> entry : extractedData.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Fetch carrier IDs using dbConnector
    static List<Integer> fetchCarrierIds(DbConnector dbConnector, List<String> carrierNames) {
        List<Integer> carrierIds = new ArrayList<>();
        for (String carrierName : carrierNames) {
            carrierIds.add(dbConnector.getCarrierId(carrierName));
        }
        System.out.println("Carrier IDs: " + carrierIds);
        return carrierIds;
    }

    // Extract column data with flexible cell type handling
    public static List<String> getColumnData(Sheet sheet, String columnName) {
        List<String> columnData = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        int columnIndex = getColumnIndex(headerRow, columnName);

        if (columnIndex == -1) {
            System.out.println("Could not find column: " + columnName);
            return columnData;
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(columnIndex);
                columnData.add(getCellValue(cell));
            } else {
                columnData.add(DEFAULT_STRING);
            }
        }
        return columnData;
    }

    // Get the column index for a given column name
    private static int getColumnIndex(Row headerRow, String columnName) {
        for (Cell cell : headerRow) {
            if (cell.getStringCellValue().equalsIgnoreCase(columnName)) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    // Improved method to handle any CellType
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return DEFAULT_STRING;
        }

        switch (cell.getCellType()) {
            case STRING:
                return sanitizeCellValue(cell.getStringCellValue());
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return DEFAULT_STRING;
        }
    }

    // Sanitize string value from the cell
    private static String sanitizeCellValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return DEFAULT_STRING;
        }

        switch (value.trim()) {
            case "Create Credentials":
                return "new";
            case "Delete and Create New":
                return "reset";
            case "Delete Credentials":
                return "delete";
            default:
                return value.trim();
        }
    }

    public void updateExcelWithCredentials(String excelFilePath, List<String> usernames, List<String> passwords) {
        try (FileInputStream file = new FileInputStream(new File(excelFilePath))) {
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            int usernameColumn = findOrCreateColumn(sheet, "Username");
            int passwordColumn = findOrCreateColumn(sheet, "Password");

            writeCredentialsToSheet(sheet, usernames, passwords, usernameColumn, passwordColumn);

            try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(outputStream);
            }
            workbook.close();
            System.out.println("Excel file updated successfully!");
        } catch (IOException e) {
            System.err.println("Error updating Excel file: " + e.getMessage());
        }
    }

    private void writeCredentialsToSheet(Sheet sheet, List<String> usernames, List<String> passwords, int usernameColumn, int passwordColumn) {
        for (int i = 0; i < usernames.size(); i++) {
            Row row = sheet.getRow(i + 1); // Skip header row
            if (row == null) {
                row = sheet.createRow(i + 1);
            }

            // Set username and password in their respective columns
            row.createCell(usernameColumn).setCellValue(usernames.get(i));
            row.createCell(passwordColumn).setCellValue(passwords.get(i));
        }
    }

    private int findOrCreateColumn(Sheet sheet, String columnName) {
        Row headerRow = sheet.getRow(0);
        for (Cell cell : headerRow) {
            if (cell.getStringCellValue().equalsIgnoreCase(columnName)) {
                return cell.getColumnIndex();
            }
        }

        // If column does not exist, create it at the end
        int newColumnIndex = headerRow.getLastCellNum();
        headerRow.createCell(newColumnIndex).setCellValue(columnName);
        return newColumnIndex;
    }
}
