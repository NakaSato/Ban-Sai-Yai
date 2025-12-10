import React from 'react';
import { 
  ShieldCheck, 
  Users, 
  Settings, 
  Activity, 
  Lock, 
  FileKey,
  Database
} from 'lucide-react';
import StatCard from './StatCard';
import { ViewState } from '../../types';

interface AdminDashboardProps {
    onNavigate: (view: ViewState) => void;
}

const AdminDashboard: React.FC<AdminDashboardProps> = ({ onNavigate }) => {
  const systemLogs = [
      { id: 1, action: 'User Login', user: 'president_01', ip: '192.168.1.10', time: '10:05 AM', status: 'Success' },
      { id: 2, action: 'Role Update', user: 'admin', ip: '192.168.1.5', time: '09:45 AM', status: 'Success' },
      { id: 3, action: 'Failed Login', user: 'unknown', ip: '203.0.113.42', time: '08:30 AM', status: 'Blocked' },
  ];

  return (
    <div className="space-y-6 animate-fade-in pb-8">
        <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
            <div>
                <h2 className="text-2xl font-bold text-gray-800">Admin Console</h2>
                <p className="text-gray-500">System Configuration & Security Audit</p>
            </div>
        </div>

        {/* KPIs */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 lg:gap-6">
            <StatCard 
                title="System Status" 
                value="Healthy" 
                icon={Activity} 
                colorClass="bg-green-50 text-green-600"
                subtext="Uptime 99.9%"
            />
            <StatCard 
                title="Active Sessions" 
                value="12" 
                icon={Users} 
                colorClass="bg-blue-50 text-blue-600"
                subtext="Concurrent Users"
            />
            <StatCard 
                title="Security Alerts" 
                value="0" 
                icon={ShieldCheck} 
                colorClass="bg-emerald-50 text-emerald-600"
                subtext="Last 24 hours"
            />
            <StatCard 
                title="Database Size" 
                value="45 MB" 
                icon={Database} 
                colorClass="bg-purple-50 text-purple-600"
                subtext="Auto-backup: ON"
            />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* User Management Mock */}
            <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6">
                <div className="flex items-center space-x-3 mb-6">
                    <div className="p-2 bg-indigo-100 rounded-lg text-indigo-600">
                        <Users className="w-6 h-6" />
                    </div>
                    <h3 className="text-xl font-bold text-gray-800">User Management</h3>
                </div>
                <div className="space-y-3">
                    <div className="flex justify-between items-center p-3 border border-gray-100 rounded-xl hover:bg-gray-50 transition">
                        <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-600">P</div>
                            <div>
                                <p className="font-bold text-sm text-gray-800">President Account</p>
                                <p className="text-xs text-gray-500">Role: PRESIDENT</p>
                            </div>
                        </div>
                        <button className="text-xs font-bold text-blue-600 hover:underline">Edit Perms</button>
                    </div>
                    <div className="flex justify-between items-center p-3 border border-gray-100 rounded-xl hover:bg-gray-50 transition">
                        <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-600">S</div>
                            <div>
                                <p className="font-bold text-sm text-gray-800">Secretary Account</p>
                                <p className="text-xs text-gray-500">Role: SECRETARY</p>
                            </div>
                        </div>
                        <button className="text-xs font-bold text-blue-600 hover:underline">Edit Perms</button>
                    </div>
                    <div className="flex justify-between items-center p-3 border border-gray-100 rounded-xl hover:bg-gray-50 transition">
                        <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-600">O</div>
                            <div>
                                <p className="font-bold text-sm text-gray-800">Officer Account</p>
                                <p className="text-xs text-gray-500">Role: OFFICER</p>
                            </div>
                        </div>
                        <button className="text-xs font-bold text-blue-600 hover:underline">Edit Perms</button>
                    </div>
                </div>
                <button className="w-full mt-4 py-2 border border-dashed border-gray-300 rounded-xl text-gray-500 text-sm hover:bg-gray-50 font-bold">
                    + Provision New User
                </button>
            </div>

            {/* Audit Logs */}
            <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6">
                <div className="flex items-center space-x-3 mb-6">
                    <div className="p-2 bg-gray-100 rounded-lg text-gray-600">
                        <FileKey className="w-6 h-6" />
                    </div>
                    <h3 className="text-xl font-bold text-gray-800">Audit Logs</h3>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm">
                        <thead className="text-xs text-gray-400 uppercase font-bold border-b border-gray-100">
                            <tr>
                                <th className="pb-2">Action</th>
                                <th className="pb-2">User</th>
                                <th className="pb-2">Time</th>
                                <th className="pb-2 text-right">Status</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-50">
                            {systemLogs.map(log => (
                                <tr key={log.id} className="group hover:bg-gray-50">
                                    <td className="py-3 font-medium text-gray-700">{log.action}</td>
                                    <td className="py-3 text-gray-500">{log.user}</td>
                                    <td className="py-3 text-gray-500">{log.time}</td>
                                    <td className="py-3 text-right">
                                        <span className={`px-2 py-1 rounded-full text-[10px] font-bold ${
                                            log.status === 'Success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                                        }`}>
                                            {log.status}
                                        </span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        {/* Configurations */}
        <div className="bg-gray-900 rounded-3xl p-6 text-white shadow-lg">
             <div className="flex items-center gap-3 mb-4">
                 <Settings className="w-6 h-6 text-emerald-400" />
                 <h3 className="font-bold text-lg">System Configurations</h3>
             </div>
             <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-sm text-gray-300">
                 <div>
                     <p className="font-bold text-white mb-1">Global Interest Rate</p>
                     <p>Savings: 1.5% APY</p>
                     <p>Loans: 6.0% APY</p>
                 </div>
                 <div>
                     <p className="font-bold text-white mb-1">Security Policy</p>
                     <p>Session Timeout: 15 mins</p>
                     <p>Password Rotation: 90 days</p>
                 </div>
                 <div>
                     <p className="font-bold text-white mb-1">Data Retention</p>
                     <p>Logs: 1 Year</p>
                     <p>Backups: Daily (Midnight)</p>
                 </div>
             </div>
        </div>
    </div>
  );
};

export default AdminDashboard;