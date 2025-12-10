import React, { useState, useEffect, useRef } from 'react';
import { generateFinancialAdvice } from '../services/geminiService';
import { api } from '../services/api';
import { Bot, Send, Loader2, Sparkles } from 'lucide-react';
import ReactMarkdown from 'react-markdown';

const GeminiAssistant: React.FC = () => {
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<{role: 'user' | 'ai', content: string}[]>([
    { role: 'ai', content: 'Hello! I am your Satja Savings Smart Assistant. I can help you analyze financial data, explain accounting terms, or summarize member statistics. How can I help you today?' }
  ]);
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim()) return;

    const userMessage = input;
    setInput('');
    setMessages(prev => [...prev, { role: 'user', content: userMessage }]);
    setLoading(true);

    try {
        // Fetch current context data from API to ensure AI answers are up-to-date
        const [members, loans] = await Promise.all([
            api.members.getAll(),
            api.loans.getAll()
        ]);

        const context = `
          Total Members: ${members.length}
          Total Savings: ${members.reduce((acc, m) => acc + m.savingsBalance, 0)}
          Total Loans Active: ${loans.length}
          Total Loan Outstanding: ${loans.reduce((acc, l) => acc + l.remainingBalance, 0)}
          Sample Member: ${members[0]?.fullName}, Status: ${members[0]?.status}
        `;

        const response = await generateFinancialAdvice(userMessage, context);
        setMessages(prev => [...prev, { role: 'ai', content: response }]);
    } catch (error) {
        setMessages(prev => [...prev, { role: 'ai', content: "I'm having trouble connecting to the database right now." }]);
    } finally {
        setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="h-[calc(100vh-6rem)] flex flex-col bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      {/* Header */}
      <div className="bg-emerald-600 p-4 flex items-center space-x-3 text-white">
        <div className="p-2 bg-white/20 rounded-full">
          <Sparkles className="w-5 h-5 text-yellow-300" />
        </div>
        <div>
          <h2 className="font-bold">Smart Financial Assistant</h2>
          <p className="text-xs text-emerald-100">Powered by Gemini AI</p>
        </div>
      </div>

      {/* Chat Area */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
        {messages.map((msg, idx) => (
          <div key={idx} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`flex max-w-[80%] ${msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'} items-start gap-2`}>
              <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${msg.role === 'user' ? 'bg-gray-200' : 'bg-emerald-100'}`}>
                {msg.role === 'user' ? <div className="w-4 h-4 bg-gray-500 rounded-full" /> : <Bot className="w-5 h-5 text-emerald-600" />}
              </div>
              <div className={`p-3 rounded-2xl text-sm leading-relaxed shadow-sm ${
                msg.role === 'user' 
                  ? 'bg-emerald-600 text-white rounded-tr-none' 
                  : 'bg-white text-gray-700 border border-gray-200 rounded-tl-none'
              }`}>
                <ReactMarkdown>{msg.content}</ReactMarkdown>
              </div>
            </div>
          </div>
        ))}
        {loading && (
          <div className="flex justify-start">
            <div className="flex items-center space-x-2 bg-white p-3 rounded-2xl rounded-tl-none border border-gray-200 shadow-sm">
               <Loader2 className="w-4 h-4 text-emerald-600 animate-spin" />
               <span className="text-xs text-gray-500">Analyzing data...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="p-4 bg-white border-t border-gray-100">
        <div className="flex items-center space-x-2 bg-gray-50 p-2 rounded-xl border border-gray-200 focus-within:ring-2 focus-within:ring-emerald-500 transition-all">
          <input 
            type="text" 
            className="flex-1 bg-transparent border-none outline-none px-2 text-sm text-gray-700"
            placeholder="Ask about dividends, member stats, or accounting..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={loading}
          />
          <button 
            onClick={handleSend}
            disabled={loading || !input.trim()}
            className="p-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
        <p className="text-center text-[10px] text-gray-400 mt-2">
          AI generated content may be inaccurate. Please verify critical financial figures.
        </p>
      </div>
    </div>
  );
};

export default GeminiAssistant;