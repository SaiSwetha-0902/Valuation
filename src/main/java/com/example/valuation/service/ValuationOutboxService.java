package com.example.valuation.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.valuation.dao.ValuationOutboxDao;
import com.example.valuation.entity.ValuationEntity;
import com.example.valuation.entity.ValuationOutboxEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
public class ValuationOutboxService {
	private static final Logger logger = LoggerFactory.getLogger(ValuationOutboxService.class);
	
    @Autowired
    private ValuationOutboxDao outboxDao;
    
    @Autowired
    private ObjectMapper objectMapper;

    private static final String BUCKET_NAME = "recon-buckets";

    @Transactional
    public ValuationOutboxEntity createOutboxEntry(ValuationEntity valuationOrder, String S3ObjectKey) {
    	String payloadJson = "";

        System.out.println("Creating outbox entry for Valuation file..");

        ValuationOutboxEntity outbox = new ValuationOutboxEntity();
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("fundHouseId", valuationOrder.getFundNumber());
        payload.put("tradeDate", valuationOrder.getTradeDateTime().toLocalDate().toString());
        payload.put("bucketName", BUCKET_NAME);
        payload.put("s3ObjectKey", S3ObjectKey); // Key is file name

        try {
			payloadJson = objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			logger.error("JSON conversion failed for S3 Object Key: {}", S3ObjectKey, e);
            throw new RuntimeException("JSON conversion failed", e);
		}

        outbox.setPayload(payloadJson);
        outbox.setStatus("NEW");
        outbox.setCreatedAt(LocalDateTime.now());
        outbox.setRetryCount(0);
        outbox.setLastAttemptAt(null);
        outboxDao.save(outbox);

        return outbox;
    }
}