package com.example.valuation.service;

import com.example.valuation.dto.CanonicalTradeDTO;
import com.example.valuation.dto.NavRecordDTO;
import com.example.valuation.entity.ValuationEntity;
import com.example.valuation.entity.ValuationOutboxEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.valuation.dao.ValuationDao;

@Service
public class ValuationService {

    @Autowired
    private NavService navService;

    @Autowired
    private ValuationDao valuationRepository;
    
    @Autowired
    private ValuationOutboxService outboxService;

    @Transactional(rollbackFor = Exception.class)
    public ValuationOutboxEntity valuation(CanonicalTradeDTO trade) throws Exception {
        // 1) All business logic (NAV, BUY/SELL calculations)
        NavRecordDTO nav = navService.getNavByFundId(trade.getFundNumber());
        LocalDate navDate = LocalDate.parse(nav.getDate());
        LocalDate tradeDate = trade.getTradeDateTime().toLocalDate();

        String calculatedBy;
        if (tradeDate.equals(navDate)) {
            calculatedBy = "CURRENT_NAV";
        } else if (tradeDate.minusDays(1).equals(navDate)) {
            calculatedBy = "PREVIOUS_NAV";
        } else {
            throw new RuntimeException("Invalid NAV date");
        }

        BigDecimal navValue = BigDecimal.valueOf(nav.getNav());
        BigDecimal finalShareQty;
        BigDecimal finalDollarAmt;

        if ("BUY".equalsIgnoreCase(trade.getTransactionType())) {
            if (trade.getDollarAmount() == null) {
                throw new RuntimeException("BUY requires dollarAmount");
            }
            finalDollarAmt = trade.getDollarAmount();
            finalShareQty = finalDollarAmt.divide(navValue, 6, RoundingMode.HALF_UP);
        } else if ("SELL".equalsIgnoreCase(trade.getTransactionType())) {
            if (trade.getShareQuantity() == null) {
                throw new RuntimeException("SELL requires shareQuantity");
            }
            finalShareQty = trade.getShareQuantity();
            finalDollarAmt = finalShareQty.multiply(navValue)
                                          .setScale(6, RoundingMode.HALF_UP);
        } else {
            throw new RuntimeException("Invalid transactionType");
        }

        BigDecimal valuationAmount = finalDollarAmt;

        // 2) Build and save ValuationEntity (JPA)
        ValuationEntity val = new ValuationEntity();
        val.setCreatedAt(LocalDateTime.now());
        val.setOriginatorType(trade.getOriginatorType());
        val.setFirmNumber(trade.getFirmNumber());
        val.setFundNumber(trade.getFundNumber());
        val.setTransactionType(trade.getTransactionType());
        val.setTransactionId(trade.getTransactionId());
        val.setRawOrderId(trade.getRawOrderId());
        val.setFileId(trade.getFileId());
        val.setOrderSource(trade.getOrderSource());
        val.setTradeDateTime(trade.getTradeDateTime());
        val.setDollarAmount(finalDollarAmt);
        val.setClientAccountNo(trade.getClientAccountNo());
        val.setClientName(trade.getClientName());
        val.setSsn(trade.getSsn());
        val.setDob(trade.getDob());
        val.setShareQuantity(finalShareQty);
        val.setRequestId(trade.getRequestId());
        val.setValuationAmount(valuationAmount);
        val.setValuationDate(navDate);
        val.setCaluclatedBy(calculatedBy);

        ValuationEntity savedTrade = valuationRepository.save(val);

        // 3) Save Outbox (status = NEW) in same transaction
        ValuationOutboxEntity savedOutbox = outboxService.createOutboxEntry(savedTrade);
        return savedOutbox;
    }
}

