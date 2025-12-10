
import React, { useState } from 'react';
import { X, ZoomIn, ZoomOut, RotateCw, Download, FileText } from 'lucide-react';
import { CollateralDocument } from '../types';

interface DocumentViewerProps {
  isOpen: boolean;
  onClose: () => void;
  document: CollateralDocument | null;
}

const DocumentViewer: React.FC<DocumentViewerProps> = ({ isOpen, onClose, document }) => {
  const [scale, setScale] = useState(1);
  const [rotation, setRotation] = useState(0);

  if (!isOpen || !document) return null;

  const handleZoomIn = () => setScale(prev => Math.min(prev + 0.25, 3));
  const handleZoomOut = () => setScale(prev => Math.max(prev - 0.25, 0.5));
  const handleRotate = () => setRotation(prev => prev + 90);

  // Reset view when closing
  const handleClose = () => {
    setScale(1);
    setRotation(0);
    onClose();
  };

  const isImage = document.fileName.match(/\.(jpg|jpeg|png|gif|webp)$/i);

  return (
    <div className="fixed inset-0 z-[60] bg-black/90 backdrop-blur-md flex flex-col animate-in fade-in duration-200">
      {/* Toolbar */}
      <div className="flex items-center justify-between p-4 bg-black/50 text-white border-b border-white/10">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-white/10 rounded-lg">
             <FileText className="w-5 h-5" />
          </div>
          <div>
             <h3 className="font-bold text-sm">{document.type}</h3>
             <p className="text-xs text-gray-400">{document.description} ({document.fileName})</p>
          </div>
        </div>
        
        <div className="flex items-center gap-4">
           {isImage && (
             <div className="flex items-center gap-2 bg-white/10 rounded-full px-2 py-1">
                <button onClick={handleZoomOut} className="p-2 hover:bg-white/20 rounded-full transition"><ZoomOut className="w-4 h-4" /></button>
                <span className="text-xs font-mono w-12 text-center">{(scale * 100).toFixed(0)}%</span>
                <button onClick={handleZoomIn} className="p-2 hover:bg-white/20 rounded-full transition"><ZoomIn className="w-4 h-4" /></button>
                <div className="w-px h-4 bg-white/20 mx-1"></div>
                <button onClick={handleRotate} className="p-2 hover:bg-white/20 rounded-full transition"><RotateCw className="w-4 h-4" /></button>
             </div>
           )}
           
           <a 
             href={document.url} 
             download={document.fileName}
             className="p-2 hover:bg-white/20 rounded-full transition text-emerald-400"
             title="Download Original"
           >
             <Download className="w-5 h-5" />
           </a>

           <button 
             onClick={handleClose}
             className="p-2 bg-white/10 hover:bg-red-500/20 hover:text-red-400 rounded-full transition ml-2"
           >
             <X className="w-6 h-6" />
           </button>
        </div>
      </div>

      {/* Viewing Area */}
      <div className="flex-1 overflow-hidden flex items-center justify-center p-8 relative">
          {isImage ? (
             <div className="transition-transform duration-200 ease-out" style={{ transform: `scale(${scale}) rotate(${rotation}deg)` }}>
                <img 
                  src={document.url} 
                  alt={document.description} 
                  className="max-h-[80vh] max-w-[90vw] object-contain shadow-2xl rounded-sm ring-1 ring-white/10"
                />
             </div>
          ) : (
            <div className="bg-white rounded-xl p-12 text-center max-w-lg">
                <FileText className="w-24 h-24 text-gray-300 mx-auto mb-4" />
                <h3 className="text-xl font-bold text-gray-800 mb-2">Preview Not Available</h3>
                <p className="text-gray-500 mb-6">This file type cannot be previewed directly. Please download it to view.</p>
                <a 
                  href={document.url} 
                  download={document.fileName}
                  className="inline-flex items-center gap-2 bg-emerald-600 text-white px-6 py-3 rounded-xl font-bold hover:bg-emerald-700 transition"
                >
                  <Download className="w-5 h-5" /> Download File
                </a>
            </div>
          )}
      </div>
    </div>
  );
};

export default DocumentViewer;
