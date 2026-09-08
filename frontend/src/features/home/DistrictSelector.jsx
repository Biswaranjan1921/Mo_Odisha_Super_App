import React from 'react';
import { translations } from '../../core/constants/translations';
import { useAuth } from '../../core/context/AuthContext';
import { Volume2, MapPin } from 'lucide-react';

const DistrictSelector = ({ language, onSelect }) => {
  const { speak } = useAuth();
  const t = translations[language] || translations.en;
  
  // Odisha's prominent districts for selection
  const districts = [
    { name: 'Khordha', orName: 'ଖୋର୍ଦ୍ଧା' },
    { name: 'Cuttack', orName: 'କଟକ' },
    { name: 'Puri', orName: 'ପୁରୀ' },
    { name: 'Ganjam', orName: 'ଗଞ୍ଜାମ' },
    { name: 'Sambalpur', orName: 'ସମ୍ବଲପୁର' },
    { name: 'Balasore', orName: 'ବାଲେଶ୍ଵର' },
    { name: 'Bhadrak', orName: 'ଭଦ୍ରକ' },
    { name: 'Mayurbhanj', orName: 'ମୟୂରଭଞ୍ଜ' },
    { name: 'Sundargarh', orName: 'ସୁନ୍ଦରଗଡ଼' },
    { name: 'Jajpur', orName: 'ଯାଜପୁର' },
    { name: 'Angul', orName: 'ଅନୁଗୋଳ' },
    { name: 'Koraput', orName: 'କୋରାପୁଟ' }
  ];

  const handleSelect = (dist) => {
    localStorage.setItem('ssl_district', dist.name);
    localStorage.setItem('ssl_district_or', dist.orName);
    
    // Voice confirmation
    speak(`Welcome to ${dist.name} district.`, `ଆପଣଙ୍କୁ ${dist.orName} ଜିଲ୍ଲାରେ ସ୍ଵାଗତ।`);

    onSelect(dist);
  };

  return (
    <div className="fixed inset-0 bg-slate-950/90 backdrop-blur-xl flex items-center justify-center z-50 p-6 overflow-y-auto">
      <div className="w-full max-w-4xl p-8 rounded-3xl glass-card border border-brand-marigold/30 text-center relative overflow-hidden my-8">
        
        {/* Lord Jagannath stylized round eyes CSS badge in center header */}
        <div className="flex justify-center items-center gap-6 mb-6">
          {/* Stylized Lord Jagannath Eye Logo */}
          <div className="flex items-center gap-1.5 p-3 bg-brand-terracotta/20 border border-brand-marigold/30 rounded-2xl">
            {/* Left Eye */}
            <div className="w-8 h-8 rounded-full bg-white border-[3px] border-brand-terracotta flex items-center justify-center relative">
              <div className="w-4 h-4 rounded-full bg-black flex items-center justify-center">
                <div className="w-1.5 h-1.5 rounded-full bg-white absolute top-1 right-1"></div>
              </div>
            </div>
            {/* Tilak */}
            <div className="w-2.5 h-7 bg-brand-gold rounded-t-sm rounded-b-md"></div>
            {/* Right Eye */}
            <div className="w-8 h-8 rounded-full bg-white border-[3px] border-brand-terracotta flex items-center justify-center relative">
              <div className="w-4 h-4 rounded-full bg-black flex items-center justify-center">
                <div className="w-1.5 h-1.5 rounded-full bg-white absolute top-1 right-1"></div>
              </div>
            </div>
          </div>
        </div>

        <div className="mb-8 space-y-2">
          <h2 className="text-3xl font-black text-slate-100 flex items-center justify-center gap-3">
            <span>{t.select_district}</span>
            <button 
              onClick={() => speak('Please select the district you belong to.', 'ଦୟାକରି ଆପଣ କେଉଁ ଜିଲ୍ଲାରେ ରହୁଛନ୍ତି ତାହା ଚୟନ କରନ୍ତୁ।')}
              className="p-1.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue cursor-pointer"
            >
              <Volume2 className="w-4 h-4" />
            </button>
          </h2>
          <p className="text-sm text-slate-400 max-w-xl mx-auto">
            {t.select_district_desc}
          </p>
        </div>

        {/* District list grid */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {districts.map((dist, idx) => (
            <button
              key={idx}
              onClick={() => handleSelect(dist)}
              className="p-5 rounded-2xl border border-white/5 bg-white/[0.01] hover:bg-brand-marigold/10 hover:border-brand-marigold/40 text-left transition-all cursor-pointer group flex flex-col justify-between h-24"
            >
              <div className="flex items-center justify-between w-full">
                <MapPin className="w-5 h-5 text-brand-marigold group-hover:animate-bounce" />
                <span className="text-2xl opacity-60">📍</span>
              </div>
              <div>
                <p className="text-sm font-bold text-slate-200">{dist.name}</p>
                <p className="text-xs text-slate-500 font-semibold group-hover:text-brand-marigold">{dist.orName}</p>
              </div>
            </button>
          ))}
        </div>

      </div>
    </div>
  );
};

export default DistrictSelector;
