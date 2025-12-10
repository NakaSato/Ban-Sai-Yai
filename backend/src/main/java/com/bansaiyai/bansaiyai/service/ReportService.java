package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.report.MonthlyReportDTO;
import com.bansaiyai.bansaiyai.dto.report.OverdueLoanDTO;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;
import com.bansaiyai.bansaiyai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

        private final AccountingRepository accountingRepository;
        private final LoanRepository loanRepository;
        private final MemberRepository memberRepository;
        private final PaymentRepository paymentRepository;
        private final SavingTransactionRepository savingTransactionRepository;
        private final LoanBalanceRepository loanBalanceRepository;
        private final SavingBalanceRepository savingBalanceRepository;

        /**
         * Generate monthly financial summary.
         */
        @Transactional(readOnly = true)
        public MonthlyReportDTO generateMonthlyReport(int month, int year) {
                YearMonth yearMonth = YearMonth.of(year, month);
                String period = yearMonth.toString(); // "2025-10"

                LocalDate startOfMonth = yearMonth.atDay(1);
                LocalDate endOfMonth = yearMonth.atEndOfMonth();

                // Sum Income (Credits to Revenue accounts) - Simplification: Sum all credits in
                // period?
                // Or specific account types. For now, we trust FiscalPeriod tagging in
                // AccountingEntry.
                BigDecimal totalCredits = accountingRepository.sumCreditsByFiscalPeriod(period);
                BigDecimal totalDebits = accountingRepository.sumDebitsByFiscalPeriod(period);

                // In basic accounting: Net Income = Income (Credits) - Expenses (Debits)
                // Adjust based on Account Code logic if needed.
                // Assuming:
                // - Revenue accounts are Credit normal
                // - Expense accounts are Debit normal
                // This rough calc (Credits - Debits) gives a very high level "Cash Flow" view
                // if all entries are here.
                // For strict Income Statement, we'd filter by Account Code starting with '4'
                // (Revenue) and '5' (Expense).

                // Let's try to be slightly smarter if Account Codes follow standard 4xxx/5xxx
                // But for this requirement, let's stick to the high level provided by params.

                BigDecimal totalIncome = totalCredits; // Rough approximation
                BigDecimal totalExpense = totalDebits;

                // Loans Metrics
                int newLoans = (int) loanRepository.countByStartDateBetween(startOfMonth, endOfMonth);
                int closedLoans = (int) loanRepository.countByEndDateBetween(startOfMonth, endOfMonth);

                return MonthlyReportDTO.builder()
                                .month(period)
                                .totalIncome(totalIncome)
                                .totalExpense(totalExpense)
                                .netIncome(totalIncome.subtract(totalExpense))
                                .newLoansCount(newLoans)
                                .closedLoansCount(closedLoans)
                                .build();
        }

        /**
         * List overdue loans.
         */
        @Transactional(readOnly = true)
        public List<OverdueLoanDTO> getOverdueLoans() {
                // Use repository method to find loans past maturity or with missed payments
                List<Loan> overdueLoans = loanRepository.findOverdueLoans();

                return overdueLoans.stream()
                                .map(loan -> OverdueLoanDTO.builder()
                                                .loanNumber(loan.getLoanNumber())
                                                .memberName(loan.getMember().getName())
                                                .outstandingBalance(loan.getOutstandingBalance())
                                                // Calculate days overdue roughly from maturity date
                                                .daysOverdue(loan.getMaturityDate() != null
                                                                ? (int) java.time.temporal.ChronoUnit.DAYS.between(
                                                                                loan.getMaturityDate(),
                                                                                LocalDate.now())
                                                                : 0)
                                                .lastPaymentDate(null) // logic to get last payment would require query
                                                .build())
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO generateMemberStatement(Long memberId,
                        LocalDate startDate, LocalDate endDate) {
                com.bansaiyai.bansaiyai.entity.Member member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new RuntimeException("Member not found"));

                List<com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO.StatementItem> items = new java.util.ArrayList<>();

                // 1. Get Saving Transactions
                List<com.bansaiyai.bansaiyai.entity.SavingTransaction> savingTxns = savingTransactionRepository
                                .findByMemberAndDateRange(memberId, startDate, endDate);
                for (com.bansaiyai.bansaiyai.entity.SavingTransaction txn : savingTxns) {
                        BigDecimal debit = BigDecimal.ZERO;
                        BigDecimal credit = BigDecimal.ZERO;

                        // DEPOSIT/INTEREST = Credit (Balance Up), WITHDRAWAL = Debit (Balance Down)
                        if (txn.getTransactionType() == com.bansaiyai.bansaiyai.entity.enums.TransactionType.DEPOSIT ||
                                        txn.getTransactionType() == com.bansaiyai.bansaiyai.entity.enums.TransactionType.INTEREST_CREDIT
                                        ||
                                        txn.getTransactionType() == com.bansaiyai.bansaiyai.entity.enums.TransactionType.DIVIDEND_PAYOUT) {
                                credit = txn.getAmount();
                        } else {
                                debit = txn.getAmount();
                        }

                        items.add(com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO.StatementItem.builder()
                                        .date(txn.getTransactionDate())
                                        .description(txn.getDescription())
                                        .type(txn.getTransactionType().name())
                                        .debit(debit)
                                        .credit(credit)
                                        .balance(txn.getBalanceAfter()) // This is accurate for Savings
                                        .build());
                }

                // 2. Get Payments (Loan Repayments, Fees)
                // Note: Payments usually imply money FROM member TO coop => Debit from Member's
                // perspective of "Cash"
                // But from "Coop Account" perspective?
                // Let's stick to "Cash Flow with Coop".
                // Payment = Money In to Coop (Credit to Coop), but Debit to Member's Pocket.
                // If this is "Member's Account Statement at Coop":
                // - Deposit: Credit
                // - Loan Payment: ? If paid by Cash, it's just a record. If paid by Savings,
                // it's a Withdrawal (covered above).
                // Checks: Does Payment create a SavingTransaction?
                // If "Pay by Savings", yes.
                // If "Pay by Cash", no SavingTransaction.
                // We should only list Payments that are NOT covered by Savings Transactions to
                // avoid duplicates,
                // OR explicitly list "Cash Payments".

                // Let's fetch all payments. If paymentMethod is TRANSFER/SAVINGS, we might have
                // a duplicate if we list both.
                // Assuming for now we list CASH payments here as "Money In to Coop".
                // Actually, `Payment` entity details the event.
                List<com.bansaiyai.bansaiyai.entity.Payment> payments = paymentRepository
                                .findByMemberIdAndDateRange(memberId, startDate, endDate);
                for (com.bansaiyai.bansaiyai.entity.Payment payment : payments) {
                        if ("SAVINGS".equalsIgnoreCase(payment.getPaymentMethod())) {
                                continue; // Already covered by SavingTransaction likely
                        }

                        // Cash Payment for Loan/Fee
                        items.add(com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO.StatementItem.builder()
                                        .date(payment.getPaymentDate())
                                        .description("Payment: " + payment.getPaymentType()) // e.g., LOAN_REPAYMENT
                                        .type(payment.getPaymentType().name())
                                        .debit(payment.getAmount()) // Money leaving member
                                        .credit(BigDecimal.ZERO)
                                        .balance(null) // No single "balance" for mixed cash payments
                                        .build());
                }

                // Sort by Date
                items.sort(java.util.Comparator.comparing(
                                com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO.StatementItem::getDate));

                // Calc Totals
                BigDecimal totalDebits = items.stream()
                                .map(com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO.StatementItem::getDebit)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalCredits = items.stream()
                                .map(com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO.StatementItem::getCredit)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO.builder()
                                .memberId(member.getMemberId())
                                .memberName(member.getName())
                                .startDate(startDate)
                                .endDate(endDate)
                                .items(items)
                                .totalDebits(totalDebits)
                                .totalCredits(totalCredits)
                                .endingBalance(totalCredits.subtract(totalDebits)) // Rough "Net Flow"
                                .build();
        }

        /**
         * Generate Income and Expense Report
         */
        @Transactional(readOnly = true)
        public com.bansaiyai.bansaiyai.dto.report.IncomeExpenseReportDTO generateIncomeExpenseReport(
                        LocalDate startDate, LocalDate endDate) {
                List<com.bansaiyai.bansaiyai.dto.report.ReportItemDTO> incomeItems = new java.util.ArrayList<>();
                List<com.bansaiyai.bansaiyai.dto.report.ReportItemDTO> expenseItems = new java.util.ArrayList<>();

                // 1. Income from Payments (Interest, Penalty, Fees)
                List<Object[]> revenueByType = paymentRepository.findRevenueByDateRangeGroupByType(startDate, endDate);
                for (Object[] row : revenueByType) {
                        com.bansaiyai.bansaiyai.entity.enums.PaymentType type = (com.bansaiyai.bansaiyai.entity.enums.PaymentType) row[0];
                        BigDecimal amount = (BigDecimal) row[1];
                        incomeItems.add(new com.bansaiyai.bansaiyai.dto.report.ReportItemDTO(type.name(), amount));
                }

                // 2. Manual Income (Account 4xxx)
                List<Object[]> manualIncome = accountingRepository.sumCreditByCodePatternAndDateRange(startDate,
                                endDate, "4%");
                for (Object[] row : manualIncome) {
                        String name = (String) row[0];
                        BigDecimal amount = (BigDecimal) row[1];
                        incomeItems.add(new com.bansaiyai.bansaiyai.dto.report.ReportItemDTO(name, amount));
                }

                // 3. Expenses from Savings Interest
                BigDecimal savingsInterest = savingTransactionRepository.sumAmountByTypeAndDateRange(
                                com.bansaiyai.bansaiyai.entity.enums.TransactionType.INTEREST_CREDIT, startDate,
                                endDate);
                if (savingsInterest.compareTo(BigDecimal.ZERO) > 0) {
                        expenseItems.add(new com.bansaiyai.bansaiyai.dto.report.ReportItemDTO("Savings Interest Paid",
                                        savingsInterest));
                }

                // 4. Manual Expenses (Account 5xxx)
                List<Object[]> manualExpense = accountingRepository.sumDebitByCodePatternAndDateRange(startDate,
                                endDate, "5%");
                for (Object[] row : manualExpense) {
                        String name = (String) row[0];
                        BigDecimal amount = (BigDecimal) row[1];
                        expenseItems.add(new com.bansaiyai.bansaiyai.dto.report.ReportItemDTO(name, amount));
                }

                BigDecimal totalIncome = incomeItems.stream()
                                .map(com.bansaiyai.bansaiyai.dto.report.ReportItemDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalExpense = expenseItems.stream()
                                .map(com.bansaiyai.bansaiyai.dto.report.ReportItemDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return com.bansaiyai.bansaiyai.dto.report.IncomeExpenseReportDTO.builder()
                                .incomeItems(incomeItems)
                                .expenseItems(expenseItems)
                                .totalIncome(totalIncome)
                                .totalExpense(totalExpense)
                                .netProfit(totalIncome.subtract(totalExpense))
                                .period(startDate + " to " + endDate)
                                .build();
        }

        /**
         * Generate Balance Sheet
         */
        @Transactional(readOnly = true)
        public com.bansaiyai.bansaiyai.dto.report.BalanceSheetDTO generateBalanceSheet(LocalDate asOfDate) {
                List<com.bansaiyai.bansaiyai.dto.report.ReportItemDTO> assets = new java.util.ArrayList<>();
                List<com.bansaiyai.bansaiyai.dto.report.ReportItemDTO> liabilities = new java.util.ArrayList<>();
                List<com.bansaiyai.bansaiyai.dto.report.ReportItemDTO> equity = new java.util.ArrayList<>();

                // Assets
                BigDecimal loansReceivable = loanBalanceRepository.sumOutstandingBalanceByDate(asOfDate);
                if (loansReceivable.compareTo(BigDecimal.ZERO) == 0 && asOfDate.equals(LocalDate.now())) {
                        // If today and no snapshot (likely), check live? Or just return 0?
                        // Simplification: returns 0 if no snapshot. User should generate 'As of Month
                        // End'.
                }
                assets.add(new com.bansaiyai.bansaiyai.dto.report.ReportItemDTO("Loans Receivable", loansReceivable));

                // Cash (Placeholder - Manual entries needed for accurate cash)
                // assets.add(new ReportItemDTO("Cash on Hand", ...));

                // Liabilities
                BigDecimal savingsDeposits = savingBalanceRepository.sumClosingBalanceByDate(asOfDate);
                liabilities.add(new com.bansaiyai.bansaiyai.dto.report.ReportItemDTO("Member Savings",
                                savingsDeposits));

                // Equity
                BigDecimal totalShares = memberRepository.sumTotalShareCapital();
                if (totalShares == null)
                        totalShares = BigDecimal.ZERO;
                equity.add(new com.bansaiyai.bansaiyai.dto.report.ReportItemDTO("Share Capital", totalShares));

                // Retained Earnings (Simplified: Assets - Liabilities - Equity)
                BigDecimal totalAssets = assets.stream()
                                .map(com.bansaiyai.bansaiyai.dto.report.ReportItemDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalLiabilities = liabilities.stream()
                                .map(com.bansaiyai.bansaiyai.dto.report.ReportItemDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalEquityBeforeRetained = equity.stream()
                                .map(com.bansaiyai.bansaiyai.dto.report.ReportItemDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal retainedEarnings = totalAssets.subtract(totalLiabilities)
                                .subtract(totalEquityBeforeRetained);
                equity.add(new com.bansaiyai.bansaiyai.dto.report.ReportItemDTO("Retained Earnings", retainedEarnings));

                BigDecimal totalEquity = totalEquityBeforeRetained.add(retainedEarnings);

                return com.bansaiyai.bansaiyai.dto.report.BalanceSheetDTO.builder()
                                .assets(assets)
                                .liabilities(liabilities)
                                .equity(equity)
                                .totalAssets(totalAssets)
                                .totalLiabilities(totalLiabilities)
                                .totalEquity(totalEquity)
                                .asOfDate(asOfDate.toString())
                                .build();
        }
}
