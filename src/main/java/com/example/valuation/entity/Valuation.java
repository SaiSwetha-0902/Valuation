package com.example.valuation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;



@Entity
@Table(name = "valuation", 
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
public class Valuation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "originator_type")
    private Integer originatorType;
    @Column(name = "firm_number")
    private Integer firmNumber;
    @Column(name = "fund_number")
    private Integer fundNumber;
    @Column(name = "transaction_type")
    private String transactionType;
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "raw_order_id")
    private UUID rawOrderId; 
    
    @Column(name = "file_id")
    private UUID fileId; 
    
    @Column(name = "order_source")
    private String orderSource;  
    
    @Column(name = "trade_datetime")
    private LocalDateTime tradeDateTime;
    @Column(name = "dollar_amount")
    private BigDecimal dollarAmount;
    @Column(name = "client_account_no")
    private Integer clientAccountNo;
    @Column(name = "client_name")
    private String clientName;
    @Column(name = "ssn")
    private String ssn;
    @Column(name = "dob")
    private LocalDate dob;
    @Column(name = "share_quantity")
    private BigDecimal shareQuantity;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "valuation_amount")
    private BigDecimal valuationAmount;

    @Column(name = "valuation_date")
    private LocalDate valuationDate;

    @Column(name = "caluclatedBy")
    private String caluclatedBy;

}