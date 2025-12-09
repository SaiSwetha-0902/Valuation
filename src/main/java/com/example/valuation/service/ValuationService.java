package com.example.valuation.service;

import com.example.valuation.dto.CanonicalTradeDTO;
import com.example.valuation.dto.NavRecordDTO;
import com.example.valuation.entity.Valuation;
import com.example.valuation.dao.ValuationDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ValuationService {

    @Autowired
    private NavService navService;

    @Autowired
    private ValuationDao valuationDao;

    public Valuation processValuation(CanonicalTradeDTO trade) {

        // 1️⃣ Fetch NAV
        NavRecordDTO nav = navService.getNavByFundId(trade.getFundNumber());
        LocalDate navDate = LocalDate.parse(nav.getDate());
        LocalDate tradeDate = trade.getTradeDateTime().toLocalDate();

        // 2️⃣ Determine CalculatedBy
        String calculatedBy;
        if (tradeDate.equals(navDate)) {
            calculatedBy = "CURRENT_NAV";
        } else if (tradeDate.minusDays(1).equals(navDate)) {
            calculatedBy = "PREVIOUS_NAV";
        } else {
            throw new RuntimeException(
                "Invalid NAV date for trade. NAV date: " + navDate + 
                " | Trade date: " + tradeDate);
        }

        BigDecimal navValue = BigDecimal.valueOf(nav.getNav());
        BigDecimal finalShareQty;
        BigDecimal finalDollarAmt;

        // 3️⃣ BUY / SELL valuation logic
        if ("BUY".equalsIgnoreCase(trade.getTransactionType())) {

            if (trade.getDollarAmount() == null) {
                throw new RuntimeException("BUY requires dollarAmount to be provided.");
            }

            finalDollarAmt = trade.getDollarAmount();

            // shares = amount / NAV
            finalShareQty = finalDollarAmt.divide(navValue, 6, RoundingMode.HALF_UP);

        } else if ("SELL".equalsIgnoreCase(trade.getTransactionType())) {

            if (trade.getShareQuantity() == null) {
                throw new RuntimeException("SELL requires shareQuantity to be provided.");
            }

            finalShareQty = trade.getShareQuantity();

            // amount = shares × NAV
            finalDollarAmt = finalShareQty.multiply(navValue)
                                          .setScale(6, RoundingMode.HALF_UP);

        } else {
            throw new RuntimeException("Invalid transactionType: " + trade.getTransactionType());
        }

        BigDecimal valuationAmount = finalDollarAmt;

        // 4️⃣ Save final valuation object
        Valuation val = new Valuation();
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

        return valuationDao.save(val);
    }
}
