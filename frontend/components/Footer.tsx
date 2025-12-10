import React from 'react';
import { Heart } from 'lucide-react';

const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="mt-auto py-6 border-t border-gray-200 text-center bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 flex flex-col items-center justify-center space-y-3">
        {/* Copyright Section - Wraps nicely on mobile */}
        <p className="text-sm text-gray-500 flex flex-wrap justify-center items-center gap-1 text-center leading-relaxed">
          <span>© {currentYear} Satja Savings Ban Sai Yai Group.</span>
          <span className="flex items-center gap-1 whitespace-nowrap">
            Made with <Heart className="w-3 h-3 text-red-400 fill-current" /> by IT Team.
          </span>
        </p>

        {/* Links Section - Flex wrap with conditional separators */}
        <div className="flex flex-wrap justify-center items-center gap-x-4 gap-y-2 text-xs text-gray-400">
          <a href="#" className="hover:text-emerald-600 transition">Privacy Policy</a>
          <span className="hidden sm:inline text-gray-300">•</span>
          <a href="#" className="hover:text-emerald-600 transition">Terms of Service</a>
          <span className="hidden sm:inline text-gray-300">•</span>
          <a href="#" className="hover:text-emerald-600 transition">Support</a>
          <span className="hidden sm:inline text-gray-300">•</span>
          <span className="bg-gray-100 px-2 py-0.5 rounded">v1.0.3</span>
        </div>
      </div>
    </footer>
  );
};

export default Footer;