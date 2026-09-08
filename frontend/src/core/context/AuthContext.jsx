import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('ssl_token') || null);
  const [loading, setLoading] = useState(true);
  const [elderlyMode, setElderlyMode] = useState(
    localStorage.getItem('ssl_elderly_mode') === 'true'
  );
  const [language, setLanguageState] = useState(
    localStorage.getItem('ssl_language') || 'en'
  );

  useEffect(() => {
    if (token) {
      const savedUser = localStorage.getItem('ssl_user');
      if (savedUser) {
        setUser(JSON.parse(savedUser));
      } else {
        // Fallback mock user if storage is cleared but token exists
        setUser({
          id: '1',
          name: 'Biswanath Patra',
          phone: '+91 9876543210',
          role: 'CUSTOMER'
        });
      }
    }
    setLoading(false);
  }, [token]);

  const speak = (textEn, textOr, langOverride) => {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const currentLang = langOverride || localStorage.getItem('ssl_language') || 'en';
      const textToSpeak = currentLang === 'or' ? (textOr || textEn) : textEn;
      const utterance = new SpeechSynthesisUtterance(textToSpeak);
      
      const setVoiceAndSpeak = () => {
        if (currentLang === 'or') {
          utterance.lang = 'or-IN';
          const voices = window.speechSynthesis.getVoices();
          const odiaVoice = voices.find(
            v => v.lang.startsWith('or') || v.lang.includes('OR') || v.lang.includes('or-IN')
          );
          if (odiaVoice) {
            utterance.voice = odiaVoice;
          }
        } else {
          utterance.lang = 'en-IN';
        }
        utterance.rate = 0.85;
        window.speechSynthesis.speak(utterance);
      };

      if (window.speechSynthesis.getVoices().length === 0) {
        window.speechSynthesis.onvoiceschanged = () => {
          setVoiceAndSpeak();
          window.speechSynthesis.onvoiceschanged = null;
        };
      } else {
        setVoiceAndSpeak();
      }
    }
  };

  const changeLanguage = (newLang) => {
    setLanguageState(newLang);
    localStorage.setItem('ssl_language', newLang);
    speak(
      'Language set to English.',
      'ଓଡ଼ିଆ ଭାଷା ଚୟନ କରାଗଲା।',
      newLang
    );
  };

  const login = async (phone, password) => {
    setLoading(true);
    try {
      // In real scenario, make API post call to /api/v1/auth/login.
      // We implement a fallback to simulated authentication for high-fidelity UI demonstration.
      const mockUser = {
        id: 'usr_9982736152',
        name: 'Biswanath Patra',
        phone: phone,
        role: 'CUSTOMER'
      };
      
      // Simulate slight networking latency
      await new Promise((resolve) => setTimeout(resolve, 600));

      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mockTokenStateSmartLife';
      setUser(mockUser);
      setToken(mockToken);
      localStorage.setItem('ssl_token', mockToken);
      localStorage.setItem('ssl_user', JSON.stringify(mockUser));
      return { success: true };
    } catch (err) {
      return { success: false, error: err.message || 'Authentication error' };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('ssl_token');
    localStorage.removeItem('ssl_user');
  };

  const toggleElderlyMode = () => {
    const nextMode = !elderlyMode;
    setElderlyMode(nextMode);
    localStorage.setItem('ssl_elderly_mode', String(nextMode));
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      token, 
      loading, 
      login, 
      logout, 
      elderlyMode, 
      toggleElderlyMode,
      language,
      changeLanguage,
      speak
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
