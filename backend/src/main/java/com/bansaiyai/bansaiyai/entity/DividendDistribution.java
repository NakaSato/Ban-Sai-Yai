package com.bansaiyai.bansaiyai.entity;

import com.bansaiyai.bansaiyai.entity.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dividend_distributions")
@EntityListeners(AuditingEntityListener.class)
public class DividendDistribution extends BaseEntity {

    @Column(name = "fiscal_year", nullable = false, unique = true)
    private Integer year;

    @Column(name = "total_profit", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalProfit;

    @Column(name = "dividend_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal dividendRate; // e.g. 5.00 for 5%

    @Column(name = "average_return_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal averageReturnRate; // e.g. 10.00 for 10%

    @Column(name = "total_dividend_amount", precision = 19, scale = 2)
    private BigDecimal totalDividendAmount;

    @Column(name = "total_average_return_amount", precision = 19, scale = 2)
    private BigDecimal totalAverageReturnAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStatus status; // DRAFT, APPROVED (Used as Distributed)

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "distributed_at")
    private LocalDateTime distributedAt;

    @OneToMany(mappedBy = "dividendDistribution", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DividendRecipient> recipients;
}
