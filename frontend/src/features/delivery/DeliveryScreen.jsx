import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { ArrowLeft, Truck, Package, MapPin, CheckCircle, Volume2 } from 'lucide-react';

const DeliveryScreen = () => {
  const { elderlyMode, speak } = useAuth();
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);

  useEffect(() => {
    const saved = localStorage.getItem('ssl_orders');
    if (saved) {
      setOrders(JSON.parse(saved));
    }
  }, []);

  const handleUpdateStatus = (orderId, nextStatus) => {
    const updated = orders.map(ord => {
      if (ord.id === orderId) {
        return { ...ord, status: nextStatus };
      }
      return ord;
    });
    setOrders(updated);
    localStorage.setItem('ssl_orders', JSON.stringify(updated));

    // Speak update out loud
    if (nextStatus === 'PICKED_UP') {
      speak('Package picked up. Delivery is now in transit.', 'ପ୍ୟାକେଜ୍ ନିଆଗଲା। ଡେଲିଭରି ବର୍ତ୍ତମାନ ବାଟରେ ଅଛି।');
    } else if (nextStatus === 'DELIVERED') {
      speak('Package delivered successfully.', 'ପ୍ୟାକେଜ୍ ସଫଳତାର ସହିତ ବିତରଣ କରାଗଲା।');
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between">
      {/* Header */}
      <header className="px-6 py-4 flex items-center justify-between border-b border-white/5 bg-slate-950/75 backdrop-blur-xl">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/dashboard')}
            className="p-2 rounded-xl bg-white/5 border border-white/10 text-slate-300 cursor-pointer"
          >
            <ArrowLeft className={elderlyMode ? 'w-6 h-6' : 'w-4 h-4'} />
          </button>
          <div>
            <h1 className={`font-black text-slate-100 ${elderlyMode ? 'text-2xl' : 'text-lg'}`}>
              Delivery Dashboard
            </h1>
            <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
              Hyperlocal Parcel Assignments
            </p>
          </div>
        </div>

        <button 
          onClick={() => speak('Delivery Agent Dashboard. Here you can pick up packages from shops and deliver them to customers.', 'ଡେଲିଭରି ଏଜେଣ୍ଟ ଡ୍ୟାସବୋର୍ଡ। ଏଠାରେ ଆପଣ ଦୋକାନରୁ ପ୍ୟାକେଜ୍ ନେଇ ଗ୍ରାହକଙ୍କୁ ଦେଇପାରିବେ।')}
          className="p-2.5 rounded-full bg-brand-blue/10 border border-brand-blue/20 text-brand-blue cursor-pointer animate-pulse-slow"
        >
          <Volume2 className="w-5 h-5" />
        </button>
      </header>

      {/* Main Grid */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 text-left">
        <div className="rounded-3xl glass-card p-6 border border-white/5 mb-8 flex items-center justify-between bg-slate-900/40">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-brand-emerald/15 border border-brand-emerald/25 text-brand-emerald rounded-2xl">
              <Truck className="w-8 h-8" />
            </div>
            <div>
              <h2 className="text-lg font-black text-slate-200">Active Delivery Worksheets</h2>
              <p className="text-xs text-slate-500">Pickup parcels from local vendors and log coordinate handovers.</p>
            </div>
          </div>
        </div>

        {/* Orders List */}
        <div className="grid md:grid-cols-2 gap-6">
          {orders.length === 0 ? (
            <div className="md:col-span-2 text-center py-16 text-slate-500 glass-card rounded-3xl border border-white/5">
              <Package className="w-14 h-14 mx-auto mb-2 opacity-30" />
              <p className="text-sm font-bold">No active packages to deliver</p>
              <p className="text-xs mt-1">Place an order in the Shopping screen to create a delivery worksheet.</p>
            </div>
          ) : (
            orders.map(ord => (
              <div key={ord.id} className="p-6 rounded-3xl glass-card border border-white/5 space-y-4">
                <div className="flex justify-between items-start border-b border-white/5 pb-3">
                  <div>
                    <h3 className="font-extrabold text-slate-200 text-sm">Package ID: {ord.id}</h3>
                    <p className="text-[10px] text-slate-500 mt-1">Ordered at: {ord.createdAt}</p>
                  </div>
                  <span className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-wider ${
                    ord.status === 'PENDING' 
                      ? 'bg-brand-amber/10 border border-brand-amber/20 text-brand-amber'
                      : ord.status === 'PICKED_UP'
                        ? 'bg-brand-blue/10 border border-brand-blue/20 text-brand-blue animate-pulse'
                        : 'bg-brand-emerald/10 border border-brand-emerald/20 text-brand-emerald'
                  }`}>
                    {ord.status === 'PENDING' ? 'READY FOR PICKUP' : ord.status === 'PICKED_UP' ? 'IN TRANSIT' : 'DELIVERED'}
                  </span>
                </div>

                <div className="space-y-2 text-xs text-slate-400">
                  <div className="flex items-start gap-2">
                    <MapPin className="w-4 h-4 text-brand-blue shrink-0 mt-0.5" />
                    <p><span className="font-semibold text-slate-300">Pickup:</span> {ord.storeName} ({ord.storeAddress})</p>
                  </div>
                  <div className="flex items-start gap-2">
                    <MapPin className="w-4 h-4 text-brand-emerald shrink-0 mt-0.5" />
                    <p><span className="font-semibold text-slate-300">Dropoff:</span> Patia Chowk Residential area</p>
                  </div>
                </div>

                <div className="border-t border-white/5 pt-3">
                  <h4 className="text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-2">Package Items</h4>
                  <ul className="text-xs text-slate-400 list-disc list-inside">
                    {ord.items.map((item, idx) => (
                      <li key={idx}>
                        {item.name} <span className="font-semibold text-slate-500">x{item.quantity}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="pt-3 border-t border-white/5 flex gap-3">
                  {ord.status === 'PENDING' && (
                    <button
                      onClick={() => handleUpdateStatus(ord.id, 'PICKED_UP')}
                      className="flex-1 py-3 rounded-xl bg-brand-blue text-slate-950 font-black text-xs hover:opacity-95 transition-all flex items-center justify-center gap-1.5 cursor-pointer"
                    >
                      <Truck className="w-4 h-4" />
                      PICK UP PARCEL
                    </button>
                  )}
                  {ord.status === 'PICKED_UP' && (
                    <button
                      onClick={() => handleUpdateStatus(ord.id, 'DELIVERED')}
                      className="flex-1 py-3 rounded-xl bg-brand-emerald text-slate-950 font-black text-xs hover:opacity-95 transition-all flex items-center justify-center gap-1.5 cursor-pointer"
                    >
                      <CheckCircle className="w-4 h-4" />
                      MARK DELIVERED
                    </button>
                  )}
                  {ord.status === 'DELIVERED' && (
                    <div className="flex-1 text-center py-2.5 rounded-xl bg-brand-emerald/10 border border-brand-emerald/20 text-brand-emerald text-xs font-bold flex items-center justify-center gap-1.5">
                      <CheckCircle className="w-4 h-4" />
                      DELIVERY COMPLETED
                    </div>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </main>

      {/* Footer */}
      <footer className="px-6 py-4 border-t border-white/5 text-center text-xs text-slate-600 bg-slate-950/20">
        © 2026 Odisha Smart City Infrastructure Commission. All rights reserved.
      </footer>
    </div>
  );
};

export default DeliveryScreen;
