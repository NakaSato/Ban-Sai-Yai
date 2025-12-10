
import React, { useState } from 'react';
import { X, Printer, Download, Loader2 } from 'lucide-react';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';

interface ReceiptModalProps {
  isOpen: boolean;
  onClose: () => void;
  type?: 'RECEIPT' | 'REPORT';
  data: {
    receiptNo?: string;
    date: string;
    receivedFrom?: string;
    title?: string;
    period?: string;
    items?: { label: string; value: string | number; isTotal?: boolean }[];
    description?: string;
    amount?: number;
    cashier?: string;
  } | null;
}

const ReceiptModal: React.FC<ReceiptModalProps> = ({ isOpen, onClose, data, type = 'RECEIPT' }) => {
  const [isGeneratingPdf, setIsGeneratingPdf] = useState(false);

  if (!isOpen || !data) return null;

  const handleDownloadPDF = async () => {
    const element = document.getElementById('printable-content');
    if (!element) return;

    setIsGeneratingPdf(true);
    try {
        await new Promise(resolve => setTimeout(resolve, 100));

        const canvas = await html2canvas(element, {
            scale: 2,
            useCORS: true,
            logging: false,
            backgroundColor: '#ffffff'
        });

        const imgData = canvas.toDataURL('image/png');
        const pdf = new jsPDF({
            orientation: 'portrait',
            unit: 'mm',
            format: 'a4'
        });

        const pdfWidth = pdf.internal.pageSize.getWidth();
        const pdfHeight = pdf.internal.pageSize.getHeight();
        
        pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
        
        const fileName = type === 'RECEIPT' 
            ? `official_receipt_${data.receiptNo || 'temp'}.pdf` 
            : `official_report_${data.period || 'doc'}.pdf`;

        pdf.save(fileName);
    } catch (err) {
        console.error("PDF generation failed:", err);
        alert("Failed to generate PDF. Please try printing instead.");
    } finally {
        setIsGeneratingPdf(false);
    }
  };

  // Helper for Thai Date
  const thaiDate = (dateStr: string) => {
      try {
        const date = new Date(dateStr);
        return date.toLocaleDateString('th-TH', { year: 'numeric', month: 'long', day: 'numeric' });
      } catch (e) { return dateStr; }
  };

  // Helper for Baht Text (Mock)
  const amountToText = (amount: number = 0) => {
      // In a real app, use a library like 'thai-baht-text'
      return `( ${amount.toLocaleString()} Baht Only )`; 
  };

  return (
    <div className="fixed inset-0 bg-gray-900/80 backdrop-blur-sm flex items-center justify-center z-[70] p-4 overflow-y-auto">
      {/* Global Print Styles for this Modal */}
      <style>
        {`
          @media print {
            @page { margin: 0; size: A4; }
            body { background: white; }
            body * { visibility: hidden; }
            #printable-content, #printable-content * { visibility: visible; }
            #printable-content {
              position: absolute;
              left: 0;
              top: 0;
              width: 210mm;
              min-height: 297mm;
              margin: 0;
              padding: 0;
              background: white;
              box-shadow: none;
              overflow: visible;
            }
            .no-print { display: none !important; }
          }
          .font-sarabun { font-family: 'Sarabun', sans-serif; }
        `}
      </style>

      <div className="relative w-full max-w-4xl flex flex-col max-h-[95vh]">
        {/* Toolbar */}
        <div className="flex justify-between items-center mb-4 bg-white/10 backdrop-blur-md p-4 rounded-2xl border border-white/20 no-print shadow-lg">
            <div className="text-white">
                <h3 className="font-bold text-lg">Official Document Preview</h3>
                <p className="text-xs text-white/70">Ready to print or sign</p>
            </div>
            <div className="flex gap-3">
                <button 
                    onClick={() => window.print()}
                    className="px-4 py-2 bg-white text-gray-900 rounded-xl font-bold text-sm hover:bg-gray-100 transition flex items-center gap-2"
                >
                    <Printer className="w-4 h-4" /> Print
                </button>
                <button 
                    onClick={handleDownloadPDF}
                    disabled={isGeneratingPdf}
                    className="px-4 py-2 bg-emerald-600 text-white rounded-xl font-bold text-sm hover:bg-emerald-700 transition flex items-center gap-2 disabled:opacity-50"
                >
                    {isGeneratingPdf ? <Loader2 className="w-4 h-4 animate-spin" /> : <Download className="w-4 h-4" />}
                    PDF
                </button>
                <button 
                    onClick={onClose} 
                    className="p-2 bg-white/20 hover:bg-red-500/20 hover:text-red-300 text-white rounded-xl transition"
                >
                    <X className="w-5 h-5" />
                </button>
            </div>
        </div>

        {/* Scrollable Preview Area */}
        <div className="overflow-y-auto flex-1 custom-scrollbar bg-gray-500/50 rounded-2xl p-4 md:p-8 flex justify-center">
            
            {/* A4 PAPER CONTAINER */}
            <div 
                id="printable-content" 
                className="bg-white shadow-2xl relative flex flex-col"
                style={{ 
                    width: '210mm', 
                    minHeight: '297mm', 
                    padding: '20mm 20mm', // Official Margins
                    boxSizing: 'border-box'
                }}
            >
                {/* 1. HEADER: GARUDA */}
                <div className="flex flex-col items-center mb-6">
                    <img 
                        src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Garuda_Emblem_of_Thailand.svg/200px-Garuda_Emblem_of_Thailand.svg.png" 
                        alt="Garuda" 
                        className="w-[3cm] h-auto mb-2"
                    />
                    <h1 className="font-sarabun text-2xl font-bold text-black tracking-wide mt-4">
                        {type === 'RECEIPT' ? 'ใบเสร็จรับเงิน (Official Receipt)' : 'บันทึกข้อความ (Memorandum)'}
                    </h1>
                    <h2 className="font-sarabun text-xl font-bold text-black">
                        กลุ่มสัจจะออมทรัพย์บ้านไสใหญ่
                    </h2>
                </div>

                {/* 2. HEADER: INFO BLOCK */}
                <div className="flex justify-between items-start font-sarabun text-lg text-black leading-normal mb-8">
                    <div className="w-[60%]">
                        <p><b>ที่ทำการ:</b> หมู่ที่ 4 บ้านไสใหญ่</p>
                        <p><b>เลขประจำตัวผู้เสียภาษี:</b> 0-9940-00XXX-XX-X</p>
                    </div>
                    <div className="text-right">
                        <p><b>เลขที่เอกสาร:</b> {data.receiptNo || 'DOC-XXXX'}</p>
                        <p><b>วันที่:</b> {thaiDate(data.date)}</p>
                    </div>
                </div>

                {/* 3. CONTENT: BASED ON TYPE */}
                {type === 'RECEIPT' ? (
                    <div className="font-sarabun text-lg text-black space-y-6 flex-1">
                        {/* Received From */}
                        <div className="bg-gray-50 border border-gray-200 p-4 rounded-lg">
                            <div className="flex">
                                <span className="font-bold w-32">ได้รับเงินจาก:</span>
                                <span className="flex-1 border-b border-dotted border-gray-400 pl-2">{data.receivedFrom || '-'}</span>
                            </div>
                            <div className="flex mt-2">
                                <span className="font-bold w-32">วัตถุประสงค์:</span>
                                <span className="flex-1 border-b border-dotted border-gray-400 pl-2">{data.description || 'ชำระเงิน'}</span>
                            </div>
                        </div>

                        {/* Line Items Table */}
                        <table className="w-full border-collapse border border-black mt-4">
                            <thead>
                                <tr className="bg-gray-100 text-center">
                                    <th className="border border-black py-2 w-16">ลำดับ</th>
                                    <th className="border border-black py-2">รายการ (Description)</th>
                                    <th className="border border-black py-2 w-40">จำนวนเงิน (บาท)</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td className="border-x border-black py-3 text-center align-top h-48">1</td>
                                    <td className="border-x border-black py-3 px-4 align-top">
                                        <p className="font-bold">{data.description}</p>
                                        <p className="text-sm text-gray-500 mt-1">- ชำระผ่านช่องทาง: {data.cashier?.includes('Online') ? 'โอนเงิน (Transfer)' : 'เงินสด (Cash)'}</p>
                                        {data.items && (
                                            <div className="mt-2 text-sm space-y-1">
                                                {data.items.map((it, idx) => (
                                                    <div key={idx} className="flex justify-between border-b border-dotted border-gray-300">
                                                        <span>{it.label}</span>
                                                        <span>{typeof it.value === 'number' ? `฿${it.value.toLocaleString()}` : it.value}</span>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </td>
                                    <td className="border-x border-black py-3 px-4 text-right align-top font-bold text-xl">
                                        {data.amount?.toLocaleString(undefined, {minimumFractionDigits: 2})}
                                    </td>
                                </tr>
                                {/* Total Row */}
                                <tr className="border-y border-black bg-gray-50 font-bold">
                                    <td colSpan={2} className="border-r border-black py-2 px-4 text-center">
                                        ( {amountToText(data.amount)} )
                                    </td>
                                    <td className="py-2 px-4 text-right text-xl">
                                        {data.amount?.toLocaleString(undefined, {minimumFractionDigits: 2})}
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                ) : (
                    // REPORT LAYOUT
                    <div className="font-sarabun text-lg text-black space-y-6 flex-1">
                        <div className="text-center font-bold text-xl mb-4 underline decoration-double decoration-gray-400">
                            {data.title || `รายงานสรุปประจำงวด: ${data.period}`}
                        </div>
                        
                        <p className="indent-8 text-justify">
                            ตามที่คณะกรรมการดำเนินการได้ตรวจสอบข้อมูลทางการเงิน สำหรับงวดบัญชี <b>{data.period}</b> นั้น 
                            ขอสรุปรายการสำคัญเพื่อเป็นหลักฐานอ้างอิง ดังนี้:
                        </p>

                        <table className="w-full border-collapse border border-black mt-4 text-lg">
                            <thead>
                                <tr className="bg-gray-100 text-center font-bold">
                                    <th className="border border-black py-3 px-4 text-left">รายการ (Item)</th>
                                    <th className="border border-black py-3 px-4 text-right">จำนวน (Amount)</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data.items?.map((item, idx) => (
                                    <tr key={idx} className={item.isTotal ? 'bg-gray-50 font-bold' : ''}>
                                        <td className="border border-black py-3 px-4">{item.label}</td>
                                        <td className="border border-black py-3 px-4 text-right">
                                            {typeof item.value === 'number' ? item.value.toLocaleString() : item.value}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        
                        <p className="indent-8 mt-4">
                            จึงเรียนมาเพื่อโปรดทราบและเก็บไว้เป็นหลักฐาน
                        </p>
                    </div>
                )}

                {/* 4. FOOTER: SIGNATURES */}
                <div className="mt-16 flex justify-between font-sarabun text-black px-4">
                    <div className="text-center w-[40%]">
                        <div className="h-16 border-b border-dotted border-black mb-2 flex items-end justify-center pb-2">
                            {/* Placeholder for manual sign */}
                        </div>
                        <p className="font-bold">( {data.cashier || 'เจ้าหน้าที่การเงิน'} )</p>
                        <p>ผู้รับเงิน / ผู้จัดทำ</p>
                        <p className="text-sm">วันที่: {thaiDate(data.date)}</p>
                    </div>

                    <div className="text-center w-[40%]">
                        <div className="h-16 mb-2 flex items-end justify-center pb-2 relative">
                             {/* Digital Stamp Simulation */}
                             <div className="absolute top-[-20px] left-1/2 -translate-x-1/2 w-24 h-24 border-[3px] border-red-800 rounded-full opacity-60 flex items-center justify-center rotate-[-15deg] pointer-events-none mix-blend-multiply">
                                <span className="text-[8px] font-bold text-red-800 text-center leading-tight">
                                    กลุ่มสัจจะ<br/>บ้านไสใหญ่<br/>OFFICIAL
                                </span>
                             </div>
                        </div>
                        <p className="font-bold">( นายสมศักดิ์ รักดี )</p>
                        <p>ประธานกลุ่ม / ผู้อนุมัติ</p>
                    </div>
                </div>

                {/* Footer Note */}
                <div className="mt-auto pt-8 text-center text-xs font-sarabun text-gray-500">
                    <p>เอกสารนี้ออกโดยระบบอิเล็กทรอนิกส์ (System Generated)</p>
                    <p>Ref: {Math.random().toString(36).substring(2, 10).toUpperCase()} | Printed: {new Date().toLocaleString()}</p>
                </div>
            </div>
        </div>
      </div>
    </div>
  );
};

export default ReceiptModal;
