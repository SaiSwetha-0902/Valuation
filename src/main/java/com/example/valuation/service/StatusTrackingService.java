package com.example.valuation.service;

import com.example.valuation.dao.ValuationOutboxDao;
import com.example.valuation.entity.ValuationOutboxEntity;
import com.example.valuation.dto.CanonicalTradeDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.stream.StreamRecords;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.beans.factory.annotation.Value;  

@Service
public class StatusTrackingService {

    @Autowired
    private ValuationOutboxDao outboxRepository;
    @Autowired  
    private StringRedisTemplate redisTemplate; 

    @Value("${app.redis.stream:status-stream}")
    private String streamKey;


    public void trackStatus(CanonicalTradeDTO trade, Exception e) 
    {
        Map<String, String> payload = new HashMap<>();
        
        if(trade.getFileId()!=null) {
            payload.put("fileid", trade.getFileId().toString());
        }else {
        	payload.put("fileid", null);
        }

        payload.put("distributorId", trade.getFirmNumber().toString());
        payload.put("orderId", trade.getRawOrderId().toString());
        payload.put("sourceservice", "valuation-service");
        if(e != null) 
        {
            payload.put("status", e.getMessage());
            
            String payloadJson = "{";
            boolean first = true;
            for (Map.Entry<String, String> entry : payload.entrySet()) {
                if (!first) payloadJson += ",";
                payloadJson += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                first = false;
            }
            payloadJson += "}";

            Map<String, String> streamData = new HashMap<>();
            streamData.put("payload", payloadJson);

            RecordId recordId = redisTemplate.opsForStream().add(
                StreamRecords.newRecord()
                    .in(streamKey)
                    .ofMap(streamData)
            );

        }else
        {  
            payload.put("status", "VALUATED");
            
            String payloadJson = "{";
            boolean first = true;
            for (Map.Entry<String, String> entry : payload.entrySet()) {
                if (!first) payloadJson += ",";
                payloadJson += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                first = false;
            }
            payloadJson += "}";

            Map<String, String> streamData = new HashMap<>();
            streamData.put("payload", payloadJson);

            RecordId recordId = redisTemplate.opsForStream().add(
                StreamRecords.newRecord()
                    .in(streamKey)
                    .ofMap(streamData)
            );

            if(recordId != null) {
               Optional<ValuationOutboxEntity> outboxOpt = outboxRepository.findByFileId(trade.getId());
                if(outboxOpt.isPresent()) {
                    ValuationOutboxEntity outbox = outboxOpt.get();
                    outbox.setStatus("PENDING");
                    outboxRepository.save(outbox);
                }
            } 

        }
    }


}
