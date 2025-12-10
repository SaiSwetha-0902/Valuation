package com.example.valuation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.example.valuation.dto.NavRecordDTO;
import java.util.Map;

@Service
public class NavService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate; 

    public NavRecordDTO getNavByFundId(Integer fundId) {
        String key = fundId.toString();  
        System.out.println("Fetching NAV for fundId: " + fundId + " with key: " + key);

        Map<Object, Object> values = redisTemplate.opsForHash().entries(key);

        System.out.println("Retrieved NAV values from Redis: " + values);
        if (values == null || values.isEmpty()) {
            throw new RuntimeException("NAV not found for fundId: " + fundId);
        }

        NavRecordDTO rec = new NavRecordDTO();
        rec.setDate((String) values.get("date"));
        rec.setTime((String) values.get("time"));
        rec.setNav(Double.parseDouble((String) values.get("nav")));

        return rec;
    }
}
