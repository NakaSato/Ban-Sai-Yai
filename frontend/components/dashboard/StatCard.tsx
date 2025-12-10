import React from 'react';
import { LucideIcon } from 'lucide-react';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: LucideIcon;
  colorClass: string;
  subtext?: string;
  onClick?: () => void;
  gradient?: boolean;
}

const StatCard: React.FC<StatCardProps> = ({ title, value, icon: Icon, colorClass, subtext, onClick, gradient }) => (
    <div 
      onClick={onClick}
      className={`relative overflow-hidden rounded-2xl p-6 shadow-sm transition-all duration-300 hover:shadow-lg hover:-translate-y-1 cursor-pointer group border ${
        gradient ? 'border-transparent text-white' : 'bg-white border-gray-100 text-gray-800'
      } ${gradient ? colorClass : ''}`}
    >
      {gradient && (
         <>
            {/* Abstract Background Shapes for Gradients */}
            <div className="absolute -top-12 -right-12 w-32 h-32 bg-white/10 rounded-full blur-2xl pointer-events-none"></div>
            <div className="absolute -bottom-8 -left-8 w-24 h-24 bg-black/5 rounded-full blur-xl pointer-events-none"></div>
            <div className="absolute top-4 right-4 p-2 opacity-20 transform rotate-12 group-hover:scale-110 group-hover:rotate-6 transition-transform duration-500">
                <Icon className="w-16 h-16" />
            </div>
         </>
      )}
      
      <div className="relative z-10 flex flex-col h-full justify-between">
          <div className="flex items-start justify-between mb-4">
             <div>
                 <p className={`text-sm font-semibold tracking-wide ${gradient ? 'text-white/90' : 'text-gray-500'}`}>{title}</p>
             </div>
             {!gradient && (
                  <div className={`p-2.5 rounded-xl ${colorClass} bg-opacity-10 shrink-0`}>
                      <Icon className={`w-6 h-6 ${colorClass.replace('bg-', 'text-')}`} />
                  </div>
              )}
          </div>
          
          <div>
              <h3 className="text-3xl font-bold tracking-tight">{value}</h3>
              {subtext && (
                  <div className={`flex items-center mt-2 text-xs font-medium ${gradient ? 'text-white/80' : 'text-gray-400'}`}>
                      {gradient ? (
                          <span className="bg-white/20 px-2 py-0.5 rounded-lg backdrop-blur-sm">{subtext}</span>
                      ) : (
                          <span>{subtext}</span>
                      )}
                  </div>
              )}
          </div>
      </div>
    </div>
);

export default StatCard;