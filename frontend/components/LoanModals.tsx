
import React, { useState, useRef } from 'react';
import { Loan, LoanStatus, UserRole, CollateralDocument } from '../types';
import { 
  X, Send, FileCheck, Image, Calculator, AlertCircle, Printer, 
  Info, ArrowDownLeft, QrCode, UploadCloud, Trash2, FileText, Plus, UserPlus,
  CreditCard, Calendar, Shield, DollarSign, Briefcase, Check, User
} from 'lucide-react';
import { MOCK_LOANS, MOCK_MEMBERS } from '../constants'; // Import for mock validation

// --- LOAN APPLICATION MODAL ---
interface LoanApplicationModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (form: any) => void;
    userRole: UserRole;
    defaultMemberId: string;
}

export const LoanApplicationModal: React.FC<LoanApplicationModalProps> = ({ 
    isOpen, onClose, onSubmit, userRole, defaultMemberId 
}) => {
    const [form, setForm] = useState({
        memberId: defaultMemberId,
        principalAmount: '',
        termMonths: '12',
        loanType: 'COMMON',
        guarantorIds: [] as string[],
        collateralRef: '' // Legacy summary field
    });

    const [documents, setDocuments] = useState<CollateralDocument[]>([]);
    const [isDragging, setIsDragging] = useState(false);
    
    // Guarantor Input State
    const [guarantorInput, setGuarantorInput] = useState('');
    const [guarantorError, setGuarantorError] = useState('');

    // New Document Input State
    const [newDocType, setNewDocType] = useState('DEED');
    const [newDocDesc, setNewDocDesc] = useState('');
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            processFile(e.target.files[0]);
        }
    };

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault();
        setIsDragging(false);
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            processFile(e.dataTransfer.files[0]);
        }
    };

    const processFile = (file: File) => {
        const mockUrl = URL.createObjectURL(file);
        
        const newDoc: CollateralDocument = {
            id: `DOC-${Date.now()}`,
            type: newDocType as any,
            description: newDocDesc || file.name,
            fileName: file.name,
            fileSize: file.size,
            url: mockUrl,
            uploadedAt: new Date().toISOString()
        };

        setDocuments([...documents, newDoc]);
        setNewDocDesc(''); // Reset desc
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const removeDocument = (id: string) => {
        setDocuments(documents.filter(d => d.id !== id));
    };

    const addGuarantor = () => {
        setGuarantorError('');
        if (!guarantorInput) return;
        
        const inputId = guarantorInput.trim();

        // 1. Check duplicate in current list
        if (form.guarantorIds.includes(inputId)) {
            setGuarantorError('Member is already added.');
            return;
        }
        
        // 2. Check self-guarantee
        if (inputId === form.memberId) {
            setGuarantorError('Borrower cannot guarantee themselves.');
            return;
        }

        // 3. Member Existence & Status Check
        const guarantor = MOCK_MEMBERS.find(m => m.id === inputId);
        if (!guarantor) {
             setGuarantorError(`Member ID ${inputId} not found in database.`);
             return;
        }
        
        if (guarantor.status !== 'ACTIVE') {
            setGuarantorError(`Guarantor is ${guarantor.status} and ineligible.`);
            return;
        }

        if (guarantor.isFrozen) {
            setGuarantorError(`Guarantor account is FROZEN due to policy violation.`);
            return;
        }

        // 4. Check for Defaulted Loans (Credit Risk)
        const defaultedLoans = MOCK_LOANS.filter(l => 
            l.memberId === inputId && l.status === LoanStatus.DEFAULTED
        );
        if (defaultedLoans.length > 0) {
            setGuarantorError(`Credit Risk: ${guarantor.fullName} has defaulted loans.`);
            return;
        }

        // 5. Guarantee Limit Check (Max 2 Active/Approved Loans)
        const activeGuarantees = MOCK_LOANS.filter(l => 
            l.guarantorIds.includes(inputId) && 
            (l.status === LoanStatus.ACTIVE || l.status === LoanStatus.APPROVED || l.status === LoanStatus.DEFAULTED)
        );

        if (activeGuarantees.length >= 2) {
            setGuarantorError(`Limit Exceeded: ${guarantor.fullName} already guarantees ${activeGuarantees.length} loans.`);
            return;
        }

        setForm(prev => ({...prev, guarantorIds: [...prev.guarantorIds, inputId]}));
        setGuarantorInput('');
    };

    const removeGuarantor = (id: string) => {
        setForm(prev => ({...prev, guarantorIds: prev.guarantorIds.filter(gid => gid !== id)}));
    };

    const handleSubmit = () => {
        // Pass documents up
        onSubmit({
            ...form,
            documents: documents,
            collateralRef: documents.length > 0 ? `${documents.length} Docs Attached` : form.collateralRef
        });
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 transition-all duration-300">
          <div className="bg-white rounded-[2rem] shadow-2xl w-full max-w-3xl overflow-hidden flex flex-col max-h-[90vh] animate-in fade-in zoom-in-95 duration-200">
             
             {/* Header */}
             <div className="px-8 py-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                <div className="flex items-center gap-4">
                    <div className="p-3 bg-emerald-100 text-emerald-600 rounded-2xl">
                        <CreditCard className="w-6 h-6" />
                    </div>
                    <div>
                        <h3 className="text-xl font-bold text-gray-900">New Loan Application</h3>
                        <p className="text-sm text-gray-500 font-medium">Create a new credit request record</p>
                    </div>
                </div>
                <button 
                    onClick={onClose} 
                    className="p-2.5 text-gray-400 hover:text-gray-600 hover:bg-white hover:shadow-sm rounded-full transition-all border border-transparent hover:border-gray-100"
                >
                    <X className="w-5 h-5" />
                </button>
             </div>
             
             <div className="p-8 overflow-y-auto custom-scrollbar flex-1 space-y-8">
                 {/* Section 1: Loan Configuration */}
                 <div className="space-y-4">
                     <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                        <Briefcase className="w-4 h-4" /> Loan Configuration
                     </h4>
                     
                     <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="md:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-1.5">Borrower ID</label>
                                <div className="relative">
                                    <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                                    <input 
                                        type="text" 
                                        className="w-full bg-gray-50 border-2 border-transparent rounded-xl pl-11 pr-4 py-3 outline-none focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all font-medium text-gray-800" 
                                        value={form.memberId}
                                        onChange={(e) => setForm({...form, memberId: e.target.value})}
                                        disabled={userRole === UserRole.MEMBER}
                                        placeholder="e.g. M001"
                                    />
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-1.5">Principal Amount (฿)</label>
                                <div className="relative">
                                    <span className="absolute left-4 top-1/2 -translate-y-1/2 text-emerald-600 font-bold">฿</span>
                                    <input 
                                        type="number" 
                                        className="w-full bg-gray-50 border-2 border-transparent rounded-xl pl-10 pr-4 py-3 outline-none focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all font-bold text-xl text-emerald-700 placeholder-emerald-700/30" 
                                        value={form.principalAmount}
                                        onChange={(e) => setForm({...form, principalAmount: e.target.value})}
                                        placeholder="0.00"
                                    />
                                </div>
                            </div>
                        </div>

                        {/* Loan Type & Term Selection */}
                        <div className="p-4 bg-gray-50 rounded-2xl border border-gray-100 md:col-span-2 space-y-4">
                            <div>
                                <label className="block text-xs font-bold text-gray-500 uppercase mb-2">Loan Type</label>
                                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                                    {[
                                        { id: 'COMMON', label: 'Common Loan', desc: 'General Purpose' },
                                        { id: 'EMERGENCY', label: 'Emergency', desc: 'Urgent Needs' },
                                        { id: 'INVESTMENT', label: 'Investment', desc: 'Business Use' }
                                    ].map((type) => (
                                        <button
                                            key={type.id}
                                            onClick={() => setForm({ ...form, loanType: type.id })}
                                            className={`flex flex-col items-center justify-center p-3 rounded-xl border-2 transition-all ${
                                                form.loanType === type.id
                                                    ? 'border-emerald-500 bg-white text-emerald-700 shadow-md ring-1 ring-emerald-500'
                                                    : 'border-transparent bg-white text-gray-600 hover:bg-gray-100'
                                            }`}
                                        >
                                            <span className="font-bold text-sm">{type.label}</span>
                                            <span className="text-[10px] opacity-70">{type.desc}</span>
                                        </button>
                                    ))}
                                </div>
                            </div>
                            
                            <div>
                                <label className="block text-xs font-bold text-gray-500 uppercase mb-1.5">Repayment Term</label>
                                <select 
                                    className="w-full bg-white border border-gray-200 rounded-xl px-4 py-2.5 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 transition-all font-medium text-gray-700"
                                    value={form.termMonths}
                                    onChange={(e) => setForm({...form, termMonths: e.target.value})}
                                >
                                    <option value="12">12 Months (Short Term)</option>
                                    <option value="24">24 Months (Medium Term)</option>
                                    <option value="36">36 Months (Long Term)</option>
                                </select>
                            </div>
                        </div>
                     </div>
                 </div>

                 <div className="h-px bg-gray-100"></div>

                 {/* Section 2: Guarantors */}
                 <div className="space-y-4">
                     <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                        <UserPlus className="w-4 h-4" /> Guarantors
                     </h4>
                     
                     <div className="bg-gray-50 p-5 rounded-2xl border border-gray-100">
                        <div className="flex gap-3 mb-3">
                            <input 
                                type="text" 
                                className="flex-1 bg-white border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 transition-all"
                                placeholder="Enter Guarantor ID (e.g. M003)"
                                value={guarantorInput}
                                onChange={(e) => setGuarantorInput(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && addGuarantor()}
                            />
                            <button 
                                onClick={addGuarantor}
                                className="bg-gray-900 text-white px-5 py-2.5 rounded-xl text-sm font-bold hover:bg-black transition shadow-lg active:scale-95"
                            >
                                <Plus className="w-4 h-4" />
                            </button>
                        </div>
                        
                        {guarantorError && (
                            <div className="mb-3 flex items-start gap-2 text-red-600 bg-red-50 p-3 rounded-xl border border-red-100">
                                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                                <p className="text-xs font-bold">{guarantorError}</p>
                            </div>
                        )}
                        
                        <div className="flex flex-wrap gap-2">
                            {form.guarantorIds.length === 0 && (
                                <div className="w-full text-center py-4 border-2 border-dashed border-gray-200 rounded-xl text-gray-400 text-sm">
                                    No guarantors added.
                                </div>
                            )}
                            {form.guarantorIds.map(id => (
                                <div key={id} className="bg-white border border-gray-200 pl-3 pr-2 py-1.5 rounded-lg flex items-center gap-2 text-sm shadow-sm group hover:border-red-200 transition-colors">
                                    <div className="w-6 h-6 rounded-full bg-purple-100 text-purple-700 flex items-center justify-center text-xs font-bold">
                                        {id.charAt(0)}
                                    </div>
                                    <span className="font-bold text-gray-700">{id}</span>
                                    <button onClick={() => removeGuarantor(id)} className="p-1 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-md transition ml-1">
                                        <X className="w-3 h-3" />
                                    </button>
                                </div>
                            ))}
                        </div>
                     </div>
                 </div>

                 <div className="h-px bg-gray-100"></div>

                 {/* Section 3: Collateral Upload */}
                 <div className="space-y-4">
                     <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                        <UploadCloud className="w-4 h-4" /> Collateral & Documents
                     </h4>
                     
                     <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                         <div className="md:col-span-1">
                             <label className="block text-xs font-bold text-gray-500 uppercase mb-1.5">Type</label>
                             <select 
                                className="w-full bg-gray-50 border-2 border-transparent rounded-xl px-3 py-2.5 outline-none text-sm font-medium text-gray-700 focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all"
                                value={newDocType}
                                onChange={(e) => setNewDocType(e.target.value)}
                             >
                                 <option value="DEED">Land Deed (Chanote)</option>
                                 <option value="VEHICLE_BOOK">Vehicle Reg. Book</option>
                                 <option value="GUARANTOR_ID">Guarantor ID Card</option>
                                 <option value="CONTRACT">Signed Contract</option>
                                 <option value="OTHER">Other Asset</option>
                             </select>
                         </div>
                         <div className="md:col-span-2">
                             <label className="block text-xs font-bold text-gray-500 uppercase mb-1.5">Description / Ref No.</label>
                             <div className="flex gap-2">
                                <input 
                                    type="text" 
                                    className="flex-1 bg-gray-50 border-2 border-transparent rounded-xl px-4 py-2.5 outline-none text-sm font-medium text-gray-700 focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all placeholder-gray-400" 
                                    placeholder="e.g. Deed No. 12345 (Ban Sai Yai)"
                                    value={newDocDesc}
                                    onChange={(e) => setNewDocDesc(e.target.value)}
                                />
                             </div>
                         </div>
                     </div>

                     {/* Drop Zone */}
                     <div 
                        className={`border-2 border-dashed rounded-2xl p-8 text-center transition-all duration-200 cursor-pointer group ${
                            isDragging ? 'border-emerald-500 bg-emerald-50/50' : 'border-gray-200 hover:border-emerald-400 hover:bg-gray-50'
                        }`}
                        onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
                        onDragLeave={() => setIsDragging(false)}
                        onDrop={handleDrop}
                        onClick={() => fileInputRef.current?.click()}
                     >
                         <input type="file" className="hidden" ref={fileInputRef} onChange={handleFileSelect} />
                         <div className="w-12 h-12 bg-white rounded-full shadow-sm flex items-center justify-center mx-auto mb-3 border border-gray-100 group-hover:scale-110 transition-transform duration-300">
                            <UploadCloud className={`w-6 h-6 ${isDragging ? 'text-emerald-500' : 'text-gray-400 group-hover:text-emerald-500'}`} />
                         </div>
                         <p className="text-sm font-bold text-gray-700">Click to upload or drag and drop</p>
                         <p className="text-xs text-gray-400 mt-1">Supports JPG, PNG, PDF (Max 10MB)</p>
                     </div>

                     {/* File List */}
                     <div className="space-y-3">
                         {documents.map((doc, idx) => (
                             <div key={doc.id} className="flex items-center justify-between bg-white p-3 rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-all group">
                                 <div className="flex items-center gap-4">
                                     <div className="w-12 h-12 rounded-xl bg-gray-100 flex items-center justify-center shrink-0 overflow-hidden relative border border-gray-200">
                                         {doc.fileName.match(/\.(jpg|jpeg|png|webp)$/i) ? (
                                             <img src={doc.url} alt="thumbnail" className="w-full h-full object-cover" />
                                         ) : (
                                             <FileText className="w-6 h-6 text-gray-400" />
                                         )}
                                     </div>
                                     <div>
                                         <p className="text-sm font-bold text-gray-800">{doc.type}</p>
                                         <p className="text-xs text-gray-500 font-medium">{doc.description}</p>
                                         <p className="text-[10px] text-gray-400 mt-0.5">{(doc.fileSize ? (doc.fileSize/1024/1024).toFixed(2) : 0)} MB</p>
                                     </div>
                                 </div>
                                 <button onClick={() => removeDocument(doc.id)} className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-xl transition">
                                     <Trash2 className="w-4 h-4" />
                                 </button>
                             </div>
                         ))}
                     </div>
                 </div>
             </div>
             
             {/* Footer */}
             <div className="px-8 py-6 border-t border-gray-100 bg-gray-50/50 flex justify-end gap-3">
                <button 
                    onClick={onClose} 
                    className="px-6 py-3 text-sm font-bold text-gray-600 hover:text-gray-800 hover:bg-white border border-transparent hover:border-gray-200 rounded-xl transition-all duration-200"
                >
                    Cancel
                </button>
                <button 
                    onClick={handleSubmit} 
                    className="px-8 py-3 text-sm font-bold text-white rounded-xl shadow-lg shadow-emerald-500/30 flex items-center gap-2 transform active:scale-[0.98] transition-all duration-200 bg-gradient-to-r from-emerald-600 to-emerald-500 hover:from-emerald-500 hover:to-emerald-400"
                >
                    <Send className="w-4 h-4" /> Submit Application
                </button>
             </div>
          </div>
        </div>
    );
};

// --- NOTIFY PAYMENT MODAL ---
interface NotifyPaymentModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (amount: string, file: File | null) => void;
}

export const NotifyPaymentModal: React.FC<NotifyPaymentModalProps> = ({ isOpen, onClose, onSubmit }) => {
    const [amount, setAmount] = useState('');
    const [file, setFile] = useState<File | null>(null);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
            <div className="bg-white rounded-3xl shadow-2xl max-w-sm w-full p-6 animate-fade-in">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-lg font-bold text-gray-800">Notify Payment</h3>
                    <button onClick={onClose} className="p-2 bg-gray-100 rounded-full text-gray-500 hover:bg-gray-200"><X className="w-4 h-4"/></button>
                </div>
                
                <div className="space-y-4">
                    <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Transfer Amount</label>
                        <input 
                            type="number" 
                            className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-emerald-500 font-bold text-xl" 
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            placeholder="0.00"
                        />
                    </div>
                    
                    <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Upload Slip</label>
                        <div className="border-2 border-dashed border-gray-300 rounded-xl p-6 text-center hover:bg-gray-50 transition cursor-pointer relative">
                            <input 
                                type="file" 
                                className="absolute inset-0 opacity-0 cursor-pointer" 
                                onChange={(e) => setFile(e.target.files?.[0] || null)}
                            />
                            <div className="flex flex-col items-center gap-2 text-gray-400">
                                {file ? (
                                    <>
                                    <FileCheck className="w-8 h-8 text-emerald-500" />
                                    <span className="text-sm font-medium text-emerald-600">{file.name}</span>
                                    </>
                                ) : (
                                    <>
                                    <Image className="w-8 h-8" />
                                    <span className="text-xs">Tap to upload image</span>
                                    </>
                                )}
                            </div>
                        </div>
                    </div>

                    <button 
                        onClick={() => onSubmit(amount, file)}
                        className="w-full py-3 bg-emerald-600 text-white rounded-xl font-bold hover:bg-emerald-700 transition shadow-lg flex items-center justify-center gap-2 mt-4"
                        disabled={!amount || !file}
                    >
                        <Send className="w-4 h-4" /> Submit Notification
                    </button>
                </div>
            </div>
        </div>
    );
};

// --- REPAYMENT MODAL (OFFICER) ---
interface RepaymentModalProps {
    isOpen: boolean;
    onClose: () => void;
    loan: Loan;
    onSubmit: (amount: string, breakdown: any) => void;
}

export const RepaymentModal: React.FC<RepaymentModalProps> = ({ isOpen, onClose, loan, onSubmit }) => {
    const [amount, setAmount] = useState('');

    const calculateBreakdown = () => {
        const inputAmount = parseFloat(amount) || 0;
        const annualRate = loan.interestRate / 100;
        const monthlyInterest = (loan.remainingBalance * annualRate) / 12;
        const accruedInterest = monthlyInterest; 
        const payInterest = Math.min(inputAmount, accruedInterest);
        const payPrincipal = Math.max(0, inputAmount - payInterest);
        const actualPrincipalPaid = Math.min(payPrincipal, loan.remainingBalance);
        const newBalance = loan.remainingBalance - actualPrincipalPaid;
        
        return { interest: payInterest, principal: actualPrincipalPaid, newBalance, accruedInterest };
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
             <div className="bg-white rounded-3xl shadow-xl max-w-lg w-full p-8 animate-fade-in relative overflow-hidden">
                  <div className="flex justify-between items-center mb-6">
                      <div>
                          <h3 className="text-xl font-bold text-gray-800">Repayment Processing</h3>
                          <p className="text-sm text-gray-500">{loan.memberName} • {loan.contractNo || 'No Contract'}</p>
                      </div>
                      <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
                          <Calculator className="w-6 h-6" />
                      </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4 mb-6">
                      <div className="p-4 bg-red-50 border border-red-100 rounded-2xl">
                          <p className="text-xs font-bold text-red-800 uppercase tracking-wide">Outstanding Balance</p>
                          <p className="text-2xl font-bold text-red-700">฿{loan.remainingBalance.toLocaleString()}</p>
                      </div>
                      <div className="p-4 bg-orange-50 border border-orange-100 rounded-2xl">
                          <p className="text-xs font-bold text-orange-800 uppercase tracking-wide">Interest Accrued</p>
                          <p className="text-2xl font-bold text-orange-700">
                              ฿{((loan.remainingBalance * (loan.interestRate / 100)) / 12).toLocaleString(undefined, { maximumFractionDigits: 0 })}
                              <span className="text-xs font-medium text-orange-600 ml-1">(Est.)</span>
                          </p>
                      </div>
                  </div>

                  <div className="space-y-4">
                      <div>
                          <label className="block text-sm font-bold text-gray-700 mb-2">Total Cash Received</label>
                          <div className="relative">
                              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-bold text-lg">฿</span>
                              <input 
                                type="number" 
                                className="w-full border-2 border-gray-200 rounded-2xl pl-10 pr-4 py-4 outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100 font-bold text-3xl text-gray-800 transition"
                                value={amount}
                                onChange={(e) => setAmount(e.target.value)}
                                placeholder="0.00"
                                autoFocus
                              />
                          </div>
                      </div>

                      {amount && parseFloat(amount) > 0 && (
                          <div className="bg-gray-50 rounded-2xl p-5 border border-gray-200 space-y-3 animate-in fade-in slide-in-from-top-2">
                              {(() => {
                                  const breakdown = calculateBreakdown();
                                  return (
                                      <>
                                          <div className="flex justify-between items-center text-sm">
                                              <span className="text-gray-500 font-medium">To Interest ({loan.interestRate}%):</span>
                                              <span className="font-bold text-orange-600">฿{breakdown.interest.toLocaleString(undefined, { minimumFractionDigits: 2 })}</span>
                                          </div>
                                          <div className="flex justify-between items-center text-sm border-b border-gray-200 pb-2">
                                              <span className="text-gray-500 font-medium">To Principal:</span>
                                              <span className="font-bold text-emerald-600">฿{breakdown.principal.toLocaleString(undefined, { minimumFractionDigits: 2 })}</span>
                                          </div>
                                          <div className="flex justify-between items-center pt-1">
                                              <span className="text-xs font-bold text-gray-400 uppercase">New Balance</span>
                                              <span className="font-bold text-lg text-gray-800">฿{breakdown.newBalance.toLocaleString(undefined, { minimumFractionDigits: 2 })}</span>
                                          </div>
                                          {parseFloat(amount) > loan.remainingBalance + breakdown.accruedInterest && (
                                              <div className="bg-red-100 text-red-700 text-xs p-2 rounded-lg mt-2 flex items-center font-bold">
                                                  <AlertCircle className="w-3 h-3 mr-1" /> Overpayment Detected
                                              </div>
                                          )}
                                      </>
                                  )
                              })()}
                          </div>
                      )}
                  </div>

                  <div className="flex flex-col-reverse sm:flex-row justify-end gap-3 mt-8 pt-4 border-t border-gray-100">
                    <button onClick={onClose} className="w-full sm:w-auto px-6 py-3 text-gray-600 hover:bg-gray-100 rounded-xl transition font-medium">Cancel</button>
                    <button 
                        onClick={() => onSubmit(amount, calculateBreakdown())} 
                        className="w-full sm:w-auto px-8 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition shadow-lg shadow-blue-200 font-bold flex items-center justify-center gap-2"
                        disabled={!amount || parseFloat(amount) <= 0}
                    >
                        <Printer className="w-4 h-4" /> Confirm & Print
                    </button>
                  </div>
             </div>
          </div>
    );
};

// --- MEMBER PAYMENT MODAL (QR) ---
interface MemberPaymentModalProps {
    isOpen: boolean;
    onClose: () => void;
    loan: Loan;
}

export const MemberPaymentModal: React.FC<MemberPaymentModalProps> = ({ isOpen, onClose, loan }) => {
    const [paymentMode, setPaymentMode] = useState<'INSTALLMENT' | 'CLOSEOUT'>('INSTALLMENT');

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
            <div className="bg-white rounded-3xl shadow-2xl max-w-sm w-full overflow-hidden animate-fade-in relative">
                <button 
                    onClick={onClose}
                    className="absolute top-4 right-4 z-10 p-2 bg-gray-100 rounded-full hover:bg-gray-200 text-gray-500 transition"
                >
                    <X className="w-4 h-4" />
                </button>

                <div className="p-6 pb-0">
                    <h3 className="text-xl font-bold text-gray-800 text-center mb-1">Payoff Quote</h3>
                    <p className="text-xs text-gray-500 text-center mb-6">Valid for payment made today</p>

                    <div className="flex bg-gray-100 p-1 rounded-xl mb-6">
                        <button 
                            className={`flex-1 py-2 rounded-lg text-xs font-bold transition ${paymentMode === 'INSTALLMENT' ? 'bg-white shadow-sm text-gray-800' : 'text-gray-500'}`}
                            onClick={() => setPaymentMode('INSTALLMENT')}
                        >
                            Installment
                        </button>
                        <button 
                            className={`flex-1 py-2 rounded-lg text-xs font-bold transition ${paymentMode === 'CLOSEOUT' ? 'bg-red-500 shadow-sm text-white' : 'text-gray-500'}`}
                            onClick={() => setPaymentMode('CLOSEOUT')}
                        >
                            Full Settlement
                        </button>
                    </div>

                    {(() => {
                        const dailyInterest = (loan.remainingBalance * (loan.interestRate / 100)) / 365;
                        const accruedInterest = Math.ceil(dailyInterest * 30); 
                        const totalPayoff = loan.remainingBalance + accruedInterest;
                        const installmentAmt = Math.ceil(loan.principalAmount / loan.termMonths) + Math.ceil(dailyInterest * 30);
                        const displayAmount = paymentMode === 'CLOSEOUT' ? totalPayoff : installmentAmt;

                        return (
                            <div className="text-center space-y-4">
                                <div className={`p-4 rounded-2xl border-2 ${paymentMode === 'CLOSEOUT' ? 'bg-red-50 border-red-100' : 'bg-gray-50 border-gray-100'}`}>
                                    <p className="text-xs font-bold text-gray-400 uppercase tracking-wide mb-1">Total Amount Due</p>
                                    <p className={`text-4xl font-bold ${paymentMode === 'CLOSEOUT' ? 'text-red-600' : 'text-gray-800'}`}>
                                        ฿{displayAmount.toLocaleString()}
                                    </p>
                                    {paymentMode === 'CLOSEOUT' && (
                                        <div className="flex justify-center gap-3 mt-3 text-[10px] text-gray-500">
                                            <span>Prin: ฿{loan.remainingBalance.toLocaleString()}</span>
                                            <span>+ Int: ฿{accruedInterest}</span>
                                        </div>
                                    )}
                                </div>
                                
                                <div className="flex items-center justify-center gap-2 text-xs text-gray-400">
                                    <Info className="w-3 h-3" />
                                    <span>Includes estimated interest to date.</span>
                                </div>
                            </div>
                        );
                    })()}
                </div>

                <div className="bg-gray-900 mt-8 p-8 text-white text-center rounded-t-[2.5rem] relative">
                    <div className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white p-2 rounded-full border-4 border-gray-900">
                        <ArrowDownLeft className="w-6 h-6 text-gray-900" />
                    </div>
                    
                    <h4 className="font-bold text-lg mb-4">Show to Officer</h4>
                    <div className="bg-white p-4 rounded-2xl w-48 h-48 mx-auto flex items-center justify-center mb-4">
                        <QrCode className="w-40 h-40 text-black" />
                    </div>
                    <p className="text-gray-400 text-xs font-mono tracking-widest">
                        REF: {loan.id}-{paymentMode === 'CLOSEOUT' ? 'FULL' : 'INST'}
                    </p>
                </div>
            </div>
        </div>
    );
};
