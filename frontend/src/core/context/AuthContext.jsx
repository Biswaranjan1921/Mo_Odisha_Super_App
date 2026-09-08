import React, { createContext, useContext, useState, useEffect } from 'react';
import apiClient from '../api/apiClient';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [profile, setProfile] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('ssl_token') || null);
  const [refreshToken, setRefreshToken] = useState(localStorage.getItem('ssl_refresh_token') || null);
  const [loading, setLoading] = useState(true);
  const [elderlyMode, setElderlyMode] = useState(
    localStorage.getItem('ssl_elderly_mode') === 'true'
  );
  const [language, setLanguageState] = useState(
    localStorage.getItem('ssl_language') || 'en'
  );

  const fetchProfile = async (accessToken) => {
    try {
      const response = await apiClient.get('/users/me', {
        headers: { Authorization: `Bearer ${accessToken}` }
      });
      setProfile(response.data);
      if (response.data.isElderly) {
        setElderlyMode(true);
        localStorage.setItem('ssl_elderly_mode', 'true');
      }
    } catch (err) {
      console.warn('Could not fetch user profile:', err);
    }
  };

  useEffect(() => {
    if (token) {
      const savedUser = localStorage.getItem('ssl_user');
      if (savedUser) {
        setUser(JSON.parse(savedUser));
      }
      fetchProfile(token);
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

  const login = async (emailOrPhone, password) => {
    setLoading(true);
    try {
      const isEmail = emailOrPhone.includes('@');
      const payload = {
        email: isEmail ? emailOrPhone : null,
        phoneNumber: !isEmail ? emailOrPhone : null,
        password: password
      };

      const res = await apiClient.post('/auth/login', payload);
      const data = res.data;

      const userObj = {
        id: data.userId,
        email: data.email,
        role: data.role
      };

      setToken(data.accessToken);
      setRefreshToken(data.refreshToken);
      setUser(userObj);

      localStorage.setItem('ssl_token', data.accessToken);
      localStorage.setItem('ssl_refresh_token', data.refreshToken);
      localStorage.setItem('ssl_user', JSON.stringify(userObj));

      await fetchProfile(data.accessToken);
      return { success: true };
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Authentication failed';
      return { success: false, error: msg };
    } finally {
      setLoading(false);
    }
  };

  const register = async (fullName, email, phoneNumber, password, dateOfBirth) => {
    setLoading(true);
    try {
      const payload = {
        fullName,
        email,
        phoneNumber,
        password,
        dateOfBirth
      };

      const res = await apiClient.post('/auth/register', payload);
      return { success: true, message: res.data.message };
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Registration failed';
      return { success: false, error: msg };
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    try {
      if (refreshToken) {
        await apiClient.post('/auth/logout', { refreshToken });
      }
    } catch (e) {
      // Ignore logout errors
    } finally {
      setUser(null);
      setProfile(null);
      setToken(null);
      setRefreshToken(null);
      localStorage.removeItem('ssl_token');
      localStorage.removeItem('ssl_refresh_token');
      localStorage.removeItem('ssl_user');
    }
  };

  const toggleElderlyMode = () => {
    const nextMode = !elderlyMode;
    setElderlyMode(nextMode);
    localStorage.setItem('ssl_elderly_mode', String(nextMode));
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      profile,
      token, 
      loading, 
      login, 
      register,
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
