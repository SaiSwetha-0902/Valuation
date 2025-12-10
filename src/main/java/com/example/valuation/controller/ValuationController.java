package com.example.valuation.controller;


import com.example.valuation.dto.CanonicalTradeDTO;
import com.example.valuation.entity.ValuationOutboxEntity;
import com.example.valuation.service.ValuationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/valuation")
public class ValuationController {

    @Autowired
    private ValuationService valuationService;

    @PostMapping("/process")
    public ResponseEntity<ValuationOutboxEntity> process(@RequestBody CanonicalTradeDTO dto) {
    try {
        ValuationOutboxEntity valuation = valuationService.valuation(dto);
        return ResponseEntity.ok(valuation);
    } catch (Exception e) {
        // e.g. convert to 500 or custom error response
        throw new RuntimeException("Error processing valuation", e);
    }
}

}
