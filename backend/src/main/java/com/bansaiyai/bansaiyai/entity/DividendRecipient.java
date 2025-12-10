package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dividend_recipients", indexes = {
        @Index(name = "idx_dist_id", columnList = "dividend_distribution_id"),
        @Index(name = "idx_mem_id", columnList = "member_id")
})
@EntityListeners(AuditingEntityListener.class)
public class DividendRecipient extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dividend_distribution_id", nullable = false)
    private DividendDistribution dividendDistribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "share_capital_snapshot", precision = 19, scale = 2)
    private BigDecimal shareCapitalSnapshot;

    @Column(name = "interest_paid_snapshot", precision = 19, scale = 2)
    private BigDecimal interestPaidSnapshot;

    @Column(name = "dividend_amount", precision = 19, scale = 2)
    private BigDecimal dividendAmount;

    @Column(name = "average_return_amount", precision = 19, scale = 2)
    private BigDecimal averageReturnAmount;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;
}
