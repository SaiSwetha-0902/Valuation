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


    @Transactional
    public ValuationOutboxEntity createOutboxEntry(ValuationEntity valuationTrade) {

        System.out.println("Creating outbox entry for Valuation ID: " + valuationTrade.getId());
        final String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(valuationTrade);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert trade to JSON", e);
        }
        ValuationOutboxEntity outbox = new ValuationOutboxEntity();

        outbox.setOutboxId(valuationTrade.getId());
        outbox.setPayload(payloadJson);
        outbox.setStatus("NEW");
        outbox.setCreatedAt(LocalDateTime.now());
        outbox.setRetryCount(0);
        outbox.setLastAttemptAt(null);
        outboxDao.save(outbox);

        return outbox;
    }
}