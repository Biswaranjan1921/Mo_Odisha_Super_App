import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import apiClient from '../../core/api/apiClient';
import { 
  ArrowLeft, Calendar, Building2, Users, DollarSign, 
  Volume2, CheckCircle2, AlertCircle, Clock, X, Send, Sparkles 
} from 'lucide-react';

const EventsScreen = () => {
  const { elderlyMode, speak } = useAuth();
  const navigate = useNavigate();

  const [venues, setVenues] = useState([]);
  const [myBookings, setMyBookings] = useState([]);
  const [selectedVenue, setSelectedVenue] = useState(null);

  const [eventDate, setEventDate] = useState('');
  const [vendorRequirements, setVendorRequirements] = useState('');

  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    fetchVenues();
    fetchMyBookings();
  }, []);

  const fetchVenues = async () => {
    try {
      const res = await apiClient.get('/events/venues');
      setVenues(res.data || []);
    } catch (err) {
      console.warn('Could not fetch event venues:', err);
    }
  };

  const fetchMyBookings = async () => {
    try {
      const res = await apiClient.get('/events/bookings/me');
      setMyBookings(res.data || []);
    } catch (err) {
      console.warn('Could not fetch event bookings:', err);
    }
  };

  const handleBookVenue = async (e) => {
    e.preventDefault();
    if (!selectedVenue || !eventDate) return;
    setLoading(true);
    setErrorMsg('');
    try {
      await apiClient.post('/events/bookings', {
        venueId: selectedVenue.id,
        eventDate,
        vendorRequirements
      });
      setSelectedVenue(null);
      setEventDate('');
      setVendorRequirements('');
      setSuccessMsg('Event venue reserved successfully!');
      fetchMyBookings();
      speak('Event venue reserved successfully.', 'ଇଭେଣ୍ଟ ସ୍ଥାନ ସଫଳତାର ସହିତ ବୁକ୍ ହୋଇଛି।');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Venue date unavailable or already booked');
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
              Smart Event Venue Reservation
            </h1>
            <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
              State Public Exhibition Halls & Cultural Grounds Engine
            </p>
          </div>
        </div>

        <button 
          onClick={() => speak('Smart Event Venue Reservation Service. Browse public event centers, check date availability, and reserve grounds.', 'ସ୍ମାର୍ଟ ଇଭେଣ୍ଟ ସ୍ଥାନ ବୁକିଂ ସେବା।')}
          className="p-2.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue cursor-pointer"
        >
          <Volume2 className="w-5 h-5" />
        </button>
      </header>

      {/* Alert Messages */}
      {successMsg && (
        <div className="max-w-7xl mx-auto w-full px-6 mt-4">
          <div className="p-4 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-xs font-bold flex items-center justify-between">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4" />
              {successMsg}
            </div>
            <button onClick={() => setSuccessMsg('')}><X className="w-4 h-4" /></button>
          </div>
        </div>
      )}

      {errorMsg && (
        <div className="max-w-7xl mx-auto w-full px-6 mt-4">
          <div className="p-4 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-bold flex items-center justify-between">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-4 h-4" />
              {errorMsg}
            </div>
            <button onClick={() => setErrorMsg('')}><X className="w-4 h-4" /></button>
          </div>
        </div>
      )}

      {/* Main Content */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-start text-left">
        
        {/* Left: Public Venues Catalogue */}
        <div className="lg:col-span-7 space-y-4">
          <h2 className="font-extrabold text-slate-200 text-sm uppercase tracking-wider">
            Available State Public Venues
          </h2>

          <div className="grid md:grid-cols-2 gap-6">
            {venues.map((venue) => (
              <div key={venue.id} className="p-6 rounded-3xl glass-card border border-white/5 space-y-4 flex flex-col justify-between">
                <div>
                  <div className="flex items-start justify-between">
                    <div className="p-3 rounded-2xl bg-brand-emerald/10 text-brand-emerald border border-brand-emerald/20">
                      <Building2 className="w-6 h-6" />
                    </div>
                    <span className="font-black text-brand-amber text-sm">₹{venue.pricePerDay} / day</span>
                  </div>

                  <h3 className="font-bold text-slate-100 text-base mt-3">{venue.name}</h3>
                  <p className="text-xs text-slate-400">{venue.address}</p>
                </div>

                <div className="space-y-2 text-xs text-slate-400 border-t border-white/5 pt-3">
                  <div className="flex items-center justify-between">
                    <span>Capacity:</span>
                    <span className="font-bold text-slate-200">{venue.capacity} People</span>
                  </div>
                  {venue.amenities && (
                    <div className="flex items-center justify-between">
                      <span>Amenities:</span>
                      <span className="font-semibold text-brand-blue">{venue.amenities}</span>
                    </div>
                  )}
                </div>

                <button
                  onClick={() => setSelectedVenue(venue)}
                  className="w-full py-3 rounded-xl bg-brand-emerald text-slate-950 font-black text-xs uppercase tracking-wider hover:opacity-90 transition-all cursor-pointer flex items-center justify-center gap-2"
                >
                  <Calendar className="w-4 h-4" />
                  RESERVE VENUE
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Right: My Event Reservations */}
        <div className="lg:col-span-5 space-y-4">
          <div className="p-6 rounded-3xl glass-card border border-white/5 space-y-4">
            <h3 className="font-extrabold text-slate-200 text-sm border-b border-white/5 pb-3">
              My Reserved Event Dates
            </h3>

            {myBookings.length === 0 ? (
              <p className="text-xs text-slate-500 italic">No venue reservations made yet.</p>
            ) : (
              <div className="space-y-3 max-h-[400px] overflow-y-auto pr-1">
                {myBookings.map((b) => (
                  <div key={b.id} className="p-4 rounded-2xl bg-white/[0.02] border border-white/5 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-slate-200 text-xs">{b.venueName}</span>
                      <span className="px-2.5 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                        {b.status}
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-xs text-slate-400 border-t border-white/5 pt-2">
                      <span>Date: {b.eventDate}</span>
                      <span className="font-black text-brand-amber">Total: ₹{b.totalBudget}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

      </main>

      {/* Booking Modal */}
      {selectedVenue && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <div className="max-w-md w-full p-6 rounded-3xl glass-card border border-white/10 space-y-4">
            <div className="flex items-center justify-between border-b border-white/5 pb-3">
              <h3 className="font-bold text-slate-100 text-sm">Reserve {selectedVenue.name}</h3>
              <button onClick={() => setSelectedVenue(null)}><X className="w-4 h-4 text-slate-400" /></button>
            </div>

            <form onSubmit={handleBookVenue} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Event Date</label>
                <input
                  type="date"
                  required
                  value={eventDate}
                  onChange={(e) => setEventDate(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-emerald"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Vendor & Setup Requirements</label>
                <textarea
                  rows="3"
                  placeholder="e.g. Stage lighting, 500 chairs, acoustic sound setup..."
                  value={vendorRequirements}
                  onChange={(e) => setVendorRequirements(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-emerald"
                />
              </div>

              <div className="p-3 bg-white/[0.02] rounded-xl border border-white/5 flex justify-between text-xs">
                <span>Price per Day:</span>
                <span className="font-black text-brand-amber">₹{selectedVenue.pricePerDay}</span>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 rounded-xl bg-brand-emerald text-slate-950 font-black text-xs uppercase tracking-wider hover:opacity-90 cursor-pointer flex items-center justify-center gap-2"
              >
                <Send className="w-4 h-4" />
                CONFIRM RESERVATION
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="px-6 py-4 border-t border-white/5 text-center text-xs text-slate-600 bg-slate-950/20">
        © 2026 Odisha Smart City Infrastructure Commission. All rights reserved.
      </footer>
    </div>
  );
};

export default EventsScreen;
