package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.*;
import com.bansaiyai.bansaiyai.entity.enums.ApprovalStatus;
import com.bansaiyai.bansaiyai.exception.BusinessException;
import com.bansaiyai.bansaiyai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DividendService {

    private final DividendDistributionRepository dividendDistributionRepository;
    private final DividendRecipientRepository dividendRecipientRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionService transactionService;
    private final AuditService auditService;
    private final SavingRepository savingRepository;

    /**
     * Calculate dividend preview and save as DRAFT.
     */
    @Transactional
    public DividendDistribution calculateDividends(Integer year, BigDecimal dividendRate, BigDecimal averageReturnRate,
            User creator) {
        if (dividendDistributionRepository.existsByYear(year)) {
            throw new BusinessException("Dividends for year " + year + " already calculated.");
        }

        // Initialize Distribution Record
        DividendDistribution dist = DividendDistribution.builder()
                .year(year)
                .dividendRate(dividendRate)
                .averageReturnRate(averageReturnRate)
                .status(ApprovalStatus.PENDING) // Treated as DRAFT
                .calculatedAt(LocalDateTime.now())
                .totalProfit(BigDecimal.ZERO)
                .totalDividendAmount(BigDecimal.ZERO)
                .totalAverageReturnAmount(BigDecimal.ZERO)
                .build();

        // This must be saved first to get ID? No, we can save cascade if properly set
        // up, but let's save dist first.
        dist = dividendDistributionRepository.save(dist);

        List<Member> activeMembers = memberRepository.findByIsActiveTrue();
        BigDecimal totalDiv = BigDecimal.ZERO;
        BigDecimal totalAvg = BigDecimal.ZERO;

        for (Member member : activeMembers) {
            // 1. Share Capital Dividend
            // Logic: Get share capital from SAVINGS account (assuming 1 main savings per
            // member)
            BigDecimal shareCapital = BigDecimal.ZERO;
            SavingAccount saving = savingRepository.findFirstByMemberIdAndIsActiveTrue(member.getId()).orElse(null);
            if (saving != null && saving.getShareCapital() != null) {
                shareCapital = saving.getShareCapital();
            }

            BigDecimal divAmount = shareCapital.multiply(dividendRate).divide(new BigDecimal("100"), 2,
                    RoundingMode.HALF_UP);

            // 2. Average Return (Interest Cashback)
            BigDecimal interestPaid = paymentRepository.sumInterestByMemberAndYear(member.getId(), year);
            if (interestPaid == null)
                interestPaid = BigDecimal.ZERO;

            BigDecimal avgRetAmount = interestPaid.multiply(averageReturnRate).divide(new BigDecimal("100"), 2,
                    RoundingMode.HALF_UP);

            BigDecimal memberTotal = divAmount.add(avgRetAmount);

            if (memberTotal.compareTo(BigDecimal.ZERO) > 0) {
                DividendRecipient recipient = DividendRecipient.builder()
                        .dividendDistribution(dist)
                        .member(member)
                        .shareCapitalSnapshot(shareCapital)
                        .interestPaidSnapshot(interestPaid)
                        .dividendAmount(divAmount)
                        .averageReturnAmount(avgRetAmount)
                        .totalAmount(memberTotal)
                        .build();
                dividendRecipientRepository.save(recipient);

                totalDiv = totalDiv.add(divAmount);
                totalAvg = totalAvg.add(avgRetAmount);
            }
        }

        dist.setTotalDividendAmount(totalDiv);
        dist.setTotalAverageReturnAmount(totalAvg);
        // Total profit field is usually manual input or calc from Journal,
        // here we leave it as 0 or set to sum of payouts if strictly pass-through?
        // User requirements usually imply "Profit" is input, "Dividend" is output.
        // For now, we update the output totals.

        auditService.logAction(creator, "DIVIDEND_CALCULATE", "DividendDistribution", dist.getId(), null, null);

        return dividendDistributionRepository.save(dist);
    }

    /**
     * Confirm and Distribute Dividends to Savings Accounts.
     */
    @Transactional
    public DividendDistribution distributeDividends(Integer year, User distributor) {
        DividendDistribution dist = dividendDistributionRepository.findByYear(year)
                .orElseThrow(() -> new BusinessException("Dividend calculation for " + year + " not found."));

        if (dist.getStatus() == ApprovalStatus.APPROVED) {
            throw new BusinessException("Dividends for " + year + " already distributed.");
        }

        List<DividendRecipient> recipients = dividendRecipientRepository.findByDividendDistributionId(dist.getId());

        for (DividendRecipient recipient : recipients) {
            if (recipient.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                transactionService.processDividendPayout(
                        recipient.getMember().getId(),
                        recipient.getTotalAmount(),
                        year,
                        distributor);
            }
        }

        dist.setStatus(ApprovalStatus.APPROVED);
        dist.setDistributedAt(LocalDateTime.now());

        auditService.logAction(distributor, "DIVIDEND_DISTRIBUTE", "DividendDistribution", dist.getId(), null, null);

        return dividendDistributionRepository.save(dist);
    }

    public DividendDistribution getDistribution(Integer year) {
        return dividendDistributionRepository.findByYear(year)
                .orElseThrow(() -> new BusinessException("Dividend calculation for " + year + " not found."));
    }

    public List<DividendRecipient> getRecipients(Long distId) {
        return dividendRecipientRepository.findByDividendDistributionId(distId);
    }
}
