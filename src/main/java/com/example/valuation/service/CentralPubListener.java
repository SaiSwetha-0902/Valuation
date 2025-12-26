package com.example.valuation.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.valuation.dto.CanonicalTradeDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.sushmithashiva04ops.centraleventpublisher.listener.DynamicOutboxListener;



@Service
public class CentralPubListener {

    private final DynamicOutboxListener outboxListener; 
    
    private static final Logger logger = LoggerFactory.getLogger(CentralPubListener.class);

    private int latestSize = 0;       // queue size from scheduler
    private int currentIndex = 0;     // next message to parse

    public CentralPubListener(DynamicOutboxListener outboxListener) {
        this.outboxListener = outboxListener;
    }
    
    @Autowired
    private ValuationService valuationService;
    
    @Autowired
    private ObjectMapper objectMapper;

    // Scheduler ONLY updates the SIZE every 1 minute
    @Scheduled(fixedRate = 60000)
    public void fetchQueueSize() {
        latestSize = outboxListener.getQueueSize("valid.mq");
        System.out.println("Updated size: " + latestSize);
    }

    // Scheduler to run every 5 seconds to process messages in queue
    @Scheduled(fixedRate = 5000)
    public void processMessagesScheduled() {
        processMessages();
    }

    // Parser method to process messages
    public void processMessages() {

        // get full list of messages (old + new)
        List<String> allMessages = outboxListener.getMessages("valid.mq");

        // if queue has no new data, stop
        if (currentIndex >= latestSize) {
            System.out.println("No new messages to parse.");
            return;
        }

        // parse from currentIndex to latestSize
        List<String> toParse = allMessages.subList(currentIndex, latestSize);

        System.out.println("Parsing messages from index " + currentIndex + " to " + (latestSize - 1));

        sendMessage(toParse);

        // move pointer forward
        currentIndex = latestSize;
    }

    public void sendMessage(List<String> messages) {
        for (String msg : messages) {
            
            System.out.println("Extracted payload: " + msg);
            
            // Payload parsing logic
            try {
				CanonicalTradeDTO order = objectMapper.readValue(msg, CanonicalTradeDTO.class);
				logger.info("Parsed payload, calling valuation service...");
				valuationService.valuation(order);
				
			} catch (JsonMappingException e) {
				logger.error("JsonMappingException: JSON conversion failed for order", e);
	            throw new RuntimeException("JSON conversion failed", e);
	            
			} catch (JsonProcessingException e) {
				logger.error("JsonProcessingException: JSON conversion failed for order", e);
	            throw new RuntimeException("JSON conversion failed", e);
	            
			}  
            
        }
    }
}

