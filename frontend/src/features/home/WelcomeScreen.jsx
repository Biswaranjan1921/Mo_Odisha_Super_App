import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { translations } from '../../core/constants/translations';
import { Shield, Eye, EyeOff, Sparkles, Building2, Volume2, ShieldCheck, Mail, Key } from 'lucide-react';

const WelcomeScreen = () => {
  const { login, token, toggleElderlyMode, elderlyMode, language, changeLanguage, speak } = useAuth();
  const navigate = useNavigate();

  const t = translations[language] || translations.en;

  // Step state: 1 (Credentials & Captcha), 2 (Aadhaar KYC), 3 (Email OTP), 4 (PIN Verification)
  const [step, setStep] = useState(1);

  // Form states
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [age, setAge] = useState('');
  
  // Math Captcha state
  const [captchaNum1, setCaptchaNum1] = useState(5);
  const [captchaNum2, setCaptchaNum2] = useState(3);
  const [captchaAnswer, setCaptchaAnswer] = useState('');

  // Aadhaar states
  const [aadhaarNum, setAadhaarNum] = useState('');
  const [aadhaarOtp, setAadhaarOtp] = useState('');
  const [simulatedAadhaarOtpCode] = useState('448822');
  const [aadhaarLoading, setAadhaarLoading] = useState(false);

  // Email states
  const [emailCode, setEmailCode] = useState('');
  const [simulatedEmailCode] = useState('998877');

  // PIN states
  const [pin, setPin] = useState('');
  const [showPin, setShowPin] = useState(false);

  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (token) {
      navigate('/dashboard');
    }
  }, [token, navigate]);

  // Generate random math captcha question
  useEffect(() => {
    setCaptchaNum1(Math.floor(Math.random() * 9) + 2);
    setCaptchaNum2(Math.floor(Math.random() * 9) + 1);
  }, [step]);

  const handleLanguageToggle = () => {
    const nextLang = language === 'en' ? 'or' : 'en';
    changeLanguage(nextLang);
  };

  // Step 1: Submit Credentials & Solve Captcha
  const handleStep1Submit = (e) => {
    e.preventDefault();
    setError('');

    if (!phone || !email || !age || !captchaAnswer) {
      const msgOr = 'ଦୟାକରି ସମସ୍ତ ତଥ୍ୟ ପୂରଣ କରନ୍ତୁ।';
      const msgEn = 'Please fill in all fields.';
      setError(language === 'or' ? msgOr : msgEn);
      speak(msgEn, msgOr);
      return;
    }

    // Verify Captcha
    if (Number(captchaAnswer) !== (captchaNum1 + captchaNum2)) {
      const msgOr = 'କ୍ୟାପଚା ଉତ୍ତର ଭୁଲ୍ ଅଟେ।';
      const msgEn = 'Wrong CAPTCHA answer.';
      setError(language === 'or' ? msgOr : msgEn);
      speak(msgEn, msgOr);
      return;
    }

    speak('Please verify your Aadhaar number.', 'ଆଧାର କାର୍ଡ ନମ୍ବର ଯାଞ୍ଚ କରନ୍ତୁ।');
    setStep(2);
  };

  // Step 2: Validate Aadhaar card & request OTP
  const handleAadhaarVerify = () => {
    setError('');
    if (aadhaarNum.length !== 12) {
      const msgOr = 'ଆଧାର ନମ୍ବର ୧୨ ଅଙ୍କ ବିଶିଷ୍ଟ ହେବା ଉଚିତ୍।';
      const msgEn = 'Aadhaar must be exactly 12 digits.';
      setError(language === 'or' ? msgOr : msgEn);
      speak(msgEn, msgOr);
      return;
    }

    setAadhaarLoading(true);
    const last4 = phone.slice(-4) || 'XXXX';
    const msgOr = `ଆଧାର ଓଟିପି ଆପଣଙ୍କ ମୋବାଇଲ୍ +91 ******${last4} କୁ ପଠାଗଲା।`;
    const msgEn = `Aadhaar OTP has been sent to your Aadhaar-linked mobile number: +91 ******${last4}.`;
    speak(msgEn, msgOr);
    setTimeout(() => {
      setAadhaarLoading(false);
      setStep(2.5); // Mode for OTP input
    }, 1500);
  };

  const handleAadhaarOtpSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (aadhaarOtp !== simulatedAadhaarOtpCode) {
      const msgOr = 'ଆଧାର ଓଟିପି ଭୁଲ୍ ଅଟେ।';
      const msgEn = 'Incorrect Aadhaar OTP.';
      setError(language === 'or' ? msgOr : msgEn);
      speak(msgEn, msgOr);
      return;
    }

    speak('Aadhaar verified. Please enter email verification code.', 'ଇମେଲ୍ କୋଡ୍ ଯାଞ୍ଚ କରନ୍ତୁ।');
    setStep(3);
  };

  // Step 3: Validate Email OTP code
  const handleEmailCodeSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (emailCode !== simulatedEmailCode) {
      const msgOr = 'ଇମେଲ୍ କୋଡ୍ ଭୁଲ୍ ଅଟେ।';
      const msgEn = 'Incorrect email verification code.';
      setError(language === 'or' ? msgOr : msgEn);
      speak(msgEn, msgOr);
      return;
    }

    speak('Email verified. Enter your 4-digit access PIN.', '୪-ଅଙ୍କ ବିଶିଷ୍ଟ ପ୍ରବେଶ ପିନ୍ ଦିଅନ୍ତୁ।');
    setStep(4);
  };

  // Step 4: Login & Validate PIN
  const handlePinSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (pin.length !== 4) {
      const msgOr = 'ପିନ୍ ୪ ଅଙ୍କ ବିଶିଷ୍ଟ ହେବା ଉଚିତ୍।';
      const msgEn = 'PIN must be exactly 4 digits.';
      setError(language === 'or' ? msgOr : msgEn);
      speak(msgEn, msgOr);
      return;
    }

    setIsSubmitting(true);
    const result = await login(phone, pin);
    setIsSubmitting(false);

    if (result.success) {
      // Auto toggle Elderly Mode if user age is 60 or older
      const numericAge = Number(age);
      if (numericAge >= 60) {
        if (!elderlyMode) toggleElderlyMode();
        localStorage.setItem('ssl_elderly_mode', 'true');
      } else {
        localStorage.setItem('ssl_elderly_mode', 'false');
      }

      speak('Access unlocked. Jay Jagannath.', 'ପ୍ରବେଶ ସଫଳ। ଜୟ ଜଗନ୍ନାଥ।');
      navigate('/dashboard');
    } else {
      setError(result.error);
    }
  };

  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-hidden">
      
      {/* Background Hero Looping Scenic Video */}
      <video 
        autoPlay 
        loop 
        muted 
        playsInline 
        className="absolute inset-0 w-full h-full object-cover opacity-30 z-0"
      >
        <source src="https://assets.mixkit.co/videos/preview/mixkit-scenic-view-of-a-beautiful-river-flowing-in-forest-42828-large.mp4" type="video/mp4" />
      </video>

      {/* Terracotta/Marigold dark gradient overlay */}
      <div className="absolute inset-0 bg-gradient-to-br from-brand-navy via-brand-navy/90 to-brand-terracotta/40 z-0"></div>

      {/* Header Bar */}
      <header className="px-6 py-4 flex items-center justify-between border-b border-white/5 bg-slate-950/40 backdrop-blur-md z-10">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-brand-terracotta/20 rounded-xl border border-brand-marigold/30">
            <Building2 className="w-6 h-6 text-brand-marigold text-glow-marigold" />
          </div>
          <div>
            <h1 className="font-extrabold text-xl tracking-tight bg-gradient-to-r from-slate-100 to-slate-400 bg-clip-text text-transparent">
              {t.welcome_title}
            </h1>
            <p className="text-[10px] text-brand-marigold font-semibold uppercase tracking-wider">
              {t.subtitle}
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-3">
          {/* Audio helper triggers */}
          <button
            onClick={() => speak(language === 'or' ? 'ମୋ ଓଡ଼ିଶା ଆପ୍ଲିକେସନ୍ କୁ ସ୍ଵାଗତ। ପ୍ରବେଶ କରିବା ପାଇଁ ତଥ୍ୟ ପୂରଣ କରନ୍ତୁ।' : 'Welcome to Mo Odisha. Please complete the verification forms to access.')}
            className="p-2.5 rounded-xl bg-brand-marigold/10 border border-brand-marigold/35 text-brand-marigold hover:bg-brand-marigold/20 transition-all cursor-pointer flex items-center gap-1.5 font-bold text-xs"
          >
            <Volume2 className="w-4 h-4 animate-bounce" />
            <span>{t.voice_help}</span>
          </button>

          {/* Language Switcher */}
          <button
            onClick={handleLanguageToggle}
            className="px-3.5 py-1.5 bg-brand-blue/15 border border-brand-blue/30 text-brand-blue font-black text-xs rounded-xl hover:bg-brand-blue/25 transition-all cursor-pointer"
          >
            {language === 'en' ? 'ଓଡ଼ିଆ' : 'English'}
          </button>
        </div>
      </header>

      {/* Form Content */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-12 flex items-center justify-center z-10">
        <div className="w-full max-w-md p-8 rounded-3xl glass-card relative overflow-hidden text-left border-2 border-brand-marigold/10">
          <div className="absolute top-0 left-0 right-0 h-[3px] bg-gradient-to-r from-brand-terracotta to-brand-marigold"></div>
          
          {/* Stylized Lord Jagannath Eye Logo */}
          <div className="flex justify-center mb-6">
            <div className="flex items-center gap-1.5 p-2 bg-brand-terracotta/20 border border-brand-marigold/30 rounded-2xl">
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

          <div className="mb-6">
            <h3 className="text-2xl font-black text-slate-100">{t.secure_gateway}</h3>
            <p className="text-slate-400 text-xs mt-1">
              {language === 'or' ? 'ଆପଣଙ୍କର ବିବରଣୀ ସୁରକ୍ଷିତ ଭାବରେ ଯାଞ୍ଚ କରନ୍ତୁ।' : 'Verify your credentials to unlock application access.'}
            </p>
          </div>

          {error && (
            <div className="mb-6 p-4 rounded-xl bg-brand-terracotta/20 border border-brand-terracotta/40 text-xs text-brand-marigold font-bold">
              {error}
            </div>
          )}

          {/* STEP 1: Basic credentials and Captcha */}
          {step === 1 && (
            <form onSubmit={handleStep1Submit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center justify-between">
                  <span>{t.phone}</span>
                  <span>📱</span>
                </label>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 text-sm font-semibold text-slate-500">+91</span>
                  <input
                    type="tel"
                    required
                    maxLength="10"
                    placeholder="98765 43210"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value.replace(/\D/g, ''))}
                    className="w-full pl-14 pr-4 py-3 rounded-xl bg-slate-950/60 border border-white/5 text-slate-200 text-sm font-medium focus:border-brand-marigold focus:ring-1 focus:ring-brand-marigold/30 focus:outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center justify-between">
                    <span>{t.email}</span>
                    <span>✉️</span>
                  </label>
                  <input
                    type="email"
                    required
                    placeholder="name@email.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl bg-slate-950/60 border border-white/5 text-slate-200 text-sm font-medium focus:border-brand-marigold focus:ring-1 focus:ring-brand-marigold/30 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center justify-between">
                    <span>{t.age}</span>
                    <span>🎂</span>
                  </label>
                  <input
                    type="number"
                    required
                    min="1"
                    max="120"
                    placeholder="Age"
                    value={age}
                    onChange={(e) => setAge(e.target.value.replace(/\D/g, ''))}
                    className="w-full px-4 py-3 rounded-xl bg-slate-950/60 border border-white/5 text-slate-200 text-sm font-medium focus:border-brand-marigold focus:ring-1 focus:ring-brand-marigold/30 focus:outline-none"
                  />
                </div>
              </div>

              {/* Captcha challenge */}
              <div className="p-4 rounded-2xl bg-white/[0.01] border border-white/5 space-y-3">
                <div className="flex items-center justify-between text-xs font-bold text-slate-400">
                  <span>{t.captcha}</span>
                  <span className="text-brand-marigold">Solve equation</span>
                </div>
                <div className="flex items-center gap-4">
                  <div className="px-4 py-2.5 rounded-xl bg-slate-950 font-black text-brand-marigold text-center text-lg select-none border border-white/5 tracking-wider">
                    {captchaNum1} + {captchaNum2} =
                  </div>
                  <input
                    type="number"
                    required
                    placeholder="Answer"
                    value={captchaAnswer}
                    onChange={(e) => setCaptchaAnswer(e.target.value.replace(/\D/g, ''))}
                    className="flex-1 px-4 py-3 rounded-xl bg-slate-950/60 border border-white/5 text-slate-200 text-sm font-bold text-center focus:border-brand-marigold focus:outline-none"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-brand-marigold text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(217,119,6,0.25)] transition-all cursor-pointer"
              >
                {language === 'or' ? 'ତଥ୍ୟ ଯାଞ୍ଚ କରନ୍ତୁ 🚀' : 'VERIFY CITIZEN KYC 🚀'}
              </button>
            </form>
          )}

          {/* STEP 2: Aadhaar Card input */}
          {step === 2 && (
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center justify-between">
                  <span>{t.aadhaar}</span>
                  <span>🛡️</span>
                </label>
                <input
                  type="text"
                  maxLength="12"
                  placeholder="1234 5678 9012"
                  value={aadhaarNum}
                  onChange={(e) => setAadhaarNum(e.target.value.replace(/\D/g, ''))}
                  className="w-full px-4 py-3 rounded-xl bg-slate-950/60 border border-white/5 text-slate-200 text-sm font-bold text-center tracking-widest focus:border-brand-marigold focus:outline-none"
                />
              </div>

              <button
                onClick={handleAadhaarVerify}
                disabled={aadhaarLoading}
                className="w-full py-3.5 rounded-xl bg-brand-marigold text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(217,119,6,0.25)] transition-all flex items-center justify-center cursor-pointer"
              >
                {aadhaarLoading ? (
                  <div className="w-5 h-5 border-2 border-slate-950 border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  language === 'or' ? 'ଆଧାର ଓଟିପି ପଠାନ୍ତୁ' : 'REQUEST AADHAAR OTP'
                )}
              </button>
            </div>
          )}

          {/* STEP 2.5: Aadhaar OTP code */}
          {step === 2.5 && (
            <form onSubmit={handleAadhaarOtpSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center justify-between">
                  <span>{t.aadhaar_otp}</span>
                  <span>💬</span>
                </label>
                <p className="text-[11px] text-slate-400 mb-2 leading-relaxed">
                  {language === 'or'
                    ? `ଆପଣଙ୍କ ଆଧାର ସଂଯୁକ୍ତ ମୋବାଇଲ୍ ନମ୍ବର +91 ******${phone.slice(-4) || 'XXXX'} କୁ ପଠାଯାଇଥିବା ଓଟିପି ଦିଅନ୍ତୁ।`
                    : `Please enter the 6-digit OTP sent to your Aadhaar-linked phone number +91 ******${phone.slice(-4) || 'XXXX'}.`
                  }
                </p>
                <input
                  type="text"
                  maxLength="6"
                  placeholder="Enter 6-digit OTP code"
                  value={aadhaarOtp}
                  onChange={(e) => setAadhaarOtp(e.target.value.replace(/\D/g, ''))}
                  className="w-full px-4 py-3 rounded-xl bg-slate-950/60 border border-white/5 text-slate-200 text-sm font-bold text-center tracking-widest focus:border-brand-marigold focus:outline-none"
                />
                <p className="text-[10px] text-brand-marigold mt-2 text-center">Hint for testing: Type **{simulatedAadhaarOtpCode}**</p>
              </div>

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-brand-marigold text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(217,119,6,0.25)] transition-all cursor-pointer"
              >
                {t.confirm}
              </button>
            </form>
          )}

          {/* STEP 3: Email OTP verification */}
          {step === 3 && (
            <form onSubmit={handleEmailCodeSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center justify-between">
                  <span>{t.email_code}</span>
                  <span>✉️</span>
                </label>
                <input
                  type="text"
                  maxLength="6"
                  placeholder="Enter 6-digit code"
                  value={emailCode}
                  onChange={(e) => setEmailCode(e.target.value.replace(/\D/g, ''))}
                  className="w-full px-4 py-3 rounded-xl bg-slate-950/60 border border-white/5 text-slate-200 text-sm font-bold text-center tracking-widest focus:border-brand-marigold focus:outline-none"
                />
                <p className="text-[10px] text-brand-marigold mt-2 text-center">Hint for testing: Type **{simulatedEmailCode}**</p>
              </div>

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-brand-marigold text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(217,119,6,0.25)] transition-all cursor-pointer"
              >
                {t.confirm}
              </button>
            </form>
          )}

          {/* STEP 4: Access PIN Pad */}
          {step === 4 && (
            <form onSubmit={handlePinSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center justify-between">
                  <span>{t.pin}</span>
                  <span>🔑</span>
                </label>
                <div className="relative">
                  <input
                    type={showPin ? 'text' : 'password'}
                    maxLength="4"
                    placeholder="••••"
                    value={pin}
                    onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
                    className="w-full px-4 py-3 rounded-xl bg-slate-950/60 border border-white/5 text-slate-200 text-sm font-bold text-center tracking-widest focus:border-brand-marigold focus:outline-none"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPin(!showPin)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
                  >
                    {showPin ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full py-3.5 rounded-xl bg-gradient-to-r from-brand-terracotta to-brand-marigold text-slate-950 font-black text-sm hover:opacity-95 shadow-[0_4px_20px_rgba(217,119,6,0.25)] transition-all flex items-center justify-center cursor-pointer"
              >
                {isSubmitting ? (
                  <div className="w-5 h-5 border-2 border-slate-950 border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  'UNLOCK ECOSYSYTEM 🏛️'
                )}
              </button>
            </form>
          )}

        </div>
      </main>

      {/* Footer */}
      <footer className="px-6 py-6 border-t border-white/5 text-center text-xs text-slate-600 bg-slate-950/20 z-10">
        © 2026 Odisha Smart City Infrastructure Commission. All rights reserved.
      </footer>
    </div>
  );
};

export default WelcomeScreen;
