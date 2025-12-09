package com.example.valuation.controller;


import com.example.valuation.dto.CanonicalTradeDTO;
import com.example.valuation.entity.Valuation;
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
    public ResponseEntity<Valuation> process(@RequestBody CanonicalTradeDTO dto) {
        Valuation valuation = valuationService.processValuation(dto);
        return ResponseEntity.ok(valuation);
    }
}
