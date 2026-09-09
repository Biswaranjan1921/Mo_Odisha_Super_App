import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import apiClient from '../../core/api/apiClient';
import { 
  ArrowLeft, Bus, Navigation, MapPin, Clock, DollarSign, 
  Volume2, Search, ArrowRight, ShieldCheck, RefreshCw 
} from 'lucide-react';

const TransportScreen = () => {
  const { elderlyMode, speak } = useAuth();
  const navigate = useNavigate();

  const [vehicleFilter, setVehicleFilter] = useState('MO_BUS'); // MO_BUS | METRO | E_RICKSHAW | AUTO
  const [startPoint, setStartPoint] = useState('');
  const [endPoint, setEndPoint] = useState('');
  const [routes, setRoutes] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchRoutes();
  }, [vehicleFilter]);

  const fetchRoutes = async () => {
    setLoading(true);
    try {
      const url = vehicleFilter ? `/transport/routes?vehicleType=${vehicleFilter}` : '/transport/routes';
      const res = await apiClient.get(url);
      setRoutes(res.data || []);
    } catch (err) {
      console.warn('Could not fetch transit routes:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchRoutes = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await apiClient.get(`/transport/routes/search?startPoint=${startPoint}&endPoint=${endPoint}`);
      setRoutes(res.data || []);
      speak(
        `Found ${res.data.length} matching transit routes`,
        `${res.data.length} ଯାତାୟାତ ରୁଟ୍ ମିଳିଲା।`
      );
    } catch (err) {
      console.warn('Route search failed:', err);
    } finally {
      setLoading(false);
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
              Mo Bus & Public Transit
            </h1>
            <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
              Capital Region Urban Transport (CRUT) Route & Fare Navigation
            </p>
          </div>
        </div>

        <button 
          onClick={() => speak('Mo Bus and Public Transit Service. Search bus routes, metro lines, and electric rickshaw frequencies.', 'ମୋ ବସ୍ ଏବଂ ସାଧାରଣ ଯାତାୟାତ ସେବା।')}
          className="p-2.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue cursor-pointer"
        >
          <Volume2 className="w-5 h-5" />
        </button>
      </header>

      {/* Main Content */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-start text-left">
        
        {/* Left: Search & Filter */}
        <div className="lg:col-span-5 flex flex-col gap-6">
          <div className="p-6 rounded-3xl glass-card border border-white/5 space-y-4">
            <h2 className="font-extrabold text-slate-200 text-sm border-b border-white/5 pb-3">
              Route & Fare Finder
            </h2>

            <div className="grid grid-cols-2 gap-2">
              {['MO_BUS', 'METRO', 'E_RICKSHAW', 'AUTO'].map((type) => (
                <button
                  key={type}
                  onClick={() => setVehicleFilter(type)}
                  className={`py-2 px-3 rounded-xl border text-xs font-bold flex items-center justify-center gap-1.5 cursor-pointer ${
                    vehicleFilter === type
                      ? 'bg-brand-emerald/20 border-brand-emerald text-brand-emerald'
                      : 'bg-white/5 border-white/10 text-slate-400'
                  }`}
                >
                  <Bus className="w-3.5 h-3.5" />
                  {type.replace('_', ' ')}
                </button>
              ))}
            </div>

            <form onSubmit={handleSearchRoutes} className="space-y-4 pt-2">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                  Start Stop / Station
                </label>
                <input
                  type="text"
                  placeholder="e.g. Master Canteen, Kalpana Square"
                  value={startPoint}
                  onChange={(e) => setStartPoint(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-emerald"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                  Destination Stop / Station
                </label>
                <input
                  type="text"
                  placeholder="e.g. Cuttack Badambadi, KIIT Campus"
                  value={endPoint}
                  onChange={(e) => setEndPoint(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-emerald"
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 rounded-xl bg-brand-emerald text-slate-950 font-black text-xs uppercase tracking-wider hover:opacity-90 transition-all cursor-pointer flex items-center justify-center gap-2"
              >
                <Search className="w-4 h-4" />
                FIND ACTIVE ROUTES
              </button>
            </form>
          </div>
        </div>

        {/* Right: Active Routes List */}
        <div className="lg:col-span-7 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-extrabold text-slate-200 text-sm uppercase tracking-wider">
              {vehicleFilter.replace('_', ' ')} Active Schedules ({routes.length})
            </h3>
            <button onClick={fetchRoutes} className="p-2 rounded-xl bg-white/5 border border-white/10 text-slate-400 hover:text-slate-200">
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            </button>
          </div>

          <div className="space-y-4">
            {routes.length === 0 ? (
              <div className="p-8 rounded-3xl glass-card border border-white/5 text-center text-xs text-slate-500 italic">
                No active routes found for search criteria.
              </div>
            ) : (
              routes.map((route) => (
                <div key={route.id} className="p-6 rounded-3xl glass-card border border-white/5 space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="px-3 py-1 rounded-xl bg-brand-emerald/15 text-brand-emerald font-black text-xs border border-brand-emerald/30">
                        ROUTE #{route.routeNumber}
                      </span>
                      <span className="text-xs font-bold text-slate-300">{route.vehicleType.replace('_', ' ')}</span>
                    </div>
                    <span className="font-black text-brand-amber text-sm">₹{route.farePrice}</span>
                  </div>

                  <div className="flex items-center gap-3 text-xs text-slate-300 py-2 border-y border-white/5">
                    <div className="flex items-center gap-1.5">
                      <MapPin className="w-3.5 h-3.5 text-brand-blue" />
                      <span>{route.startPoint}</span>
                    </div>
                    <ArrowRight className="w-3.5 h-3.5 text-slate-600 shrink-0" />
                    <div className="flex items-center gap-1.5">
                      <MapPin className="w-3.5 h-3.5 text-brand-emerald" />
                      <span>{route.endPoint}</span>
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-[11px] text-slate-400 pt-1">
                    <span className="flex items-center gap-1">
                      <Clock className="w-3.5 h-3.5 text-brand-blue" />
                      Frequency: Every {route.frequencyMinutes} mins
                    </span>
                    <span className="text-emerald-400 font-bold">OPERATIONAL</span>
                  </div>
                </div>
              ))
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

export default TransportScreen;
