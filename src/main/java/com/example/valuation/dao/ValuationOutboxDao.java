package com.example.valuation.dao;

import com.example.valuation.entity.ValuationOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ValuationOutboxDao extends JpaRepository<ValuationOutboxEntity, UUID> {
}
