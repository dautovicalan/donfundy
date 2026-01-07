package hr.algebra.donfundy.task;

import hr.algebra.donfundy.service.ExcelReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task for generating campaign and donation reports.
 *
 * This task runs automatically to generate Excel reports containing:
 * - Campaign information (name, goal, raised amount, status, etc.)
 * - Donation details (donor name, amount, date, payment method, etc.)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduledTask {

    private final ExcelReportService excelReportService;

    /**
     * Generates campaign report every day at midnight.
     * Schedule format: second, minute, hour, day of month, month, day of week
     *
     * Current schedule: 0 0 0 * * * (every day at 00:00:00)
     *
     * Alternative schedules you can use:
     * - Every hour: @Scheduled(cron = "0 0 * * * *")
     * - Every day at 8 AM: @Scheduled(cron = "0 0 8 * * *")
     * - Every Monday at 9 AM: @Scheduled(cron = "0 0 9 * * MON")
     * - Every first day of month: @Scheduled(cron = "0 0 0 1 * *")
     * - Every 6 hours: @Scheduled(fixedRate = 21600000) // milliseconds
     */
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

    /**
     * Alternative: Generate report weekly on Monday at 9 AM
     * Uncomment to use this instead of daily reports
     */
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

    /**
     * Alternative: Generate report monthly on the 1st at midnight
     * Uncomment to use this instead of daily reports
     */
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
