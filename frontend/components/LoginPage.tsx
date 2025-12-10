
import React, { useState } from 'react';
import { 
  Building2, Lock, User, Loader2, ArrowRight, ShieldCheck, HelpCircle, 
  FileKey, KeyRound, ChevronLeft, CreditCard, Phone, CheckCircle, Briefcase, FileText 
} from 'lucide-react';
import { api } from '../services/api';
import { UserRole } from '../types';

interface LoginPageProps {
  onLoginSuccess: (role: UserRole) => void;
}

const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Request Access State
  const [showRequestAccess, setShowRequestAccess] = useState(false);
  const [requestStatus, setRequestStatus] = useState<'IDLE' | 'LOADING' | 'SUCCESS'>('IDLE');
  const [requestRole, setRequestRole] = useState<'MEMBER' | 'OFFICER'>('MEMBER');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Authenticate with API (checks static credentials)
      const response: any = await api.auth.login({ username, password });
      
      // Store token and user for persistence
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.user));
      
      const role = response.user.role;

      // Small delay for UX
      setTimeout(() => {
        onLoginSuccess(role);
      }, 500);

    } catch (err) {
      setError('Invalid credentials. Please try again.');
      setLoading(false);
    }
  };

  const autoFillAndLogin = (roleUser: string) => {
      setUsername(roleUser);
      setPassword(`${roleUser}123`);
      // Trigger login immediately
      setError('');
      setLoading(true);
      setTimeout(async () => {
          try {
            const response: any = await api.auth.login({ username: roleUser, password: `${roleUser}123` });
            
            // Store token and user for persistence
            localStorage.setItem('token', response.token);
            localStorage.setItem('user', JSON.stringify(response.user));

            onLoginSuccess(response.user.role);
          } catch(e) {
              setLoading(false);
              setError('Demo login failed');
          }
      }, 600);
  };

  const handleRequestAccessSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      setRequestStatus('LOADING');
      
      // Simulate API call
      setTimeout(() => {
          setRequestStatus('SUCCESS');
      }, 1500);
  };

  const resetRequestForm = () => {
      setShowRequestAccess(false);
      setRequestStatus('IDLE');
      setRequestRole('MEMBER');
  };

  return (
    <div className="min-h-screen flex bg-gray-50 font-sans">
      {/* Left Side - Visual & Branding */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-[#064e3b] overflow-hidden flex-col justify-between p-12 text-white">
        <div className="absolute inset-0 bg-gradient-to-br from-[#064e3b] to-[#022c22] opacity-90 z-0"></div>
        
        {/* Background Pattern */}
        <div className="absolute inset-0 z-0 opacity-10" style={{ backgroundImage: 'radial-gradient(#10b981 1px, transparent 1px)', backgroundSize: '32px 32px' }}></div>

        <div className="relative z-10">
            {/* <div className="bg-emerald-500/20 p-3 rounded-2xl w-fit border border-emerald-400/20 mb-6 backdrop-blur-sm shadow-lg shadow-emerald-900/20">
                <Building2 className="w-10 h-10 text-emerald-300" />
            </div> */}
            <h1 className="text-5xl font-bold font-display tracking-tight mb-6 leading-tight">Satja Savings<br/><span className="text-emerald-300">Ban Sai Yai</span> Group</h1>
            <p className="text-emerald-100/80 text-lg max-w-md leading-relaxed border-l-4 border-emerald-500/30 pl-4">
                Empowering our community through secure, transparent, and efficient financial management systems.
            </p>
        </div>

        <div className="relative z-10 space-y-6">
            <div className="flex items-center gap-4 bg-white/5 p-4 rounded-2xl border border-white/10 backdrop-blur-sm hover:bg-white/10 transition-colors cursor-default">
                <div className="p-3 bg-emerald-500/20 rounded-xl">
                    <ShieldCheck className="w-6 h-6 text-emerald-300" />
                </div>
                <div>
                    <h3 className="font-bold text-emerald-50">Official Government System</h3>
                    <p className="text-sm text-emerald-200/70">Verified & Secured Access Control</p>
                </div>
            </div>
            <div className="flex items-center justify-between text-xs text-emerald-300/50 font-mono">
                <span>© 2023 Satja Savings Group</span>
                <span>v1.0.3 Secure Build</span>
            </div>
        </div>
      </div>

      {/* Right Side - Login Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12 relative overflow-y-auto">
        <div className="w-full max-w-md space-y-8">
            
            {/* Mobile Logo */}
            <div className="lg:hidden text-center mb-8">
                <div className="bg-[#064e3b] p-3 rounded-2xl w-fit mx-auto mb-4 shadow-lg">
                    <Building2 className="w-8 h-8 text-white" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900">Satja Savings</h2>
                <p className="text-gray-500 text-sm">Ban Sai Yai Group</p>
            </div>

            {!showRequestAccess ? (
                // LOGIN FORM
                <div className="animate-in fade-in slide-in-from-right-4 duration-500">
                    <div className="text-center lg:text-left mb-8">
                        <h2 className="text-3xl font-bold text-gray-900 tracking-tight">Welcome back</h2>
                        <p className="text-gray-500 mt-2">Please enter your credentials to access the system.</p>
                    </div>

                    <form onSubmit={handleLogin} className="space-y-6">
                        <div className="space-y-4">
                            <div className="space-y-2">
                                <label className="text-sm font-bold text-gray-700 ml-1">Username</label>
                                <div className="relative group">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <User className="h-5 w-5 text-gray-400 group-focus-within:text-emerald-600 transition-colors" />
                                    </div>
                                    <input
                                        type="text"
                                        required
                                        className="block w-full pl-11 pr-4 py-3.5 border border-gray-200 rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all bg-gray-50/50 focus:bg-white"
                                        placeholder="Enter your username"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                    />
                                </div>
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-bold text-gray-700 ml-1">Password</label>
                                <div className="relative group">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <Lock className="h-5 w-5 text-gray-400 group-focus-within:text-emerald-600 transition-colors" />
                                    </div>
                                    <input
                                        type="password"
                                        required
                                        className="block w-full pl-11 pr-4 py-3.5 border border-gray-200 rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all bg-gray-50/50 focus:bg-white"
                                        placeholder="••••••••"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                    />
                                </div>
                            </div>
                        </div>

                        {error && (
                            <div className="bg-red-50 text-red-600 text-sm p-3 rounded-xl flex items-center gap-2 animate-in fade-in slide-in-from-top-1 border border-red-100">
                                <HelpCircle className="w-4 h-4" /> {error}
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full flex justify-center items-center py-3.5 px-4 border border-transparent rounded-xl shadow-lg shadow-emerald-600/20 text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-500 transition-all active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed"
                        >
                            {loading ? (
                                <Loader2 className="w-5 h-5 animate-spin" />
                            ) : (
                                <>
                                    Sign In <ArrowRight className="ml-2 w-4 h-4" />
                                </>
                            )}
                        </button>
                    </form>

                    {/* Quick Access Grid */}
                    <div className="mt-8">
                        <div className="flex items-center gap-2 mb-4">
                            <KeyRound className="w-4 h-4 text-gray-400" />
                            <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">Demo Quick Access</span>
                        </div>
                        <div className="grid grid-cols-2 gap-3">
                            <button onClick={() => autoFillAndLogin('president')} className="p-3 bg-white border border-gray-200 rounded-xl hover:border-emerald-500 hover:bg-emerald-50 transition text-left group shadow-sm hover:shadow-md">
                                <p className="text-xs font-bold text-gray-800 group-hover:text-emerald-700">President</p>
                                <p className="text-[10px] text-gray-400">Approver</p>
                            </button>
                            <button onClick={() => autoFillAndLogin('secretary')} className="p-3 bg-white border border-gray-200 rounded-xl hover:border-blue-500 hover:bg-blue-50 transition text-left group shadow-sm hover:shadow-md">
                                <p className="text-xs font-bold text-gray-800 group-hover:text-blue-700">Secretary</p>
                                <p className="text-[10px] text-gray-400">Accounting</p>
                            </button>
                            <button onClick={() => autoFillAndLogin('officer')} className="p-3 bg-white border border-gray-200 rounded-xl hover:border-indigo-500 hover:bg-indigo-50 transition text-left group shadow-sm hover:shadow-md">
                                <p className="text-xs font-bold text-gray-800 group-hover:text-indigo-700">Officer</p>
                                <p className="text-[10px] text-gray-400">Teller / Loans</p>
                            </button>
                            <button onClick={() => autoFillAndLogin('member')} className="p-3 bg-white border border-gray-200 rounded-xl hover:border-orange-500 hover:bg-orange-50 transition text-left group shadow-sm hover:shadow-md">
                                <p className="text-xs font-bold text-gray-800 group-hover:text-orange-700">Member</p>
                                <p className="text-[10px] text-gray-400">View Only</p>
                            </button>
                        </div>
                    </div>

                    <div className="mt-8 pt-6 border-t border-gray-100">
                        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
                            <p className="text-sm text-gray-500">Don't have access?</p>
                            <button 
                                onClick={() => setShowRequestAccess(true)}
                                className="text-sm font-bold text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 px-4 py-2 rounded-lg transition-colors flex items-center gap-2 border border-transparent hover:border-emerald-100"
                            >
                                <FileKey className="w-4 h-4" /> Request Access
                            </button>
                        </div>
                    </div>
                </div>
            ) : (
                // REQUEST ACCESS FORM
                <div className="animate-in fade-in slide-in-from-right-4 duration-500">
                    {requestStatus === 'SUCCESS' ? (
                        <div className="flex flex-col items-center text-center p-6 bg-white rounded-3xl border border-gray-100 shadow-xl animate-in zoom-in-95 duration-300">
                            <div className="w-20 h-20 bg-green-100 text-green-600 rounded-full flex items-center justify-center mb-6 shadow-sm">
                                <CheckCircle className="w-10 h-10" />
                            </div>
                            <h3 className="text-2xl font-bold text-gray-900 mb-2">Request Submitted</h3>
                            <p className="text-gray-500 text-sm mb-8 leading-relaxed">
                                Your registration request has been sent to the System Administrator. 
                                Please allow <strong>24-48 hours</strong> for verification. You will receive an email once approved.
                            </p>
                            <button 
                                onClick={resetRequestForm}
                                className="w-full py-3.5 bg-gray-900 text-white rounded-xl font-bold hover:bg-black transition-all shadow-lg flex items-center justify-center gap-2"
                            >
                                <ArrowRight className="w-4 h-4 rotate-180" /> Back to Login
                            </button>
                        </div>
                    ) : (
                        <div>
                            <div className="mb-6">
                                <button 
                                    onClick={resetRequestForm}
                                    className="text-xs font-bold text-gray-500 hover:text-gray-900 flex items-center gap-1 mb-4 transition-colors bg-white px-3 py-1.5 rounded-lg border border-gray-200 hover:border-gray-300 w-fit"
                                >
                                    <ChevronLeft className="w-4 h-4" /> Back to Login
                                </button>
                                <h2 className="text-2xl font-bold text-gray-900 tracking-tight">Request Access</h2>
                                <p className="text-gray-500 mt-2 text-sm">Fill out the form below to request a new account or upgrade permissions.</p>
                            </div>

                            <form onSubmit={handleRequestAccessSubmit} className="space-y-5">
                                {/* Role Selection */}
                                <div className="grid grid-cols-2 gap-3 p-1 bg-gray-100 rounded-xl mb-6">
                                    <button
                                        type="button"
                                        onClick={() => setRequestRole('MEMBER')}
                                        className={`flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-bold transition-all duration-200 ${requestRole === 'MEMBER' ? 'bg-white shadow-sm text-emerald-600' : 'text-gray-500 hover:text-gray-700'}`}
                                    >
                                        <User className="w-4 h-4" /> Member
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setRequestRole('OFFICER')}
                                        className={`flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-bold transition-all duration-200 ${requestRole === 'OFFICER' ? 'bg-white shadow-sm text-indigo-600' : 'text-gray-500 hover:text-gray-700'}`}
                                    >
                                        <Briefcase className="w-4 h-4" /> Officer/Staff
                                    </button>
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-1">
                                        <label className="text-xs font-bold text-gray-700 uppercase ml-1">First Name</label>
                                        <input type="text" required className="w-full px-4 py-3 border border-gray-200 bg-white rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none transition" placeholder="Somsak" />
                                    </div>
                                    <div className="space-y-1">
                                        <label className="text-xs font-bold text-gray-700 uppercase ml-1">Last Name</label>
                                        <input type="text" required className="w-full px-4 py-3 border border-gray-200 bg-white rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none transition" placeholder="Jaidee" />
                                    </div>
                                </div>
                                
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-1">
                                        <label className="text-xs font-bold text-gray-700 uppercase ml-1">ID Card No.</label>
                                        <div className="relative">
                                            <CreditCard className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                                            <input type="text" required maxLength={13} className="w-full pl-10 pr-4 py-3 border border-gray-200 bg-white rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none transition font-mono text-sm" placeholder="1-xxxx-xxxxx-xx-x" />
                                        </div>
                                    </div>
                                    <div className="space-y-1">
                                        <label className="text-xs font-bold text-gray-700 uppercase ml-1">Phone</label>
                                        <div className="relative">
                                            <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                                            <input type="tel" required className="w-full pl-10 pr-4 py-3 border border-gray-200 bg-white rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none transition font-mono text-sm" placeholder="08x-xxx-xxxx" />
                                        </div>
                                    </div>
                                </div>

                                <div className="space-y-1">
                                    <label className="text-xs font-bold text-gray-700 uppercase ml-1">Request Reason / Note</label>
                                    <div className="relative">
                                        <FileText className="absolute left-3 top-3.5 w-4 h-4 text-gray-400" />
                                        <textarea required rows={2} className="w-full pl-10 pr-4 py-3 border border-gray-200 bg-white rounded-xl focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none transition text-sm resize-none" placeholder={requestRole === 'MEMBER' ? "New member registration request..." : "Requesting promotion to Officer role..."}></textarea>
                                    </div>
                                </div>

                                <button
                                    type="submit"
                                    disabled={requestStatus === 'LOADING'}
                                    className="w-full py-3.5 bg-gray-900 text-white rounded-xl font-bold hover:bg-black transition-all shadow-lg flex items-center justify-center gap-2 mt-2 disabled:opacity-70 disabled:cursor-not-allowed"
                                >
                                    {requestStatus === 'LOADING' ? (
                                        <>
                                            <Loader2 className="w-5 h-5 animate-spin" /> Processing...
                                        </>
                                    ) : (
                                        "Submit Request"
                                    )}
                                </button>
                            </form>
                        </div>
                    )}
                </div>
            )}
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
