
import React, { useState, useEffect } from 'react';
import { 
  CheckCircle, ChevronRight, Eye, FileText, Printer, FileCheck, Shield, 
  X, AlertTriangle, Activity, Check, Search
} from 'lucide-react';
import { Loan, Member, ViewState, CollateralDocument } from '../../types';

// --- Approval Queue Table ---
interface ApprovalQueueTableProps {
    pendingLoans: Loan[];
    onNavigate: (view: ViewState) => void;
    onReview: (loan: Loan) => void;
}

export const ApprovalQueueTable: React.FC<ApprovalQueueTableProps> = ({ pendingLoans, onNavigate, onReview }) => {
    return (
        <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-6 border-b border-gray-100 flex justify-between items-center">
                <div className="flex items-center space-x-3">
                        <div className="p-2 bg-emerald-100 rounded-lg">
                        <CheckCircle className="w-5 h-5 text-emerald-600" />
                        </div>
                        <h3 className="font-bold text-gray-800">Loan Approval Queue</h3>
                </div>
                <button 
                    onClick={() => onNavigate('LOANS')}
                    className="text-emerald-600 hover:text-emerald-700 text-sm font-bold flex items-center gap-1"
                >
                    Manage All <ChevronRight className="w-4 h-4" />
                </button>
            </div>
            {pendingLoans.length > 0 ? (
                    <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-gray-50/50 text-xs uppercase text-gray-500">
                            <tr>
                                <th className="p-4">Applicant</th>
                                <th className="p-4 text-right">Amount</th>
                                <th className="p-4">Term</th>
                                <th className="p-4">Guarantors</th>
                                <th className="p-4 text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100 text-sm">
                            {pendingLoans.map((loan) => (
                                <tr key={loan.id} className="hover:bg-gray-50 transition group">
                                    <td className="p-4 font-medium text-gray-800">{loan.memberName}</td>
                                    <td className="p-4 text-right font-bold text-emerald-600">฿{loan.principalAmount.toLocaleString()}</td>
                                    <td className="p-4">{loan.termMonths} Mo.</td>
                                    <td className="p-4 text-gray-500">{loan.guarantorIds.length} Verified</td>
                                    <td className="p-4 text-center">
                                        <button 
                                            onClick={() => onReview(loan)}
                                            className="bg-emerald-50 text-emerald-600 hover:bg-emerald-100 px-4 py-2 rounded-xl text-sm font-semibold transition flex items-center justify-center space-x-2 mx-auto group/btn"
                                        >
                                            <Eye className="w-4 h-4 transition-transform" /> <span>Review</span>
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div className="p-12 text-center text-gray-500 flex flex-col items-center">
                    <CheckCircle className="w-12 h-12 text-gray-200 mb-3" />
                    <p>All clear. No pending approvals.</p>
                </div>
            )}
        </div>
    );
};

// --- Executive Summary Panel ---
interface ExecutiveSummaryProps {
    closingConfirmed: boolean;
    setClosingConfirmed: (val: boolean) => void;
    dividendApproved: boolean;
    setDividendApproved: (val: boolean) => void;
    onNavigate: (view: ViewState) => void;
    onOpenSignOff: (type: 'CLOSING' | 'DIVIDEND') => void;
}

export const ExecutiveSummary: React.FC<ExecutiveSummaryProps> = ({ 
    closingConfirmed, dividendApproved, onOpenSignOff
}) => {
    return (
        <div className="space-y-6">
            <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
                <h3 className="font-bold text-gray-800 mb-6 flex items-center gap-2">
                <FileCheck className="w-5 h-5 text-gray-500" /> Executive Sign-off
                </h3>
                
                <div className="space-y-4">
                    {/* Balance Confirmation */}
                    <div className={`p-5 rounded-2xl border transition group cursor-pointer ${closingConfirmed ? 'bg-green-50 border-green-200' : 'bg-gray-50 border-gray-100 hover:shadow-sm'}`}>
                        <div className="flex justify-between items-center mb-3">
                        <h4 className={`font-bold ${closingConfirmed ? 'text-green-800' : 'text-gray-800'}`}>Monthly Closing</h4>
                        <span className={`text-[10px] px-2 py-1 rounded-full font-bold uppercase transition-transform ${closingConfirmed ? 'bg-green-200 text-green-900' : 'bg-orange-100 text-orange-700'}`}>
                            {closingConfirmed ? 'Confirmed' : 'Pending Signature'}
                        </span>
                        </div>
                        <div className="text-sm space-y-2 mb-4 text-gray-600">
                            <div className="flex justify-between items-center">
                                <span>Period:</span> <span className="font-medium">October 2023</span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span>Status:</span> <span>Secretary Verified</span>
                            </div>
                        </div>
                        {!closingConfirmed ? (
                        <button 
                            onClick={() => onOpenSignOff('CLOSING')}
                            className="w-full bg-emerald-600 text-white text-sm py-3 rounded-xl hover:bg-emerald-700 transition flex justify-center items-center gap-2 font-medium shadow-sm duration-150 group/btn"
                        >
                            <Shield className="w-4 h-4 transition-transform" /> Review & Sign
                        </button>
                        ) : (
                        <div className="text-center text-xs font-bold text-green-700 flex items-center justify-center gap-1 py-2">
                            <CheckCircle className="w-4 h-4" /> Signed by President
                        </div>
                        )}
                    </div>

                    {/* Dividend Confirmation */}
                    <div className={`p-5 rounded-2xl border transition group ${dividendApproved ? 'bg-blue-50 border-blue-200' : 'bg-gray-50 border-gray-100 hover:shadow-sm'}`}>
                        <div className="flex justify-between items-center mb-3">
                        <h4 className={`font-bold ${dividendApproved ? 'text-blue-800' : 'text-gray-800'}`}>Annual Dividend</h4>
                        <span className={`text-[10px] px-2 py-1 rounded-full font-bold uppercase transition-transform ${dividendApproved ? 'bg-blue-200 text-blue-900' : 'bg-blue-100 text-blue-700'}`}>
                            {dividendApproved ? 'Approved' : 'Proposal'}
                        </span>
                        </div>
                        <div className="text-sm space-y-2 mb-4 text-gray-600">
                            <div className="flex justify-between"><span>Rate:</span> <span>4.5%</span></div>
                            <div className="flex justify-between"><span>Status:</span> <span>Pending Auth</span></div>
                        </div>
                        {!dividendApproved ? (
                        <button 
                            onClick={() => onOpenSignOff('DIVIDEND')}
                            className="w-full bg-blue-600 text-white text-sm py-3 rounded-xl hover:bg-blue-700 transition font-medium shadow-sm duration-150 flex items-center justify-center gap-2"
                        >
                            <Shield className="w-4 h-4" /> Review & Sign
                        </button>
                        ) : (
                        <div className="text-center text-xs font-bold text-blue-700 flex items-center justify-center gap-1 py-2">
                            <CheckCircle className="w-4 h-4" /> Approved for Distribution
                        </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

// --- RISK PROFILE REPORT (OFFICIAL DOCUMENT STYLE) ---
interface RiskProfileModalProps {
    isOpen: boolean;
    onClose: () => void;
    member: Member;
    loans: Loan[];
}
export const RiskProfileModal: React.FC<RiskProfileModalProps> = ({ isOpen, onClose, member, loans }) => {
    if (!isOpen || !member) return null;

    // Calculate Risk Metrics
    const activeLoans = loans.filter(l => l.memberId === member.id && (l.status === 'ACTIVE' || l.status === 'APPROVED'));
    const debtBalance = activeLoans.reduce((sum, l) => sum + l.remainingBalance, 0);
    
    // Find loans where this member is a guarantor
    const guaranteedLoans = loans.filter(l => l.guarantorIds.includes(member.id) && (l.status === 'ACTIVE' || l.status === 'APPROVED' || l.status === 'DEFAULTED'));
    const guaranteeLiability = guaranteedLoans.reduce((sum, l) => sum + l.remainingBalance, 0);

    const isHighRisk = guaranteedLoans.length >= 2 || (debtBalance > 0 && guaranteeLiability > 0);

    return (
        <div className="fixed inset-0 bg-gray-900/80 backdrop-blur-md flex items-center justify-center z-[60] p-4 overflow-y-auto">
            {/* Toolbar */}
            <div className="fixed top-0 left-0 right-0 p-4 flex justify-between items-center pointer-events-none z-10 no-print">
                <div className="bg-white/10 backdrop-blur-sm px-4 py-2 rounded-full border border-white/20 pointer-events-auto">
                    <span className="text-white font-bold text-sm flex items-center gap-2">
                        <Activity className="w-4 h-4 text-yellow-400" /> 
                        Risk Inspection System
                    </span>
                </div>
                <button 
                    onClick={onClose} 
                    className="p-3 bg-white/10 hover:bg-white/20 rounded-full text-white pointer-events-auto transition"
                >
                    <X className="w-6 h-6" />
                </button>
            </div>

            {/* A4 REPORT */}
            <div className="relative bg-white shadow-2xl mx-auto my-12 overflow-hidden animate-in zoom-in-95 duration-300 transform origin-top" 
                 style={{ width: '210mm', minHeight: '297mm', padding: '20mm' }}>
                
                {/* Header: Garuda */}
                <div className="flex flex-col items-center mb-6">
                    <img 
                        src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Garuda_Emblem_of_Thailand.svg/200px-Garuda_Emblem_of_Thailand.svg.png" 
                        alt="Garuda" 
                        className="w-[2.5cm] h-auto mb-4"
                    />
                    <h1 className="font-sarabun text-2xl font-bold text-black tracking-wide">
                        รายงานตรวจสอบสถานะเครดิตสมาชิก
                    </h1>
                    <h2 className="font-sarabun text-lg font-bold text-gray-600">
                        (Member Credit Risk Assessment Report)
                    </h2>
                </div>

                <div className="text-right font-sarabun text-black mb-8">
                    <p>วันที่พิมพ์: {new Date().toLocaleDateString('th-TH', { year: 'numeric', month: 'long', day: 'numeric'})}</p>
                    <p>ผู้ตรวจสอบ: ประธานกลุ่ม (Executive Review)</p>
                </div>

                {/* Member Info */}
                <div className="border-t-2 border-b-2 border-black py-4 mb-6 font-sarabun text-lg text-black">
                    <div className="grid grid-cols-2 gap-4">
                        <p><b>ชื่อ-สกุล:</b> {member.fullName}</p>
                        <p><b>รหัสสมาชิก:</b> {member.id}</p>
                        <p><b>สถานะภาพ:</b> {member.status} {member.isFrozen ? '(ระงับบัญชี)' : ''}</p>
                        <p><b>วันที่เป็นสมาชิก:</b> {member.joinedDate}</p>
                    </div>
                </div>

                {/* Risk Analysis Content */}
                <div className="font-sarabun text-black space-y-6">
                    {/* 1. Debt Obligations */}
                    <div>
                        <h3 className="text-xl font-bold border-b border-gray-300 mb-2 pb-1">1. ภาระหนี้สินคงค้าง (Debt Obligations)</h3>
                        <table className="w-full text-left border-collapse mb-2">
                            <thead className="bg-gray-100 font-bold">
                                <tr>
                                    <th className="p-2 border">สัญญา</th>
                                    <th className="p-2 border text-right">วงเงินกู้</th>
                                    <th className="p-2 border text-right">คงเหลือ</th>
                                    <th className="p-2 border">สถานะ</th>
                                </tr>
                            </thead>
                            <tbody>
                                {activeLoans.length > 0 ? activeLoans.map(l => (
                                    <tr key={l.id}>
                                        <td className="p-2 border">{l.contractNo || l.id}</td>
                                        <td className="p-2 border text-right">{l.principalAmount.toLocaleString()}</td>
                                        <td className="p-2 border text-right">{l.remainingBalance.toLocaleString()}</td>
                                        <td className="p-2 border">{l.status}</td>
                                    </tr>
                                )) : (
                                    <tr><td colSpan={4} className="p-2 border text-center">- ไม่มีหนี้สินคงค้าง -</td></tr>
                                )}
                                <tr className="bg-gray-50 font-bold">
                                    <td colSpan={2} className="p-2 border text-right">รวมหนี้สิน (Total Debt)</td>
                                    <td className="p-2 border text-right text-red-600">{debtBalance.toLocaleString()}</td>
                                    <td className="p-2 border">บาท</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    {/* 2. Guarantee Liability */}
                    <div>
                        <h3 className="text-xl font-bold border-b border-gray-300 mb-2 pb-1">2. ภาระการค้ำประกัน (Guarantee Liability)</h3>
                        <p className="mb-2">สมาชิกรายนี้เป็นผู้ค้ำประกันให้กับบุคคลอื่น ดังนี้:</p>
                        <table className="w-full text-left border-collapse mb-2">
                            <thead className="bg-gray-100 font-bold">
                                <tr>
                                    <th className="p-2 border">ผู้กู้ (Borrower)</th>
                                    <th className="p-2 border">สัญญาอ้างอิง</th>
                                    <th className="p-2 border text-right">ภาระคงเหลือ</th>
                                </tr>
                            </thead>
                            <tbody>
                                {guaranteedLoans.length > 0 ? guaranteedLoans.map(l => (
                                    <tr key={l.id}>
                                        <td className="p-2 border">{l.memberName}</td>
                                        <td className="p-2 border">{l.contractNo || l.id}</td>
                                        <td className="p-2 border text-right">{l.remainingBalance.toLocaleString()}</td>
                                    </tr>
                                )) : (
                                    <tr><td colSpan={3} className="p-2 border text-center">- ไม่มีการค้ำประกัน -</td></tr>
                                )}
                                <tr className="bg-gray-50 font-bold">
                                    <td colSpan={2} className="p-2 border text-right">รวมภาระค้ำประกัน (Total Guarantee)</td>
                                    <td className="p-2 border text-right text-orange-600">{guaranteeLiability.toLocaleString()}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    {/* 3. Conclusion */}
                    <div className={`p-4 border-2 rounded-lg mt-8 ${isHighRisk ? 'border-red-500 bg-red-50' : 'border-green-500 bg-green-50'}`}>
                        <h3 className="font-bold text-lg mb-2">สรุปผลการประเมินความเสี่ยง (Conclusion)</h3>
                        <div className="flex items-start gap-4">
                            {isHighRisk ? <AlertTriangle className="w-8 h-8 text-red-600" /> : <CheckCircle className="w-8 h-8 text-green-600" />}
                            <div>
                                <p className="font-bold text-xl">{isHighRisk ? 'มีความเสี่ยงสูง (High Risk)' : 'ปกติ (Normal)'}</p>
                                <p className="text-sm mt-1">
                                    {isHighRisk 
                                        ? 'คำเตือน: สมาชิกมีภาระหนี้สินหรือภาระค้ำประกันสูงกว่าเกณฑ์ที่กำหนด ควรพิจารณาอย่างรอบคอบก่อนอนุมัติสินเชื่อใหม่' 
                                        : 'สมาชิกมีสถานะทางการเงินปกติ สามารถทำธุรกรรมได้ตามระเบียบของกลุ่มฯ'}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="absolute bottom-12 left-0 right-0 px-[20mm] text-center no-print">
                    <button onClick={() => window.print()} className="bg-gray-800 text-white px-6 py-3 rounded-xl font-bold flex items-center justify-center gap-2 mx-auto hover:bg-black transition">
                        <Printer className="w-5 h-5" /> Print Report
                    </button>
                </div>
            </div>
        </div>
    );
};

// --- FINANCIAL SIGN-OFF MODAL (OFFICIAL MEMO) ---
interface FinancialSignOffModalProps {
    isOpen: boolean;
    onClose: () => void;
    type: 'CLOSING' | 'DIVIDEND';
    onConfirm: () => void;
}

export const FinancialSignOffModal: React.FC<FinancialSignOffModalProps> = ({ 
    isOpen, onClose, type, onConfirm 
}) => {
    const [signed, setSigned] = useState(false);
    const [stampVisible, setStampVisible] = useState(false);

    useEffect(() => {
        if (signed) {
            const timer = setTimeout(() => setStampVisible(true), 600);
            return () => clearTimeout(timer);
        }
    }, [signed]);

    const handleSign = () => setSigned(true);
    const handleFinalConfirm = () => { onConfirm(); onClose(); };

    if (!isOpen) return null;

    const subject = type === 'CLOSING' ? 'ขอรับรองความถูกต้องของงบการเงินประจำเดือน ตุลาคม 2566' : 'ขออนุมัติจัดสรรกำไรสุทธิและจ่ายเงินปันผล ประจำปี 2566';

    return (
        <div className="fixed inset-0 bg-gray-900/80 backdrop-blur-md flex items-center justify-center z-[60] p-4 overflow-y-auto">
             <div className="fixed top-0 left-0 right-0 p-4 flex justify-end pointer-events-none z-10 no-print">
                <button onClick={onClose} className="p-3 bg-white/10 hover:bg-white/20 rounded-full text-white pointer-events-auto transition">
                    <X className="w-6 h-6" />
                </button>
            </div>

            <div className="relative bg-white shadow-2xl mx-auto my-12 overflow-hidden animate-in zoom-in-95 duration-300" 
                 style={{ width: '210mm', minHeight: '297mm', padding: '20mm' }}>
                
                {/* Header */}
                <div className="flex flex-col items-center mb-8">
                    <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Garuda_Emblem_of_Thailand.svg/200px-Garuda_Emblem_of_Thailand.svg.png" alt="Garuda" className="w-[3cm] h-auto mb-4" />
                    <h1 className="font-sarabun text-2xl font-bold text-black tracking-wide">บันทึกข้อความ</h1>
                </div>

                <div className="font-sarabun text-lg text-black leading-loose mb-8">
                    <p><b>ส่วนราชการ:</b> กลุ่มสัจจะออมทรัพย์บ้านไสใหญ่</p>
                    <p><b>ที่:</b> สอ.บสญ/พิเศษ/{new Date().getFullYear()+543} &nbsp;&nbsp;&nbsp; <b>วันที่:</b> {new Date().toLocaleDateString('th-TH', { year: 'numeric', month: 'long', day: 'numeric'})}</p>
                    <p><b>เรื่อง:</b> {subject}</p>
                </div>

                <div className="font-sarabun text-lg text-black leading-loose text-justify indent-12 mb-8">
                    <p>
                        ตามที่ฝ่ายเลขานุการและเหรัญญิก ได้ดำเนินการสรุปข้อมูลทางการเงินและตรวจสอบความถูกต้องของบัญชี 
                        {type === 'CLOSING' ? ' ประจำงวดเดือน ตุลาคม 2566 ' : ' ประจำปีบัญชี 2566 '} 
                        เรียบร้อยแล้วนั้น ข้าพเจ้าในฐานะประธานกลุ่มฯ ได้ตรวจสอบรายงานดังกล่าวแล้ว ขอรับรองว่า:
                    </p>
                    
                    {type === 'CLOSING' ? (
                        <ul className="list-disc pl-16 mt-4 space-y-2">
                            <li>ยอดเงินสดคงเหลือและเงินฝากธนาคาร ถูกต้องตรงกับบัญชี</li>
                            <li>ยอดลูกหนี้เงินกู้คงเหลือ ถูกต้องและมีการติดตามหนี้ตามระเบียบ</li>
                            <li>ยอดเงินสัจจะสะสมและหุ้นของสมาชิก ถูกต้องครบถ้วน</li>
                        </ul>
                    ) : (
                        <ul className="list-disc pl-16 mt-4 space-y-2">
                            <li>กำไรสุทธิประจำปี ถูกต้องตามมาตรฐานการบัญชี</li>
                            <li>การจัดสรรเงินปันผล อัตรา 4.5% เป็นไปตามมติที่ประชุมใหญ่</li>
                            <li>การจัดสรรเงินเฉลี่ยคืน อัตรา 12.0% มีความเหมาะสมและเป็นธรรม</li>
                        </ul>
                    )}

                    <p className="indent-12 mt-6">
                        จึงลงนามไว้เพื่อเป็นหลักฐานและอนุมัติให้ดำเนินการ{type === 'CLOSING' ? 'ปิดงวดบัญชีและยกยอดไปงวดถัดไป' : 'เบิกจ่ายเงินปันผลให้แก่สมาชิก'} ต่อไป
                    </p>
                </div>

                {/* Interactive Signature */}
                <div className="mt-16 float-right w-[9cm] text-center relative font-sarabun">
                    <p className="mb-8 text-lg">ลงชื่อ</p>
                    
                    <div className="relative h-[4cm] flex items-center justify-center mb-2">
                        {!signed ? (
                            <button 
                                onClick={handleSign}
                                className="border-2 border-dashed border-blue-800 bg-blue-50 text-blue-900 px-8 py-6 rounded-lg font-bold text-xl hover:bg-blue-100 hover:border-solid transition-all transform hover:scale-105 active:scale-95 shadow-sm"
                            >
                                ลงนามอนุมัติ <br/>
                                <span className="text-sm font-normal opacity-70">(Sign to Approve)</span>
                            </button>
                        ) : (
                            <div className="relative animate-in zoom-in duration-500">
                                <div className="font-handwriting text-4xl text-blue-900 italic transform -rotate-3" style={{fontFamily: 'cursive'}}>
                                    Somsak President
                                </div>
                                <div className={`absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-32 h-32 border-4 border-red-800 rounded-full flex items-center justify-center opacity-80 mix-blend-multiply stamp-texture transition-all duration-700 ${stampVisible ? 'scale-100 opacity-80' : 'scale-150 opacity-0'}`}>
                                    <div className="border border-red-800 rounded-full w-28 h-28 flex items-center justify-center p-2 text-center">
                                        <span className="text-[10px] font-bold text-red-800 uppercase tracking-widest leading-tight">
                                            Satja Savings<br/>Ban Sai Yai<br/>OFFICIAL
                                        </span>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>

                    <p className="text-lg font-bold">( นายสมศักดิ์ รักดี )</p>
                    <p className="text-lg">ประธานกลุ่มสัจจะออมทรัพย์บ้านไสใหญ่</p>
                </div>

                {signed && (
                    <div className="absolute bottom-8 left-0 right-0 px-[20mm] no-print flex justify-center">
                        <button 
                            onClick={handleFinalConfirm}
                            className="bg-emerald-600 text-white px-8 py-4 rounded-xl font-bold shadow-lg animate-pulse flex items-center gap-2 hover:bg-emerald-700 transition"
                        >
                            <CheckCircle className="w-6 h-6" /> Confirm & Process
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

// --- OFFICIAL LOAN REVIEW DOCUMENT ---
interface LoanReviewModalProps {
    isOpen: boolean;
    onClose: () => void;
    loan: Loan;
    members: Member[];
    transactions: any[];
    onDecision: (approved: boolean) => void;
    onViewDocument: (doc: CollateralDocument) => void;
}

export const LoanReviewModal: React.FC<LoanReviewModalProps> = ({ 
    isOpen, onClose, loan, members, onDecision, onViewDocument 
}) => {
    const [signed, setSigned] = useState(false);
    const [stampVisible, setStampVisible] = useState(false);

    // Auto-close effect after signing
    React.useEffect(() => {
        if (signed) {
            const timer = setTimeout(() => {
                setStampVisible(true);
            }, 600);
            return () => clearTimeout(timer);
        }
    }, [signed]);

    if (!isOpen || !loan) return null;

    const borrower = members.find(m => m.id === loan.memberId);

    const handleSign = () => {
        setSigned(true);
    };

    const handleConfirm = () => {
        onDecision(true);
    };

    return (
        <div className="fixed inset-0 bg-gray-900/80 backdrop-blur-md flex items-center justify-center z-[60] p-4 overflow-y-auto">
            {/* Global Print Styles for this Modal */}
            <style>
                {`
                @media print {
                    @page { margin: 0; size: A4; }
                    body { background: white; }
                    body * { visibility: hidden; }
                    #official-loan-doc, #official-loan-doc * { visibility: visible; }
                    #official-loan-doc {
                        position: absolute;
                        left: 0;
                        top: 0;
                        width: 210mm;
                        min-height: 297mm;
                        margin: 0;
                        padding: 20mm;
                        background: white;
                        box-shadow: none;
                        overflow: visible;
                        transform: none !important;
                    }
                    .no-print { display: none !important; }
                }
                .font-sarabun { font-family: 'Sarabun', sans-serif; }
                `}
            </style>

            {/* Toolbar */}
            <div className="fixed top-0 left-0 right-0 p-4 flex justify-between items-center pointer-events-none z-10 no-print">
                <div className="bg-white/10 backdrop-blur-sm px-4 py-2 rounded-full border border-white/20 pointer-events-auto">
                    <span className="text-white font-bold text-sm flex items-center gap-2">
                        <FileCheck className="w-4 h-4 text-emerald-400" /> 
                        Digital Approval System
                    </span>
                </div>
                <button 
                    onClick={onClose} 
                    className="p-3 bg-white/10 hover:bg-white/20 rounded-full text-white pointer-events-auto transition"
                >
                    <X className="w-6 h-6" />
                </button>
            </div>

            {/* A4 PAPER CONTAINER */}
            <div id="official-loan-doc" className="relative bg-white shadow-2xl mx-auto my-12 overflow-hidden animate-in zoom-in-95 duration-300 transform origin-top" 
                 style={{ width: '210mm', minHeight: '297mm', padding: '20mm' }}>
                
                {/* 1. Header: Crest */}
                <div className="flex flex-col items-center mb-8">
                    <img 
                        src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Garuda_Emblem_of_Thailand.svg/200px-Garuda_Emblem_of_Thailand.svg.png" 
                        alt="Garuda" 
                        className="w-[3cm] h-auto mb-6"
                    />
                </div>

                {/* 2. Header: Number/Date */}
                <div className="absolute top-[20mm] right-[20mm] text-right font-sarabun text-lg leading-relaxed text-black">
                    <p>ที่ สอ.บสญ {loan.id.replace('L','')} / {new Date().getFullYear() + 543}</p>
                    <p>วันที่ {new Date().toLocaleDateString('th-TH', { year: 'numeric', month: 'long', day: 'numeric'})}</p>
                </div>

                {/* 3. Subject/To */}
                <div className="mb-8 font-sarabun text-lg text-black leading-relaxed">
                    <div className="flex">
                        <span className="font-bold w-[2cm]">เรื่อง</span>
                        <span>ขออนุมัติ{loan.loanType === 'COMMON' ? 'เงินกู้สามัญ' : loan.loanType === 'EMERGENCY' ? 'เงินกู้ฉุกเฉิน' : 'เงินกู้เพื่อการลงทุน'}</span>
                    </div>
                    <div className="flex">
                        <span className="font-bold w-[2cm]">เรียน</span>
                        <span>ประธานกลุ่มสัจจะออมทรัพย์บ้านไสใหญ่</span>
                    </div>
                </div>

                {/* 4. Body Content */}
                <div className="font-sarabun text-lg text-black leading-loose text-justify space-y-6">
                    {/* Paragraph 1: Intro & Borrower */}
                    <p className="indent-[2.5cm]">
                        ตามที่ <b>{borrower?.fullName || 'สมาชิก'}</b> (รหัสสมาชิก {loan.memberId}) 
                        สมาชิกกลุ่มสัจจะออมทรัพย์บ้านไสใหญ่ ได้ยื่นคำขอกู้เงินประเภท 
                        <b>{loan.loanType}</b> เพื่อวัตถุประสงค์ในการใช้จ่ายส่วนตัวและประกอบอาชีพนั้น
                        คณะกรรมการฝ่ายสินเชื่อได้ตรวจสอบคุณสมบัติและหลักทรัพย์ค้ำประกันแล้ว 
                        เห็นควรนำเสนอเพื่อพิจารณาอนุมัติ โดยมีรายละเอียดดังนี้:
                    </p>

                    {/* Paragraph 2: Terms */}
                    <div className="pl-[2.5cm] space-y-1">
                        <p>๑. วงเงินกู้: <b>฿{loan.principalAmount.toLocaleString()}</b> (ห้าหมื่นบาทถ้วน)</p>
                        <p>๒. อัตราดอกเบี้ย: <b>{loan.interestRate}% ต่อปี</b></p>
                        <p>๓. ระยะเวลาผ่อนชำระ: <b>{loan.termMonths} งวดเดือน</b> (งวดละ ฿{((loan.principalAmount/loan.termMonths) + (loan.principalAmount * (loan.interestRate/100)/12)).toLocaleString(undefined, {maximumFractionDigits:0})})</p>
                    </div>

                    {/* Paragraph 3: Guarantors */}
                    <p className="indent-[2.5cm]">
                        ในการนี้ ผู้กู้ได้จัดหาบุคคลค้ำประกันที่มีคุณสมบัติครบถ้วนตามระเบียบของกลุ่มฯ จำนวน {loan.guarantorIds.length} ราย ได้แก่
                    </p>
                    <div className="pl-[2.5cm] space-y-1">
                        {loan.guarantorIds.length > 0 ? loan.guarantorIds.map((gid, idx) => (
                            <p key={gid}>{idx + 1}. สมาชิกหมายเลข <b>{gid}</b> - สถานะ: <span className="text-green-800 font-bold">ผ่านการตรวจสอบ</span></p>
                        )) : <p className="text-red-600">- ไม่ระบุผู้ค้ำประกัน -</p>}
                    </div>

                    {/* Paragraph 4: Closing */}
                    <p className="indent-[2.5cm]">
                        จึงเรียนมาเพื่อโปรดพิจารณาอนุมัติ
                    </p>
                </div>

                {/* 5. Signature Block (Interactive) */}
                <div className="mt-16 float-right w-[9cm] text-center relative font-sarabun">
                    <p className="mb-10 text-lg">ขอแสดงความนับถือ</p>
                    
                    {/* Interactive Signature Area */}
                    <div className="relative h-[4cm] flex items-center justify-center mb-2">
                        {!signed ? (
                            // State A: Pending
                            <button 
                                onClick={handleSign}
                                className="border-2 border-dashed border-blue-800 bg-blue-50 text-blue-900 px-8 py-6 rounded-lg font-bold text-xl hover:bg-blue-100 hover:border-solid transition-all transform hover:scale-105 active:scale-95 shadow-sm no-print"
                            >
                                ลงนามอนุมัติ <br/>
                                <span className="text-sm font-normal opacity-70">(Sign & Approve)</span>
                            </button>
                        ) : (
                            // State B: Signed
                            <div className="relative animate-in zoom-in duration-500">
                                {/* Digital Signature */}
                                <div className="font-handwriting text-4xl text-blue-900 italic transform -rotate-3" style={{fontFamily: 'cursive'}}>
                                    Somsak President
                                </div>
                                
                                {/* Official Seal (Overlay) */}
                                <div className={`absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-32 h-32 border-4 border-red-800 rounded-full flex items-center justify-center opacity-80 mix-blend-multiply stamp-texture transition-all duration-700 ${stampVisible ? 'scale-100 opacity-80' : 'scale-150 opacity-0'}`}>
                                    <div className="border border-red-800 rounded-full w-28 h-28 flex items-center justify-center p-2 text-center">
                                        <span className="text-[10px] font-bold text-red-800 uppercase tracking-widest leading-tight">
                                            Satja Savings<br/>Ban Sai Yai<br/>OFFICIAL
                                        </span>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>

                    <p className="text-lg font-bold">( นายสมศักดิ์ รักดี )</p>
                    <p className="text-lg">ประธานกลุ่มสัจจะออมทรัพย์บ้านไสใหญ่</p>
                    
                    {signed && (
                        <p className="text-xs text-gray-500 mt-2 font-sans">
                            Digitally Signed: {new Date().toLocaleString('th-TH')} <br/>
                            Ref: {Math.random().toString(36).substr(2, 9).toUpperCase()}
                        </p>
                    )}
                </div>

                {/* 6. Action Bar (Secondary Actions) */}
                <div className="absolute bottom-8 left-0 right-0 px-[20mm] flex justify-between items-end no-print">
                    {!signed ? (
                        <button 
                            onClick={() => onDecision(false)}
                            className="text-red-600 font-sarabun font-bold text-lg hover:underline flex items-center gap-1"
                        >
                            <X className="w-5 h-5" /> ปฏิเสธคำขอ (Reject)
                        </button>
                    ) : (
                        <div className="flex gap-4 w-full">
                            <button 
                                onClick={() => window.print()}
                                className="flex-1 py-3 bg-gray-100 text-gray-700 rounded-xl font-bold font-sans flex items-center justify-center gap-2 hover:bg-gray-200 transition"
                            >
                                <Printer className="w-5 h-5" /> Print Copy
                            </button>
                            <button 
                                onClick={handleConfirm}
                                className="flex-[2] py-3 bg-emerald-600 text-white rounded-xl font-bold font-sans flex items-center justify-center gap-2 hover:bg-emerald-700 shadow-lg animate-pulse"
                            >
                                <Check className="w-5 h-5" /> Complete Process
                            </button>
                        </div>
                    )}
                </div>

                {/* APPROVED STAMP (Appears after signing) */}
                {stampVisible && (
                    <div className="absolute top-[20mm] left-[20mm] border-[6px] border-red-700 text-red-700 px-6 py-2 text-4xl font-black uppercase tracking-widest transform -rotate-12 opacity-80 mix-blend-multiply pointer-events-none animate-in zoom-in duration-300 stamp-texture" style={{fontFamily: 'sans-serif'}}>
                        อนุมัติแล้ว
                        <div className="text-sm font-medium tracking-normal text-center mt-1 border-t border-red-700 pt-1">APPROVED</div>
                    </div>
                )}
            </div>
        </div>
    );
};
