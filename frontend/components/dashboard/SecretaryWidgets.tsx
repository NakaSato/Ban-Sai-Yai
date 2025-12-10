
import React from 'react';
import { 
  FileBarChart, FileText, TrendingUp, Receipt, Lock, Download 
} from 'lucide-react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer 
} from 'recharts';
import { ViewState } from '../../types';

// --- Financial Chart ---
interface FinancialSummaryChartProps {
    data: any[];
}

export const FinancialSummaryChart: React.FC<FinancialSummaryChartProps> = ({ data }) => {
    return (
        <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 h-full">
            <div className="flex items-center space-x-3 mb-6">
                <div className="p-2 bg-gray-100 rounded-lg">
                    <FileBarChart className="w-5 h-5 text-gray-600" />
                </div>
                <h3 className="font-bold text-gray-800">Income vs. Expenses</h3>
            </div>
            <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                        <XAxis dataKey="name" stroke="#9ca3af" axisLine={false} tickLine={false} />
                        <YAxis stroke="#9ca3af" axisLine={false} tickLine={false} />
                        <Tooltip 
                            contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                            cursor={{ fill: '#f9fafb' }}
                        />
                        <Legend wrapperStyle={{ paddingTop: '20px' }} />
                        <Bar dataKey="savings" name="Income" fill="#10b981" radius={[6, 6, 0, 0]} />
                        <Bar dataKey="loans" name="Expenses" fill="#ef4444" radius={[6, 6, 0, 0]} />
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};

// --- Controls & Tools ---
interface SecretaryControlsProps {
    onNavigate: (view: ViewState) => void;
}

export const SecretaryControls: React.FC<SecretaryControlsProps> = ({ onNavigate }) => {
    return (
        <div className="space-y-6 h-full">
             {/* Reporting Tools */}
            <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
                <h3 className="font-bold text-gray-800 mb-6 flex items-center gap-2">
                    <FileText className="w-5 h-5 text-gray-500" /> Reporting Tools
                </h3>
                <div className="space-y-3">
                    <ControlButton label="Generate Balance Sheet" icon={FileText} onClick={() => onNavigate('ACCOUNTING')} />
                    <ControlButton label="Generate P&L Statement" icon={TrendingUp} onClick={() => onNavigate('ACCOUNTING')} />
                    <ControlButton label="Calculate Dividends" icon={Receipt} onClick={() => onNavigate('ACCOUNTING')} />
                </div>
            </div>

            {/* Accounting Controls */}
            <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
                <h3 className="font-bold text-gray-800 mb-6 flex items-center gap-2">
                    <Lock className="w-5 h-5 text-gray-500" /> Controls
                </h3>
                 <div className="space-y-3">
                    <div 
                        onClick={() => onNavigate('ACCOUNTING')}
                        className="flex justify-between items-center p-4 bg-orange-50 rounded-2xl border border-orange-100 cursor-pointer hover:shadow-sm transition group"
                    >
                        <span className="text-sm font-bold text-orange-800">Monthly Closing</span>
                        <span className="text-[10px] bg-orange-200 text-orange-900 px-2 py-1 rounded-full uppercase font-bold transition-transform">Pending</span>
                    </div>
                     <button 
                        onClick={() => onNavigate('ACCOUNTING')}
                        className="w-full text-sm text-gray-500 hover:text-emerald-600 hover:underline text-center p-2 transition"
                     >
                        Manage Chart of Accounts
                    </button>
                 </div>
            </div>
        </div>
    );
};

const ControlButton: React.FC<any> = ({ label, icon: Icon, onClick }) => (
    <button 
        onClick={onClick}
        className="w-full flex items-center justify-between p-4 bg-gray-50 hover:bg-emerald-50 text-gray-700 hover:text-emerald-700 rounded-2xl transition group border border-gray-100 hover:border-emerald-200 duration-150"
    >
        <span className="font-medium">{label}</span>
        <Icon className="w-4 h-4 opacity-50 group-hover:opacity-100 transition-transform" />
    </button>
);

// --- Report History Table ---
interface ReportHistoryProps {
    reports: any[];
}

export const ReportHistoryTable: React.FC<ReportHistoryProps> = ({ reports }) => {
    return (
        <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-6 border-b border-gray-100">
                 <h3 className="font-bold text-gray-800">Report History</h3>
            </div>
            <div className="overflow-x-auto">
                <table className="w-full text-left">
                    <thead className="bg-gray-50/50 text-xs uppercase text-gray-500">
                        <tr>
                            <th className="p-4">Report Name</th>
                            <th className="p-4">Date Generated</th>
                            <th className="p-4">Type</th>
                            <th className="p-4 text-right">Action</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100 text-sm">
                        {reports.map((report) => (
                            <tr key={report.id} className="hover:bg-gray-50 transition group">
                                <td className="p-4 font-medium text-gray-700 flex items-center gap-3">
                                    <div className="p-1.5 bg-gray-100 rounded-lg text-gray-500 transition-transform">
                                        <FileText className="w-4 h-4" />
                                    </div>
                                    {report.name}
                                </td>
                                <td className="p-4 text-gray-500">{report.date}</td>
                                <td className="p-4"><span className="bg-gray-100 text-gray-600 px-2 py-1 rounded text-xs font-bold">{report.type}</span></td>
                                <td className="p-4 text-right">
                                    <button className="text-emerald-600 hover:text-emerald-800 font-medium text-sm flex items-center justify-end gap-1 ml-auto group/btn">
                                        <Download className="w-4 h-4 mr-1 transition-transform" /> Download
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
