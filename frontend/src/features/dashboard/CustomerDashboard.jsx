import React, { useEffect, useState } from 'react';
import { useAuth } from '../../core/context/AuthContext';
import apiClient from '../../core/api/apiClient';
import { SUPER_APP_MODULES } from '../../core/constants/odishaTheme';
import { 
  User, 
  Phone, 
  Mail, 
  MapPin, 
  HeartHandshake, 
  ShieldAlert, 
  Clock, 
  CheckCircle2, 
  ArrowRight, 
  Volume2, 
  Eye, 
  Globe, 
  LogOut,
  Bell,
  Sparkles
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const CustomerDashboard = () => {
  const { user, profile, token, logout, elderlyMode, toggleElderlyMode, language, changeLanguage, speak } = useAuth();
  const navigate = useNavigate();

  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboardSummary = async () => {
      try {
        if (token) {
          const res = await apiClient.get('/users/dashboard-summary', {
            headers: { Authorization: `Bearer ${token}` }
          });
          setDashboardData(res.data);
        }
      } catch (err) {
        console.warn('Could not fetch dashboard summary:', err);
      } finally {
        setLoading(false);
      }
    };
    loadDashboardSummary();
  }, [token]);

  const activeProfile = dashboardData?.profile || profile;

  const handleModuleClick = (moduleId) => {
    switch (moduleId) {
      case 'commerce':
        navigate('/shopping');
        break;
      case 'emergency':
        navigate('/emergency');
        break;
      case 'healthcare':
        navigate('/healthcare');
        break;
      case 'transport':
        navigate('/transport');
        break;
      case 'tourism':
        navigate('/tourism');
        break;
      case 'egov':
        navigate('/trust');
        break;
      default:
        break;
    }
  };

  const handleSpeakDashboard = () => {
    speak(
      `Welcome to your citizen dashboard, ${activeProfile?.fullName || 'Odisha Citizen'}. ${activeProfile?.isElderly ? 'Elderly mode is active with enhanced text size.' : ''}`,
      `ଆପଣଙ୍କ ନାଗରିକ ଡ୍ୟାସବୋର୍ଡକୁ ସ୍ୱାଗତ, ${activeProfile?.fullName || 'ଓଡ଼ିଆ ନାଗରିକ'}।`,
      language
    );
  };

  return (
    <div className={`min-h-screen transition-colors duration-300 ${
      elderlyMode 
        ? 'bg-amber-50 text-gray-900 text-lg' 
        : 'bg-stone-50 text-gray-800 text-base'
    }`}>
      
      {/* Top Navbar */}
      <header className="sticky top-0 z-40 bg-stone-900 text-white border-b border-amber-600/30 shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/')}>
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-600 to-orange-500 flex items-center justify-center font-black text-xl text-white shadow">
              ମୋ
            </div>
            <div>
              <h1 className="font-bold text-amber-400 leading-none">CITIZEN PORTAL</h1>
              <p className="text-[10px] text-stone-400">State Smart Life Super App</p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={() => changeLanguage(language === 'en' ? 'or' : 'en')}
              className="px-2.5 py-1.5 rounded-lg bg-stone-800 text-amber-300 text-xs font-semibold flex items-center space-x-1"
            >
              <Globe className="w-3.5 h-3.5" />
              <span>{language === 'en' ? 'ଓଡ଼ିଆ' : 'EN'}</span>
            </button>

            <button
              onClick={toggleElderlyMode}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1 border ${
                elderlyMode 
                  ? 'bg-amber-500 text-stone-950 border-amber-400 shadow' 
                  : 'bg-stone-800 text-stone-300 border-stone-700'
              }`}
            >
              <Eye className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">{elderlyMode ? 'Elderly Mode: ON' : 'Elderly Mode'}</span>
            </button>

            <button
              onClick={logout}
              className="px-3 py-1.5 rounded-lg bg-red-900/80 text-red-100 text-xs font-bold flex items-center space-x-1 hover:bg-red-800 transition-all"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>Logout</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        
        {/* Profile Card Header */}
        <div className="bg-white rounded-3xl p-6 sm:p-8 border border-stone-200/80 shadow-md relative overflow-hidden">
          <div className="absolute top-0 right-0 w-64 h-64 bg-gradient-to-bl from-amber-500/10 to-transparent rounded-bl-full pointer-events-none"></div>

          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 relative z-10">
            
            <div className="flex items-center space-x-4">
              <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center text-white text-3xl font-black shadow-lg">
                {activeProfile?.fullName ? activeProfile.fullName.charAt(0) : 'N/A'}
              </div>
              
              <div className="space-y-1">
                <div className="flex items-center space-x-3">
                  <h2 className="text-2xl font-black text-stone-900">{activeProfile?.fullName || 'Odisha Citizen'}</h2>
                  {activeProfile?.isElderly && (
                    <span className="inline-flex items-center space-x-1 px-3 py-1 rounded-full bg-amber-100 text-amber-900 border border-amber-300 text-xs font-extrabold">
                      <HeartHandshake className="w-3.5 h-3.5 text-amber-700" />
                      <span>Senior Citizen (Elderly Mode)</span>
                    </span>
                  )}
                </div>

                <div className="flex flex-wrap gap-4 text-xs text-stone-600 pt-1">
                  <div className="flex items-center space-x-1">
                    <Mail className="w-3.5 h-3.5 text-stone-400" />
                    <span>{user?.email || 'citizen@odisha.gov.in'}</span>
                  </div>
                  {activeProfile?.dateOfBirth && (
                    <div className="flex items-center space-x-1 font-semibold text-stone-700">
                      <span>Age: {activeProfile.age} yrs (DOB: {activeProfile.dateOfBirth})</span>
                    </div>
                  )}
                  {activeProfile?.homeAddress && (
                    <div className="flex items-center space-x-1">
                      <MapPin className="w-3.5 h-3.5 text-stone-400" />
                      <span>{activeProfile.homeAddress}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>

            <button
              onClick={handleSpeakDashboard}
              className="px-4 py-2.5 rounded-xl bg-amber-50 hover:bg-amber-100 text-amber-900 border border-amber-300 text-xs font-bold flex items-center space-x-2 transition-colors self-start md:self-auto"
            >
              <Volume2 className="w-4 h-4 text-amber-700" />
              <span>Read Summary</span>
            </button>
          </div>

          {/* System Notice Banner */}
          <div className="mt-6 pt-4 border-t border-stone-100 flex items-center justify-between text-xs text-stone-600 bg-stone-50 p-3 rounded-xl">
            <div className="flex items-center space-x-2">
              <Sparkles className="w-4 h-4 text-amber-600" />
              <span className="font-semibold text-stone-800">
                {dashboardData?.systemNotice || 'State Smart Life Services Operational • 24x7 Support Enabled'}
              </span>
            </div>
            <span className="text-emerald-600 font-bold hidden sm:inline">● Verified Citizen Account</span>
          </div>
        </div>

        {/* Quick Action Grid */}
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-xl font-bold text-stone-900 tracking-tight">Super App Citizen Services</h3>
            <span className="text-xs text-stone-500 font-medium">Select a service module to launch</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {SUPER_APP_MODULES.map((mod) => (
              <div
                key={mod.id}
                onClick={() => handleModuleClick(mod.id)}
                className={`bg-white rounded-2xl p-6 border border-stone-200 shadow-sm hover:shadow-lg transition-all cursor-pointer group flex flex-col justify-between ${
                  elderlyMode ? 'border-amber-300 ring-2 ring-amber-400/20' : ''
                }`}
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <div className="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
                      {mod.icon}
                    </div>
                    <span className="text-[11px] font-semibold bg-stone-100 text-stone-700 px-2.5 py-1 rounded-full border border-stone-200">
                      {mod.badge}
                    </span>
                  </div>
                  <h4 className="font-bold text-lg text-stone-900 group-hover:text-amber-700 transition-colors">
                    {mod.title}
                  </h4>
                  <p className="text-xs text-stone-600 mt-1 leading-relaxed">
                    {mod.description}
                  </p>
                </div>

                <div className="pt-4 mt-4 border-t border-stone-100 flex items-center justify-between text-xs font-bold text-amber-700">
                  <span>Open {mod.title}</span>
                  <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Active Orders & Requests Widget */}
        <section className="bg-white rounded-3xl p-6 sm:p-8 border border-stone-200/80 shadow-md space-y-4">
          <div className="flex items-center justify-between border-b pb-4">
            <div>
              <h3 className="text-lg font-bold text-stone-900">Active Service Tracker</h3>
              <p className="text-xs text-stone-500">Real-time status of your orders, medical appointments, and transit passes</p>
            </div>
            <span className="px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-bold">
              0 Active Alerts
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-center">
            <div className="bg-stone-50 p-4 rounded-2xl border border-stone-200">
              <p className="text-2xl font-black text-stone-900">{dashboardData?.activeOrdersCount || 0}</p>
              <p className="text-xs text-stone-500 font-semibold mt-1">Hyperlocal Orders</p>
            </div>
            <div className="bg-stone-50 p-4 rounded-2xl border border-stone-200">
              <p className="text-2xl font-black text-stone-900">{dashboardData?.activeAppointmentsCount || 0}</p>
              <p className="text-xs text-stone-500 font-semibold mt-1">Doctor Appointments</p>
            </div>
            <div className="bg-stone-50 p-4 rounded-2xl border border-stone-200">
              <p className="text-2xl font-black text-stone-900">{dashboardData?.activeTransitPassesCount || 0}</p>
              <p className="text-xs text-stone-500 font-semibold mt-1">Mo Bus Passes</p>
            </div>
          </div>
        </section>

      </main>
    </div>
  );
};

export default CustomerDashboard;
