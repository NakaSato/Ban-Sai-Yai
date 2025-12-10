package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.JournalEntryRequest;
import com.bansaiyai.bansaiyai.entity.AccountingEntry;
import com.bansaiyai.bansaiyai.repository.AccountingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountingService {

    private final AccountingRepository accountingRepository;

    @Transactional
    public AccountingEntry createEntry(JournalEntryRequest request) {
        AccountingEntry entry = new AccountingEntry();
        entry.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
        entry.setFiscalPeriod(entry.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        entry.setAccountCode(request.getAccountCode());
        entry.setAccountName(request.getAccountName());
        entry.setDescription(request.getDescription());
        entry.setReferenceType("MANUAL_JOURNAL");

        if ("INCOME".equalsIgnoreCase(request.getType())) {
            entry.setCredit(request.getAmount());
            entry.setDebit(BigDecimal.ZERO);
        } else {
            entry.setDebit(request.getAmount());
            entry.setCredit(BigDecimal.ZERO);
        }

        return accountingRepository.save(entry);
    }

    public Map<String, BigDecimal> getDailySummary(LocalDate date) {
        List<AccountingEntry> entries = accountingRepository.findAll(); // Optimization: Add date filter in Repo later

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (AccountingEntry entry : entries) {
            if (entry.getTransactionDate().equals(date) && "MANUAL_JOURNAL".equals(entry.getReferenceType())) {
                if (entry.getCredit() != null && entry.getCredit().compareTo(BigDecimal.ZERO) > 0) {
                    totalIncome = totalIncome.add(entry.getCredit());
                }
                if (entry.getDebit() != null && entry.getDebit().compareTo(BigDecimal.ZERO) > 0) {
                    totalExpense = totalExpense.add(entry.getDebit());
                }
            }
        }

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        return summary;
    }
}
