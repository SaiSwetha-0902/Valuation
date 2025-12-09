package com.example.valuation.dao;

import com.example.valuation.entity.Valuation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ValuationDao extends JpaRepository<Valuation, UUID> {
}
