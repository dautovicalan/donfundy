package hr.algebra.donfundy.task;

import hr.algebra.donfundy.service.ExcelReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduledTask {

    private final ExcelReportService excelReportService;


    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void generateDailyCampaignReport() {
        log.info("Starting scheduled campaign report generation");

        try {
            String reportPath = excelReportService.generateCampaignReport();
            log.info("Scheduled report generation completed successfully. Report saved at: {}", reportPath);
        } catch (Exception e) {
            log.error("Error during scheduled report generation", e);
        }
    }
}
