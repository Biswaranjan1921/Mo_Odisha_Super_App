import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { 
  ArrowLeft, AlertTriangle, ShieldCheck, MapPin, 
  PhoneCall, ShieldAlert, Truck, X, Volume2
} from 'lucide-react';

const EmergencyScreen = () => {
  const { elderlyMode, speak } = useAuth();
  const navigate = useNavigate();
  const [sosActive, setSosActive] = useState(false);
  const [sosProgress, setSosProgress] = useState(0); // 0: Idle, 1: Dispatched, 2: In-Route
  const [secondsElapsed, setSecondsElapsed] = useState(0);
  
  const latitude = 20.2961;
  const longitude = 85.8245;
  
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markerInstanceRef = useRef(null);

  useEffect(() => {
    if (!window.L || !mapContainerRef.current) return;

    if (!mapInstanceRef.current) {
      mapInstanceRef.current = window.L.map(mapContainerRef.current, {
        zoomControl: !elderlyMode
      }).setView([latitude, longitude], 14);

      window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
      }).addTo(mapInstanceRef.current);

      const customIcon = window.L.divIcon({
        className: 'custom-div-icon',
        html: `<div class="w-6 h-6 rounded-full bg-brand-blue/30 border-2 border-brand-blue flex items-center justify-center animate-pulse-slow">
                 <div class="w-3.5 h-3.5 rounded-full bg-brand-blue"></div>
               </div>`,
        iconSize: [24, 24],
        iconAnchor: [12, 12]
      });

      markerInstanceRef.current = window.L.marker([latitude, longitude], { icon: customIcon })
        .addTo(mapInstanceRef.current)
        .bindPopup('Your Current Verified Location')
        .openPopup();
    }

    return () => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, [elderlyMode]);

  useEffect(() => {
    let interval = null;
    if (sosActive) {
      interval = setInterval(() => {
        setSecondsElapsed((prev) => {
          const nextSec = prev + 1;
          if (nextSec === 5) {
            setSosProgress(1);
            speak('Emergency center response. Operator Patnaik is monitoring your location.', 'ଜରୁରୀକାଳୀନ କେନ୍ଦ୍ର ଉତ୍ତର। ଅପରେଟର ପଟ୍ଟନାୟକ ଆପଣଙ୍କ ସ୍ଥାନ ଉପରେ ନଜର ରଖିଛନ୍ତି।');
          } else if (nextSec === 12) {
            setSosProgress(2);
            speak('Ambulance dispatched from Capital Hospital. Arrival in 5 minutes.', 'କ୍ୟାପିଟାଲ୍ ହସ୍ପିଟାଲ୍‌ରୁ ଆମ୍ବୁଲାନ୍ସ ପଠାଯାଇଛି। ୫ ମିନିଟ୍ ମଧ୍ୟରେ ପହଞ୍ଚିବ।');
          }
          return nextSec;
        });
      }, 1000);
    } else {
      setSecondsElapsed(0);
      setSosProgress(0);
    }
    return () => clearInterval(interval);
  }, [sosActive]);

  const handlePanicTrigger = () => {
    if (!sosActive) {
      setSosActive(true);
      speak('SOS Activated. Transmitting GPS coordinates. Help is on the way.', 'SOS ସକ୍ରିୟ ହୋଇଛି। ଜିପିଏସ ସ୍ଥାନ ପଠାଯାଉଛି। ସାହାଯ୍ୟ ଆସୁଛି।');
    } else {
      handleCancelSOS();
    }
  };

  const handleCancelSOS = () => {
    setSosActive(false);
    setSosProgress(0);
    setSecondsElapsed(0);
    speak('Emergency broadcast cancelled.', 'ଜରୁରୀକାଳୀନ ପ୍ରସାରଣ ବାତିଲ ହେଲା।');
  };

  return (
    <div className="min-h-screen flex flex-col justify-between">
      {/* Header bar */}
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
              SOS Emergency Panel 🚨
            </h1>
            <p className="text-[9px] text-brand-crimson font-semibold uppercase tracking-wider">
              Emergency Response Subsystem
            </p>
          </div>
        </div>

        <button 
          onClick={() => speak('Emergency SOS Panel. Tap the large center button to trigger an emergency alert. Tap the blue telephone button at the bottom to call direct.', 'ଜରୁରୀକାଳୀନ SOS ପ୍ୟାନେଲ୍। ଜରୁରୀକାଳୀନ ଆଲର୍ଟ ପାଇଁ ମଝିରେ ଥିବା ବଡ଼ ବଟନକୁ ଚିପନ୍ତୁ। ସିଧାସଳଖ କଲ୍ କରିବା ପାଇଁ ତଳେ ଥିବା ନୀଳ ଟେଲିଫୋନ୍ ବଟନକୁ ଚିପନ୍ତୁ।')}
          className="p-2.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue cursor-pointer animate-pulse-slow"
        >
          <Volume2 className="w-5 h-5" />
        </button>
      </header>

      {/* Main console content */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-stretch">
        
        {/* Left Column: Panic trigger */}
        <div className="lg:col-span-5 flex flex-col gap-6">
          
          <div className={`p-8 rounded-3xl text-center glass-card border border-white/5 flex flex-col items-center justify-center relative overflow-hidden ${
            sosActive ? 'bg-brand-crimson/5 border-brand-crimson/20' : ''
          }`}>
            
            {sosActive && (
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div className="w-56 h-56 rounded-full border border-brand-crimson/20 animate-ping absolute"></div>
                <div className="w-80 h-80 rounded-full border border-brand-crimson/10 animate-pulse absolute"></div>
              </div>
            )}

            <div className="mb-6 relative z-10">
              <h3 className={`font-black text-slate-100 ${elderlyMode ? 'text-3xl font-black' : 'text-xl'}`}>
                {sosActive ? '🚨 SOS TRANSMITTING 🚨' : '🔴 PANIC SWITCH 🔴'}
              </h3>
              <p className={`text-slate-400 mt-1.5 ${elderlyMode ? 'text-lg font-medium' : 'text-xs'}`}>
                {sosActive 
                  ? 'Help is on the way. Tracking...' 
                  : 'Tap the big button below for immediate rescue assistance'
                }
              </p>
            </div>

            {/* Panic Button */}
            <button
              onClick={handlePanicTrigger}
              className={`w-48 h-48 rounded-full border-8 flex flex-col items-center justify-center cursor-pointer transition-all duration-300 relative z-10 select-none ${
                sosActive 
                  ? 'bg-brand-crimson border-brand-crimson/35 shadow-[0_0_40px_rgba(239,68,68,0.5)] scale-95 text-glow-crimson'
                  : 'bg-brand-crimson border-brand-crimson/25 hover:opacity-95 shadow-[0_0_30px_rgba(239,68,68,0.3)] animate-pulse'
              }`}
            >
              <AlertTriangle className="w-14 h-14 text-slate-950" />
              <span className="text-slate-950 font-black tracking-wider text-sm mt-1">
                {sosActive ? 'TAP TO CANCEL' : 'PRESS HELP'}
              </span>
            </button>

            {sosActive && (
              <div className="mt-8 w-full space-y-4 relative z-10">
                <button
                  onClick={handleCancelSOS}
                  className="w-full py-3.5 rounded-xl bg-slate-900 text-brand-crimson border border-brand-crimson/30 font-black text-xs hover:bg-slate-800 transition-all flex items-center justify-center gap-1.5 cursor-pointer"
                >
                  <X className="w-4 h-4" />
                  CANCEL SOS BROADCAST
                </button>
              </div>
            )}
          </div>

          {/* Dispatch updates status log */}
          <div className="p-6 rounded-3xl glass-card border border-white/5 flex-1 flex flex-col justify-between text-left">
            <h4 className={`font-bold text-slate-200 border-b border-white/5 pb-3 ${elderlyMode ? 'text-2xl font-black' : 'text-sm'}`}>
              Incident Dispatcher Stream 🚑
            </h4>

            <div className="flex-1 py-4 flex flex-col justify-center space-y-4">
              {sosActive ? (
                <div className="space-y-4">
                  <div className="flex items-start gap-3.5">
                    <div className="p-1 rounded-full bg-brand-emerald/15 border border-brand-emerald/30 text-brand-emerald mt-1">
                      <ShieldCheck className="w-4 h-4" />
                    </div>
                    <div>
                      <p className={`font-bold text-slate-100 ${elderlyMode ? 'text-xl' : 'text-sm'}`}>
                        GPS Coordinates Dispatched 🗺️
                      </p>
                      <p className={`text-slate-400 ${elderlyMode ? 'text-lg' : 'text-xs'}`}>
                        Sent lat: {latitude}, lng: {longitude} at {secondsElapsed}s
                      </p>
                    </div>
                  </div>

                  {sosProgress >= 1 && (
                    <div className="flex items-start gap-3.5 transition-all">
                      <div className="p-1 rounded-full bg-brand-emerald/15 border border-brand-emerald/30 text-brand-emerald mt-1">
                        <ShieldCheck className="w-4 h-4" />
                      </div>
                      <div>
                        <p className={`font-bold text-slate-100 ${elderlyMode ? 'text-xl' : 'text-sm'}`}>
                          State Command Center Response 🏛️
                        </p>
                        <p className={`text-slate-400 ${elderlyMode ? 'text-lg' : 'text-xs'}`}>
                          Operator matched: Comm Officer Patnaik
                        </p>
                      </div>
                    </div>
                  )}

                  {sosProgress >= 2 && (
                    <div className="flex items-start gap-3.5 transition-all">
                      <div className="p-1 rounded-full bg-brand-crimson/15 border border-brand-crimson/30 text-brand-crimson mt-1 animate-pulse">
                        <Truck className="w-4 h-4 animate-bounce" />
                      </div>
                      <div>
                        <p className={`font-bold text-slate-100 ${elderlyMode ? 'text-xl animate-pulse text-brand-crimson' : 'text-sm'}`}>
                          Ambulance in Transit 🚑
                        </p>
                        <p className={`text-slate-400 ${elderlyMode ? 'text-lg' : 'text-xs'}`}>
                          Capital Hospital Unit #2B (2.8 km away). ETA 5 mins
                        </p>
                      </div>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-6">
                  <ShieldCheck className="w-10 h-10 text-brand-emerald mx-auto mb-2 opacity-60" />
                  <p className={`font-bold text-slate-300 ${elderlyMode ? 'text-xl' : 'text-sm'}`}>
                    All Systems Ready
                  </p>
                  <p className={`text-slate-500 mt-1 ${elderlyMode ? 'text-lg' : 'text-xs'}`}>
                    Panic trigger will instantly alert local response vehicles
                  </p>
                </div>
              )}
            </div>

            <div className="border-t border-white/5 pt-4">
              <a
                href="tel:108"
                className="w-full py-3.5 rounded-2xl bg-brand-crimson/15 hover:bg-brand-crimson/25 text-brand-crimson border border-brand-crimson/25 hover:border-brand-crimson font-black text-sm transition-all flex items-center justify-center gap-2 cursor-pointer"
              >
                <PhoneCall className="w-4 h-4 animate-bounce" />
                CALL 108 AMBULANCE DIRECT 📞
              </a>
            </div>
          </div>
        </div>

        {/* Right Column: Map */}
        <div className="lg:col-span-7 rounded-3xl glass-card border border-white/5 overflow-hidden flex flex-col relative min-h-[400px]">
          <div className="absolute top-4 left-4 z-10 px-4 py-2 bg-slate-950/80 backdrop-blur-md rounded-2xl border border-white/10 flex items-center gap-2.5 shadow-xl text-left">
            <div className="w-3 h-3 rounded-full bg-brand-emerald animate-pulse"></div>
            <div>
              <p className="text-[9px] text-slate-500 font-bold uppercase tracking-wider">GPS Target Lock</p>
              <p className="text-xs font-semibold text-slate-200">{latitude.toFixed(4)}° N, {longitude.toFixed(4)}° E</p>
            </div>
          </div>

          <div ref={mapContainerRef} className="flex-1 w-full h-full z-0"></div>
        </div>

      </main>

      <footer className="px-6 py-6 border-t border-white/5 text-center text-xs text-slate-600 bg-slate-950/20">
        © 2026 Odisha Smart City Infrastructure Commission. All rights reserved.
      </footer>
    </div>
  );
};

export default EmergencyScreen;
