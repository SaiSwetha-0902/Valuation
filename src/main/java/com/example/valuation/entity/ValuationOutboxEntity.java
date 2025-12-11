package com.example.valuation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;


@Data
@Entity
@Table(name = "valuation_outbox")
public class ValuationOutboxEntity {
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID outboxId; // ID of each row (record) in outbox

    @NotNull(message = "Trade record ID can\'t be null!")
    @Column(name = "aggregate_id")
    private UUID aggregateId; // ID of each trade order

    @NotNull(message = "Payload can\'t be null!")
    @Column(name = "payload", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;  // JSON string trade order payload

    @NotNull(message = "Status in outbox can\'t be null!")
    @Column(name = "status", length = 20)
    private String status;  // Status of each order is outbox like ARRIVED, SENT, FAILED

    @NotNull(message = "Created time of order request in outbox can\'t be null!")
    @Column(name = "created_at")
    private LocalDateTime createdAt; // Time at which order was placed in outbox

    @Column(name = "last_attempt_at") // Can be null when no failure on send occurs
    private LocalDateTime lastAttemptAt; // Last attempted retry time of order when sending it fails

    @NotNull(message = "Retry count of order in outbox can\'t be null!")
    @Column(name = "retry_count", columnDefinition = "integer default 0")
    private Integer retryCount; // Number of times order was re-sent to consumer upon failure
}