import React, { useState, useMemo, useEffect } from "react";
import { Member, UserRole, Transaction } from "../types";
import { api } from "../services/api";
import { Search, Plus, Loader2 } from "lucide-react";
import ReceiptModal from "./ReceiptModal";
import { MemberProfile } from "./MemberProfile";
import { MemberTable } from "./MemberTable";
import { ShareModal, MemberFormModal } from "./MemberModals";

interface MemberManagementProps {
  userRole: UserRole;
}

const MemberManagement: React.FC<MemberManagementProps> = ({ userRole }) => {
  const [members, setMembers] = useState<Member[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  // Modals State
  const [showMemberModal, setShowMemberModal] = useState(false);
  const [showShareModal, setShowShareModal] = useState(false);
  const [showFineModal, setShowFineModal] = useState(false); // Kept locally or extract if needed
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  // Receipt State
  const [receiptData, setReceiptData] = useState<any>(null);
  const [showReceipt, setShowReceipt] = useState(false);

  const [editingMember, setEditingMember] = useState<Member | null>(null);
  const [selectedMember, setSelectedMember] = useState<Member | null>(null);
  const [memberToDelete, setMemberToDelete] = useState<Member | null>(null);
  const [amountInput, setAmountInput] = useState<string>("");

  const [formData, setFormData] = useState<Partial<Member>>({
    fullName: "",
    idCardNumber: "",
    birthDate: "",
    address: "",
    phoneNumber: "",
    joinedDate: "",
    monthlyIncome: 0,
    occupation: "",
  });

  // Loan summary state for member view
  const [loanSummary, setLoanSummary] = useState<any>(null);

  // Fetch Members and Transactions on Mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        if (userRole === UserRole.MEMBER) {
          // For members: fetch their own profile, transactions, and loan summary
          const myProfile = await api.members.getMyProfile();
          const [myTransactions, myLoanSummary] = await Promise.all([
            api.members.getTransactions(myProfile.id).catch(() => []),
            api.members.getLoanSummary(myProfile.id).catch(() => null),
          ]);
          setMembers([myProfile]);
          setTransactions(myTransactions);
          setLoanSummary(myLoanSummary);
        } else {
          // For officers/admin: fetch all members and general transactions
          const [membersData, transactionsData] = await Promise.all([
            api.members.getAll(),
            api.accounting.getTransactions(),
          ]);
          setMembers(membersData);
          setTransactions(transactionsData);
        }
      } catch (error) {
        console.error("Failed to fetch data", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [userRole]);

  const filteredMembers = useMemo(() => {
    return members.filter(
      (m) =>
        m.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        m.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (m.idCardNumber && m.idCardNumber.includes(searchTerm)) ||
        (m.phoneNumber && m.phoneNumber.includes(searchTerm))
    );
  }, [members, searchTerm]);

  const handleSaveMember = async () => {
    if (editingMember) {
      setMembers((prev) =>
        prev.map((m) =>
          m.id === editingMember.id ? ({ ...m, ...formData } as Member) : m
        )
      );
      await api.members.update(editingMember.id, formData);
    } else {
      const newMember = {
        ...formData,
        id: "TEMP_ID",
        shareBalance: 0,
        savingsBalance: 0,
        status: "ACTIVE" as "ACTIVE" | "INACTIVE",
        isFrozen: false,
      } as Member;

      const created = await api.members.create(newMember);
      setMembers([...members, created]);

      // --- AUTOMATED APPLICATION FEE ---
      const feeAmount = 100;
      const feeTx: Transaction = {
        id: `T-FEE-${Date.now()}`,
        date: new Date().toISOString().split("T")[0],
        type: "INCOME", // Use INCOME to record in journal
        amount: feeAmount,
        memberId: created.id,
        category: "Fee Income",
        description: "Membership Application Fee",
        receiptId: `REC-FEE-${Date.now().toString().slice(-6)}`,
      };

      // API call to record fee (Simulated)
      await api.accounting.createEntry(feeTx);
      setTransactions((prev) => [feeTx, ...prev]);

      // Trigger Receipt Immediately
      setReceiptData({
        receiptNo: feeTx.receiptId,
        date: new Date().toLocaleDateString(),
        receivedFrom: created.fullName,
        description: "Application Fee (ค่าธรรมเนียมแรกเข้า)",
        amount: feeAmount,
        cashier: "Officer",
      });
      setShowReceipt(true);
    }
    setShowMemberModal(false);
    setEditingMember(null);
    setFormData({
      fullName: "",
      idCardNumber: "",
      birthDate: "",
      address: "",
      phoneNumber: "",
      joinedDate: "",
      monthlyIncome: 0,
      occupation: "",
    });
  };

  const handleBuyShares = async () => {
    if (selectedMember && amountInput) {
      if (selectedMember.isFrozen) {
        alert("Account is Frozen. Transactions not allowed.");
        return;
      }
      const amount = parseFloat(amountInput);

      setMembers((prev) =>
        prev.map((m) =>
          m.id === selectedMember.id
            ? { ...m, shareBalance: m.shareBalance + amount }
            : m
        )
      );
      await api.savings.deposit(selectedMember.id, amount);

      const newTx = {
        receiptNo: `REC-${Date.now().toString().slice(-6)}`,
        date: new Date().toLocaleDateString(),
        receivedFrom: selectedMember.fullName,
        description: "Share Capital Purchase",
        amount: amount,
        cashier: "Officer (Auto)",
      };

      // Optimistically add transaction
      setTransactions((prev) => [
        {
          id: `T-${Date.now()}`,
          date: new Date().toISOString().split("T")[0],
          type: "SHARE_PURCHASE",
          amount: amount,
          memberId: selectedMember.id,
          description: "Share Capital Purchase",
          receiptId: newTx.receiptNo,
        } as Transaction,
        ...prev,
      ]);

      setReceiptData(newTx);
      setShowShareModal(false);
      setAmountInput("");
      setSelectedMember(null);
      setShowReceipt(true);
    }
  };

  const handleDeleteClick = (member: Member) => {
    setMemberToDelete(member);
    setShowDeleteModal(true);
  };
  const confirmDelete = async () => {
    if (memberToDelete) {
      await api.members.delete(memberToDelete.id);
      setMembers((prev) => prev.filter((m) => m.id !== memberToDelete.id));
      setShowDeleteModal(false);
      setMemberToDelete(null);
    }
  };

  const toggleFreeze = async (member: Member) => {
    const newStatus = !member.isFrozen;
    setMembers((prev) =>
      prev.map((m) => (m.id === member.id ? { ...m, isFrozen: newStatus } : m))
    );
    await api.members.update(member.id, { isFrozen: newStatus });
  };

  const openAddModal = () => {
    setEditingMember(null);
    setFormData({
      fullName: "",
      idCardNumber: "",
      birthDate: "",
      address: "",
      phoneNumber: "",
      joinedDate: new Date().toISOString().split("T")[0],
      monthlyIncome: 0,
      occupation: "",
    });
    setShowMemberModal(true);
  };
  const openEditModal = (member: Member) => {
    setEditingMember(member);
    setFormData(member);
    setShowMemberModal(true);
  };
  const openShareModal = (member: Member) => {
    setSelectedMember(member);
    setAmountInput("");
    setShowShareModal(true);
  };
  const openFineModal = (member: Member) => {
    setSelectedMember(member);
    setAmountInput("");
    setShowFineModal(true);
  };

  // Permission Logic
  // President: View Only
  // Secretary: Can Edit, Cannot Register
  // Officer: Can Register, Can Edit, Can Freeze (Full Action)
  const canModify =
    userRole === UserRole.OFFICER || userRole === UserRole.SECRETARY;
  const canRegister = userRole === UserRole.OFFICER;

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center">
        <Loader2 className="w-10 h-10 text-emerald-600 animate-spin" />
      </div>
    );
  }

  // --- MEMBER VIEW ---
  if (userRole === UserRole.MEMBER && members.length > 0) {
    return (
      <MemberProfile
        member={members[0]}
        transactions={transactions}
        loanSummary={loanSummary}
      />
    );
  }

  // --- OFFICER/ADMIN VIEW ---
  return (
    <div className="space-y-6 pb-8">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            Member Management
          </h2>
          <p className="text-sm text-gray-500">
            {canModify || canRegister
              ? "Manage registrations, status, and details"
              : "View member information"}
          </p>
        </div>

        {canRegister && (
          <button
            onClick={openAddModal}
            className="w-full sm:w-auto bg-emerald-600 text-white px-5 py-2.5 rounded-xl hover:bg-emerald-700 transition flex items-center justify-center space-x-2 shadow-lg shadow-emerald-200/50 active:scale-95 duration-150 font-bold text-sm"
          >
            <Plus className="w-5 h-5" />
            <span>Register Member</span>
          </button>
        )}
      </div>

      <div className="bg-white px-4 py-2 rounded-2xl shadow-sm border border-gray-200 flex items-center space-x-3 focus-within:ring-2 focus-within:ring-emerald-500 transition-all">
        <Search className="w-5 h-5 text-gray-400" />
        <input
          type="text"
          placeholder="Search members by Name, ID, ID Card, or Phone..."
          className="flex-1 outline-none text-sm text-gray-700 placeholder-gray-400 bg-transparent min-w-0 py-2"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <MemberTable
        members={filteredMembers}
        transactions={transactions}
        userRole={userRole}
        onOpenShare={openShareModal}
        onOpenFine={openFineModal}
        onOpenEdit={openEditModal}
        onDelete={handleDeleteClick}
        onToggleFreeze={toggleFreeze}
      />

      <ShareModal
        isOpen={showShareModal}
        onClose={() => setShowShareModal(false)}
        member={selectedMember}
        amount={amountInput}
        setAmount={setAmountInput}
        onConfirm={handleBuyShares}
      />

      <MemberFormModal
        isOpen={showMemberModal}
        onClose={() => setShowMemberModal(false)}
        isEditing={!!editingMember}
        formData={formData}
        setFormData={setFormData}
        onSave={handleSaveMember}
      />

      <ReceiptModal
        isOpen={showReceipt}
        onClose={() => setShowReceipt(false)}
        data={receiptData}
      />
    </div>
  );
};

export default MemberManagement;
