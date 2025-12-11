package com.example.valuation.dao;

import com.example.valuation.entity.ValuationOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValuationOutboxDao extends JpaRepository<ValuationOutboxEntity, UUID> {
	/*
     Optional<ValuationOutboxEntity> findByFirmNumberAndRawOrderIdAndTransactionIdAndFileId(
        Integer firmNumber,      // matches DTO.firmNumber
        UUID rawOrderId,         // matches DTO.rawOrderId  
        String transactionId,    // matches DTO.transactionId
        UUID fileId              // matches DTO.fileId
    );
    */
	
	Optional<ValuationOutboxEntity> findByOutboxId(UUID id);
}
