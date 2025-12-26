package com.example.valuation.fileservice;

import com.example.valuation.dao.ValuationDao;
import com.example.valuation.entity.ValuationEntity;
import com.example.valuation.service.ValuationOutboxService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DailyFileScheduler {

    @Autowired
    private ValuationDao valuationDao;
    
    @Autowired
    private ValuationOutboxService valuationOutboxService;

    @Autowired
    private S3Client s3Client;

    private static final String BUCKET_NAME = "recon-buckets";

//@Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(fixedDelay = 1000)
    public void runDailyJob() {

        try {
            LocalDate today = LocalDate.now();
            log.info("Starting daily valuation export for date {}", today);

            List<ValuationEntity> records =
                    valuationDao.findByValuationDate(today);

            if (records.isEmpty()) {
                log.warn("No valuation records found for {}", today);
                return;
            }

            Map<Integer, List<ValuationEntity>> recordsByFund =
                    records.stream()
                           .collect(Collectors.groupingBy(
                                   ValuationEntity::getFundNumber));

            for (Map.Entry<Integer, List<ValuationEntity>> entry
                    : recordsByFund.entrySet()) 
            {

                Integer fundNumber = entry.getKey();
                List<ValuationEntity> fundRecords = entry.getValue();

                String fundStr = String.format("%04d", fundNumber);
                String filename =
                        "valuation_" + today + "_fund_" + fundStr + ".txt";

                if (s3FileExists(filename)) {
                    log.info("File already exists in S3: {} (skipping)", filename);
                    continue;
                }else {
                	// Store in outbox, needs to be transactional
                	valuationOutboxService.createOutboxEntry(fundRecords.get(0), filename);
                }

                File file = new File(filename);

                try (BufferedWriter writer =
                             new BufferedWriter(new FileWriter(file))) {

                    for (ValuationEntity rec : fundRecords) {
                        writer.write(buildLine(rec));
                        writer.newLine();
                    }
                }

                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(BUCKET_NAME)
                                .key(filename)
                                .build(),
                        Paths.get(file.getAbsolutePath())
                );

                log.info("Uploaded valuation file {} for fund {}",
                        filename, fundNumber);
            }

        } catch (Exception ex) {
            log.error("Daily valuation export failed", ex);
        }
    }

    private String buildLine(ValuationEntity rec) {

        boolean rejected =
                rec.getRejectReason() != null &&
                !rec.getRejectReason().isBlank();

        String recordType   = rejected ? "120" : "005";
        String orderRef     = padRight(rec.getRawOrderId().toString(), 36);
        String distId       = padLeftNumeric(rec.getFirmNumber(), 4);
        String fundId       = padLeftNumeric(rec.getFundNumber(), 4);
        String qty          = padLeftDecimal(rec.getShareQuantity(), 10, 6);
        String nav          = padLeftDecimal(rec.getNavValue(), 10, 4);
        String amountDue    = padLeftDecimal(rec.getValuationAmount(), 10, 4);
        String currency     = padRight("USD", 3);
        String date         = rec.getTradeDateTime()
                                 .toLocalDate()
                                 .toString()
                                 .replace("-", "");
        String transRef     = padRight(rec.getTransactionId(), 16);
        String rejectReason =
                rejected ? padRight(rec.getRejectReason(), 30) : "";
        String txnType = rec.getTransactionType();
        String type = padRight(
        "SELL".equalsIgnoreCase(txnType) ? "SEL" :
        "BUY".equalsIgnoreCase(txnType) ? "BUY" : txnType, 3);
        String line =
                recordType +
                orderRef +
                distId +
                fundId +
                qty +
                nav +
                amountDue +
                currency +
                date +
                transRef +
                rejectReason+
                type;

        int expectedLength = rejected ? 137 : 107;
        if (line.length() != expectedLength) {
            throw new RuntimeException(
                    "Invalid record length " + line.length() +
                    ", expected " + expectedLength +
                    ", transactionId=" + rec.getTransactionId()
            );
        }

        return line;
    }


    private String padRight(String s, int len) {
        if (s == null) s = "";
        return String.format("%1$-" + len + "s", s).substring(0, len);
    }

    private String padLeftNumeric(Integer val, int len) {
        if (val == null) val = 0;
        return String.format("%0" + len + "d", val);
    }

    private String padLeftDecimal(BigDecimal num, int len, int decimals) {
        if (num == null) num = BigDecimal.ZERO;
        num = num.setScale(decimals);
        return String.format("%0" + len + "." + decimals + "f", num);
    }

    private boolean s3FileExists(String key) {
        try {
            s3Client.headObject(b -> b.bucket(BUCKET_NAME).key(key));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
