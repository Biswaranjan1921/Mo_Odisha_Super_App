import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../core/context/AuthContext';
import { ArrowLeft, ShoppingBag, Plus, Minus, Trash2, CheckCircle2, ShoppingCart } from 'lucide-react';

const ShoppingScreen = () => {
  const { elderlyMode } = useAuth();
  const navigate = useNavigate();

  const [stores] = useState([
    { id: 'str-1', name: 'Kalinga Pharmacy', category: 'PHARMACY', address: 'Patia Chowk, Bhubaneswar' },
    { id: 'str-2', name: 'Bhubaneswar Daily Groceries', category: 'GROCERY', address: 'Master Canteen, Bhubaneswar' },
    { id: 'str-3', name: 'Digital & Restock Plaza', category: 'ELECTRONICS', address: 'Saheed Nagar, Bhubaneswar' }
  ]);

  const [products] = useState([
    { id: 'prd-1', storeId: 'str-1', name: 'Paracetamol 650mg', price: 30, desc: 'Pain relief & fever reduction', isMedicine: true },
    { id: 'prd-2', storeId: 'str-1', name: 'Antiseptic Bandages', price: 45, desc: 'Sterile adhesive strips (20 units)', isMedicine: false },
    { id: 'prd-3', storeId: 'str-1', name: 'Cough Syrup (Oditek)', price: 95, desc: 'Relief from wet & dry cough', isMedicine: true },
    
    { id: 'prd-4', storeId: 'str-2', name: 'Fresh Organic Apples', price: 180, desc: 'Premium Kashmiri apples (1 Kg)', isMedicine: false },
    { id: 'prd-5', storeId: 'str-2', name: 'Pure Cow Milk', price: 60, desc: 'Pasteurized whole milk (1 Liter)', isMedicine: false },
    { id: 'prd-6', storeId: 'str-2', name: 'Brown Basmati Rice', price: 110, desc: 'High-fiber long grain rice (1 Kg)', isMedicine: false },

    { id: 'prd-7', storeId: 'str-3', name: 'USB-C Charging Cable', price: 299, desc: 'Braided fast charging wire (1.5m)', isMedicine: false },
    { id: 'prd-8', storeId: 'str-3', name: 'In-Ear Wired Earphones', price: 499, desc: 'Deep bass sound with mic', isMedicine: false }
  ]);

  const [selectedStoreId, setSelectedStoreId] = useState(stores[0].id);
  const [cart, setCart] = useState([]);
  const [showSuccess, setShowSuccess] = useState(false);
  const [lastOrderId, setLastOrderId] = useState('');

  const activeStore = stores.find(s => s.id === selectedStoreId);
  const activeProducts = products.filter(p => p.storeId === selectedStoreId);

  const addToCart = (product) => {
    const existing = cart.find(item => item.product.id === product.id);
    if (existing) {
      setCart(cart.map(item => 
        item.product.id === product.id 
          ? { ...item, quantity: item.quantity + 1 } 
          : item
      ));
    } else {
      setCart([...cart, { product, quantity: 1 }]);
    }
  };

  const updateQuantity = (productId, delta) => {
    setCart(cart.map(item => {
      if (item.product.id === productId) {
        const nextQty = item.quantity + delta;
        return nextQty > 0 ? { ...item, quantity: nextQty } : null;
      }
      return item;
    }).filter(Boolean));
  };

  const removeFromCart = (productId) => {
    setCart(cart.filter(item => item.product.id !== productId));
  };

  const getCartTotal = () => {
    return cart.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
  };

  const handleCheckout = () => {
    if (cart.length === 0) return;

    const total = getCartTotal();
    const orderId = 'ord-' + Math.floor(1000 + Math.random() * 9000);
    
    // Create new order mapping to db schemas
    const newOrder = {
      id: orderId,
      storeName: activeStore.name,
      storeAddress: activeStore.address,
      items: cart.map(item => ({
        name: item.product.name,
        price: item.product.price,
        quantity: item.quantity
      })),
      totalAmount: total,
      status: 'PENDING', // Will be read/updated by Delivery module
      createdAt: new Date().toLocaleTimeString()
    };

    // Save order in localStorage for delivery matching
    const savedOrders = JSON.parse(localStorage.getItem('ssl_orders') || '[]');
    localStorage.setItem('ssl_orders', JSON.stringify([newOrder, ...savedOrders]));

    // Trigger success panel
    setLastOrderId(orderId);
    setCart([]);
    setShowSuccess(true);
    setTimeout(() => setShowSuccess(false), 4000);
  };

  return (
    <div className="min-h-screen flex flex-col justify-between">
      {/* Header */}
      <header className="px-6 py-4 flex items-center gap-3 border-b border-white/5 bg-slate-950/75 backdrop-blur-xl">
        <button
          onClick={() => navigate('/dashboard')}
          className="p-2 rounded-xl bg-white/5 border border-white/10 text-slate-300 cursor-pointer"
        >
          <ArrowLeft className={elderlyMode ? 'w-6 h-6' : 'w-4 h-4'} />
        </button>
        <div>
          <h1 className={`font-black text-slate-100 ${elderlyMode ? 'text-2xl' : 'text-lg'}`}>
            Hyperlocal Shopping
          </h1>
          <p className="text-[9px] text-brand-emerald font-semibold uppercase tracking-wider">
            Local Stores & Inventory Catalog
          </p>
        </div>
      </header>

      {/* Main Grid */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 py-8 grid lg:grid-cols-12 gap-8 items-stretch text-left">
        
        {/* Left Column: Stores & Products */}
        <div className="lg:col-span-8 space-y-6">
          {/* Store Selection */}
          <div className="p-4 rounded-3xl glass-card border border-white/5">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">Select Nearby Store</h3>
            <div className="grid sm:grid-cols-3 gap-3">
              {stores.map(store => (
                <button
                  key={store.id}
                  onClick={() => {
                    setSelectedStoreId(store.id);
                    setCart([]); // Clear cart when switching stores to maintain schema limits
                  }}
                  className={`p-4 rounded-2xl text-left border cursor-pointer transition-all ${
                    selectedStoreId === store.id
                      ? 'bg-brand-blue/15 border-brand-blue text-slate-100 shadow-[0_0_15px_rgba(59,130,246,0.15)]'
                      : 'bg-white/[0.01] border-white/5 text-slate-400 hover:bg-white/[0.03]'
                  }`}
                >
                  <p className="text-sm font-bold">{store.name}</p>
                  <p className="text-[10px] text-slate-500 font-semibold uppercase mt-1 tracking-wider">{store.category}</p>
                </button>
              ))}
            </div>
          </div>

          {/* Products Catalogue */}
          <div className="space-y-4">
            <h3 className="text-lg font-black text-slate-200 uppercase tracking-wider">Available Products</h3>
            <div className="grid sm:grid-cols-2 gap-4">
              {activeProducts.map(prod => (
                <div key={prod.id} className="p-5 rounded-3xl glass-card border border-white/5 flex flex-col justify-between h-full space-y-4">
                  <div>
                    <div className="flex items-start justify-between border-b border-white/5 pb-2">
                      <h4 className="font-bold text-slate-200 text-sm">{prod.name}</h4>
                      <span className="text-xs font-bold text-brand-emerald">₹{prod.price}</span>
                    </div>
                    <p className="text-xs text-slate-500 mt-2 leading-relaxed">{prod.desc}</p>
                  </div>
                  <button
                    onClick={() => addToCart(prod)}
                    className="w-full py-2.5 rounded-xl bg-brand-blue hover:opacity-95 text-slate-950 font-black text-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    ADD TO CART
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column: Checkout Cart Drawer */}
        <div className="lg:col-span-4 flex flex-col">
          <div className="p-6 rounded-3xl glass-card border border-white/5 flex-1 flex flex-col justify-between min-h-[400px]">
            <div className="space-y-4">
              <div className="flex items-center gap-2 border-b border-white/5 pb-3">
                <ShoppingCart className="w-5 h-5 text-brand-emerald" />
                <h3 className="font-black text-slate-100 text-sm">Shopping Cart</h3>
              </div>

              {showSuccess && (
                <div className="p-4 rounded-2xl bg-brand-emerald/15 border border-brand-emerald/30 text-brand-emerald text-xs font-bold space-y-1">
                  <div className="flex items-center gap-1.5">
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Order checkout success!</span>
                  </div>
                  <p className="text-[10px] text-slate-400 font-semibold uppercase tracking-wider">Order ID: {lastOrderId}</p>
                  <p className="text-[10px] text-slate-500 font-medium normal-case">Assigned to delivery agent. You can monitor progress on the Delivery panel.</p>
                </div>
              )}

              {/* Cart Items list */}
              <div className="overflow-y-auto max-h-[300px] pr-1 space-y-3">
                {cart.length === 0 ? (
                  <div className="text-center py-12 text-slate-500">
                    <ShoppingBag className="w-10 h-10 mx-auto mb-2 opacity-30" />
                    <p className="text-xs font-bold">Cart is empty</p>
                    <p className="text-[10px]">Select items from the catalog to build cart.</p>
                  </div>
                ) : (
                  cart.map(item => (
                    <div key={item.product.id} className="flex items-center justify-between p-3 rounded-xl bg-white/[0.01] border border-white/5">
                      <div className="text-left flex-1 min-w-0 pr-2">
                        <p className="text-xs font-bold text-slate-200 truncate">{item.product.name}</p>
                        <p className="text-[10px] text-slate-500 mt-0.5">₹{item.product.price} each</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => updateQuantity(item.product.id, -1)}
                          className="p-1 rounded bg-white/5 text-slate-400 hover:text-white"
                        >
                          <Minus className="w-3 h-3" />
                        </button>
                        <span className="text-xs font-bold text-slate-300 w-4 text-center">{item.quantity}</span>
                        <button
                          onClick={() => updateQuantity(item.product.id, 1)}
                          className="p-1 rounded bg-white/5 text-slate-400 hover:text-white"
                        >
                          <Plus className="w-3 h-3" />
                        </button>
                        <button
                          onClick={() => removeFromCart(item.product.id)}
                          className="p-1 rounded bg-brand-crimson/10 text-brand-crimson hover:bg-brand-crimson/25 ml-1"
                        >
                          <Trash2 className="w-3 h-3" />
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>

            {/* Cart Calculations */}
            {cart.length > 0 && (
              <div className="border-t border-white/5 pt-4 space-y-4">
                <div className="flex items-center justify-between font-bold">
                  <span className="text-xs text-slate-400">Total Amount</span>
                  <span className="text-sm text-brand-emerald">₹{getCartTotal()}</span>
                </div>
                <button
                  onClick={handleCheckout}
                  className="w-full py-3.5 rounded-xl bg-brand-emerald text-slate-950 font-black text-xs hover:opacity-95 shadow-[0_4px_20px_rgba(16,185,129,0.2)] transition-all cursor-pointer"
                >
                  PLACE ORDER & REQUEST DELIVERY
                </button>
              </div>
            )}
          </div>
        </div>

      </main>

      {/* Footer */}
      <footer className="px-6 py-4 border-t border-white/5 text-center text-xs text-slate-600 bg-slate-950/20">
        © 2026 Odisha Smart City Infrastructure Commission. All rights reserved.
      </footer>
    </div>
  );
};

export default ShoppingScreen;
