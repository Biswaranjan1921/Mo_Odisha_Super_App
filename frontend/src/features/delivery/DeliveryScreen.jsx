import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import apiClient from '../../core/api/apiClient';
import { ArrowLeft, Truck, Package, MapPin, CheckCircle, Volume2, AlertTriangle, Navigation } from 'lucide-react';

const MOCK_DELIVERIES = [
  {
    id: 'dlv-101',
    orderId: 'ord-8801',
    status: 'ASSIGNED',
    pickupAddress: {
      addressLine1: 'Patra Groceries & Staples, Unit 1 Market',
      city: 'Bhubaneswar',
      state: 'Odisha',
      pincode: '751001'
    },
    dropoffAddress: {
      addressLine1: 'Plot 102, Saheed Nagar',
      city: 'Bhubaneswar',
      state: 'Odisha',
      pincode: '751007'
    },
    deliveryFee: 40.00,
    items: [
      { productName: 'Odia Premium Aromatic Rice 5kg', quantity: 2 },
      { productName: 'Pure Organic Mustard Oil 1L', quantity: 1 }
    ],
    createdAt: '2026-09-08T20:00:00Z'
  }
];

const DeliveryScreen = () => {
  const { elderlyMode, speak } = useAuth();
  const navigate = useNavigate();
  const [deliveries, setDeliveries] = useState(MOCK_DELIVERIES);
  const [loading, setLoading] = useState(false);
  const [actionError, setActionError] = useState(null);

  const fetchDeliveries = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get('/deliveries/assigned?page=0&size=20');
      if (response.data?.content && response.data.content.length > 0) {
        setDeliveries(response.data.content);
      }
    } catch (err) {
      console.warn('API connection fallback to active mock delivery worksheet:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDeliveries();
  }, []);

  const handleUpdateStatus = async (deliveryId, nextStatus) => {
    setActionError(null);
    try {
      const response = await apiClient.put(`/deliveries/${deliveryId}/status`, {
        status: nextStatus
      });
      
      setDeliveries((prev) => prev.map((dlv) => dlv.id === deliveryId ? response.data : dlv));

      if (nextStatus === 'PICKED_UP') {
        speak('Package picked up. Delivery is now in transit.', 'ପ୍ୟାକେଜ୍ ନିଆଗଲା। ଡେଲିଭରି ବର୍ତ୍ତମାନ ବାଟରେ ଅଛି।');
      } else if (nextStatus === 'IN_TRANSIT') {
        speak('Parcel is in transit to customer dropoff address.', 'ପାର୍ସଲ ଗ୍ରାହକଙ୍କ ସଠିକ ଠିକଣାକୁ ଯାଉଛି।');
      } else if (nextStatus === 'DELIVERED') {
        speak('Package delivered successfully.', 'ପ୍ୟାକେଜ୍ ସଫଳତାର ସହିତ ବିତରଣ କରାଗଲା।');
      }
    } catch (err) {
      // Local optimistic fallback for demo UI if backend unreachable
      setDeliveries((prev) => prev.map((dlv) => dlv.id === deliveryId ? { ...dlv, status: nextStatus } : dlv));
      const errMsg = err.response?.data?.message || 'Status updated locally.';
      setActionError(errMsg);
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between bg-slate-950 text-slate-100">
      {/* Header */}
      <header className="px-6 py-4 flex items-center justify-between border-b border-white/5 bg-slate-950/75 backdrop-blur-xl sticky top-0 z-40">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/dashboard')}
            className="p-2 rounded-xl bg-white/5 border border-white/10 text-slate-300 cursor-pointer hover:bg-white/10"
          >
            <ArrowLeft className={elderlyMode ? 'w-6 h-6' : 'w-4 h-4'} />
          </button>
          <div>
            <h1 className={`font-black text-slate-100 ${elderlyMode ? 'text-2xl' : 'text-lg'}`}>
              Delivery Partner Worksheet
            </h1>
            <p className="text-[9px] text-emerald-400 font-semibold uppercase tracking-wider">
              Hyperlocal Parcel Lifecycle & Handover
            </p>
          </div>
        </div>

        <button 
          onClick={() => speak('Delivery Agent Dashboard. Pickup parcels from local vendors and log coordinate handovers.', 'ଡେଲିଭରି ଏଜେଣ୍ଟ ଡ୍ୟାସବୋର୍ଡ।')}
          className="p-2.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 cursor-pointer animate-pulse"
        >
          <Volume2 className="w-5 h-5" />
        </button>
      </header>

      {/* Main Grid */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 text-left space-y-6">
        <div className="rounded-3xl bg-slate-900/60 p-6 border border-white/10 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-emerald-500/15 border border-emerald-500/25 text-emerald-400 rounded-2xl">
              <Truck className="w-8 h-8" />
            </div>
            <div>
              <h2 className="text-lg font-black text-slate-200">Active Delivery Worksheets</h2>
              <p className="text-xs text-slate-400">Track assigned parcels, store pickup snapshots, and customer dropoff handovers.</p>
            </div>
          </div>
        </div>

        {actionError && (
          <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-300 text-xs font-semibold flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-amber-400 flex-shrink-0" />
            <span>{actionError}</span>
          </div>
        )}

        {loading ? (
          <div className="py-20 text-center text-slate-500 space-y-3">
            <div className="animate-spin rounded-full h-10 w-10 border-4 border-emerald-500 border-t-transparent mx-auto"></div>
            <p className="text-sm font-semibold">Loading assigned delivery worksheets...</p>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 gap-6">
            {deliveries.length === 0 ? (
              <div className="md:col-span-2 text-center py-16 text-slate-500 bg-slate-900/40 rounded-3xl border border-white/5 space-y-2">
                <Package className="w-14 h-14 mx-auto mb-2 opacity-30" />
                <p className="text-sm font-bold">No active packages assigned to deliver</p>
                <p className="text-xs">Place an order in the Shopping screen to generate paid delivery assignments.</p>
              </div>
            ) : (
              deliveries.map((dlv) => (
                <div key={dlv.id} className="p-6 rounded-3xl bg-slate-900/80 border border-white/10 space-y-4 shadow-xl">
                  
                  {/* Worksheet Header */}
                  <div className="flex justify-between items-start border-b border-white/10 pb-3">
                    <div>
                      <h3 className="font-extrabold text-slate-200 text-sm">Delivery ID: {dlv.id?.substring(0, 16)}...</h3>
                      <p className="text-[10px] text-slate-400 mt-1">Order Ref: <span className="font-mono text-emerald-400">{dlv.orderId?.substring(0, 16)}...</span></p>
                    </div>
                    <span className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-wider ${
                      dlv.status === 'ASSIGNED' 
                        ? 'bg-amber-500/10 border border-amber-500/30 text-amber-400'
                        : dlv.status === 'PICKED_UP'
                          ? 'bg-blue-500/10 border border-blue-500/30 text-blue-400 animate-pulse'
                          : dlv.status === 'IN_TRANSIT'
                            ? 'bg-purple-500/10 border border-purple-500/30 text-purple-400 animate-pulse'
                            : dlv.status === 'DELIVERED'
                              ? 'bg-emerald-500/10 border border-emerald-500/30 text-emerald-400'
                              : 'bg-red-500/10 border border-red-500/30 text-red-400'
                    }`}>
                      {dlv.status}
                    </span>
                  </div>

                  {/* Dual Address Snapshots */}
                  <div className="space-y-3 text-xs text-slate-300">
                    <div className="flex items-start gap-2 bg-slate-950/60 p-3 rounded-2xl border border-white/5">
                      <MapPin className="w-4 h-4 text-blue-400 shrink-0 mt-0.5" />
                      <div>
                        <p className="font-bold text-blue-300 text-[11px] uppercase tracking-wider">Pickup Location (Store Snapshot)</p>
                        <p className="text-slate-300 mt-0.5">{dlv.pickupAddress?.addressLine1}, {dlv.pickupAddress?.city} - {dlv.pickupAddress?.pincode}</p>
                      </div>
                    </div>

                    <div className="flex items-start gap-2 bg-slate-950/60 p-3 rounded-2xl border border-white/5">
                      <MapPin className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                      <div>
                        <p className="font-bold text-emerald-300 text-[11px] uppercase tracking-wider">Dropoff Location (Customer Snapshot)</p>
                        <p className="text-slate-300 mt-0.5">{dlv.dropoffAddress?.addressLine1}, {dlv.dropoffAddress?.city} - {dlv.dropoffAddress?.pincode}</p>
                      </div>
                    </div>
                  </div>

                  {/* Items List */}
                  {dlv.items && dlv.items.length > 0 && (
                    <div className="border-t border-white/10 pt-3">
                      <h4 className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">Package Contents</h4>
                      <ul className="text-xs text-slate-300 space-y-1">
                        {dlv.items.map((item, idx) => (
                          <li key={idx} className="flex justify-between">
                            <span>{item.productName}</span>
                            <span className="font-bold text-emerald-400">x{item.quantity}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {/* Action Buttons with Explicit Transition Matrix */}
                  <div className="pt-3 border-t border-white/10 flex gap-3">
                    {dlv.status === 'ASSIGNED' && (
                      <button
                        onClick={() => handleUpdateStatus(dlv.id, 'PICKED_UP')}
                        className="flex-1 py-3 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-black text-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer shadow-lg shadow-blue-600/20"
                      >
                        <Truck className="w-4 h-4" />
                        PICK UP PARCEL
                      </button>
                    )}

                    {dlv.status === 'PICKED_UP' && (
                      <button
                        onClick={() => handleUpdateStatus(dlv.id, 'IN_TRANSIT')}
                        className="flex-1 py-3 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-black text-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer shadow-lg shadow-purple-600/20"
                      >
                        <Navigation className="w-4 h-4" />
                        MARK IN TRANSIT
                      </button>
                    )}

                    {dlv.status === 'IN_TRANSIT' && (
                      <button
                        onClick={() => handleUpdateStatus(dlv.id, 'DELIVERED')}
                        className="flex-1 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-black text-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer shadow-lg shadow-emerald-600/20"
                      >
                        <CheckCircle className="w-4 h-4" />
                        MARK DELIVERED
                      </button>
                    )}

                    {dlv.status === 'DELIVERED' && (
                      <div className="flex-1 text-center py-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-bold flex items-center justify-center gap-1.5">
                        <CheckCircle className="w-4 h-4" />
                        DELIVERY COMPLETED
                      </div>
                    )}
                  </div>

                </div>
              ))
            )}
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="px-6 py-4 border-t border-white/5 text-center text-xs text-slate-600 bg-slate-950/20">
        © 2026 Odisha Smart City Infrastructure Commission. All rights reserved.
      </footer>
    </div>
  );
};

export default DeliveryScreen;
