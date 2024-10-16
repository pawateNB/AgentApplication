package org.example;

import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ReadExcelFile {
    private static String getString(Cell cell) {
        String value = (String) cell.getStringCellValue();
        String word="string";
        if(value.equalsIgnoreCase("Create Credentials")) {
            word = "new";
        } else if (value.equals("Delete and Create New")) {
            word = "reset";
        } else if (value.equals("Delete Credentials")) {
            word = "delete";
        } else if (value.isEmpty()) {
            word = "string";
        }else{
            word = value.trim();
        }
        return (word.equals(""))?"string":word;
    }
    //ColumnData Extraction Method
    public static ArrayList<String> getColumnData(Sheet sheet,String columnName,CellType cellType){
        ArrayList<String> columnData = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        int columnIndex=-1;

        for(Cell cell : headerRow){
            if(cell.getStringCellValue().equalsIgnoreCase(columnName)){
                columnIndex = cell.getColumnIndex();
                break;
            }
        }
        if(columnIndex==-1){
            System.out.println("Could not find"+columnName);
            return columnData;
        }

        for(int i=1;i<=sheet.getLastRowNum();i++){
            Row row = sheet.getRow(i);
            if(row!=null){
                Cell cell = row.getCell(columnIndex);
                if(cell!=null && cell.getCellType() == cellType){
                    String word = getString(cell);
                    columnData.add(word);
                } else if (cell == null){
                    columnData.add("string");
                }
            }
        }
        return columnData;
    }

    public static void main(String[] args) {
        DbConnector dbConnector = new DbConnector();
        String excelFilePath = "C:\\Users\\ParthAwate\\Downloads\\credentials.xlsx";
        String columnName1 = "CarrierName";
        String columnName2 = "PortalType";
        String columnName3 = "ActionType";
        String columnName4 = "FirstName";
        String columnName5 = "SecondName";
        String columnName6 = "Email";
        String columnName7 = "Username";
        ArrayList<String> carrierNames = new ArrayList<>();
        ArrayList<String> firstNames = new ArrayList<>();
        ArrayList<String> secondNames = new ArrayList<>();
        ArrayList<String> emails = new ArrayList<>();
        ArrayList<String> portalTypes = new ArrayList<>();
        ArrayList<String> actionTypes = new ArrayList<>();
        ArrayList<String> userNames = new ArrayList<>();

        try(
                FileInputStream file = new FileInputStream(new File(excelFilePath));
                Workbook workbook = new XSSFWorkbook(file)
                ){
            Sheet sheet = workbook.getSheetAt(0);
            carrierNames = getColumnData(sheet,columnName1,CellType.STRING);
            portalTypes = getColumnData(sheet,columnName2,CellType.STRING);
            actionTypes = getColumnData(sheet,columnName3,CellType.STRING);
            firstNames = getColumnData(sheet,columnName4,CellType.STRING);
            secondNames = getColumnData(sheet,columnName5,CellType.STRING);
            emails = getColumnData(sheet,columnName6,CellType.STRING);
            userNames = getColumnData(sheet,columnName7,CellType.STRING);

            System.out.println(carrierNames);
            System.out.println(portalTypes);
            System.out.println(actionTypes);
            System.out.println(firstNames);
            System.out.println(secondNames);
            System.out.println(emails);
            System.out.println(userNames);
        } catch(Exception e){
            e.printStackTrace();
        }

        ArrayList<Integer> carrierIds = new ArrayList<>();
        for(String carriername:carrierNames){
            carrierIds.add(dbConnector.getCarrierId(carriername));
        }


        ApiConnector apiConnector = new ApiConnector();
        apiConnector.connectToApi(carrierIds,portalTypes,actionTypes,firstNames,secondNames,emails,userNames);
    }
}
