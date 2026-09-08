import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { ArrowLeft, Calendar, User, Clock, HeartPulse, CheckCircle2 } from 'lucide-react';

const HealthcareScreen = () => {
  const { elderlyMode } = useAuth();
  const navigate = useNavigate();
  
  const [doctors] = useState([
    { id: 'doc-1', name: 'Dr. Alok Mishra', spec: 'Cardiologist', fee: 500, availability: 'Mon, Wed, Fri' },
    { id: 'doc-2', name: 'Dr. Suchitra Dash', spec: 'Paediatrician', fee: 400, availability: 'Tue, Thu, Sat' },
    { id: 'doc-3', name: 'Dr. Priyabrata Sen', spec: 'General Physician', fee: 300, availability: 'Daily' }
  ]);

  const [selectedDocId, setSelectedDocId] = useState(doctors[0].id);
  const [appointmentDate, setAppointmentDate] = useState('');
  const [timeSlot, setTimeSlot] = useState('10:00 AM');
  const [symptoms, setSymptoms] = useState('');
  const [appointments, setAppointments] = useState([]);
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem('ssl_appointments');
    if (saved) {
      setAppointments(JSON.parse(saved));
    }
  }, []);

  const handleBook = (e) => {
    e.preventDefault();
    if (!appointmentDate || !symptoms) return;

    const doc = doctors.find(d => d.id === selectedDocId);
    const newAppointment = {
      id: 'apt-' + Date.now(),
      doctorName: doc.name,
      specialization: doc.spec,
      date: appointmentDate,
      time: timeSlot,
      symptoms: symptoms,
      fee: doc.fee
    };

    const updated = [newAppointment, ...appointments];
    setAppointments(updated);
    localStorage.setItem('ssl_appointments', JSON.stringify(updated));
    
    // Reset Form
    setSymptoms('');
    setAppointmentDate('');
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
            Healthcare Scheduling
          </h1>
          <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
            Telehealth & Clinic Directory
          </p>
        </div>
      </header>

      {/* Main Panel */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-stretch">
        
        {/* Left: Booking Form */}
        <div className="lg:col-span-6 flex flex-col gap-6">
          <div className="p-6 rounded-3xl glass-card border border-white/5 text-left">
            <h2 className={`font-extrabold text-slate-100 mb-4 ${elderlyMode ? 'text-2xl' : 'text-base'}`}>
              Book a Consultation
            </h2>

            {showSuccess && (
              <div className="mb-6 p-4 rounded-xl bg-brand-emerald/15 border border-brand-emerald/30 text-brand-emerald text-sm font-semibold flex items-center gap-2">
                <CheckCircle2 className="w-5 h-5" />
                Appointment booked successfully!
              </div>
            )}

            <form onSubmit={handleBook} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Select Doctor
                </label>
                <select
                  value={selectedDocId}
                  onChange={(e) => setSelectedDocId(e.target.value)}
                  className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-semibold focus:outline-none"
                >
                  {doctors.map(d => (
                    <option key={d.id} value={d.id}>
                      {d.name} ({d.spec}) — ₹{d.fee}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Consultation Date
                  </label>
                  <input
                    type="date"
                    required
                    value={appointmentDate}
                    onChange={(e) => setAppointmentDate(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-semibold focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Time Slot
                  </label>
                  <select
                    value={timeSlot}
                    onChange={(e) => setTimeSlot(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-semibold focus:outline-none"
                  >
                    <option value="10:00 AM">10:00 AM</option>
                    <option value="11:30 AM">11:30 AM</option>
                    <option value="02:00 PM">02:00 PM</option>
                    <option value="04:30 PM">04:30 PM</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Symptoms description
                </label>
                <textarea
                  required
                  rows="3"
                  placeholder="Describe your sickness or symptoms details..."
                  value={symptoms}
                  onChange={(e) => setSymptoms(e.target.value)}
                  className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-white/5 text-slate-200 text-sm font-medium focus:outline-none"
                />
              </div>

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-brand-emerald text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(16,185,129,0.25)] transition-all cursor-pointer"
              >
                CONFIRM BOOKING
              </button>
            </form>
          </div>
        </div>

        {/* Right: Booked Appointments */}
        <div className="lg:col-span-6 flex flex-col gap-6">
          <div className="p-6 rounded-3xl glass-card border border-white/5 text-left flex-1 flex flex-col">
            <h2 className={`font-extrabold text-slate-100 border-b border-white/5 pb-3 ${elderlyMode ? 'text-2xl' : 'text-base'}`}>
              Your Booked Consultations
            </h2>

            <div className="flex-1 overflow-y-auto max-h-[380px] pr-2 mt-4 space-y-4">
              {appointments.length === 0 ? (
                <div className="text-center py-12 text-slate-500">
                  <HeartPulse className="w-12 h-12 mx-auto mb-2 opacity-30" />
                  <p className="text-sm font-bold">No active bookings found</p>
                  <p className="text-xs">Select a doctor and confirm a date above to book.</p>
                </div>
              ) : (
                appointments.map(apt => (
                  <div key={apt.id} className="p-4 rounded-2xl bg-white/[0.02] border border-white/5 space-y-3">
                    <div className="flex items-center justify-between border-b border-white/5 pb-2">
                      <div>
                        <p className="text-sm font-bold text-slate-200">{apt.doctorName}</p>
                        <p className="text-[10px] text-brand-emerald font-semibold uppercase tracking-wider">{apt.specialization}</p>
                      </div>
                      <span className="text-xs font-bold text-brand-blue">
                        ₹{apt.fee} Paid
                      </span>
                    </div>
                    <div className="grid grid-cols-2 gap-2 text-xs text-slate-400">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-slate-500" />
                        <span>{apt.date}</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Clock className="w-3.5 h-3.5 text-slate-500" />
                        <span>{apt.time}</span>
                      </div>
                    </div>
                    <p className="text-xs text-slate-500 bg-slate-900/60 p-2.5 rounded-lg border border-white/5">
                      <span className="font-bold text-slate-400">Symptoms:</span> {apt.symptoms}
                    </p>
                  </div>
                ))
              )}
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

export default HealthcareScreen;
