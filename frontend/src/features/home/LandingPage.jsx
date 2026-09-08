import React, { useState } from 'react';
import { useAuth } from '../../core/context/AuthContext';
import { CULTURAL_SYMBOLS, SUPER_APP_MODULES } from '../../core/constants/odishaTheme';
import AuthModal from '../auth/AuthModal';
import { 
  Compass, 
  ShieldCheck, 
  Eye, 
  Volume2, 
  Globe, 
  ExternalLink, 
  UserCheck, 
  HeartHandshake, 
  ArrowRight,
  Sun,
  BookOpen
} from 'lucide-react';

const LandingPage = () => {
  const { user, profile, logout, elderlyMode, toggleElderlyMode, language, changeLanguage, speak } = useAuth();
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [selectedModule, setSelectedModule] = useState(null);

  const handleSpeakWelcome = () => {
    speak(
      'Welcome to State Smart Life, Mo Odisha Super App. One State. One Digital Experience.',
      'ଷ୍ଟେଟ୍ ସ୍ମାର୍ଟ ଲାଇଫ୍, ମୋ ଓଡ଼ିଶା ସୁପର ଆପ୍ କୁ ସ୍ୱାଗତ। ଏକ ରାଜ୍ୟ, ଏକ ଡିଜିଟାଲ୍ ଅନୁଭବ।',
      language
    );
  };

  return (
    <div className={`min-h-screen transition-colors duration-300 ${
      elderlyMode 
        ? 'bg-amber-50 text-gray-900 text-lg selection:bg-amber-300' 
        : 'bg-stone-50 text-gray-800 text-base'
    }`}>
      
      {/* Navigation Bar */}
      <header className="sticky top-0 z-40 bg-stone-900/95 backdrop-blur-md text-white border-b border-amber-600/30 shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          
          {/* Logo */}
          <div className="flex items-center space-x-3 cursor-pointer" onClick={handleSpeakWelcome}>
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-amber-600 via-orange-500 to-yellow-400 flex items-center justify-center shadow-lg border border-amber-300/40">
              <span className="font-extrabold text-2xl text-white tracking-wider">ମୋ</span>
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="font-black text-xl tracking-tight text-amber-400">MO ODISHA</h1>
                <span className="text-xs bg-amber-500/20 text-amber-300 px-2 py-0.5 rounded-full border border-amber-500/40 font-semibold">SUPER APP</span>
              </div>
              <p className="text-xs text-stone-400">STATE SMART LIFE — Digital Odisha</p>
            </div>
          </div>

          {/* Controls & Account */}
          <div className="flex items-center space-x-3 sm:space-x-4">
            
            {/* Language Switcher */}
            <button
              onClick={() => changeLanguage(language === 'en' ? 'or' : 'en')}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-stone-800 hover:bg-stone-700 text-amber-300 text-sm font-semibold border border-amber-500/30 transition-all"
            >
              <Globe className="w-4 h-4" />
              <span>{language === 'en' ? 'ଓଡ଼ିଆ' : 'English'}</span>
            </button>

            {/* Elderly Mode Toggle */}
            <button
              onClick={toggleElderlyMode}
              className={`flex items-center space-x-2 px-3 py-1.5 rounded-xl text-sm font-semibold transition-all border ${
                elderlyMode
                  ? 'bg-amber-500 text-stone-950 border-amber-400 shadow-md shadow-amber-500/20 ring-2 ring-amber-400/50'
                  : 'bg-stone-800 text-stone-300 border-stone-700 hover:bg-stone-700'
              }`}
            >
              <Eye className="w-4 h-4" />
              <span className="hidden sm:inline">{elderlyMode ? 'Elderly Mode: ON' : 'Elderly Mode'}</span>
            </button>

            {/* Swagger API Specs Link */}
            <a
              href="http://localhost:8081/swagger-ui.html"
              target="_blank"
              rel="noopener noreferrer"
              className="hidden md:flex items-center space-x-1 px-3 py-1.5 rounded-xl bg-stone-800 hover:bg-stone-700 text-amber-400 text-xs font-mono border border-amber-500/20"
            >
              <BookOpen className="w-3.5 h-3.5" />
              <span>OpenAPI Docs</span>
              <ExternalLink className="w-3 h-3" />
            </a>

            {/* Auth Action */}
            {user ? (
              <div className="flex items-center space-x-3">
                <div className="hidden sm:block text-right">
                  <p className="text-xs font-bold text-amber-300">{profile?.fullName || user.email}</p>
                  <p className="text-[10px] text-stone-400">
                    {profile?.isElderly ? 'Elderly Citizen' : 'Citizen'} {profile?.age ? `(${profile.age} yrs)` : ''}
                  </p>
                </div>
                <button
                  onClick={logout}
                  className="px-3.5 py-2 rounded-xl bg-red-900/80 hover:bg-red-800 text-red-100 text-sm font-bold border border-red-500/30 shadow transition-all"
                >
                  Logout
                </button>
              </div>
            ) : (
              <button
                onClick={() => setIsAuthModalOpen(true)}
                className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-gradient-to-r from-amber-600 to-orange-600 hover:from-amber-700 hover:to-orange-700 text-white font-bold text-sm shadow-lg shadow-orange-600/30 transition-all border border-amber-400/40"
              >
                <UserCheck className="w-4 h-4" />
                <span>Sign In / Register</span>
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative bg-gradient-to-b from-stone-900 via-stone-850 to-stone-900 text-white py-16 lg:py-24 overflow-hidden border-b border-amber-600/20">
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#D4AF37_1px,transparent_1px)] [background-size:16px_16px]"></div>
        
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            
            {/* Left Content */}
            <div className="lg:col-span-7 space-y-6">
              <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-400 text-sm font-semibold">
                <Sun className="w-4 h-4 animate-spin-slow" />
                <span>GOVERNMENT OF ODISHA DIGITAL STATE INITIATIVE</span>
              </div>

              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black tracking-tight text-white leading-tight">
                One State.<br />
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-400 via-orange-400 to-yellow-300">
                  One Digital Experience.
                </span>
              </h1>

              <p className="text-stone-300 text-lg sm:text-xl max-w-2xl font-light leading-relaxed">
                Seamless hyperlocal shopping, 24x7 emergency response, health services, bus commute, and citizen governance — built for 45 Million citizens of Odisha.
              </p>

              <div className="flex flex-wrap gap-4 pt-4">
                <button
                  onClick={() => setIsAuthModalOpen(true)}
                  className="px-6 py-3.5 rounded-2xl bg-gradient-to-r from-amber-600 via-orange-600 to-amber-700 hover:from-amber-700 hover:to-orange-800 text-white font-bold text-lg shadow-xl shadow-orange-600/25 transition-all flex items-center space-x-3 border border-amber-300/30"
                >
                  <span>Get Started with Mo Odisha</span>
                  <ArrowRight className="w-5 h-5" />
                </button>

                <button
                  onClick={handleSpeakWelcome}
                  className="px-5 py-3.5 rounded-2xl bg-stone-800 hover:bg-stone-700 text-amber-300 font-semibold text-lg border border-amber-500/30 transition-all flex items-center space-x-2"
                >
                  <Volume2 className="w-5 h-5" />
                  <span>Voice Welcome (Odia/En)</span>
                </button>
              </div>

              {/* Status Indicator */}
              <div className="pt-6 flex items-center space-x-6 text-xs text-stone-400">
                <div className="flex items-center space-x-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
                  <span>System Operational (1,000 VUs Ready)</span>
                </div>
                <div>• PostgreSQL + PostGIS Enabled</div>
                <div>• Automated Elderly Engine Active</div>
              </div>
            </div>

            {/* Right Konark Wheel Visual */}
            <div className="lg:col-span-5 flex justify-center">
              <div className="relative w-72 h-72 sm:w-96 sm:h-96 rounded-full bg-gradient-to-tr from-amber-700/30 via-orange-600/20 to-amber-400/10 border-4 border-amber-500/30 flex items-center justify-center p-8 shadow-2xl backdrop-blur-md">
                <div className="absolute inset-4 rounded-full border-2 border-dashed border-amber-400/30 animate-spin-slow"></div>
                <div className="text-center space-y-3">
                  <div className="text-6xl sm:text-7xl">🚩</div>
                  <h3 className="font-extrabold text-2xl text-amber-300">ମୋ ଓଡ଼ିଶା</h3>
                  <p className="text-xs text-stone-300 font-medium px-4">
                    Puri • Konark • Bhubaneswar • Cuttack • Sambalpur • Rourkela
                  </p>
                </div>
              </div>
            </div>

          </div>
        </div>
      </section>

      {/* Cultural Heritage Section */}
      <section className="py-16 bg-stone-100 border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-12">
            <h2 className="text-3xl font-black text-stone-900 tracking-tight">
              Rooted in Heritage. Powered by Innovation.
            </h2>
            <p className="text-stone-600 mt-2">
              Celebrating Odisha's architectural, spiritual, and ecological majesty while delivering modern digital state governance.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-6">
            {CULTURAL_SYMBOLS.map((item, idx) => (
              <div 
                key={idx} 
                className="bg-white rounded-2xl p-6 shadow-sm hover:shadow-md border border-stone-200/80 transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="text-4xl mb-3">{item.icon}</div>
                  <h3 className="font-bold text-stone-900 text-lg leading-snug">{item.title}</h3>
                  <p className="text-xs text-amber-700 font-semibold mb-2">{item.subtitle}</p>
                  <p className="text-xs text-stone-600 leading-relaxed">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Super App Modules Grid */}
      <section className="py-16 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-12">
          <span className="text-xs font-bold text-orange-600 tracking-widest uppercase bg-orange-100 px-3 py-1 rounded-full">
            SUPER APP SERVICES
          </span>
          <h2 className="text-3xl sm:text-4xl font-black text-gray-900 mt-3 tracking-tight">
            Integrated State Services at Your Fingertips
          </h2>
          <p className="text-gray-600 mt-2">
            One single unified portal connecting citizens, local merchants, emergency dispatchers, and state transport.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {SUPER_APP_MODULES.map((mod) => (
            <div
              key={mod.id}
              onClick={() => setSelectedModule(mod)}
              className={`rounded-2xl p-6 bg-white border border-stone-200 shadow-sm hover:shadow-xl transition-all cursor-pointer group flex flex-col justify-between ${
                elderlyMode ? 'border-amber-300 ring-1 ring-amber-400/30' : ''
              }`}
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className="w-14 h-14 rounded-2xl bg-amber-100/80 flex items-center justify-center text-3xl group-hover:scale-110 transition-transform">
                    {mod.icon}
                  </div>
                  <span className="text-xs font-semibold bg-stone-100 text-stone-700 px-2.5 py-1 rounded-full border border-stone-200">
                    {mod.badge}
                  </span>
                </div>
                <h3 className="font-bold text-xl text-stone-900 group-hover:text-amber-700 transition-colors">
                  {mod.title}
                </h3>
                <p className="text-sm text-stone-600 mt-2 leading-relaxed">
                  {mod.description}
                </p>
              </div>

              <div className="pt-4 mt-4 border-t border-stone-100 flex items-center justify-between text-xs font-bold text-amber-700">
                <span>Explore Service</span>
                <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Elderly Mode Preview Banner */}
      <section className="bg-amber-600 text-stone-950 py-12 border-t border-b border-amber-500">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="space-y-2 max-w-2xl">
            <div className="inline-flex items-center space-x-2 bg-stone-950/20 text-stone-950 font-bold text-xs px-3 py-1 rounded-full">
              <HeartHandshake className="w-4 h-4" />
              <span>AUTOMATED ELDERLY MODE (AGE ≥ 60)</span>
            </div>
            <h3 className="text-2xl sm:text-3xl font-black">
              Accessible Governance for Senior Citizens
            </h3>
            <p className="text-stone-900 font-medium">
              Registered citizens aged 60 and above automatically receive large touch targets, high contrast, simplified navigation, and voice prompts.
            </p>
          </div>

          <button
            onClick={toggleElderlyMode}
            className="px-6 py-3.5 rounded-2xl bg-stone-950 text-amber-300 font-bold text-lg hover:bg-stone-900 shadow-lg transition-all flex items-center space-x-2 flex-shrink-0"
          >
            <Eye className="w-5 h-5" />
            <span>{elderlyMode ? 'Disable Preview' : 'Test Elderly Mode'}</span>
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-stone-900 text-stone-400 py-12 border-t border-stone-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center space-y-4">
          <div className="flex justify-center space-x-2 items-center text-white font-bold">
            <span className="text-2xl text-amber-400">ମୋ</span>
            <span className="text-lg">STATE SMART LIFE — MO ODISHA SUPER APP</span>
          </div>
          <p className="text-xs text-stone-500">
            Designed for 1,000 concurrent users baseline • PostGIS Enabled • Modular Monolith Architecture
          </p>
          <div className="flex justify-center space-x-6 text-xs text-amber-400 font-mono pt-2">
            <a href="http://localhost:8081/swagger-ui.html" target="_blank" rel="noopener noreferrer" className="hover:underline flex items-center space-x-1">
              <span>Swagger UI Documentation</span>
              <ExternalLink className="w-3 h-3" />
            </a>
            <span>•</span>
            <a href="http://localhost:8081/actuator/health" target="_blank" rel="noopener noreferrer" className="hover:underline flex items-center space-x-1">
              <span>Actuator Health Check</span>
              <ExternalLink className="w-3 h-3" />
            </a>
          </div>
          <p className="text-[11px] text-stone-600 pt-4">
            © 2026 Government of Odisha. All rights reserved. Built with Java 17, Spring Boot 3.1.5, PostgreSQL, and React 18.
          </p>
        </div>
      </footer>

      {/* Auth Modal */}
      <AuthModal 
        isOpen={isAuthModalOpen} 
        onClose={() => setIsAuthModalOpen(false)} 
      />
    </div>
  );
};

export default LandingPage;
