import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import apiClient from '../../core/api/apiClient';
import { 
  ArrowLeft, Compass, MapPin, Star, Award, ShieldCheck, 
  Calendar, Clock, DollarSign, Volume2, UserCheck, Image, Plus, 
  FileText, CheckCircle2, AlertCircle, Eye, Edit3, X, Sparkles, Send 
} from 'lucide-react';

const TourismScreen = () => {
  const { elderlyMode, speak, user } = useAuth();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('DESTINATIONS'); // DESTINATIONS | GUIDES | MY_PROFILE | APPLY
  const [categoryFilter, setCategoryFilter] = useState('');
  
  const [places, setPlaces] = useState([]);
  const [guides, setGuides] = useState([]);
  const [myGuideProfile, setMyGuideProfile] = useState(null);
  const [publicProfile, setPublicProfile] = useState(null);
  const [myBookings, setMyBookings] = useState([]);

  // Guide Application State
  const [licenseNumber, setLicenseNumber] = useState('');
  const [experienceYears, setExperienceYears] = useState(3);
  const [languagesSpoken, setLanguagesSpoken] = useState('Odia, English, Hindi');

  // Guide Profile Edit State
  const [headline, setHeadline] = useState('');
  const [aboutMe, setAboutMe] = useState('');
  const [profileImg, setProfileImg] = useState('');
  const [coverImg, setCoverImg] = useState('');
  const [hourlyRate, setHourlyRate] = useState(1000);
  const [specialization, setSpecialization] = useState('Temple Tours, Heritage Sites');

  // Gallery Upload
  const [galleryImgUrl, setGalleryImgUrl] = useState('');
  const [galleryCaption, setGalleryCaption] = useState('');

  // Guide Booking Modal
  const [selectedGuideForBooking, setSelectedGuideForBooking] = useState(null);
  const [bookingDate, setBookingDate] = useState('');
  const [bookingHours, setBookingHours] = useState(4);

  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    fetchPlaces();
    fetchGuides();
    fetchMyBookings();
    fetchMyGuideProfile();
  }, []);

  const fetchPlaces = async () => {
    try {
      const url = categoryFilter ? `/tourism/places?category=${categoryFilter}` : '/tourism/places';
      const res = await apiClient.get(url);
      setPlaces(res.data || []);
    } catch (err) {
      console.warn('Could not fetch tourism places:', err);
    }
  };

  const fetchGuides = async () => {
    try {
      const res = await apiClient.get('/tourism/guides/verified');
      setGuides(res.data || []);
    } catch (err) {
      console.warn('Could not fetch verified guides:', err);
    }
  };

  const fetchMyBookings = async () => {
    try {
      const res = await apiClient.get('/tourism/bookings/me');
      setMyBookings(res.data || []);
    } catch (err) {
      console.warn('Could not fetch guide bookings:', err);
    }
  };

  const fetchMyGuideProfile = async () => {
    try {
      const res = await apiClient.get('/tourism/guides/me');
      if (res.data) {
        setMyGuideProfile(res.data);
        setHeadline(res.data.professionalHeadline || '');
        setAboutMe(res.data.aboutMe || '');
        setProfileImg(res.data.profileImageUrl || '');
        setCoverImg(res.data.coverImageUrl || '');
        setHourlyRate(res.data.hourlyRate || 1000);
        setSpecialization(res.data.specialization || '');
      }
    } catch (err) {
      // User is not yet a certified guide
      setMyGuideProfile(null);
    }
  };

  const handleApplyGuide = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg('');
    try {
      await apiClient.post('/tourism/guides/apply', {
        licenseNumber,
        experienceYears,
        languagesSpoken
      });
      setSuccessMsg('Guide application submitted successfully! Pending Tourism Authority approval.');
      speak('Guide application submitted successfully.', 'ଆପଣଙ୍କର ଗାଇଡ୍ ଆବେଦନ ସଫଳତାର ସହିତ ଦାଖଲ ହୋଇଛି।');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to submit guide application');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateGuideProfile = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await apiClient.put('/tourism/guides/me', {
        professionalHeadline: headline,
        aboutMe,
        profileImageUrl: profileImg,
        coverImageUrl: coverImg,
        hourlyRate,
        specialization
      });
      setMyGuideProfile(res.data);
      setSuccessMsg('Guide profile updated successfully!');
      speak('Guide profile updated.', 'ଗାଇଡ୍ ପ୍ରୋଫାଇଲ୍ ଅପଡେଟ୍ ହୋଇଛି।');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to update guide profile');
    } finally {
      setLoading(false);
    }
  };

  const handleAddGalleryImage = async (e) => {
    e.preventDefault();
    if (!galleryImgUrl) return;
    try {
      await apiClient.post('/tourism/guides/me/gallery', {
        imageUrl: galleryImgUrl,
        caption: galleryCaption,
        isCover: false
      });
      setGalleryImgUrl('');
      setGalleryCaption('');
      setSuccessMsg('Gallery photo uploaded!');
      if (myGuideProfile) handlePreviewPublic(myGuideProfile.id);
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to upload photo');
    }
  };

  const handlePreviewPublic = async (guideId) => {
    try {
      const res = await apiClient.get(`/tourism/guides/${guideId}/public`);
      setPublicProfile(res.data);
    } catch (err) {
      console.warn('Could not load public guide profile:', err);
    }
  };

  const handleBookGuide = async (e) => {
    e.preventDefault();
    if (!selectedGuideForBooking || !bookingDate) return;
    setLoading(true);
    setErrorMsg('');
    try {
      await apiClient.post('/tourism/guides/book', {
        guideId: selectedGuideForBooking.id,
        bookingDate,
        durationHours: bookingHours
      });
      setSelectedGuideForBooking(null);
      setSuccessMsg('Certified tour guide booked successfully!');
      fetchMyBookings();
      speak('Tour guide booked successfully.', 'ଟୁର୍ ଗାଇଡ୍ ସଫଳତାର ସହିତ ବୁକ୍ ହୋଇଛି।');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Guide booking failed or date slot taken');
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
              Odisha Tourism & Heritage
            </h1>
            <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
              Explore Destinations, Book Certified Guides & Public Transit
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button 
            onClick={() => speak('Tourism and Heritage Portal. Explore heritage destinations and book certified tour guides.', 'ଓଡ଼ିଶା ପର୍ଯ୍ୟଟନ ଏବଂ ଐତିହ୍ୟ ସେବା।')}
            className="p-2.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue cursor-pointer"
          >
            <Volume2 className="w-5 h-5" />
          </button>
        </div>
      </header>

      {/* Navigation Tabs */}
      <div className="px-6 pt-4 max-w-7xl mx-auto w-full flex items-center gap-3 border-b border-white/5 pb-3">
        <button
          onClick={() => { setActiveTab('DESTINATIONS'); fetchPlaces(); }}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
            activeTab === 'DESTINATIONS' ? 'bg-brand-emerald text-slate-950 shadow-lg' : 'bg-white/5 text-slate-400 border border-white/10'
          }`}
        >
          Destinations
        </button>

        <button
          onClick={() => { setActiveTab('GUIDES'); fetchGuides(); }}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
            activeTab === 'GUIDES' ? 'bg-brand-emerald text-slate-950 shadow-lg' : 'bg-white/5 text-slate-400 border border-white/10'
          }`}
        >
          Verified Tour Guides
        </button>

        {myGuideProfile ? (
          <button
            onClick={() => { setActiveTab('MY_PROFILE'); handlePreviewPublic(myGuideProfile.id); }}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'MY_PROFILE' ? 'bg-brand-blue text-white shadow-lg' : 'bg-white/5 text-slate-400 border border-white/10'
            }`}
          >
            <UserCheck className="w-3.5 h-3.5" />
            My Guide Profile
          </button>
        ) : (
          <button
            onClick={() => setActiveTab('APPLY')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
              activeTab === 'APPLY' ? 'bg-brand-amber text-slate-950 shadow-lg' : 'bg-white/5 text-slate-400 border border-white/10'
            }`}
          >
            Apply as Tour Guide
          </button>
        )}
      </div>

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
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-6">

        {/* TAB 1: DESTINATIONS */}
        {activeTab === 'DESTINATIONS' && (
          <div className="space-y-6">
            <div className="flex items-center gap-2 overflow-x-auto pb-2">
              {['', 'HERITAGE', 'TEMPLE', 'NATURE', 'BEACH'].map((cat) => (
                <button
                  key={cat}
                  onClick={() => { setCategoryFilter(cat); fetchPlaces(); }}
                  className={`px-3 py-1.5 rounded-xl border text-xs font-bold cursor-pointer ${
                    categoryFilter === cat ? 'bg-brand-emerald/20 border-brand-emerald text-brand-emerald' : 'bg-white/5 border-white/10 text-slate-400'
                  }`}
                >
                  {cat || 'ALL CATEGORIES'}
                </button>
              ))}
            </div>

            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {places.map((place) => (
                <div key={place.id} className="rounded-3xl glass-card border border-white/5 overflow-hidden flex flex-col justify-between">
                  <div className="h-44 bg-slate-900 relative">
                    <img 
                      src={place.imageUrl || 'https://images.unsplash.com/photo-1590050752117-238cb0fb12b1?w=800'} 
                      alt={place.name} 
                      className="w-full h-full object-cover" 
                    />
                    <span className="absolute top-3 right-3 px-3 py-1 rounded-full bg-slate-950/80 border border-white/10 text-[10px] font-bold text-brand-emerald uppercase">
                      {place.category}
                    </span>
                  </div>

                  <div className="p-5 space-y-3">
                    <h3 className="font-extrabold text-slate-100 text-base">{place.name}</h3>
                    <p className="text-xs text-slate-400 line-clamp-2">{place.descriptionEn}</p>

                    <div className="flex items-center justify-between text-xs text-slate-400 border-t border-white/5 pt-3">
                      <span className="flex items-center gap-1">
                        <Clock className="w-3.5 h-3.5 text-brand-blue" />
                        {place.averageVisitDurationMinutes} mins visit
                      </span>
                      <span className="font-bold text-brand-amber">
                        ₹{place.entryFee} entry
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* TAB 2: VERIFIED TOUR GUIDES */}
        {activeTab === 'GUIDES' && (
          <div className="space-y-6">
            <h2 className="text-sm font-extrabold text-slate-200 uppercase tracking-wider">
              State Certified Tour Guides
            </h2>

            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {guides.map((guide) => (
                <div key={guide.id} className="p-6 rounded-3xl glass-card border border-white/5 space-y-4 flex flex-col justify-between">
                  <div className="flex items-start gap-4">
                    <div className="w-16 h-16 rounded-2xl bg-slate-800 border border-white/10 overflow-hidden shrink-0">
                      <img 
                        src={guide.profileImageUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'} 
                        alt={guide.fullName} 
                        className="w-full h-full object-cover" 
                      />
                    </div>
                    <div>
                      <div className="flex items-center gap-1.5">
                        <h3 className="font-bold text-slate-100 text-sm">{guide.fullName}</h3>
                        <ShieldCheck className="w-4 h-4 text-brand-emerald shrink-0" />
                      </div>
                      <p className="text-xs text-slate-400">{guide.professionalHeadline || 'Certified Odisha Tour Guide'}</p>
                      <span className="inline-block mt-1 text-[10px] font-bold px-2 py-0.5 rounded bg-brand-emerald/15 text-brand-emerald border border-brand-emerald/30">
                        VERIFIED GUIDE
                      </span>
                    </div>
                  </div>

                  <p className="text-xs text-slate-400 line-clamp-2">{guide.aboutMe || 'Experienced local guide providing immersive walks through temples, coastal spots, and heritage monuments.'}</p>

                  <div className="space-y-2 text-xs text-slate-400 border-t border-white/5 pt-3">
                    <div className="flex justify-between">
                      <span>Languages:</span>
                      <span className="font-semibold text-slate-200">{guide.languagesSpoken}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Hourly Rate:</span>
                      <span className="font-black text-brand-amber text-sm">₹{guide.hourlyRate} / hr</span>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-2 pt-2">
                    <button
                      onClick={() => handlePreviewPublic(guide.id)}
                      className="py-2.5 rounded-xl bg-white/5 border border-white/10 text-slate-300 text-xs font-bold hover:bg-white/10 cursor-pointer flex items-center justify-center gap-1.5"
                    >
                      <Eye className="w-3.5 h-3.5" />
                      View Profile
                    </button>

                    <button
                      onClick={() => setSelectedGuideForBooking(guide)}
                      className="py-2.5 rounded-xl bg-brand-emerald text-slate-950 text-xs font-black hover:opacity-90 cursor-pointer flex items-center justify-center gap-1.5"
                    >
                      <Calendar className="w-3.5 h-3.5" />
                      Book Guide
                    </button>
                  </div>
                </div>
              ))}
            </div>

            {/* Tourist Bookings Section */}
            {myBookings.length > 0 && (
              <div className="mt-8 p-6 rounded-3xl glass-card border border-white/5 space-y-4">
                <h3 className="font-bold text-slate-200 text-xs uppercase tracking-wider">
                  My Active Guide Reservations
                </h3>
                <div className="space-y-3">
                  {myBookings.map(b => (
                    <div key={b.id} className="p-4 rounded-2xl bg-white/[0.02] border border-white/5 flex items-center justify-between text-xs">
                      <div>
                        <p className="font-bold text-slate-100">{b.guideFullName}</p>
                        <p className="text-slate-400 mt-0.5">Date: {b.bookingDate} ({b.durationHours} hours)</p>
                      </div>
                      <div className="text-right">
                        <span className="px-2.5 py-1 rounded text-[10px] font-bold bg-brand-emerald/15 text-brand-emerald border border-brand-emerald/30">
                          {b.status}
                        </span>
                        <p className="font-black text-slate-200 mt-1">₹{b.totalFee}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 3: MY GUIDE PROFILE SETUP (For Approved Guides) */}
        {activeTab === 'MY_PROFILE' && myGuideProfile && (
          <div className="grid lg:grid-cols-12 gap-8 items-start">
            
            {/* Left: Edit Profile Form */}
            <div className="lg:col-span-6 p-6 rounded-3xl glass-card border border-white/5 space-y-4">
              <h2 className="font-extrabold text-slate-200 text-sm border-b border-white/5 pb-3 flex items-center gap-2">
                <Edit3 className="w-4 h-4 text-brand-blue" />
                Edit Guide Profile
              </h2>

              <form onSubmit={handleUpdateGuideProfile} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Professional Headline</label>
                  <input
                    type="text"
                    value={headline}
                    onChange={(e) => setHeadline(e.target.value)}
                    placeholder="e.g. Certified Odisha Heritage Tour Guide"
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-blue"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">About Me</label>
                  <textarea
                    rows="3"
                    value={aboutMe}
                    onChange={(e) => setAboutMe(e.target.value)}
                    placeholder="Describe your tour guide experience and expertise..."
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-blue"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Profile Image URL</label>
                    <input
                      type="text"
                      value={profileImg}
                      onChange={(e) => setProfileImg(e.target.value)}
                      placeholder="https://..."
                      className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-blue"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Hourly Rate (₹)</label>
                    <input
                      type="number"
                      value={hourlyRate}
                      onChange={(e) => setHourlyRate(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-blue"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Specialization</label>
                  <input
                    type="text"
                    value={specialization}
                    onChange={(e) => setSpecialization(e.target.value)}
                    placeholder="e.g. Temple Tours, Heritage Sites, Wildlife"
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-blue"
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-3 rounded-xl bg-brand-blue text-white font-black text-xs uppercase tracking-wider hover:opacity-90 transition-all cursor-pointer"
                >
                  SAVE PROFILE CHANGES
                </button>
              </form>

              {/* Upload Gallery Photo */}
              <div className="pt-4 border-t border-white/5 space-y-3">
                <h3 className="font-bold text-xs text-slate-300 uppercase tracking-wider">Add Photo to Gallery</h3>
                <form onSubmit={handleAddGalleryImage} className="space-y-3">
                  <input
                    type="text"
                    required
                    placeholder="Photo Image URL (https://...)"
                    value={galleryImgUrl}
                    onChange={(e) => setGalleryImgUrl(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs"
                  />
                  <input
                    type="text"
                    placeholder="Caption (e.g. Temple walk with tourists)"
                    value={galleryCaption}
                    onChange={(e) => setGalleryCaption(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs"
                  />
                  <button
                    type="submit"
                    className="w-full py-2.5 rounded-xl bg-white/5 border border-white/10 text-slate-200 font-bold text-xs hover:bg-white/10 flex items-center justify-center gap-1.5 cursor-pointer"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    Upload Photo
                  </button>
                </form>
              </div>
            </div>

            {/* Right: Public Profile Live Preview */}
            <div className="lg:col-span-6 space-y-4">
              <h2 className="font-extrabold text-slate-200 text-sm flex items-center gap-2">
                <Eye className="w-4 h-4 text-brand-emerald" />
                Live Public Profile Preview
              </h2>

              {publicProfile && (
                <div className="p-6 rounded-3xl glass-card border border-white/10 space-y-6">
                  <div className="flex items-center gap-4">
                    <div className="w-20 h-20 rounded-full border-2 border-brand-emerald overflow-hidden shrink-0">
                      <img src={publicProfile.profileImageUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200'} alt="Profile" className="w-full h-full object-cover" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="font-black text-slate-100 text-base">{publicProfile.fullName}</h3>
                        <span className="px-2 py-0.5 rounded text-[9px] font-bold bg-brand-emerald/20 text-brand-emerald border border-brand-emerald/30">
                          VERIFIED GUIDE
                        </span>
                      </div>
                      <p className="text-xs text-slate-400 mt-0.5">{publicProfile.professionalHeadline || 'Certified Odisha Guide'}</p>
                      <p className="text-xs font-black text-brand-amber mt-1">₹{publicProfile.hourlyRate} / hour</p>
                    </div>
                  </div>

                  <div>
                    <h4 className="font-bold text-xs text-slate-300 uppercase tracking-wider mb-1">About Me</h4>
                    <p className="text-xs text-slate-400">{publicProfile.aboutMe || 'No description provided yet.'}</p>
                  </div>

                  {publicProfile.gallery?.length > 0 && (
                    <div>
                      <h4 className="font-bold text-xs text-slate-300 uppercase tracking-wider mb-2">Photo Gallery</h4>
                      <div className="grid grid-cols-3 gap-2">
                        {publicProfile.gallery.map(img => (
                          <div key={img.id} className="h-20 rounded-xl bg-slate-900 border border-white/10 overflow-hidden">
                            <img src={img.imageUrl} alt={img.caption} className="w-full h-full object-cover" />
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

          </div>
        )}

        {/* TAB 4: APPLY AS TOUR GUIDE */}
        {activeTab === 'APPLY' && !myGuideProfile && (
          <div className="max-w-2xl mx-auto p-8 rounded-3xl glass-card border border-white/5 space-y-6">
            <div>
              <h2 className="text-lg font-black text-slate-100">Apply for Certified Tour Guide License</h2>
              <p className="text-xs text-slate-400 mt-1">Register your credentials with the Odisha Tourism Authority to guide visitors across state heritage sites.</p>
            </div>

            <form onSubmit={handleApplyGuide} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">License / Registration Number</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. OTDC-GUIDE-2026-99"
                  value={licenseNumber}
                  onChange={(e) => setLicenseNumber(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-amber"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Experience (Years)</label>
                  <input
                    type="number"
                    required
                    min="0"
                    value={experienceYears}
                    onChange={(e) => setExperienceYears(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-amber"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Languages Spoken</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Odia, English, Hindi"
                    value={languagesSpoken}
                    onChange={(e) => setLanguagesSpoken(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-amber"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3.5 rounded-xl bg-brand-amber text-slate-950 font-black text-xs uppercase tracking-wider hover:opacity-95 shadow-lg transition-all cursor-pointer flex items-center justify-center gap-2"
              >
                <Send className="w-4 h-4" />
                SUBMIT OFFICIAL APPLICATION
              </button>
            </form>
          </div>
        )}

      </main>

      {/* Guide Booking Modal */}
      {selectedGuideForBooking && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <div className="max-w-md w-full p-6 rounded-3xl glass-card border border-white/10 space-y-4">
            <div className="flex items-center justify-between border-b border-white/5 pb-3">
              <h3 className="font-bold text-slate-100 text-sm">Book {selectedGuideForBooking.fullName}</h3>
              <button onClick={() => setSelectedGuideForBooking(null)}><X className="w-4 h-4 text-slate-400" /></button>
            </div>

            <form onSubmit={handleBookGuide} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Tour Date</label>
                <input
                  type="date"
                  required
                  value={bookingDate}
                  onChange={(e) => setBookingDate(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-emerald"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Duration (Hours)</label>
                <input
                  type="number"
                  required
                  min="1"
                  max="12"
                  value={bookingHours}
                  onChange={(e) => setBookingHours(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-white/10 text-slate-200 text-xs focus:outline-none focus:border-brand-emerald"
                />
              </div>

              <div className="p-3 bg-white/[0.02] rounded-xl border border-white/5 flex justify-between text-xs">
                <span>Calculated Total:</span>
                <span className="font-black text-brand-amber">₹{selectedGuideForBooking.hourlyRate * bookingHours}</span>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 rounded-xl bg-brand-emerald text-slate-950 font-black text-xs uppercase tracking-wider hover:opacity-90 cursor-pointer"
              >
                CONFIRM GUIDE BOOKING
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Public Profile View Modal */}
      {publicProfile && (
        <div className="fixed inset-0 bg-slate-950/85 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <div className="max-w-lg w-full p-6 rounded-3xl glass-card border border-white/10 space-y-4 max-h-[85vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-white/5 pb-3">
              <div className="flex items-center gap-2">
                <h3 className="font-bold text-slate-100 text-sm">{publicProfile.fullName}</h3>
                <span className="px-2 py-0.5 rounded text-[9px] font-bold bg-brand-emerald/20 text-brand-emerald border border-brand-emerald/30">
                  VERIFIED
                </span>
              </div>
              <button onClick={() => setPublicProfile(null)}><X className="w-4 h-4 text-slate-400" /></button>
            </div>

            <div className="space-y-4 text-xs">
              <p className="text-slate-300 italic">{publicProfile.professionalHeadline || 'State Certified Tour Guide'}</p>
              <p className="text-slate-400">{publicProfile.aboutMe || 'Experienced local guide.'}</p>
              
              <div className="p-3 bg-white/[0.02] rounded-xl border border-white/5 space-y-1">
                <p><strong>Languages:</strong> {publicProfile.languagesSpoken}</p>
                <p><strong>Specialization:</strong> {publicProfile.specialization || 'Heritage & Temple Tours'}</p>
                <p><strong>Hourly Rate:</strong> ₹{publicProfile.hourlyRate} / hour</p>
              </div>

              {publicProfile.gallery?.length > 0 && (
                <div>
                  <p className="font-bold text-slate-300 mb-2 uppercase tracking-wider text-[10px]">Guide Activity Gallery</p>
                  <div className="grid grid-cols-3 gap-2">
                    {publicProfile.gallery.map(img => (
                      <div key={img.id} className="h-20 rounded-xl bg-slate-900 border border-white/10 overflow-hidden">
                        <img src={img.imageUrl} alt={img.caption} className="w-full h-full object-cover" />
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
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

export default TourismScreen;
