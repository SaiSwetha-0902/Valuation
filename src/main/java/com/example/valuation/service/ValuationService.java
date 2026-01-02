package com.example.valuation.service;

import com.example.valuation.dto.CanonicalTradeDTO;
import com.example.valuation.dto.NavRecordDTO;
import com.example.valuation.entity.ValuationEntity;
import com.example.valuation.entity.ValuationStatus;
import com.example.valuation.dao.ValuationDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValuationService {
	
	private static final Logger logger = LoggerFactory.getLogger(ValuationService.class);

    @Autowired
    private NavService navService;

    @Autowired
    private ValuationDao valuationDao;

    /* ================= SINGLE ================= */

    @Transactional
    public ValuationEntity valuation(CanonicalTradeDTO trade) {
        ValuationEntity entity = buildEntity(trade);
        return valuationDao.save(entity);
    }

    /* ================= BATCH ================= */

    @Transactional
    public List<ValuationEntity> valuationBatch(List<CanonicalTradeDTO> trades) {

        List<ValuationEntity> entities = new ArrayList<>();
        
        logger.info("starting batch");

        for (CanonicalTradeDTO trade : trades) {
            entities.add(buildEntity(trade));
        }

        valuationDao.saveAll(entities);
        valuationDao.flush();
        
        return entities;
    }

    /* ================= CORE LOGIC ================= */

    private ValuationEntity buildEntity(CanonicalTradeDTO trade) {
    	
    	logger.info("strating tarde"+ trade.getRequestId());

        long tradeCount = valuationDao.count() + 1;

        boolean isRejected = tradeCount % 10 == 0;

        ValuationEntity val = new ValuationEntity();
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
        val.setValuationDate(LocalDate.now());

        /* ========= REJECT FLOW ========= */
        if (isRejected) {
            val.setConfirmedStatus("REJECT");
            val.setRejectReason("ACCOUNT_SUSPENDED");

            // NAV-related fields intentionally NOT set
            val.setShareQuantity(null);
            val.setValuationAmount(null);
            val.setCaluclatedBy(null);
            val.setNavValue(null);

            return val;
        }

        /* ========= CONFIRMED FLOW ========= */
        val.setConfirmedStatus("CONFIRMED");
        val.setRejectReason(null);

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
        BigDecimal shareQty;
        BigDecimal amount;
        
        logger.info("calulcating amount/shares"+ navValue);
        String txnType = trade.getTransactionType();

        if ("B".equalsIgnoreCase(txnType)) {
            txnType = "BUY";
        } 
        else if ("S".equalsIgnoreCase(txnType)) {
        	txnType = "SELL";
        	
        }
        

        if ("BUY".equalsIgnoreCase(txnType)) {
            if (trade.getDollarAmount() == null) {
                throw new RuntimeException("BUY requires dollarAmount");
            }
            amount = trade.getDollarAmount();
            shareQty = amount.divide(navValue, 6, RoundingMode.HALF_UP);
        } else if ("SELL".equalsIgnoreCase(txnType)) {
            if (trade.getShareQuantity() == null) {
                throw new RuntimeException("SELL requires shareQuantity");
            }
            shareQty = trade.getShareQuantity();
            amount = shareQty.multiply(navValue).setScale(6, RoundingMode.HALF_UP);
        } else {
            throw new RuntimeException("Invalid transactionType");
        }

        val.setShareQuantity(shareQty);
        val.setValuationAmount(amount);
        val.setCaluclatedBy(calculatedBy);
        val.setNavValue(navValue);
        val.setStatus(ValuationStatus.NEW);
        logger.info("val entity for" + trade.getRequestId()+"......"+ val);

        return val;
    }
}
