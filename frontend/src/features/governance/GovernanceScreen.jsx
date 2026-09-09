import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import apiClient from '../../core/api/apiClient';
import { 
  ArrowLeft, ShieldAlert, Building2, Users, ShoppingBag, DollarSign, 
  Stethoscope, Award, FileText, Search, RefreshCw, Volume2, Activity, MapPin 
} from 'lucide-react';

const GovernanceScreen = () => {
  const { elderlyMode, speak } = useAuth();
  const navigate = useNavigate();

  const [overview, setOverview] = useState(null);
  const [districts, setDistricts] = useState([]);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchGovernanceData();
  }, []);

  const fetchGovernanceData = async () => {
    setLoading(true);
    try {
      const [overviewRes, districtsRes] = await Promise.all([
        apiClient.get('/governance/analytics/overview'),
        apiClient.get('/governance/analytics/districts')
      ]);
      setOverview(overviewRes.data);
      setDistricts(districtsRes.data || []);
    } catch (err) {
      console.warn('Could not fetch governance data:', err);
    } finally {
      setLoading(false);
    }
  };

  const filteredDistricts = districts.filter(d => 
    d.districtName.toLowerCase().includes(searchKeyword.toLowerCase()) ||
    d.districtCode.toLowerCase().includes(searchKeyword.toLowerCase())
  );

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
              State Governance Command Center
            </h1>
            <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
              30-District Digital Telemetry & Administrative Dashboard
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button 
            onClick={fetchGovernanceData}
            className="p-2.5 rounded-xl bg-white/5 border border-white/10 text-slate-300 hover:bg-white/10 cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          <button 
            onClick={() => speak('State Governance Command Center. Live analytics across all 30 districts of Odisha.', 'ଶାସନ କମାଣ୍ଡ ସେଣ୍ଟର।')}
            className="p-2.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue cursor-pointer"
          >
            <Volume2 className="w-5 h-5" />
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 space-y-8 text-left">

        {/* State Overview KPI Cards */}
        {overview && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-2">
              <div className="flex items-center justify-between text-brand-blue">
                <Users className="w-5 h-5" />
                <span className="text-[10px] font-extrabold uppercase">Citizens</span>
              </div>
              <p className="text-2xl font-black text-slate-100">{overview.totalCitizens.toLocaleString()}</p>
              <p className="text-[10px] text-slate-400">Registered Citizen KYC Accounts</p>
            </div>

            <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-2">
              <div className="flex items-center justify-between text-brand-emerald">
                <Building2 className="w-5 h-5" />
                <span className="text-[10px] font-extrabold uppercase">Verified Stores</span>
              </div>
              <p className="text-2xl font-black text-slate-100">{overview.totalStores.toLocaleString()}</p>
              <p className="text-[10px] text-slate-400">Hyperlocal Commerce Outlets</p>
            </div>

            <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-2">
              <div className="flex items-center justify-between text-brand-amber">
                <DollarSign className="w-5 h-5" />
                <span className="text-[10px] font-extrabold uppercase">Paid Revenue</span>
              </div>
              <p className="text-2xl font-black text-brand-amber">₹{overview.totalCommerceRevenue.toLocaleString()}</p>
              <p className="text-[10px] text-slate-400">Paid/Delivered Order GMV</p>
            </div>

            <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-2">
              <div className="flex items-center justify-between text-rose-400">
                <ShieldAlert className="w-5 h-5" />
                <span className="text-[10px] font-extrabold uppercase">Active SOS</span>
              </div>
              <p className="text-2xl font-black text-rose-400">{overview.activeSosEmergencies}</p>
              <p className="text-[10px] text-slate-400">Active Emergency Dispatches</p>
            </div>

            <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-2">
              <div className="flex items-center justify-between text-cyan-400">
                <Stethoscope className="w-5 h-5" />
                <span className="text-[10px] font-extrabold uppercase">Telehealth</span>
              </div>
              <p className="text-2xl font-black text-slate-100">{overview.telehealthBookings.toLocaleString()}</p>
              <p className="text-[10px] text-slate-400">Doctor Telehealth Consults</p>
            </div>

            <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-2">
              <div className="flex items-center justify-between text-amber-400">
                <FileText className="w-5 h-5" />
                <span className="text-[10px] font-extrabold uppercase">Disputes</span>
              </div>
              <p className="text-2xl font-black text-amber-400">{overview.disputesUnderReview}</p>
              <p className="text-[10px] text-slate-400">Tickets Under Audit Review</p>
            </div>

            <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-2">
              <div className="flex items-center justify-between text-purple-400">
                <Award className="w-5 h-5" />
                <span className="text-[10px] font-extrabold uppercase">Tour Guides</span>
              </div>
              <p className="text-2xl font-black text-slate-100">{overview.verifiedTourGuides}</p>
              <p className="text-[10px] text-slate-400">Certified Tour Guides</p>
            </div>

            <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-2">
              <div className="flex items-center justify-between text-emerald-400">
                <Activity className="w-5 h-5" />
                <span className="text-[10px] font-extrabold uppercase">Districts</span>
              </div>
              <p className="text-2xl font-black text-emerald-400">30 / 30</p>
              <p className="text-[10px] text-slate-400">Full State Telemetry Coverage</p>
            </div>
          </div>
        )}

        {/* 30 District Performance Table */}
        <div className="p-6 rounded-3xl glass-card border border-white/5 space-y-4">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-white/5 pb-3">
            <div>
              <h2 className="font-extrabold text-slate-100 text-base">30 District Telemetry & Performance Breakdown</h2>
              <p className="text-xs text-slate-400">Live district metrics with outer-join guarantee across all 30 districts of Odisha</p>
            </div>

            <div className="relative w-full md:w-64">
              <Search className="w-3.5 h-3.5 text-slate-500 absolute left-3 top-3" />
              <input
                type="text"
                placeholder="Search district or code..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                className="w-full pl-9 pr-4 py-2 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-emerald"
              />
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-white/10 text-slate-400 font-bold uppercase tracking-wider text-[10px]">
                  <th className="py-3 px-3">Code</th>
                  <th className="py-3 px-3">District Name</th>
                  <th className="py-3 px-3 text-right">Citizens</th>
                  <th className="py-3 px-3 text-right">Stores</th>
                  <th className="py-3 px-3 text-right">Orders</th>
                  <th className="py-3 px-3 text-right">Revenue (₹)</th>
                  <th className="py-3 px-3 text-right">SOS Alerts</th>
                  <th className="py-3 px-3 text-right">Telehealth</th>
                  <th className="py-3 px-3 text-right">Disputes</th>
                  <th className="py-3 px-3 text-right">Guides</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5 text-slate-300 font-mono">
                {filteredDistricts.map((d) => (
                  <tr key={d.districtId} className="hover:bg-white/[0.02] transition-colors">
                    <td className="py-3 px-3 font-bold text-brand-emerald">{d.districtCode}</td>
                    <td className="py-3 px-3 font-sans font-bold text-slate-100">{d.districtName}</td>
                    <td className="py-3 px-3 text-right">{d.totalCitizens.toLocaleString()}</td>
                    <td className="py-3 px-3 text-right">{d.totalStores}</td>
                    <td className="py-3 px-3 text-right">{d.totalOrders}</td>
                    <td className="py-3 px-3 text-right text-brand-amber font-bold">₹{d.totalRevenue.toLocaleString()}</td>
                    <td className="py-3 px-3 text-right font-bold text-rose-400">{d.activeSosAlerts}</td>
                    <td className="py-3 px-3 text-right">{d.telehealthBookings}</td>
                    <td className="py-3 px-3 text-right">{d.disputesUnderReview}</td>
                    <td className="py-3 px-3 text-right">{d.verifiedGuides}</td>
                  </tr>
                ))}
              </tbody>
            </table>
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

export default GovernanceScreen;
