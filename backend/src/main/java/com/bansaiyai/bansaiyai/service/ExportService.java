package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.report.MonthlyReportDTO;
import com.bansaiyai.bansaiyai.dto.report.OverdueLoanDTO;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Service
public class ExportService {

    private static final String CSV_HEADER_MONTHLY = "Month,Total Income,Total Expense,Net Income,New Loans,Closed Loans";
    private static final String CSV_HEADER_OVERDUE = "Loan Number,Member Name,Outstanding Balance,Days Overdue,Last Payment Date";
    private static final String CSV_HEADER_INCOME_EXPENSE = "Category,Name,Amount";
    private static final String CSV_HEADER_BALANCE_SHEET = "Category,Item,Amount";
    private static final String CSV_HEADER_DIVIDEND = "Member ID,Name,Share Capital,Interest Paid,Dividend Amount,Average Return,Total Payout";

    public String generateMonthlyReportCsv(MonthlyReportDTO report) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println(CSV_HEADER_MONTHLY);
        pw.printf("%s,%.2f,%.2f,%.2f,%d,%d%n",
                report.getMonth(),
                report.getTotalIncome(),
                report.getTotalExpense(),
                report.getNetIncome(),
                report.getNewLoansCount(),
                report.getClosedLoansCount());

        return sw.toString();
    }

    public String generateOverdueLoansCsv(List<OverdueLoanDTO> loans) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println(CSV_HEADER_OVERDUE);
        for (OverdueLoanDTO loan : loans) {
            pw.printf("%s,%s,%.2f,%d,%s%n",
                    loan.getLoanNumber(),
                    escapeSpecialCharacters(loan.getMemberName()),
                    loan.getOutstandingBalance(),
                    loan.getDaysOverdue(),
                    loan.getLastPaymentDate() != null ? loan.getLastPaymentDate().toString() : "N/A");
        }

        return sw.toString();
    }

    public String generateIncomeExpenseReportCsv(com.bansaiyai.bansaiyai.dto.report.IncomeExpenseReportDTO report) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Period," + report.getPeriod());
        pw.println(CSV_HEADER_INCOME_EXPENSE);

        for (com.bansaiyai.bansaiyai.dto.report.ReportItemDTO item : report.getIncomeItems()) {
            pw.printf("Income,%s,%.2f%n", escapeSpecialCharacters(item.getCategory()), item.getAmount());
        }
        for (com.bansaiyai.bansaiyai.dto.report.ReportItemDTO item : report.getExpenseItems()) {
            pw.printf("Expense,%s,%.2f%n", escapeSpecialCharacters(item.getCategory()), item.getAmount());
        }

        pw.println();
        pw.printf("Total Income,,%.2f%n", report.getTotalIncome());
        pw.printf("Total Expense,,%.2f%n", report.getTotalExpense());
        pw.printf("Net Profit,,%.2f%n", report.getNetProfit());

        return sw.toString();
    }

    public String generateBalanceSheetCsv(com.bansaiyai.bansaiyai.dto.report.BalanceSheetDTO report) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("As of Date," + report.getAsOfDate());
        pw.println(CSV_HEADER_BALANCE_SHEET);

        for (com.bansaiyai.bansaiyai.dto.report.ReportItemDTO item : report.getAssets()) {
            pw.printf("Asset,%s,%.2f%n", escapeSpecialCharacters(item.getCategory()), item.getAmount());
        }
        for (com.bansaiyai.bansaiyai.dto.report.ReportItemDTO item : report.getLiabilities()) {
            pw.printf("Liability,%s,%.2f%n", escapeSpecialCharacters(item.getCategory()), item.getAmount());
        }
        for (com.bansaiyai.bansaiyai.dto.report.ReportItemDTO item : report.getEquity()) {
            pw.printf("Equity,%s,%.2f%n", escapeSpecialCharacters(item.getCategory()), item.getAmount());
        }

        pw.println();
        pw.printf("Total Assets,,%.2f%n", report.getTotalAssets());
        pw.printf("Total Liabilities,,%.2f%n", report.getTotalLiabilities());
        pw.printf("Total Equity,,%.2f%n", report.getTotalEquity());

        return sw.toString();
    }

    public String generateDividendRecipientsCsv(List<com.bansaiyai.bansaiyai.entity.DividendRecipient> recipients) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println(CSV_HEADER_DIVIDEND);
        for (com.bansaiyai.bansaiyai.entity.DividendRecipient recipient : recipients) {
            pw.printf("%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                    recipient.getMember().getMemberId(),
                    escapeSpecialCharacters(recipient.getMember().getName()),
                    recipient.getShareCapitalSnapshot(),
                    recipient.getInterestPaidSnapshot(),
                    recipient.getDividendAmount(),
                    recipient.getAverageReturnAmount(),
                    recipient.getTotalAmount());
        }

        return sw.toString();
    }

    public String generateMemberStatementCsv(com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO statement) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Member Statement");
        pw.println("Member Name," + escapeSpecialCharacters(statement.getMemberName()));
        pw.println("Member ID," + statement.getMemberId());
        pw.println("Period," + statement.getStartDate() + " to " + statement.getEndDate());
        pw.println();
        pw.println("Date,Description,Type,Debit,Credit,Balance");

        for (com.bansaiyai.bansaiyai.dto.report.MemberStatementDTO.StatementItem item : statement.getItems()) {
            pw.printf("%s,%s,%s,%.2f,%.2f,%s%n",
                    item.getDate(),
                    escapeSpecialCharacters(item.getDescription()),
                    item.getType(),
                    item.getDebit(),
                    item.getCredit(),
                    item.getBalance() != null ? String.format("%.2f", item.getBalance()) : "-");
        }

        pw.println();
        pw.printf("Total,,,%.2f,%.2f,%n", statement.getTotalDebits(), statement.getTotalCredits());
        pw.printf("Net Movement,,,,%.2f%n", statement.getTotalCredits().subtract(statement.getTotalDebits()));

        return sw.toString();
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
