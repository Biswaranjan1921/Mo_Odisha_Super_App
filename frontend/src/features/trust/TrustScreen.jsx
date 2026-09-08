import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { ArrowLeft, Award, ShieldCheck, CheckCircle2, Volume2, AlertTriangle, FileText } from 'lucide-react';

const TrustScreen = () => {
  const { elderlyMode, speak } = useAuth();
  const navigate = useNavigate();

  const [score, setScore] = useState(98.50);
  const [complaintsCount, setComplaintsCount] = useState(0);
  const [complaintText, setComplaintText] = useState('');
  const [complaints, setComplaints] = useState([]);
  const [showSuccess, setShowSuccess] = useState(false);

  const handleRegisterComplaint = (e) => {
    e.preventDefault();
    if (!complaintText) return;

    const newComplaint = {
      id: 'cmp-' + Date.now(),
      details: complaintText,
      status: 'UNDER_REVIEW',
      date: new Date().toLocaleDateString()
    };

    setComplaints([newComplaint, ...complaints]);
    setComplaintsCount(prev => prev + 1);
    setScore(prev => Math.max(0, prev - 1.5)); // Complaints lower score slightly
    setComplaintText('');
    setShowSuccess(true);
    setTimeout(() => setShowSuccess(false), 3000);

    speak('Complaint registered successfully. Your trust score has been adjusted.', 'ଅଭିଯୋଗ ସଫଳତାର ସହିତ ପଞ୍ଜୀକୃତ ହେଲା। ଆପଣଙ୍କର ବିଶ୍ୱାସ ସ୍କୋର ବଦଳାଯାଇଛି।');
  };

  return (
    <div className="min-h-screen flex flex-col justify-between">
      {/* Header */}
      <header className="px-6 py-4 flex items-center justify-between border-b border-white/5 bg-slate-950/75 backdrop-blur-xl">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/dashboard')}
            className="p-2 rounded-xl bg-white/5 border border-white/10 text-slate-300 cursor-pointer"
          >
            <ArrowLeft className={elderlyMode ? 'w-6 h-6' : 'w-4 h-4'} />
          </button>
          <div>
            <h1 className={`font-black text-slate-100 ${elderlyMode ? 'text-2xl' : 'text-lg'}`}>
              Citizen Trust Score
            </h1>
            <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
              Verification Badges & Accountability Logs
            </p>
          </div>
        </div>

        <button 
          onClick={() => speak('Citizen Trust Score Panel. Here you can check your verification status, view your gold badge level, and file complaints.', 'ନାଗରିକ ବିଶ୍ୱାସ ସ୍କୋର ପ୍ୟାନେଲ୍। ଏଠାରେ ଆପଣ ଯାଞ୍ଚ ସ୍ଥିତି, ସୁନା ବ୍ୟାଜ୍ ସ୍ତର ଏବଂ ଅଭିଯୋଗ ଦାଖଲ କରିପାରିବେ।')}
          className="p-2.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue cursor-pointer animate-pulse-slow"
        >
          <Volume2 className="w-5 h-5" />
        </button>
      </header>

      {/* Main Grid */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-stretch text-left">
        
        {/* Left Column: Trust Stats */}
        <div className="lg:col-span-5 flex flex-col gap-6">
          <div className="p-8 rounded-3xl glass-card border border-white/5 text-center flex flex-col items-center justify-center space-y-4">
            <div className="p-4 bg-brand-emerald/10 border border-brand-emerald/25 text-brand-emerald rounded-full">
              <Award className="w-12 h-12" />
            </div>
            
            <div>
              <p className="text-xs text-slate-400 font-bold uppercase tracking-wider">Your Trust Rating</p>
              <h2 className="text-4xl font-black text-slate-100 mt-1">{score.toFixed(2)}%</h2>
            </div>

            <div className="px-4 py-2 bg-brand-emerald/15 border border-brand-emerald/30 text-brand-emerald text-xs font-bold rounded-2xl flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4" />
              GOLD MEMBER BADGE
            </div>

            <div className="grid grid-cols-2 gap-4 w-full pt-4 border-t border-white/5 text-xs text-slate-400">
              <div className="p-3 bg-white/[0.01] rounded-2xl border border-white/5 text-center">
                <p className="font-bold text-slate-300">Complaints</p>
                <p className="text-lg font-black text-brand-amber mt-1">{complaintsCount}</p>
              </div>
              <div className="p-3 bg-white/[0.01] rounded-2xl border border-white/5 text-center">
                <p className="font-bold text-slate-300">Verified Orders</p>
                <p className="text-lg font-black text-brand-blue mt-1">24</p>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Register Complaint */}
        <div className="lg:col-span-7 flex flex-col gap-6">
          <div className="p-6 rounded-3xl glass-card border border-white/5 flex-1 flex flex-col">
            <h3 className="font-extrabold text-slate-200 border-b border-white/5 pb-3 text-sm">
              File Citizen Complaint / Feedback
            </h3>

            {showSuccess && (
              <div className="mt-4 p-4 rounded-xl bg-brand-emerald/15 border border-brand-emerald/30 text-brand-emerald text-xs font-bold flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4" />
                Incident ticket filed successfully!
              </div>
            )}

            <form onSubmit={handleRegisterComplaint} className="mt-4 space-y-4 flex-1 flex flex-col justify-between">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Complaint Details
                </label>
                <textarea
                  required
                  rows="4"
                  placeholder="State the problem clearly (e.g. Shop owner overcharging for milk, or delay in bus arrival)..."
                  value={complaintText}
                  onChange={(e) => setComplaintText(e.target.value)}
                  className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-medium focus:outline-none"
                />
              </div>

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-brand-amber text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(245,158,11,0.2)] transition-all cursor-pointer"
              >
                SUBMIT ACCUMULATED FEEDBACK
              </button>
            </form>
          </div>

          {/* Active Incident List */}
          {complaints.length > 0 && (
            <div className="p-6 rounded-3xl glass-card border border-white/5 max-h-[220px] overflow-y-auto">
              <h4 className="font-bold text-slate-300 text-xs mb-3">Submitted Tickets</h4>
              <div className="space-y-2">
                {complaints.map(cmp => (
                  <div key={cmp.id} className="p-3 rounded-xl bg-white/[0.01] border border-white/5 flex justify-between items-start gap-4 text-xs">
                    <div className="flex items-start gap-2">
                      <FileText className="w-4 h-4 text-slate-500 mt-0.5 shrink-0" />
                      <p className="text-slate-400 font-medium">{cmp.details}</p>
                    </div>
                    <span className="text-[9px] font-bold bg-brand-amber/10 text-brand-amber border border-brand-amber/20 px-2 py-0.5 rounded uppercase tracking-wider shrink-0">
                      REVIEWING
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

      </main>

      {/* Footer */}
      <footer className="px-6 py-4 border-t border-white/5 text-center text-xs text-slate-600 bg-slate-950/20">
        © 2026 Odisha Smart City Infrastructure Commission. All rights reserved.
      </footer>
    </div>
  );
};

export default TrustScreen;
