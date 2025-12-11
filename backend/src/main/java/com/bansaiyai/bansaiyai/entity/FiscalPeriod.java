package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_period", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "month", "fiscal_year" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalPeriod extends BaseEntity {

    @Column(nullable = false)
    private Integer month;

    @Column(name = "fiscal_year", nullable = false)
    private Integer year;

    @Column(nullable = false, length = 20)
    private String status; // OPEN, CLOSED

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by")
    private String closedBy;
}
