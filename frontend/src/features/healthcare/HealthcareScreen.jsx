import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import apiClient from '../../core/api/apiClient';
import { ArrowLeft, Calendar, User, Clock, HeartPulse, CheckCircle2, AlertCircle } from 'lucide-react';

const FALLBACK_DOCTORS = [
  { id: 'doc-1', name: 'Dr. Alok Mishra', specialization: 'Cardiologist', consultationFee: 500, availabilitySchedule: 'Mon, Wed, Fri (10:00 AM - 02:00 PM)' },
  { id: 'doc-2', name: 'Dr. Suchitra Dash', specialization: 'Paediatrician', consultationFee: 400, availabilitySchedule: 'Tue, Thu, Sat (10:00 AM - 01:00 PM)' },
  { id: 'doc-3', name: 'Dr. Priyabrata Sen', specialization: 'General Physician', consultationFee: 300, availabilitySchedule: 'Daily (09:00 AM - 01:00 PM)' }
];

const HealthcareScreen = () => {
  const { elderlyMode } = useAuth();
  const navigate = useNavigate();

  const [doctors, setDoctors] = useState(FALLBACK_DOCTORS);
  const [selectedDocId, setSelectedDocId] = useState('');
  const [appointmentDate, setAppointmentDate] = useState('');
  const [timeSlot, setTimeSlot] = useState('10:00');
  const [symptoms, setSymptoms] = useState('');
  const [appointments, setAppointments] = useState([]);
  const [showSuccess, setShowSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchDoctors();
    fetchMyAppointments();
  }, []);

  const fetchDoctors = async () => {
    try {
      const res = await apiClient.get('/healthcare/doctors?page=0&size=20');
      if (res.data?.content && res.data.content.length > 0) {
        setDoctors(res.data.content);
        setSelectedDocId(res.data.content[0].id);
      } else if (doctors.length > 0) {
        setSelectedDocId(doctors[0].id);
      }
    } catch (err) {
      console.warn('Using local fallback doctor directory:', err);
      if (doctors.length > 0) {
        setSelectedDocId(doctors[0].id);
      }
    }
  };

  const fetchMyAppointments = async () => {
    try {
      const res = await apiClient.get('/healthcare/appointments/me?page=0&size=20');
      if (res.data?.content) {
        setAppointments(res.data.content);
      }
    } catch (err) {
      const saved = localStorage.getItem('ssl_appointments');
      if (saved) {
        setAppointments(JSON.parse(saved));
      }
    }
  };

  const handleBook = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    if (!selectedDocId || !appointmentDate || !symptoms) {
      setErrorMessage('Please select a doctor, date, and provide symptoms description.');
      return;
    }

    setLoading(true);
    const appointmentTimeIso = `${appointmentDate}T${timeSlot}:00`;

    try {
      const res = await apiClient.post('/healthcare/appointments', {
        doctorId: selectedDocId,
        appointmentTime: appointmentTimeIso,
        symptomsDescription: symptoms
      });

      if (res.data) {
        setAppointments([res.data, ...appointments]);
      }

      setSymptoms('');
      setShowSuccess(true);
      setTimeout(() => setShowSuccess(false), 4000);
    } catch (err) {
      const apiErr = err.response?.data?.message || err.response?.data?.error || 'Appointment booking failed. Slot may be occupied.';
      setErrorMessage(apiErr);

      // Local fallback simulation if offline
      if (!err.response) {
        const doc = doctors.find(d => d.id === selectedDocId) || doctors[0];
        const localApt = {
          id: 'apt-' + Date.now(),
          doctorName: doc.name,
          doctorSpecialization: doc.specialization,
          appointmentTime: appointmentTimeIso,
          symptomsDescription: symptoms,
          consultationFee: doc.consultationFee,
          status: 'BOOKED'
        };
        const updated = [localApt, ...appointments];
        setAppointments(updated);
        localStorage.setItem('ssl_appointments', JSON.stringify(updated));
        setSymptoms('');
        setShowSuccess(true);
        setTimeout(() => setShowSuccess(false), 4000);
      }
    } finally {
      setLoading(false);
    }
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

            {errorMessage && (
              <div className="mb-6 p-4 rounded-xl bg-red-500/15 border border-red-500/30 text-red-400 text-sm font-semibold flex items-center gap-2">
                <AlertCircle className="w-5 h-5" />
                {errorMessage}
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
                      {d.name} ({d.specialization}) — ₹{d.consultationFee}
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
                    <option value="10:00">10:00 AM</option>
                    <option value="10:30">10:30 AM</option>
                    <option value="11:00">11:00 AM</option>
                    <option value="11:30">11:30 AM</option>
                    <option value="12:00">12:00 PM</option>
                    <option value="12:30">12:30 PM</option>
                    <option value="14:00">02:00 PM</option>
                    <option value="14:30">02:30 PM</option>
                    <option value="15:00">03:00 PM</option>
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
                disabled={loading}
                className="w-full py-3.5 rounded-xl bg-brand-emerald text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(16,185,129,0.25)] transition-all cursor-pointer disabled:opacity-50"
              >
                {loading ? 'BOOKING APPOINTMENT...' : 'CONFIRM BOOKING'}
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
                        <p className="text-sm font-bold text-slate-200">{apt.doctorName || 'Dr. Assigned'}</p>
                        <p className="text-[10px] text-brand-emerald font-semibold uppercase tracking-wider">{apt.doctorSpecialization || 'Specialist'}</p>
                      </div>
                      <div className="text-right">
                        <span className="text-xs font-bold text-brand-blue block">
                          ₹{apt.consultationFee || apt.fee || '0.00'}
                        </span>
                        <span className={`text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-md ${
                          apt.status === 'COMPLETED' ? 'bg-emerald-500/20 text-emerald-400' :
                          apt.status === 'CONFIRMED' ? 'bg-blue-500/20 text-blue-400' :
                          apt.status === 'CANCELLED' ? 'bg-red-500/20 text-red-400' :
                          'bg-amber-500/20 text-amber-400'
                        }`}>
                          {apt.status || 'BOOKED'}
                        </span>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-2 text-xs text-slate-400">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-slate-500" />
                        <span>{apt.appointmentTime ? apt.appointmentTime.split('T')[0] : apt.date}</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Clock className="w-3.5 h-3.5 text-slate-500" />
                        <span>{apt.appointmentTime ? apt.appointmentTime.split('T')[1]?.substring(0, 5) : apt.time}</span>
                      </div>
                    </div>
                    <p className="text-xs text-slate-500 bg-slate-900/60 p-2.5 rounded-lg border border-white/5">
                      <span className="font-bold text-slate-400">Symptoms:</span> {apt.symptomsDescription || apt.symptoms}
                    </p>
                    {apt.prescriptionNotes && (
                      <div className="p-2.5 rounded-lg bg-emerald-500/10 border border-emerald-500/20 text-xs text-emerald-300">
                        <span className="font-bold block text-emerald-400 mb-1">Rx Digital Prescription:</span>
                        <p>{apt.prescriptionNotes}</p>
                      </div>
                    )}
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
