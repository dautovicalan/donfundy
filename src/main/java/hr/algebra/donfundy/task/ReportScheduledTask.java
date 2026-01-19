package hr.algebra.donfundy.task;

import hr.algebra.donfundy.service.ExcelReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduledTask {

    private final ExcelReportService excelReportService;


    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void generateDailyCampaignReport() {
        log.info("Starting scheduled campaign report generation");

        try {
            String reportPath = excelReportService.generateCampaignReport();
            log.info("Scheduled report generation completed successfully. Report saved at: {}", reportPath);
        } catch (Exception e) {
            log.error("Error during scheduled report generation", e);
        }
    }

    // @Scheduled(cron = "0 0 9 * * MON")
    // public void generateWeeklyCampaignReport() {
    //     log.info("Starting scheduled weekly campaign report generation");
    //
    //     try {
    //         String reportPath = excelReportService.generateCampaignReport();
    //         log.info("Scheduled weekly report generation completed successfully. Report saved at: {}", reportPath);
    //     } catch (Exception e) {
    //         log.error("Error during scheduled weekly report generation", e);
    //     }
    // }

    // @Scheduled(cron = "0 0 0 1 * *")
    // public void generateMonthlyCampaignReport() {
    //     log.info("Starting scheduled monthly campaign report generation");
    //
    //     try {
    //         String reportPath = excelReportService.generateCampaignReport();
    //         log.info("Scheduled monthly report generation completed successfully. Report saved at: {}", reportPath);
    //     } catch (Exception e) {
    //         log.error("Error during scheduled monthly report generation", e);
    //     }
    // }
}
