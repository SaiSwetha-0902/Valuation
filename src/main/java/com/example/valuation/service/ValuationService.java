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
    private ValuationDao valuationDao;
    
    @Autowired
    private ValuationOutboxService outboxService;

    @Transactional
    public ValuationEntity valuation(CanonicalTradeDTO trade) 
    {
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
        BigDecimal valuationAmount;

        if ("BUY".equalsIgnoreCase(trade.getTransactionType())) {
            if (trade.getDollarAmount() == null) {
                throw new RuntimeException("BUY requires dollarAmount");
            }
            valuationAmount = trade.getDollarAmount();
            finalShareQty = valuationAmount.divide(navValue, 6, RoundingMode.HALF_UP);
        } else if ("SELL".equalsIgnoreCase(trade.getTransactionType())) {
            if (trade.getShareQuantity() == null) {
                throw new RuntimeException("SELL requires shareQuantity");
            }
            finalShareQty = trade.getShareQuantity();
            valuationAmount = finalShareQty.multiply(navValue)
                                          .setScale(6, RoundingMode.HALF_UP);
        } else {
            throw new RuntimeException("Invalid transactionType");
        }


       long tradeCount = valuationDao.count() + 1;

       
        ValuationEntity val = new ValuationEntity();
         if (tradeCount % 10 == 0) {
            val.setConfirmedStatus("REJECT");
            val.setRejectReason("ACCOUNT_SUSPENDED");
        } else {
            val.setConfirmedStatus("CONFIRMED");
            val.setRejectReason(null);
        }
        val.setId(trade.getId());
        val.setCreatedAt(trade.getCreatedAt());
        val.setOriginatorType(trade.getOriginatorType());
        val.setFirmNumber(trade.getFirmNumber());
        val.setFundNumber(trade.getFundNumber());
        val.setTransactionType(trade.getTransactionType());
        val.setTransactionId(trade.getTransactionId());
        val.setRawOrderId(trade.getRawOrderId());
        val.setFileId(trade.getFileId());
        val.setOrderSource(trade.getOrderSource());
        val.setTradeDateTime(trade.getTradeDateTime());
        val.setClientAccountNo(trade.getClientAccountNo());
        val.setClientName(trade.getClientName());
        val.setSsn(trade.getSsn());
        val.setDob(trade.getDob());
        val.setShareQuantity(finalShareQty);
        val.setValuationAmount(valuationAmount);
        val.setValuationDate(LocalDate.now());
        val.setCaluclatedBy(calculatedBy);
        val.setNavValue(navValue);
        ValuationEntity savedTrade = valuationDao.save(val);
        //ValuationOutboxEntity savedOutbox = outboxService.createOutboxEntry(savedTrade);
        return savedTrade;
    }
}

