package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CredentialGeneratorApp extends JFrame {

    private JComboBox<String> dbTypeComboBox;
    private JButton uploadButton;
    private JButton generateButton;
    private JLabel statusLabel;
    private File selectedFile;

    public CredentialGeneratorApp() {
        setTitle("Credential Generator");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Database Type Selection
        dbTypeComboBox = new JComboBox<>(new String[]{"NationsHearing", "Elevance"});
        add(new JLabel("Select DB Type:"));
        add(dbTypeComboBox);

        // Upload Button
        uploadButton = new JButton("Upload Excel File");
        uploadButton.addActionListener(new UploadButtonListener());
        add(uploadButton);

        // Generate Button
        generateButton = new JButton("Generate Credentials");
        generateButton.addActionListener(new GenerateButtonListener());
        add(generateButton);

        // Status Label
        statusLabel = new JLabel("");
        add(statusLabel);

        setVisible(true);
    }

    private class UploadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Excel File");
            int result = fileChooser.showOpenDialog(CredentialGeneratorApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                statusLabel.setText("Selected file: " + selectedFile.getAbsolutePath());
            }
        }
    }

    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFile == null) {
                statusLabel.setText("Please upload an Excel file first.");
                return;
            }

            char dbType = dbTypeComboBox.getSelectedItem().equals("NationsHearing") ? 'N' : 'E';
            try {
                DbConnector dbConnector = new DbConnector(dbType);
                ReadExcelFile excelReader = new ReadExcelFile();
                ApiConnector apiConnector = new ApiConnector();

                // Load the Excel file and extract data
                Workbook workbook = excelReader.loadExcelFile(selectedFile.getAbsolutePath());
                Sheet sheet = workbook.getSheetAt(0);
                Map<String, List<String>> extractedData = excelReader.extractColumnsData(sheet, ReadExcelFile.COLUMN_NAMES);

                // Fetch carrier IDs and connect to API
                List<Integer> carrierIds = excelReader.fetchCarrierIds(dbConnector, extractedData.get(ReadExcelFile.COLUMN_NAMES[0]));
                apiConnector.connectToApi(carrierIds,
                        extractedData.get(ReadExcelFile.COLUMN_NAMES[1]), // portalTypes
                        extractedData.get(ReadExcelFile.COLUMN_NAMES[2]), // actionTypes
                        extractedData.get(ReadExcelFile.COLUMN_NAMES[3]), // firstNames
                        extractedData.get(ReadExcelFile.COLUMN_NAMES[4]), // secondNames
                        extractedData.get(ReadExcelFile.COLUMN_NAMES[5]), // emails
                        extractedData.get(ReadExcelFile.COLUMN_NAMES[6]), // userNames
                        dbType);

                // Update the output Excel file with credentials
                excelReader.updateExcelWithCredentials(selectedFile.getAbsolutePath(), apiConnector.returnedUsernames, apiConnector.returnedPasswords);

                statusLabel.setText("Credentials generated successfully!");

            } catch (IOException ex) {
                statusLabel.setText("Error processing file: " + ex.getMessage());
            } catch (Exception ex) {
                statusLabel.setText("An error occurred: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CredentialGeneratorApp::new);
    }
}