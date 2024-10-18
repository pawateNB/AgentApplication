package org.example;

import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadExcelFile {

    private static final String DEFAULT_STRING = "string";

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

    // Extract column data with flexible cell type handling
    public static List<String> getColumnData(Sheet sheet, String columnName) {
        List<String> columnData = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        int columnIndex = -1;

        for (Cell cell : headerRow) {
            if (cell.getStringCellValue().equalsIgnoreCase(columnName)) {
                columnIndex = cell.getColumnIndex();
                break;
            }
        }

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

    public static void main(String[] args) {
        DbConnector dbConnector = new DbConnector();

        final String excelFilePath = "C:\\Users\\ParthAwate\\ExcelSheets\\CredCheck.xlsx";
        final String[] columnNames = { "CarrierName", "PortalType", "ActionType", "FirstName", "SecondName", "Email", "Username" };

        try (FileInputStream file = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<String> carrierNames = getColumnData(sheet, columnNames[0]);
            List<String> portalTypes = getColumnData(sheet, columnNames[1]);
            List<String> actionTypes = getColumnData(sheet, columnNames[2]);
            List<String> firstNames = getColumnData(sheet, columnNames[3]);
            List<String> secondNames = getColumnData(sheet, columnNames[4]);
            List<String> emails = getColumnData(sheet, columnNames[5]);
            List<String> userNames = getColumnData(sheet, columnNames[6]);

            System.out.println(carrierNames);
            System.out.println(portalTypes);
            System.out.println(actionTypes);
            System.out.println(firstNames);
            System.out.println(secondNames);
            System.out.println(emails);
            System.out.println(userNames);

            // Extract carrier IDs and proceed with API connection
            List<Integer> carrierIds = new ArrayList<>();
            for (String carrierName : carrierNames) {
                carrierIds.add(dbConnector.getCarrierId(carrierName));
            }
            System.out.println(carrierIds);
            ApiConnector apiConnector = new ApiConnector();
            apiConnector.connectToApi(carrierIds, portalTypes, actionTypes, firstNames, secondNames, emails, userNames);

        } catch (FileNotFoundException e) {
            System.err.println("Excel file not found: " + excelFilePath);
        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
        }
    }
}
