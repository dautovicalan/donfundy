package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.Donation;
import hr.algebra.donfundy.repository.CampaignRepository;
import hr.algebra.donfundy.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelReportService {

    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;

    private static final String REPORTS_DIRECTORY = "reports";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Transactional(readOnly = true)
    public String generateCampaignReport() {
        log.info("Starting campaign report generation");

        try {
            Path reportsPath = Paths.get(REPORTS_DIRECTORY);
            if (!Files.exists(reportsPath)) {
                Files.createDirectories(reportsPath);
            }

            Workbook workbook = new XSSFWorkbook();

            createCampaignsSummarySheet(workbook);
            createDonationsDetailSheets(workbook);

            String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            String filename = String.format("%s/campaign_report_%s.xlsx", REPORTS_DIRECTORY, timestamp);

            try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                workbook.write(fileOut);
            }

            workbook.close();

            log.info("Campaign report generated successfully: {}", filename);
            return filename;

        } catch (IOException e) {
            log.error("Error generating campaign report", e);
            throw new RuntimeException("Failed to generate campaign report", e);
        }
    }

    private void createCampaignsSummarySheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Campaigns Summary");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Campaign Name", "Description", "Goal Amount", "Raised Amount",
                           "Progress %", "Status", "Start Date", "End Date", "Created By", "Total Donations"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Campaign> campaigns = campaignRepository.findAll();

        int rowNum = 1;
        for (Campaign campaign : campaigns) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(campaign.getId());
            row.createCell(1).setCellValue(campaign.getName());
            row.createCell(2).setCellValue(campaign.getDescription() != null ? campaign.getDescription() : "");

            Cell goalCell = row.createCell(3);
            goalCell.setCellValue(campaign.getGoalAmount());
            goalCell.setCellStyle(currencyStyle);

            Cell raisedCell = row.createCell(4);
            raisedCell.setCellValue(campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0);
            raisedCell.setCellStyle(currencyStyle);

            double raisedAmount = campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0;
            double progress = campaign.getGoalAmount() > 0 ? (raisedAmount / campaign.getGoalAmount()) * 100 : 0;
            row.createCell(5).setCellValue(String.format("%.2f%%", progress));

            row.createCell(6).setCellValue(campaign.getStatus().toString());
            row.createCell(7).setCellValue(campaign.getStartDate().toString());
            row.createCell(8).setCellValue(campaign.getEndDate() != null ? campaign.getEndDate().toString() : "");

            String createdBy = "";
            if (campaign.getCreatedBy() != null) {
                createdBy = campaign.getCreatedBy().getFirstName() + " " + campaign.getCreatedBy().getLastName();
            }
            row.createCell(9).setCellValue(createdBy);

            List<Donation> donations = donationRepository.findByCampaignId(campaign.getId());
            row.createCell(10).setCellValue(donations.size());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDonationsDetailSheets(Workbook workbook) {
        List<Campaign> campaigns = campaignRepository.findAll();

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        for (Campaign campaign : campaigns) {
            List<Donation> donations = donationRepository.findByCampaignId(campaign.getId());

            if (donations.isEmpty()) {
                continue;
            }

            String sheetName = campaign.getName().length() > 28
                ? campaign.getName().substring(0, 28) + "..."
                : campaign.getName();
            sheetName = sheetName.replaceAll("[\\\\/:*?\\[\\]\"<>|]", "_"); // Remove invalid characters

            Sheet sheet = workbook.createSheet(sheetName);

            Row campaignInfoRow = sheet.createRow(0);
            campaignInfoRow.createCell(0).setCellValue("Campaign: " + campaign.getName());

            Row campaignGoalRow = sheet.createRow(1);
            campaignGoalRow.createCell(0).setCellValue("Goal: $" + campaign.getGoalAmount());
            campaignGoalRow.createCell(1).setCellValue("Raised: $" + (campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0));

            sheet.createRow(2);

            Row headerRow = sheet.createRow(3);
            String[] headers = {"Donation ID", "Donor Name", "Donor Email", "Amount",
                              "Donation Date", "Payment Method", "Message"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 4;
            for (Donation donation : donations) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(donation.getId());
                row.createCell(1).setCellValue(donation.getDonor().getFirstName() + " " + donation.getDonor().getLastName());
                row.createCell(2).setCellValue(donation.getDonor().getEmail());

                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(donation.getAmount().doubleValue());
                amountCell.setCellStyle(currencyStyle);

                row.createCell(4).setCellValue(donation.getDonationDate().toString());
                row.createCell(5).setCellValue(donation.getPaymentMethod().toString());
                row.createCell(6).setCellValue(donation.getMessage() != null ? donation.getMessage() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }
}
