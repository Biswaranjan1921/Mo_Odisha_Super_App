import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { ArrowLeft, Calendar, Calculator, CheckCircle2, Building } from 'lucide-react';

const EventsScreen = () => {
  const { elderlyMode } = useAuth();
  const navigate = useNavigate();

  const [venues] = useState([
    { id: 'vn-1', name: 'Mayfair Convention Center', capacity: 1000, pricePerDay: 75000, address: 'Jayadev Vihar, Bhubaneswar' },
    { id: 'vn-2', name: 'Exhibition Ground Hall A', capacity: 2000, pricePerDay: 50000, address: 'Unit-3, Bhubaneswar' },
    { id: 'vn-3', name: 'Patia Kalyan Mandap', capacity: 500, pricePerDay: 30000, address: 'Patia, Bhubaneswar' }
  ]);

  const [selectedVenueId, setSelectedVenueId] = useState(venues[0].id);
  const [guestsCount, setGuestsCount] = useState(200);
  const [cateringPackage, setCateringPackage] = useState(400); // Cost per guest: 0, 400 (Standard), 850 (Premium)
  const [bookingDate, setBookingDate] = useState('');
  const [bookings, setBookings] = useState([]);
  const [showSuccess, setShowSuccess] = useState(false);

  const activeVenue = venues.find(v => v.id === selectedVenueId);

  useEffect(() => {
    const saved = localStorage.getItem('ssl_event_bookings');
    if (saved) {
      setBookings(JSON.parse(saved));
    }
  }, []);

  const calculateTotal = () => {
    if (!activeVenue) return 0;
    const cateringTotal = cateringPackage * guestsCount;
    return activeVenue.pricePerDay + cateringTotal;
  };

  const handleBook = (e) => {
    e.preventDefault();
    if (!bookingDate || guestsCount <= 0) return;

    const total = calculateTotal();
    const newBooking = {
      id: 'evt-' + Date.now(),
      venueName: activeVenue.name,
      date: bookingDate,
      guests: guestsCount,
      catering: cateringPackage === 0 ? 'None' : cateringPackage === 400 ? 'Standard' : 'Premium',
      totalCost: total
    };

    const updated = [newBooking, ...bookings];
    setBookings(updated);
    localStorage.setItem('ssl_event_bookings', JSON.stringify(updated));

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
            Event venue booking
          </h1>
          <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
            Marriage Halls, Convention Centers & Catering Planners
          </p>
        </div>
      </header>

      {/* Main Grid */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-stretch text-left">
        
        {/* Left Column: Venues & Calculator */}
        <div className="lg:col-span-7 space-y-6 flex flex-col">
          {/* Venues Selector */}
          <div className="p-5 rounded-3xl glass-card border border-white/5 space-y-3">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">Select Venue</h3>
            <div className="grid gap-3">
              {venues.map(v => (
                <button
                  key={v.id}
                  onClick={() => setSelectedVenueId(v.id)}
                  className={`p-4 rounded-2xl text-left border flex items-start gap-4 transition-all cursor-pointer ${
                    selectedVenueId === v.id
                      ? 'bg-brand-blue/15 border-brand-blue text-slate-100'
                      : 'bg-white/[0.01] border-white/5 text-slate-400 hover:bg-white/[0.03]'
                  }`}
                >
                  <Building className="w-5 h-5 text-brand-blue mt-0.5" />
                  <div className="flex-1">
                    <div className="flex justify-between items-start">
                      <p className="text-sm font-bold text-slate-200">{v.name}</p>
                      <span className="text-xs font-bold text-brand-emerald">₹{v.pricePerDay.toLocaleString()}/day</span>
                    </div>
                    <p className="text-xs text-slate-500 mt-1">Capacity: up to {v.capacity} guests | {v.address}</p>
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Calculator and Form */}
          <div className="p-6 rounded-3xl glass-card border border-white/5 flex-1">
            <h3 className="text-sm font-extrabold text-slate-100 mb-4">Budget Estimation & Booking</h3>

            {showSuccess && (
              <div className="mb-6 p-4 rounded-xl bg-brand-emerald/15 border border-brand-emerald/30 text-brand-emerald text-xs font-bold flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4" />
                Venue booked and deposit processed!
              </div>
            )}

            <form onSubmit={handleBook} className="space-y-4">
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Estimated Guests
                  </label>
                  <input
                    type="number"
                    required
                    min="1"
                    max={activeVenue?.capacity}
                    value={guestsCount}
                    onChange={(e) => setGuestsCount(Math.max(1, Number(e.target.value)))}
                    className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-semibold focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Catering Plan
                  </label>
                  <select
                    value={cateringPackage}
                    onChange={(e) => setCateringPackage(Number(e.target.value))}
                    className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-semibold focus:outline-none"
                  >
                    <option value={0}>None</option>
                    <option value={400}>Standard (₹400/guest)</option>
                    <option value={850}>Premium (₹850/guest)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Event Date
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

              {/* Instant calculation total */}
              <div className="p-4 rounded-2xl bg-white/[0.01] border border-white/5 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Calculator className="w-5 h-5 text-brand-blue" />
                  <div>
                    <p className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Estimated Cost</p>
                    <p className="text-xs text-slate-400">Venue + Catering package</p>
                  </div>
                </div>
                <span className="text-lg font-black text-brand-emerald">₹{calculateTotal().toLocaleString()}</span>
              </div>

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-brand-blue text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(59,130,246,0.2)] transition-all cursor-pointer"
              >
                BOOK EVENT VENUE
              </button>
            </form>
          </div>
        </div>

        {/* Right Column: Reservation Logs list */}
        <div className="lg:col-span-5">
          <div className="p-6 rounded-3xl glass-card border border-white/5 h-full flex flex-col justify-between min-h-[350px]">
            <div>
              <div className="flex items-center gap-2 border-b border-white/5 pb-3">
                <Calendar className="w-5 h-5 text-brand-emerald" />
                <h3 className="font-black text-slate-100 text-sm">Your Event Bookings</h3>
              </div>

              <div className="overflow-y-auto max-h-[350px] pr-1 mt-4 space-y-4">
                {bookings.length === 0 ? (
                  <div className="text-center py-12 text-slate-500">
                    <Building className="w-10 h-10 mx-auto mb-2 opacity-30" />
                    <p className="text-xs font-bold">No active venue bookings</p>
                    <p className="text-[10px]">Select a venue and estimate pricing above.</p>
                  </div>
                ) : (
                  bookings.map(bk => (
                    <div key={bk.id} className="p-4 rounded-2xl bg-white/[0.02] border border-white/5 space-y-3">
                      <div className="flex items-start justify-between border-b border-white/5 pb-2">
                        <div>
                          <p className="text-sm font-bold text-slate-200">{bk.venueName}</p>
                          <p className="text-[10px] text-brand-blue font-semibold uppercase tracking-wider">Catering: {bk.catering}</p>
                        </div>
                        <span className="text-xs font-bold text-brand-emerald">
                          ₹{bk.totalCost.toLocaleString()}
                        </span>
                      </div>
                      <div className="grid grid-cols-2 gap-2 text-[10px] text-slate-400">
                        <div className="flex items-center gap-1">
                          <Calendar className="w-3.5 h-3.5 text-slate-500" />
                          <span>{bk.date}</span>
                        </div>
                        <p className="text-right">{bk.guests} Guests estimated</p>
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

export default EventsScreen;
