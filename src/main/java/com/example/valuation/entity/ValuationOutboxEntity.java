package com.example.valuation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;



@Entity
@Table(name = "valuation_outbox", 
    indexes = {
        @Index(name = "idx_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_trade_datetime", columnList = "trade_datetime"),
        @Index(name = "idx_client_account", columnList = "client_account_no")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_raw_order_transaction", columnNames = {"raw_order_id", "transaction_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValuationOutboxEntity {
     @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    
    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @NotNull
    @Column(name = "originator_type")

    private Integer originatorType;
    @NotNull
    @Column(name = "firm_number")
    private Integer firmNumber;
    @NotNull
    @Column(name = "fund_number")
    private Integer fundNumber;
    @NotNull
    @NotBlank
    @Column(name = "transaction_type")
    @NotNull
    @NotBlank
    private String transactionType;
    @NotNull
    @NotBlank
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "raw_order_id")
    @NotNull

    private UUID rawOrderId; 
    
    @Column(name = "file_id")
    @NotNull

    private UUID fileId; 
    
    @Column(name = "order_source")
    @NotNull
    @NotBlank
    private String orderSource;  
    
    @Column(name = "trade_datetime")
    @NotNull
    private LocalDateTime tradeDateTime;

    @Column(name = "dollar_amount")
    @NotNull
    private BigDecimal dollarAmount;

    @Column(name = "client_account_no")
    @NotNull
    private Integer clientAccountNo;
    @Column(name = "client_name")
    @NotNull
    @NotBlank
    private String clientName;
    @Column(name = "ssn")
    @NotNull
    @NotBlank
    private String ssn;
    @Column(name = "dob")
    @NotNull
    private LocalDate dob;
    @Column(name = "share_quantity")
    @NotNull
    private BigDecimal shareQuantity;

    @Column(name = "request_id", length = 100)
    @NotNull
    @NotBlank
    private String requestId;

// @NotNull(message = "Payload can\'t be null!")
//     @Column(name = "payload", columnDefinition = "jsonb")
//     @JdbcTypeCode(SqlTypes.JSON)
//     private String payload;  // JSON string trade order payload

    @Column(name = "valuation_amount")
      @NotNull

    private BigDecimal valuationAmount;

    @Column(name = "valuation_date") 
     @NotNull
    private LocalDate valuationDate;

    @Column(name = "caluclatedBy")
      @NotNull
    @NotBlank
    private String caluclatedBy;

    @Column(name = "status")
      @NotNull
    @NotBlank
    private String status;

}