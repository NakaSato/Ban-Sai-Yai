
import * as XLSX from 'xlsx';
import { Transaction, Account, Member } from '../types';

export const exportToExcel = (
    reportType: 'INCOME_EXPENSE' | 'BALANCE_SHEET' | 'DIVIDEND',
    data: any,
    period: { start: string; end: string }
) => {
    const wb = XLSX.utils.book_new();
    let ws: XLSX.WorkSheet;
    let fileName = `Report_${reportType}_${period.end}.xlsx`;

    // 1. Common Header Generation
    const createHeader = (title: string) => [
        ['Ban Sai Yai Savings Group'], // Row 1: Org Name
        [title],                       // Row 2: Report Name
        [`Period: ${period.start} to ${period.end}`], // Row 3: Date
        [''],                          // Row 4: Spacer
    ];

    if (reportType === 'INCOME_EXPENSE') {
        const transactions: Transaction[] = data;
        const header = createHeader('Statement of Income & Expenses');
        
        // Data Columns
        const tableHead = [['Date', 'Txn ID', 'Type', 'Category', 'Description', 'Amount (THB)']];
        
        const tableBody = transactions.map(t => [
            t.date,
            t.id,
            t.type,
            t.category || '-',
            t.description,
            t.amount
        ]);

        // Calculate Totals
        const totalIncome = transactions
            .filter(t => ['DEPOSIT', 'SHARE_PURCHASE', 'INCOME', 'LOAN_REPAYMENT'].includes(t.type))
            .reduce((sum, t) => sum + t.amount, 0);
            
        const totalExpense = transactions
            .filter(t => !['DEPOSIT', 'SHARE_PURCHASE', 'INCOME', 'LOAN_REPAYMENT'].includes(t.type))
            .reduce((sum, t) => sum + t.amount, 0);

        const net = totalIncome - totalExpense;

        const footer = [
            [''],
            ['', '', '', '', 'Total Income', totalIncome],
            ['', '', '', '', 'Total Expense', totalExpense],
            ['', '', '', '', 'NET PROFIT/LOSS', net]
        ];

        const wsData = [...header, ...tableHead, ...tableBody, ...footer];
        ws = XLSX.utils.aoa_to_sheet(wsData);
        fileName = `IncomeExpense_Report_${period.end}.xlsx`;
    } 
    else if (reportType === 'BALANCE_SHEET') {
        const { assets, liabilities, equity, accounts } = data;
        const header = createHeader('Statement of Financial Position');

        const wsData = [
            ...header,
            ['ASSETS'],
            ['Account Code', 'Account Name', 'Balance'],
            ...accounts.filter((a: Account) => a.category === 'ASSET').map((a: Account) => [a.code, a.name, a.balance]),
            ['TOTAL ASSETS', '', assets],
            [''],
            ['LIABILITIES'],
            ['Account Code', 'Account Name', 'Balance'],
            ...accounts.filter((a: Account) => a.category === 'LIABILITY').map((a: Account) => [a.code, a.name, a.balance]),
            ['TOTAL LIABILITIES', '', liabilities],
            [''],
            ['EQUITY'],
            ['Account Code', 'Account Name', 'Balance'],
            ...accounts.filter((a: Account) => a.category === 'EQUITY').map((a: Account) => [a.code, a.name, a.balance]),
            ['TOTAL EQUITY', '', equity],
            [''],
            ['CHECK BALANCE (A - L - E)', '', assets - liabilities - equity] // Should be 0
        ];

        ws = XLSX.utils.aoa_to_sheet(wsData);
        fileName = `BalanceSheet_${period.end}.xlsx`;
    }
    else if (reportType === 'DIVIDEND') {
        const { members, dividendRate, avgReturnRate } = data;
        const header = createHeader('Dividend & Average Return Distribution');
        
        const tableHead = [['Member ID', 'Name', 'Total Shares', `Dividend (${dividendRate}%)`, 'Interest Paid', `Return (${avgReturnRate}%)`, 'Total Payout', 'Signature']];
        
        const tableBody = members.map((m: any) => [
            m.id,
            m.fullName,
            m.shareBalance,
            m.dividendAmount,
            m.interestPaid,
            m.returnAmount,
            m.totalPayout,
            '' // Signature placeholder
        ]);

        const totalPayout = members.reduce((sum: number, m: any) => sum + m.totalPayout, 0);
        const footer = [
            [''],
            ['', '', '', '', '', 'GRAND TOTAL', totalPayout]
        ];

        const wsData = [...header, ...tableHead, ...tableBody, ...footer];
        ws = XLSX.utils.aoa_to_sheet(wsData);
        fileName = `Dividend_Distribution_${period.end}.xlsx`;
    } 
    else {
        ws = XLSX.utils.json_to_sheet([]);
    }

    // Append Sheet and Trigger Download
    XLSX.utils.book_append_sheet(wb, ws, "Report");
    XLSX.writeFile(wb, fileName);
};
