import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import apiClient from '../../core/api/apiClient';
import { 
  ArrowLeft, Award, ShieldCheck, CheckCircle2, Volume2, 
  AlertTriangle, FileText, Send, Building2, User, Stethoscope, RefreshCw 
} from 'lucide-react';

const TrustScreen = () => {
  const { elderlyMode, speak, user } = useAuth();
  const navigate = useNavigate();

  const [entityType, setEntityType] = useState('STORE');
  const [entityId, setEntityId] = useState('');
  const [subject, setSubject] = useState('');
  const [description, setDescription] = useState('');
  
  const [trustScore, setTrustScore] = useState(null);
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    fetchMyIncidents();
  }, []);

  const fetchMyIncidents = async () => {
    try {
      const res = await apiClient.get('/trust/incidents/me');
      if (res.data) {
        setIncidents(res.data);
      }
    } catch (err) {
      console.warn('Could not fetch incident tickets:', err);
    }
  };

  const handleFetchScore = async (e) => {
    if (e) e.preventDefault();
    if (!entityId) return;
    setLoading(true);
    setErrorMessage('');
    try {
      const res = await apiClient.get(`/trust/score/${entityType}/${entityId}`);
      setTrustScore(res.data);
      speak(
        `Trust score for entity is ${res.data.trustScore}% with badge ${res.data.badgeLevel}`,
        `ସଂସ୍ଥାର ବିଶ୍ୱାସ ସ୍କୋର ${res.data.trustScore}% ଏବଂ ବ୍ୟାଜ୍ ${res.data.badgeLevel}`
      );
    } catch (err) {
      setErrorMessage(err.response?.data?.message || 'Entity not found or score unavailable');
      setTrustScore(null);
    } finally {
      setLoading(false);
    }
  };

  const handleFileIncident = async (e) => {
    e.preventDefault();
    if (!entityId || !subject || !description) return;
    setLoading(true);
    setErrorMessage('');
    try {
      await apiClient.post('/trust/incidents', {
        targetEntityId: entityId,
        targetEntityType: entityType,
        subject,
        description
      });
      setShowSuccess(true);
      setSubject('');
      setDescription('');
      fetchMyIncidents();
      setTimeout(() => setShowSuccess(false), 4000);
      speak('Incident dispute ticket filed successfully.', 'ଆପଣଙ୍କର ଅଭିଯୋଗ ଟିକେଟ୍ ସଫଳତାର ସହିତ ଦାଖଲ ହୋଇଛି।');
    } catch (err) {
      setErrorMessage(err.response?.data?.message || 'Failed to file incident ticket');
    } finally {
      setLoading(false);
    }
  };

  const getBadgeColor = (badge) => {
    switch (badge) {
      case 'PLATINUM': return 'bg-cyan-500/20 text-cyan-300 border-cyan-500/40';
      case 'GOLD': return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
      case 'SILVER': return 'bg-slate-300/20 text-slate-200 border-slate-300/40';
      case 'BRONZE': return 'bg-amber-700/20 text-amber-500 border-amber-700/40';
      default: return 'bg-rose-500/20 text-rose-300 border-rose-500/40';
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'OPEN': return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30">OPEN</span>;
      case 'UNDER_REVIEW': return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-blue-500/20 text-blue-400 border border-blue-500/30">UNDER REVIEW</span>;
      case 'RESOLVED': return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">RESOLVED</span>;
      case 'REJECTED': return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/20 text-rose-400 border border-rose-500/30">REJECTED</span>;
      default: return null;
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between bg-slate-950 text-slate-100">
      {/* Header */}
      <header className="px-6 py-4 flex items-center justify-between border-b border-white/5 bg-slate-950/75 backdrop-blur-xl">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/dashboard')}
            className="p-2 rounded-xl bg-white/5 border border-white/10 text-slate-300 cursor-pointer hover:bg-white/10"
          >
            <ArrowLeft className={elderlyMode ? 'w-6 h-6' : 'w-4 h-4'} />
          </button>
          <div>
            <h1 className={`font-black text-slate-100 ${elderlyMode ? 'text-2xl' : 'text-lg'}`}>
              Trust Graph & Verification
            </h1>
            <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
              State Verification Tiers & Citizen Incident Management
            </p>
          </div>
        </div>

        <button 
          onClick={() => speak('Trust Graph & Verification Workstation. Check store, delivery partner, or doctor trust scores, and file incident tickets.', 'ବିଶ୍ୱାସ ଗ୍ରାଫ୍ ଏବଂ ଯାଞ୍ଚ ସେବା।')}
          className="p-2.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue cursor-pointer"
        >
          <Volume2 className="w-5 h-5" />
        </button>
      </header>

      {/* Main Content */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-start text-left">
        
        {/* Left Column: Entity Score Lookup */}
        <div className="lg:col-span-5 flex flex-col gap-6">
          <div className="p-6 rounded-3xl glass-card border border-white/5 space-y-4">
            <h2 className="font-extrabold text-slate-200 text-sm border-b border-white/5 pb-3">
              Lookup Entity Trust Score
            </h2>

            <form onSubmit={handleFetchScore} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Entity Category
                </label>
                <div className="grid grid-cols-3 gap-2">
                  <button
                    type="button"
                    onClick={() => setEntityType('STORE')}
                    className={`py-2 px-3 rounded-xl border text-xs font-bold flex items-center justify-center gap-1.5 cursor-pointer ${
                      entityType === 'STORE'
                        ? 'bg-brand-emerald/20 border-brand-emerald text-brand-emerald'
                        : 'bg-white/5 border-white/10 text-slate-400'
                    }`}
                  >
                    <Building2 className="w-3.5 h-3.5" />
                    Store
                  </button>
                  <button
                    type="button"
                    onClick={() => setEntityType('DELIVERY_PARTNER')}
                    className={`py-2 px-3 rounded-xl border text-xs font-bold flex items-center justify-center gap-1.5 cursor-pointer ${
                      entityType === 'DELIVERY_PARTNER'
                        ? 'bg-brand-emerald/20 border-brand-emerald text-brand-emerald'
                        : 'bg-white/5 border-white/10 text-slate-400'
                    }`}
                  >
                    <User className="w-3.5 h-3.5" />
                    Delivery
                  </button>
                  <button
                    type="button"
                    onClick={() => setEntityType('DOCTOR')}
                    className={`py-2 px-3 rounded-xl border text-xs font-bold flex items-center justify-center gap-1.5 cursor-pointer ${
                      entityType === 'DOCTOR'
                        ? 'bg-brand-emerald/20 border-brand-emerald text-brand-emerald'
                        : 'bg-white/5 border-white/10 text-slate-400'
                    }`}
                  >
                    <Stethoscope className="w-3.5 h-3.5" />
                    Doctor
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Entity UUID
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. 00000000-0000-0000-0000-000000000001"
                  value={entityId}
                  onChange={(e) => setEntityId(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs font-mono focus:outline-none focus:border-brand-emerald"
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 rounded-xl bg-brand-emerald text-slate-950 font-black text-xs uppercase tracking-wider hover:opacity-90 transition-all cursor-pointer flex items-center justify-center gap-2"
              >
                <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
                Fetch Trust Rating
              </button>
            </form>

            {trustScore && (
              <div className="mt-6 p-6 rounded-2xl bg-slate-900 border border-white/10 text-center space-y-4">
                <div className="p-3 bg-white/5 rounded-full inline-block">
                  <Award className="w-10 h-10 text-brand-emerald" />
                </div>
                <div>
                  <p className="text-xs text-slate-400 font-bold uppercase tracking-wider">Calculated Trust Score</p>
                  <h3 className="text-3xl font-black text-slate-100 mt-1">{trustScore.trustScore}%</h3>
                </div>
                <div className={`px-4 py-1.5 rounded-xl border text-xs font-bold inline-flex items-center gap-1.5 ${getBadgeColor(trustScore.badgeLevel)}`}>
                  <ShieldCheck className="w-4 h-4" />
                  {trustScore.badgeLevel} BADGE
                </div>
                <div className="grid grid-cols-2 gap-3 pt-3 border-t border-white/5 text-xs text-slate-400">
                  <div className="p-2 bg-white/[0.02] rounded-xl border border-white/5">
                    <p className="text-[10px] uppercase">Verified Transactions</p>
                    <p className="font-bold text-slate-200 text-sm">{trustScore.verifiedTransactionsCount}</p>
                  </div>
                  <div className="p-2 bg-white/[0.02] rounded-xl border border-white/5">
                    <p className="text-[10px] uppercase">Complaints Filed</p>
                    <p className="font-bold text-amber-400 text-sm">{trustScore.complaintsCount}</p>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: File Incident Ticket & Ticket Tracker */}
        <div className="lg:col-span-7 flex flex-col gap-6">
          <div className="p-6 rounded-3xl glass-card border border-white/5 space-y-4">
            <h3 className="font-extrabold text-slate-200 text-sm border-b border-white/5 pb-3">
              File Citizen Incident / Dispute Ticket
            </h3>

            {showSuccess && (
              <div className="p-4 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-xs font-bold flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4" />
                Incident ticket filed successfully!
              </div>
            )}

            {errorMessage && (
              <div className="p-4 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-bold flex items-center gap-2">
                <AlertTriangle className="w-4 h-4" />
                {errorMessage}
              </div>
            )}

            <form onSubmit={handleFileIncident} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                  Subject
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Counterfeit product delivered or doctor no-show"
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-blue"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                  Dispute Details & Evidence Summary
                </label>
                <textarea
                  required
                  rows="3"
                  placeholder="Provide explicit details regarding the merchant, doctor, or delivery partner incident..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-blue"
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 rounded-xl bg-brand-amber text-slate-950 font-black text-xs uppercase tracking-wider hover:opacity-95 shadow-[0_4px_20px_rgba(245,158,11,0.2)] transition-all cursor-pointer flex items-center justify-center gap-2"
              >
                <Send className="w-4 h-4" />
                SUBMIT DISPUTE TICKET
              </button>
            </form>
          </div>

          {/* Citizen Filed Incident Tickets */}
          <div className="p-6 rounded-3xl glass-card border border-white/5 space-y-4">
            <h4 className="font-bold text-slate-200 text-xs uppercase tracking-wider">
              My Filed Incident Tickets
            </h4>

            {incidents.length === 0 ? (
              <p className="text-xs text-slate-500 italic">No incident tickets filed yet.</p>
            ) : (
              <div className="space-y-3 max-h-[280px] overflow-y-auto pr-1">
                {incidents.map((t) => (
                  <div key={t.id} className="p-4 rounded-2xl bg-white/[0.02] border border-white/5 space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <FileText className="w-4 h-4 text-brand-blue" />
                        <span className="font-bold text-slate-200 text-xs">{t.subject}</span>
                      </div>
                      {getStatusBadge(t.status)}
                    </div>
                    <p className="text-xs text-slate-400">{t.description}</p>
                    <div className="flex items-center justify-between text-[10px] text-slate-500 border-t border-white/5 pt-2">
                      <span>Target: {t.targetEntityType}</span>
                      <span>Filed: {new Date(t.createdAt).toLocaleDateString()}</span>
                    </div>
                    {t.resolutionNotes && (
                      <div className="mt-2 p-2.5 rounded-xl bg-slate-900 border border-white/10 text-xs">
                        <p className="text-[10px] font-bold text-slate-400">Resolution Notes:</p>
                        <p className="text-slate-300 mt-0.5">{t.resolutionNotes}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
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
