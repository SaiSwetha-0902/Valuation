package com.example.valuation.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.valuation.dao.ValuationOutboxDao;
import com.example.valuation.entity.ValuationEntity;
import com.example.valuation.entity.ValuationOutboxEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;


@Service
public class ValuationOutboxService {
    @Autowired
    private ValuationOutboxDao outboxDao;

    @Autowired
    private ObjectMapper objectMapper;

    // Saving single entry in outbox
    @Transactional
    public ValuationOutboxEntity createOutboxEntry(ValuationEntity valuationTrade) {
    	// Convert saved trade to JSON for outbox payload
        final String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(valuationTrade);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert trade to JSON", e);
        }

        // Store in outbox
        ValuationOutboxEntity outbox = new ValuationOutboxEntity();

        outbox.setAggregateId(valuationTrade.getId());
        outbox.setPayload(payloadJson);
        outbox.setStatus("NEW");
        outbox.setCreatedAt(LocalDateTime.now());
        outbox.setRetryCount(0);
        outbox.setLastAttemptAt(null);
        
        outboxDao.save(outbox);

        return outbox;
    }
}