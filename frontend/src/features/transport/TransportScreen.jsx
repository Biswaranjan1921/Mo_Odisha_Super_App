import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { ArrowLeft, Bus, Clock, MapPin, Navigation } from 'lucide-react';

const TransportScreen = () => {
  const { elderlyMode } = useAuth();
  const navigate = useNavigate();

  const [routes] = useState([
    { 
      id: 'rt-10a', 
      number: 'Route 10A', 
      type: 'BUS',
      start: 'Master Canteen', 
      end: 'Patia Chowk', 
      stops: ['Master Canteen', 'Vani Vihar', 'Acharya Vihar', 'Jayadev Vihar', 'Patia Chowk'],
      coords: [
        [20.2644, 85.8402], // Master Canteen
        [20.2917, 85.8456], // Vani Vihar
        [20.2952, 85.8344], // Acharya Vihar
        [20.3088, 85.8312], // Jayadev Vihar
        [20.3532, 85.8190]  // Patia Chowk
      ]
    },
    { 
      id: 'rt-22', 
      number: 'Route 22', 
      type: 'BUS',
      start: 'Biju Patnaik Airport', 
      end: 'Kiit Square', 
      stops: ['Airport', 'Capital Hospital', 'Rajmahal Chowk', 'Acharya Vihar', 'Kiit Square'],
      coords: [
        [20.2526, 85.8178], // Airport
        [20.2678, 85.8242], // Hospital
        [20.2694, 85.8384], // Rajmahal
        [20.2952, 85.8344], // Acharya Vihar
        [20.3538, 85.8175]  // Kiit Square
      ]
    }
  ]);

  const [selectedRouteId, setSelectedRouteId] = useState(routes[0].id);
  const activeRoute = routes.find(r => r.id === selectedRouteId);
  const [busIdx, setBusIdx] = useState(2); // Bus is currently at stop index 2 (e.g. Acharya Vihar)

  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const pathInstanceRef = useRef(null);
  const markerInstanceRef = useRef(null);

  // Simulating bus moving along coords
  useEffect(() => {
    const interval = setInterval(() => {
      setBusIdx((prev) => {
        const next = prev + 1;
        return next < activeRoute.coords.length ? next : 0;
      });
    }, 4500);
    return () => clearInterval(interval);
  }, [activeRoute]);

  // Handle Leaflet Map Initialization & Path Rendering
  useEffect(() => {
    if (!window.L || !mapContainerRef.current) return;

    const busPos = activeRoute.coords[busIdx];

    if (!mapInstanceRef.current) {
      mapInstanceRef.current = window.L.map(mapContainerRef.current).setView(busPos, 13);

      window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
      }).addTo(mapInstanceRef.current);

      // Render polyline path connecting stops
      pathInstanceRef.current = window.L.polyline(activeRoute.coords, {
        color: '#3b82f6',
        weight: 5,
        opacity: 0.7
      }).addTo(mapInstanceRef.current);

      // Custom icon mapping for vehicle
      const vehicleIcon = window.L.divIcon({
        className: 'custom-bus-icon',
        html: `<div class="p-1.5 rounded-full bg-brand-amber border-2 border-slate-950 flex items-center justify-center shadow-lg animate-bounce">
                 <span class="text-xs">🚌</span>
               </div>`,
        iconSize: [28, 28],
        iconAnchor: [14, 14]
      });

      markerInstanceRef.current = window.L.marker(busPos, { icon: vehicleIcon })
        .addTo(mapInstanceRef.current)
        .bindPopup(`Bus ${activeRoute.number} is here`)
        .openPopup();
    } else {
      // Update existing map, route path line and vehicle marker
      mapInstanceRef.current.setView(busPos, 13);
      
      if (pathInstanceRef.current) {
        pathInstanceRef.current.setLatLngs(activeRoute.coords);
      }

      if (markerInstanceRef.current) {
        markerInstanceRef.current.setLatLng(busPos);
        markerInstanceRef.current.setPopupContent(`Bus ${activeRoute.number} is here`);
      }
    }
  }, [activeRoute, busIdx]);

  return (
    <div className="min-h-screen flex flex-col justify-between">
      {/* Header */}
      <header className="px-6 py-4 flex items-center gap-3 border-b border-white/5 bg-slate-950/75 backdrop-blur-xl">
        <button
          onClick={() => navigate('/dashboard')}
          className="p-2 rounded-xl bg-white/5 border border-white/10 text-slate-300 cursor-pointer"
        >
          <ArrowLeft className={elderlyMode ? 'w-6 h-6' : 'w-4 h-4'} />
        </button>
        <div>
          <h1 className={`font-black text-slate-100 ${elderlyMode ? 'text-2xl' : 'text-lg'}`}>
            Public Transport Monitor
          </h1>
          <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
            Live Bus Schedules & Routing Telemetry
          </p>
        </div>
      </header>

      {/* Main Grid */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-stretch text-left">
        
        {/* Left Column: Routes List & Stop Timeline */}
        <div className="lg:col-span-5 flex flex-col gap-6">
          {/* Active Routes list */}
          <div className="p-5 rounded-3xl glass-card border border-white/5">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">Select Active Route</h3>
            <div className="space-y-2">
              {routes.map(route => (
                <button
                  key={route.id}
                  onClick={() => {
                    setSelectedRouteId(route.id);
                    setBusIdx(1); // Reset index for new route
                  }}
                  className={`w-full p-4 rounded-2xl text-left border flex items-center justify-between cursor-pointer transition-all ${
                    selectedRouteId === route.id
                      ? 'bg-brand-blue/15 border-brand-blue text-slate-100 shadow-[0_0_15px_rgba(59,130,246,0.15)]'
                      : 'bg-white/[0.01] border-white/5 text-slate-400 hover:bg-white/[0.03]'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-brand-blue/10 rounded-xl border border-brand-blue/20">
                      <Bus className="w-5 h-5 text-brand-blue" />
                    </div>
                    <div>
                      <p className="text-sm font-bold text-slate-200">{route.number}</p>
                      <p className="text-[10px] text-slate-500 mt-0.5">{route.start} ➜ {route.end}</p>
                    </div>
                  </div>
                  <Clock className="w-4 h-4 text-slate-600" />
                </button>
              ))}
            </div>
          </div>

          {/* Timeline stops */}
          <div className="p-6 rounded-3xl glass-card border border-white/5 flex-1 flex flex-col">
            <h4 className="font-bold text-slate-200 border-b border-white/5 pb-3 text-sm">
              Route Stop Timeline
            </h4>
            
            <div className="flex-1 overflow-y-auto max-h-[300px] mt-4 pr-1 relative pl-6 space-y-5">
              {/* Vertical line indicator */}
              <div className="absolute left-2.5 top-2.5 bottom-2.5 w-0.5 bg-white/5"></div>

              {activeRoute.stops.map((stop, idx) => {
                const isBusHere = idx === busIdx;
                const isBusPassed = idx < busIdx;

                return (
                  <div key={idx} className="relative flex items-start gap-4">
                    {/* Circle Stop Node */}
                    <div className={`absolute -left-6 top-1.5 w-3.5 h-3.5 rounded-full border-2 transition-all ${
                      isBusHere 
                        ? 'bg-brand-amber border-slate-950 scale-125 ring-4 ring-brand-amber/35'
                        : isBusPassed
                          ? 'bg-brand-blue border-brand-blue'
                          : 'bg-slate-900 border-white/20'
                    }`}></div>

                    <div>
                      <p className={`text-sm font-bold ${isBusHere ? 'text-brand-amber' : 'text-slate-300'}`}>
                        {stop}
                      </p>
                      <p className="text-[10px] text-slate-500 mt-0.5">
                        {isBusHere 
                          ? '● Vehicle currently stopped here' 
                          : isBusPassed 
                            ? '✓ Passed' 
                            : 'Upcoming stop'
                        }
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Right Column: Telemetry map display */}
        <div className="lg:col-span-7 rounded-3xl glass-card border border-white/5 overflow-hidden flex flex-col relative min-h-[400px]">
          {/* Floating Telemetry Info Bar */}
          <div className="absolute top-4 left-4 right-4 z-10 p-3 bg-slate-950/80 backdrop-blur-md rounded-2xl border border-white/10 flex items-center justify-between shadow-xl">
            <div className="flex items-center gap-2">
              <Navigation className="w-4 h-4 text-brand-emerald animate-pulse" />
              <div>
                <p className="text-[9px] text-slate-500 font-bold uppercase tracking-wider">Live Position</p>
                <p className="text-xs font-semibold text-slate-200">
                  {activeRoute.stops[busIdx]}
                </p>
              </div>
            </div>
            <span className="px-2.5 py-1 bg-brand-emerald/10 border border-brand-emerald/25 text-brand-emerald text-[10px] font-bold rounded-lg tracking-wider">
              ONLINE
            </span>
          </div>

          <div ref={mapContainerRef} className="flex-1 w-full h-full z-0"></div>
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
