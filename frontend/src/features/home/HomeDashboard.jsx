import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { translations } from '../../core/constants/translations';
import DistrictSelector from './DistrictSelector';
import { 
  ShoppingBag, HeartPulse, AlertOctagon, Compass, 
  Bus, Calendar, Truck, Award, LogOut, User, Accessibility, Volume2
} from 'lucide-react';

const HomeDashboard = () => {
  const { user, logout, elderlyMode, toggleElderlyMode, language, changeLanguage, speak } = useAuth();
  const navigate = useNavigate();

  // Multi-lingual & District states
  const t = translations[language] || translations.en;
  
  const [district, setDistrict] = useState(localStorage.getItem('ssl_district') || '');
  const [districtOr, setDistrictOr] = useState(localStorage.getItem('ssl_district_or') || '');

  const handleLogout = () => {
    logout();
    localStorage.removeItem('ssl_district');
    localStorage.removeItem('ssl_district_or');
    navigate('/');
  };

  const handleLanguageToggle = () => {
    const nextLang = language === 'en' ? 'or' : 'en';
    changeLanguage(nextLang);
  };

  // Define modules utilizing translations
  const modules = [
    {
      id: 'emergency',
      name: t.emergency,
      desc: t.emergency_desc,
      icon: AlertOctagon,
      color: 'text-brand-crimson',
      bgColor: 'bg-brand-crimson/15',
      borderColor: 'border-brand-crimson/35',
      route: '/emergency',
      emoji: '🚨',
      highPriority: true
    },
    {
      id: 'healthcare',
      name: t.healthcare,
      desc: t.healthcare_desc,
      icon: HeartPulse,
      color: 'text-brand-emerald',
      bgColor: 'bg-brand-emerald/15',
      borderColor: 'border-brand-emerald/35',
      route: '/healthcare',
      emoji: '🏥',
      highPriority: true
    },
    {
      id: 'shopping',
      name: t.shopping,
      desc: t.shopping_desc,
      icon: ShoppingBag,
      color: 'text-brand-blue',
      bgColor: 'bg-brand-blue/15',
      borderColor: 'border-brand-blue/35',
      route: '/shopping',
      emoji: '🛒'
    },
    {
      id: 'transport',
      name: t.transport,
      desc: t.transport_desc,
      icon: Bus,
      color: 'text-brand-amber',
      bgColor: 'bg-brand-amber/15',
      borderColor: 'border-brand-amber/35',
      route: '/transport',
      emoji: '🚌'
    },
    {
      id: 'tourism',
      name: t.tourism,
      desc: t.tourism_desc,
      icon: Compass,
      color: 'text-indigo-400',
      bgColor: 'bg-indigo-400/15',
      borderColor: 'border-indigo-400/35',
      route: '/tourism',
      emoji: '🧭'
    },
    {
      id: 'events',
      name: t.events,
      desc: t.events_desc,
      icon: Calendar,
      color: 'text-pink-400',
      bgColor: 'bg-pink-400/15',
      borderColor: 'border-pink-400/35',
      route: '/events',
      emoji: '🏛️'
    },
    {
      id: 'delivery',
      name: t.delivery,
      desc: t.delivery_desc,
      icon: Truck,
      color: 'text-teal-400',
      bgColor: 'bg-teal-400/15',
      borderColor: 'border-teal-400/35',
      route: '/delivery',
      emoji: '📦'
    },
    {
      id: 'trust',
      name: t.trust,
      desc: t.trust_desc,
      icon: Award,
      color: 'text-purple-400',
      bgColor: 'bg-purple-400/15',
      borderColor: 'border-purple-400/35',
      route: '/trust',
      emoji: '🎖️'
    }
  ];

  // Render District Selector overlay if none is set
  if (!district) {
    return (
      <DistrictSelector 
        language={language} 
        onSelect={(dist) => {
          setDistrict(dist.name);
          setDistrictOr(dist.orName);
        }} 
      />
    );
  }

  return (
    <div className="min-h-screen flex flex-col justify-between transition-all duration-300">
      
      {/* Header Bar */}
      <header className="sticky top-0 z-20 px-6 py-4 flex items-center justify-between border-b border-white/5 bg-slate-950/75 backdrop-blur-xl">
        <div className="flex items-center gap-3">
          {/* Stylized Lord Jagannath Eye Logo */}
          <div className="flex items-center gap-1 p-2 bg-brand-terracotta/20 border border-brand-marigold/30 rounded-xl">
            {/* Left Eye */}
            <div className="w-5 h-5 rounded-full bg-white border-[2px] border-brand-terracotta flex items-center justify-center relative">
              <div className="w-2.5 h-2.5 rounded-full bg-black flex items-center justify-center">
                <div className="w-0.5 h-0.5 bg-white rounded-full absolute top-[1px] right-[1px]"></div>
              </div>
            </div>
            {/* Tilak */}
            <div className="w-1.5 h-4.5 bg-brand-gold rounded-sm"></div>
            {/* Right Eye */}
            <div className="w-5 h-5 rounded-full bg-white border-[2px] border-brand-terracotta flex items-center justify-center relative">
              <div className="w-2.5 h-2.5 rounded-full bg-black flex items-center justify-center">
                <div className="w-0.5 h-0.5 bg-white rounded-full absolute top-[1px] right-[1px]"></div>
              </div>
            </div>
          </div>
          <div>
            <h1 className={`font-black text-slate-100 ${elderlyMode ? 'text-2xl' : 'text-lg'}`}>
              {t.welcome_title}
            </h1>
            <p className="text-[9px] text-brand-marigold font-semibold uppercase tracking-wider">
              {t.subtitle}
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-3">
          {/* Audio assistance help button */}
          <button
            onClick={() => speak('Welcome to Mo Odisha. Selected resident district is ' + district + '.', 'ମୋ ଓଡ଼ିଶା ସ୍ମାର୍ଟ ଆପ୍ଲିକେସନ୍ କୁ ସ୍ଵାଗତ। ଚୟନ କରାଯାଇଥିବା ଜିଲ୍ଲା ' + districtOr + ' ଅଟେ।')}
            className="p-2.5 rounded-xl bg-brand-marigold/10 border border-brand-marigold/35 text-brand-marigold hover:bg-brand-marigold/20 transition-all cursor-pointer flex items-center gap-1.5 font-bold text-xs"
          >
            <Volume2 className="w-4 h-4 animate-bounce" />
            {!elderlyMode && <span>{t.voice_help}</span>}
          </button>

          {/* Language Switch */}
          <button
            onClick={handleLanguageToggle}
            className="px-3.5 py-1.5 bg-brand-blue/15 border border-brand-blue/30 text-brand-blue font-black text-xs rounded-xl hover:bg-brand-blue/25 transition-all cursor-pointer"
          >
            {language === 'en' ? 'ଓଡ଼ିଆ' : 'English'}
          </button>

          {/* Large text toggle */}
          <button
            onClick={toggleElderlyMode}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl font-bold transition-all border cursor-pointer ${
              elderlyMode 
                ? 'bg-brand-amber text-slate-950 border-brand-amber text-base shadow-[0_0_15px_rgba(245,158,11,0.4)]'
                : 'bg-white/5 text-slate-300 border-white/10 hover:bg-white/10 text-xs'
            }`}
          >
            <Accessibility className={elderlyMode ? 'w-5 h-5' : 'w-4 h-4'} />
            <span>{t.large_text}</span>
          </button>

          {/* Logout */}
          <button
            onClick={handleLogout}
            className="p-2.5 rounded-xl bg-brand-crimson/15 border border-brand-crimson/30 text-brand-crimson hover:bg-brand-crimson/25 cursor-pointer transition-all"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </header>

      {/* Main Grid Content */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 space-y-6">
        
        {/* Welcome Greeting Card */}
        <div className="rounded-3xl glass-card p-6 border border-white/5 text-left relative overflow-hidden bg-slate-900/40">
          <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <h2 className={`font-black text-slate-100 flex items-center gap-2 ${elderlyMode ? 'text-4xl' : 'text-2xl'}`}>
                <span>{t.welcome_greeting}, {user?.name}!</span>
                <span className="text-brand-marigold text-glow-marigold">{t.jay_jagannath}</span>
              </h2>
              <p className={`text-slate-400 mt-1 ${elderlyMode ? 'text-lg font-medium' : 'text-xs'}`}>
                {t.dashboard_desc} (District: <span className="text-brand-marigold font-bold">{language === 'or' ? districtOr : district}</span>)
              </p>
            </div>
            
            <button
              onClick={() => speak(`Hello ${user?.name}. Jay Jagannath. Your registered district is ${district}.`, `ନମସ୍କାର ${user?.name}। ଜୟ ଜଗନ୍ନାଥ। ଆପଣଙ୍କ ରହୁଥିବା ଜିଲ୍ଲା ${districtOr} ଅଟେ।`)}
              className="px-4 py-2 bg-white/5 border border-white/10 text-slate-300 hover:bg-white/10 rounded-xl flex items-center justify-center gap-2 font-semibold text-xs cursor-pointer w-fit"
            >
              <Volume2 className="w-4 h-4 text-brand-marigold" />
              {language === 'or' ? 'ଶୁଣନ୍ତୁ' : 'Listen'}
            </button>
          </div>
        </div>

        {/* Lord Jagannath Daily Blessing Card */}
        <div className="p-6 rounded-3xl border-2 border-brand-marigold/20 bg-brand-terracotta/10 text-left relative overflow-hidden flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <span className="text-3xl">🎖️</span>
            <div>
              <h4 className="text-sm font-black text-brand-marigold uppercase tracking-wider">{t.blessing_title}</h4>
              <p className={`text-slate-300 font-semibold italic mt-1 ${elderlyMode ? 'text-xl' : 'text-xs'}`}>
                "{t.blessing_text}"
              </p>
            </div>
          </div>
          <button
            onClick={() => speak(translations.en.blessing_text, translations.or.blessing_text)}
            className="px-4 py-2 bg-brand-marigold text-slate-950 font-black text-xs rounded-xl hover:opacity-90 shadow-md flex items-center gap-1.5 cursor-pointer shrink-0"
          >
            <Volume2 className="w-4 h-4" />
            {t.read_blessing}
          </button>
        </div>

        {/* Core Services Grid */}
        <div className={`grid gap-6 ${
          elderlyMode 
            ? 'grid-cols-1 md:grid-cols-2' 
            : 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-4'
        }`}>
          {modules.map((mod) => {
            const IconComponent = mod.icon;
            
            return (
              <div
                key={mod.id}
                className={`relative rounded-3xl overflow-hidden flex flex-col justify-between transition-all ${
                  elderlyMode 
                    ? 'p-8 bg-slate-900 border-2 border-white/10 hover:border-brand-marigold text-slate-100' 
                    : 'p-6 glass-card border border-white/5 hover:glass-card-hover'
                }`}
              >
                {/* Big Visual Sign (Emoji + Icon) */}
                <div className="flex items-start justify-between">
                  <button
                    onClick={() => navigate(mod.route)}
                    className={`p-5 rounded-2xl cursor-pointer ${mod.bgColor} border ${mod.borderColor} flex items-center gap-2.5`}
                  >
                    <IconComponent className={`w-8 h-8 ${mod.color}`} />
                    <span className="text-2xl">{mod.emoji}</span>
                  </button>

                  <button
                    onClick={() => speak(
                      translations.en[mod.id] + ". " + translations.en[mod.id + '_desc'],
                      translations.or[mod.id] + ". " + translations.or[mod.id + '_desc']
                    )}
                    className="p-2.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue hover:bg-brand-blue/20 cursor-pointer"
                  >
                    <Volume2 className="w-4.5 h-4.5" />
                  </button>
                </div>

                <div className="mt-8 text-left space-y-2">
                  <button
                    onClick={() => navigate(mod.route)}
                    className={`font-black text-slate-100 block text-left hover:text-brand-marigold cursor-pointer w-full ${
                      elderlyMode ? 'text-2xl' : 'text-base'
                    }`}
                  >
                    {mod.name}
                  </button>
                  <p className={`text-slate-400 font-medium leading-relaxed ${
                    elderlyMode ? 'text-lg' : 'text-[11px]'
                  }`}>
                    {mod.desc}
                  </p>
                </div>

                <button
                  onClick={() => navigate(mod.route)}
                  className={`w-full py-3 mt-5 rounded-xl font-black text-xs text-center border transition-all cursor-pointer ${
                    elderlyMode
                      ? 'bg-brand-marigold text-slate-950 border-brand-marigold text-sm'
                      : 'bg-white/5 border-white/10 text-slate-300 hover:bg-white/10'
                  }`}
                >
                  OPEN {mod.emoji}
                </button>
              </div>
            );
          })}
        </div>

      </main>

      {/* Footer */}
      <footer className="px-6 py-6 border-t border-white/5 text-center text-xs text-slate-600 bg-slate-950/20">
        © 2026 Odisha Smart City Infrastructure Commission. All rights reserved.
      </footer>
    </div>
  );
};

export default HomeDashboard;
