import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { ArrowLeft, MapPin, Compass, User, Calendar, CheckCircle2 } from 'lucide-react';

const TourismScreen = () => {
  const { elderlyMode } = useAuth();
  const navigate = useNavigate();

  const [places] = useState([
    { id: 'plc-1', name: 'Lingaraj Temple', desc: '11th-century temple dedicated to Lord Shiva, classic Kalinga architecture.', category: 'TEMPLE' },
    { id: 'plc-2', name: 'Konark Sun Temple', desc: '13th-century Sun Temple, UNESCO World Heritage Site in chariot shape.', category: 'HISTORIC' },
    { id: 'plc-3', name: 'Udayagiri & Khandagiri', desc: 'Ancient rock-cut caves carved during the reign of King Kharavela.', category: 'HISTORIC' }
  ]);

  const [guides] = useState([
    { id: 'gd-1', name: 'Manoj Das', rate: 250, languages: ['Odia', 'English', 'Hindi'], rating: 4.9 },
    { id: 'gd-2', name: 'Priyaranjan Sahu', rate: 300, languages: ['Odia', 'English', 'Bengali'], rating: 4.8 },
    { id: 'gd-3', name: 'Smita Mohanty', rate: 280, languages: ['Odia', 'English', 'Sanskrit'], rating: 5.0 }
  ]);

  const [selectedPlaceId, setSelectedPlaceId] = useState(places[0].id);
  const [selectedGuideId, setSelectedGuideId] = useState(guides[0].id);
  const [bookingDate, setBookingDate] = useState('');
  const [durationHours, setDurationHours] = useState(2);
  const [bookings, setBookings] = useState([]);
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem('ssl_tourism_bookings');
    if (saved) {
      setBookings(JSON.parse(saved));
    }
  }, []);

  const handleBook = (e) => {
    e.preventDefault();
    if (!bookingDate) return;

    const place = places.find(p => p.id === selectedPlaceId);
    const guide = guides.find(g => g.id === selectedGuideId);
    const totalCost = guide.rate * durationHours;

    const newBooking = {
      id: 'tbk-' + Date.now(),
      placeName: place.name,
      guideName: guide.name,
      date: bookingDate,
      duration: durationHours,
      totalCost: totalCost
    };

    const updated = [newBooking, ...bookings];
    setBookings(updated);
    localStorage.setItem('ssl_tourism_bookings', JSON.stringify(updated));

    setBookingDate('');
    setShowSuccess(true);
    setTimeout(() => setShowSuccess(false), 3000);
  };

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
            Odisha Tourism
          </h1>
          <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
            Historic Sights & Certified Tour Guides
          </p>
        </div>
      </header>

      {/* Main Grid */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-stretch text-left">
        
        {/* Left Column: Sights & Booking */}
        <div className="lg:col-span-7 space-y-6 flex flex-col">
          {/* Sights selector */}
          <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-3">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">Odisha Sights</h3>
            <div className="grid gap-3">
              {places.map(p => (
                <button
                  key={p.id}
                  onClick={() => setSelectedPlaceId(p.id)}
                  className={`p-4 rounded-2xl text-left border flex items-start gap-4 transition-all cursor-pointer ${
                    selectedPlaceId === p.id
                      ? 'bg-brand-blue/15 border-brand-blue text-slate-100'
                      : 'bg-white/[0.01] border-white/5 text-slate-400 hover:bg-white/[0.03]'
                  }`}
                >
                  <MapPin className="w-5 h-5 text-brand-blue mt-0.5" />
                  <div>
                    <p className="text-sm font-bold text-slate-200">{p.name}</p>
                    <p className="text-xs text-slate-500 mt-1">{p.desc}</p>
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Guide Booking Form */}
          <div className="p-6 rounded-3xl glass-card border border-white/5 flex-1">
            <h3 className="text-sm font-extrabold text-slate-100 mb-4">Book Tour Guide</h3>

            {showSuccess && (
              <div className="mb-6 p-4 rounded-xl bg-brand-emerald/15 border border-brand-emerald/30 text-brand-emerald text-xs font-bold flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4" />
                Tour guide allocated successfully!
              </div>
            )}

            <form onSubmit={handleBook} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Select Guide
                  </label>
                  <select
                    value={selectedGuideId}
                    onChange={(e) => setSelectedGuideId(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-semibold focus:outline-none"
                  >
                    {guides.map(g => (
                      <option key={g.id} value={g.id}>
                        {g.name} (₹{g.rate}/hr)
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Visit Date
                  </label>
                  <input
                    type="date"
                    required
                    value={bookingDate}
                    onChange={(e) => setBookingDate(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-semibold focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Duration (hours)
                </label>
                <select
                  value={durationHours}
                  onChange={(e) => setDurationHours(Number(e.target.value))}
                  className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-semibold focus:outline-none"
                >
                  <option value={2}>2 Hours</option>
                  <option value={4}>4 Hours</option>
                  <option value={6}>6 Hours (Half Day)</option>
                  <option value={8}>8 Hours (Full Day)</option>
                </select>
              </div>

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-brand-blue text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(59,130,246,0.2)] transition-all cursor-pointer"
              >
                BOOK TOUR GUIDE
              </button>
            </form>
          </div>
        </div>

        {/* Right Column: Bookings Logs list */}
        <div className="lg:col-span-5">
          <div className="p-6 rounded-3xl glass-card border border-white/5 h-full flex flex-col justify-between min-h-[350px]">
            <div>
              <div className="flex items-center gap-2 border-b border-white/5 pb-3">
                <Compass className="w-5 h-5 text-brand-emerald" />
                <h3 className="font-black text-slate-100 text-sm">Your Guide Bookings</h3>
              </div>

              <div className="overflow-y-auto max-h-[350px] pr-1 mt-4 space-y-4">
                {bookings.length === 0 ? (
                  <div className="text-center py-12 text-slate-500">
                    <User className="w-10 h-10 mx-auto mb-2 opacity-30" />
                    <p className="text-xs font-bold">No active tour guide bookings</p>
                    <p className="text-[10px]">Select a sight and configure booking details.</p>
                  </div>
                ) : (
                  bookings.map(bk => (
                    <div key={bk.id} className="p-4 rounded-2xl bg-white/[0.02] border border-white/5 space-y-3">
                      <div className="flex items-start justify-between border-b border-white/5 pb-2">
                        <div>
                          <p className="text-sm font-bold text-slate-200">{bk.placeName}</p>
                          <p className="text-[10px] text-brand-blue font-semibold uppercase tracking-wider">Guide: {bk.guideName}</p>
                        </div>
                        <span className="text-xs font-bold text-brand-emerald">
                          ₹{bk.totalCost} Paid
                        </span>
                      </div>
                      <div className="grid grid-cols-2 gap-2 text-[10px] text-slate-400">
                        <div className="flex items-center gap-1">
                          <Calendar className="w-3.5 h-3.5 text-slate-500" />
                          <span>{bk.date}</span>
                        </div>
                        <p className="text-right">{bk.duration} hours allocated</p>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
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

export default TourismScreen;
