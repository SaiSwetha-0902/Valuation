package com.example.valuation.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CanonicalTradeDTO {

    private UUID id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeDateTime;
    private Integer originatorType;
    private Integer firmNumber;
    private Integer fundNumber;
    private String transactionType;
    private String transactionId;

    private UUID rawOrderId;
    private UUID fileId;

    private String orderSource;

    private BigDecimal dollarAmount;
    private Integer clientAccountNo;
    private String clientName;
    private String ssn;
    private LocalDate dob;
    private BigDecimal shareQuantity;
}
