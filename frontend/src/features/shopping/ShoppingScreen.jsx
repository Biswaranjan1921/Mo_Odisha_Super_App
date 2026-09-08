import React, { useEffect, useState } from 'react';
import { useAuth } from '../../core/context/AuthContext';
import apiClient from '../../core/api/apiClient';
import { 
  ShoppingBag, 
  Search, 
  AlertTriangle, 
  ShieldAlert, 
  CheckCircle2, 
  Plus, 
  Minus, 
  ShoppingCart, 
  ArrowLeft, 
  Globe, 
  Eye, 
  Pill,
  Carrot,
  Package,
  Layers,
  CreditCard,
  MapPin,
  CheckCircle,
  FileText
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const CATEGORY_TABS = [
  { key: 'ALL', label: 'All Services', icon: Layers },
  { key: 'GROCERY', label: 'Groceries', icon: Package },
  { key: 'PHARMACY', label: 'Pharmacy & Medicines', icon: Pill },
  { key: 'FRESH_PRODUCE', label: 'Fresh Vegetables', icon: Carrot },
  { key: 'HANDLOOM', label: 'Handlooms & Crafts', icon: ShoppingBag }
];

const MOCK_STORES = [
  {
    id: 'str-101',
    name: 'Patra Groceries & Staples',
    category: 'GROCERY',
    address: 'Unit 1 Market, Bhubaneswar',
    openingTime: '07:00',
    closingTime: '21:30',
    active: true
  },
  {
    id: 'str-102',
    name: 'Utkal Medico & Chemist',
    category: 'PHARMACY',
    address: 'Master Canteen, Bhubaneswar',
    openingTime: '08:00',
    closingTime: '23:00',
    active: true
  },
  {
    id: 'str-103',
    name: 'Sambalpuri Weavers Society',
    category: 'HANDLOOM',
    address: 'Saheed Nagar, Bhubaneswar',
    openingTime: '10:00',
    closingTime: '20:00',
    active: true
  }
];

const MOCK_PRODUCTS = [
  {
    id: 'prd-01',
    storeId: 'str-101',
    storeName: 'Patra Groceries',
    name: 'Odia Premium Aromatic Rice 5kg',
    description: 'Freshly milled indigenous aromatic rice',
    price: 380.00,
    category: 'GROCERY',
    stockQuantity: 15,
    inStock: true,
    isMedicine: false,
    requiresPrescription: false
  },
  {
    id: 'prd-02',
    storeId: 'str-102',
    storeName: 'Utkal Medico',
    name: 'Essential Wellness Multivitamin (60 Tabs)',
    description: 'Daily health supplement with zinc and vitamin D3',
    price: 290.00,
    category: 'PHARMACY',
    stockQuantity: 8,
    inStock: true,
    isMedicine: true,
    requiresPrescription: false
  },
  {
    id: 'prd-03',
    storeId: 'str-102',
    storeName: 'Utkal Medico',
    name: 'Prescription Heart Regular 50mg',
    description: 'Cardiovascular maintenance medicine',
    price: 145.00,
    category: 'PHARMACY',
    stockQuantity: 4,
    inStock: true,
    isMedicine: true,
    requiresPrescription: true
  },
  {
    id: 'prd-04',
    storeId: 'str-101',
    storeName: 'Patra Groceries',
    name: 'Pure Organic Mustard Oil 1L',
    description: 'Cold-pressed traditional Kachi Ghani mustard oil',
    price: 165.00,
    category: 'GROCERY',
    stockQuantity: 0,
    inStock: false,
    isMedicine: false,
    requiresPrescription: false
  },
  {
    id: 'prd-05',
    storeId: 'str-103',
    storeName: 'Sambalpuri Weavers',
    name: 'Authentic Sambalpuri Ikat Cotton Saree',
    description: 'Handwoven traditional tie-dye motif saree',
    price: 2450.00,
    category: 'HANDLOOM',
    stockQuantity: 3,
    inStock: true,
    isMedicine: false,
    requiresPrescription: false
  }
];

const ShoppingScreen = () => {
  const { elderlyMode, toggleElderlyMode, language, changeLanguage, speak } = useAuth();
  const navigate = useNavigate();

  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [products, setProducts] = useState(MOCK_PRODUCTS);
  const [stores, setStores] = useState(MOCK_STORES);
  const [loading, setLoading] = useState(false);
  const [checkoutError, setCheckoutError] = useState(null);

  // Local Cart State
  const [cart, setCart] = useState([]);
  const [isCartOpen, setIsCartOpen] = useState(false);

  // Checkout & Payment Modals State
  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);
  const [isProcessingOrder, setIsProcessingOrder] = useState(false);
  const [deliveryAddress, setDeliveryAddress] = useState({
    addressLine1: 'Plot 102, Saheed Nagar',
    city: 'Bhubaneswar',
    state: 'Odisha',
    pincode: '751007'
  });

  // Placed Order & Receipt State
  const [placedOrder, setPlacedOrder] = useState(null);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState('MOCK_UPI');
  const [isPaymentCompleted, setIsPaymentCompleted] = useState(false);

  useEffect(() => {
    const fetchStoresAndProducts = async () => {
      setLoading(true);
      try {
        const storeRes = await apiClient.get('/stores?page=0&size=20');
        if (storeRes.data?.content && storeRes.data.content.length > 0) {
          setStores(storeRes.data.content);
        }
        const prodRes = await apiClient.get('/products?page=0&size=20');
        if (prodRes.data?.content && prodRes.data.content.length > 0) {
          setProducts(prodRes.data.content);
        }
      } catch (err) {
        console.warn('API connection fallback to verified catalog data:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchStoresAndProducts();
  }, []);

  const filteredProducts = products.filter((item) => {
    const matchesCategory = selectedCategory === 'ALL' || item.category === selectedCategory;
    const matchesSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          (item.description && item.description.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesCategory && matchesSearch;
  });

  const addToCart = (product) => {
    if (product.stockQuantity <= 0) return;
    setCart((prev) => {
      const existing = prev.find((i) => i.id === product.id);
      if (existing) {
        return prev.map((i) => i.id === product.id ? { ...i, quantity: i.quantity + 1 } : i);
      }
      return [...prev, { ...product, quantity: 1 }];
    });
    speak(`Added ${product.name} to cart.`, `କାର୍ଟରେ ${product.name} ଯୋଡାଗଲା।`, language);
  };

  const updateQuantity = (productId, delta) => {
    setCart((prev) => prev.map((item) => {
      if (item.id === productId) {
        const newQty = item.quantity + delta;
        return newQty > 0 ? { ...item, quantity: newQty } : null;
      }
      return item;
    }).filter(Boolean));
  };

  const cartTotal = cart.reduce((acc, item) => acc + item.price * item.quantity, 0);
  const cartItemCount = cart.reduce((acc, item) => acc + item.quantity, 0);

  // STEP 1: Handle Order Placement (POST /orders)
  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    setCheckoutError(null);

    if (cart.length === 0) return;

    const storeId = cart[0].storeId || (stores.length > 0 ? stores[0].id : 'str-101');
    const orderPayload = {
      storeId: storeId,
      items: cart.map((item) => ({
        productId: item.id,
        quantity: item.quantity
      })),
      deliveryAddress: deliveryAddress
    };

    setIsProcessingOrder(true);
    try {
      const response = await apiClient.post('/orders', orderPayload);
      setPlacedOrder(response.data);
      setIsCheckoutOpen(false);
      setIsCartOpen(false);
      speak('Order placed successfully. Please process payment.', 'ଅର୍ଡର ସଫଳତାର ସହ ରଖାଗଲା। ଦୟାକରି ଦେୟ ପ୍ରଦାନ କରନ୍ତୁ।', language);
    } catch (err) {
      const errMsg = err.response?.data?.message || err.response?.data?.detail || 'Failed to place order. Please verify stock and prescription rules.';
      setCheckoutError(errMsg);
      console.error('Order placement error:', err);
    } finally {
      setIsProcessingOrder(false);
    }
  };

  // STEP 2: Handle Payment Execution (POST /orders/{id}/pay)
  const handleProcessPayment = async () => {
    if (!placedOrder) return;

    setIsProcessingOrder(true);
    setCheckoutError(null);
    try {
      const response = await apiClient.post(`/orders/${placedOrder.id}/pay`, {
        paymentMethod: selectedPaymentMethod
      });
      setPlacedOrder(response.data);
      setIsPaymentCompleted(true);
      setCart([]);
      speak('Payment completed successfully. Order status is now paid.', 'ଦେୟ ସଫଳତାର ସହ ସମ୍ପୂର୍ଣ୍ଣ ହେଲା।', language);
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Payment execution failed. Please try again.';
      setCheckoutError(errMsg);
    } finally {
      setIsProcessingOrder(false);
    }
  };

  return (
    <div className={`min-h-screen transition-colors duration-300 ${
      elderlyMode 
        ? 'bg-amber-50 text-gray-900 text-lg' 
        : 'bg-stone-50 text-gray-800 text-base'
    }`}>
      
      {/* Top Navbar */}
      <header className="sticky top-0 z-40 bg-stone-900 text-white border-b border-amber-600/30 shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/dashboard')}>
            <button className="p-1 rounded-lg bg-stone-800 hover:bg-stone-700 text-amber-300">
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div className="flex items-center space-x-2">
              <span className="font-extrabold text-amber-400 text-lg">MO COMMERCE</span>
              <span className="text-xs bg-amber-500/20 text-amber-300 px-2 py-0.5 rounded-full border border-amber-500/30">Hyperlocal Stores</span>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={() => changeLanguage(language === 'en' ? 'or' : 'en')}
              className="px-2.5 py-1.5 rounded-lg bg-stone-800 text-amber-300 text-xs font-semibold flex items-center space-x-1"
            >
              <Globe className="w-3.5 h-3.5" />
              <span>{language === 'en' ? 'ଓଡ଼ିଆ' : 'EN'}</span>
            </button>

            <button
              onClick={toggleElderlyMode}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1 border ${
                elderlyMode 
                  ? 'bg-amber-500 text-stone-950 border-amber-400 shadow' 
                  : 'bg-stone-800 text-stone-300 border-stone-700'
              }`}
            >
              <Eye className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">{elderlyMode ? 'Elderly Mode: ON' : 'Elderly Mode'}</span>
            </button>

            {/* Cart Button */}
            <button
              onClick={() => setIsCartOpen(!isCartOpen)}
              className="relative px-3 py-2 rounded-xl bg-gradient-to-r from-amber-600 to-orange-600 text-white font-bold text-xs flex items-center space-x-2 shadow"
            >
              <ShoppingCart className="w-4 h-4" />
              <span className="hidden sm:inline">Cart</span>
              {cartItemCount > 0 && (
                <span className="w-5 h-5 rounded-full bg-white text-orange-700 font-extrabold text-[11px] flex items-center justify-center">
                  {cartItemCount}
                </span>
              )}
            </button>
          </div>
        </div>
      </header>

      {/* Hero / Header Banner */}
      <section className="bg-gradient-to-r from-amber-900 via-stone-900 to-amber-950 text-white py-8 px-4 sm:px-6 lg:px-8 border-b border-amber-600/30">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="space-y-2">
            <span className="text-xs font-bold text-amber-400 uppercase tracking-widest bg-amber-500/10 px-3 py-1 rounded-full border border-amber-500/30">
              10 - 30 MIN HYPERLOCAL DELIVERY
            </span>
            <h1 className="text-3xl font-black tracking-tight">Neighborhood Stores & Pharmacies</h1>
            <p className="text-stone-300 text-sm max-w-xl">
              Buy directly from verified Odisha local grocers, chemists, and weavers with live stock updates.
            </p>
          </div>

          {/* Search Input */}
          <div className="w-full md:w-80 relative">
            <Search className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Search rice, medicine, sarees..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-white/10 border border-white/20 text-white placeholder-stone-400 focus:outline-none focus:ring-2 focus:ring-amber-400"
            />
          </div>
        </div>
      </section>

      {/* Category Tabs Bar */}
      <section className="bg-white border-b border-stone-200 sticky top-16 z-30 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3 flex space-x-2 overflow-x-auto no-scrollbar">
          {CATEGORY_TABS.map((tab) => {
            const IconComp = tab.icon;
            const isSelected = selectedCategory === tab.key;
            return (
              <button
                key={tab.key}
                onClick={() => setSelectedCategory(tab.key)}
                className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all ${
                  isSelected
                    ? 'bg-amber-600 text-white shadow-md shadow-amber-600/20'
                    : 'bg-stone-100 text-stone-700 hover:bg-stone-200'
                }`}
              >
                <IconComp className="w-4 h-4" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>
      </section>

      {/* Main Product Catalog Grid */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        
        {loading && (
          <div className="py-20 text-center space-y-3">
            <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-600 border-t-transparent mx-auto"></div>
            <p className="text-stone-500 font-semibold text-sm">Fetching verified local stores & inventory catalog...</p>
          </div>
        )}

        {!loading && filteredProducts.length === 0 && (
          <div className="py-16 text-center bg-white rounded-3xl p-8 border border-stone-200 max-w-md mx-auto space-y-4">
            <div className="text-5xl">🛍️</div>
            <h3 className="text-xl font-bold text-stone-900">No Products Found</h3>
            <p className="text-sm text-stone-500">No stores matching your search query or category filter were found.</p>
            <button
              onClick={() => { setSelectedCategory('ALL'); setSearchQuery(''); }}
              className="px-4 py-2 rounded-xl bg-amber-600 text-white font-bold text-xs"
            >
              Reset Filters
            </button>
          </div>
        )}

        {!loading && filteredProducts.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredProducts.map((prod) => (
              <div
                key={prod.id}
                className={`bg-white rounded-2xl p-6 border border-stone-200 shadow-sm hover:shadow-md transition-all flex flex-col justify-between ${
                  elderlyMode ? 'border-amber-300 ring-1 ring-amber-400/20' : ''
                }`}
              >
                <div className="space-y-3">
                  
                  <div className="flex items-center justify-between">
                    <span className="text-[11px] font-bold bg-stone-100 text-stone-700 px-2.5 py-1 rounded-full border border-stone-200">
                      {prod.category}
                    </span>

                    {prod.stockQuantity > 0 ? (
                      <span className="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2.5 py-0.5 rounded-full border border-emerald-200 flex items-center space-x-1">
                        <CheckCircle2 className="w-3 h-3" />
                        <span>In Stock ({prod.stockQuantity})</span>
                      </span>
                    ) : (
                      <span className="text-[11px] font-bold text-red-700 bg-red-50 px-2.5 py-0.5 rounded-full border border-red-200 flex items-center space-x-1">
                        <AlertTriangle className="w-3 h-3" />
                        <span>Out of Stock</span>
                      </span>
                    )}
                  </div>

                  <div>
                    <h3 className="font-bold text-lg text-stone-900 leading-snug">{prod.name}</h3>
                    <p className="text-xs text-stone-500 mt-1 leading-relaxed">{prod.description}</p>
                  </div>

                  {prod.requiresPrescription && (
                    <div className="p-2.5 rounded-xl bg-amber-50 border border-amber-200 flex items-center space-x-2 text-amber-900 text-xs font-semibold">
                      <ShieldAlert className="w-4 h-4 text-amber-700 flex-shrink-0" />
                      <span>Prescription Required at Checkout</span>
                    </div>
                  )}

                  <div className="pt-2 flex items-baseline space-x-1">
                    <span className="text-2xl font-black text-amber-800">₹{prod.price.toFixed(2)}</span>
                    <span className="text-xs text-stone-400 font-medium">inclusive of taxes</span>
                  </div>
                </div>

                <button
                  onClick={() => addToCart(prod)}
                  disabled={prod.stockQuantity === 0}
                  className={`mt-6 w-full py-3 px-4 rounded-xl font-bold text-sm flex items-center justify-center space-x-2 transition-all ${
                    prod.stockQuantity > 0
                      ? 'bg-gradient-to-r from-amber-600 to-orange-600 hover:from-amber-700 hover:to-orange-700 text-white shadow-md shadow-orange-600/20'
                      : 'bg-stone-200 text-stone-400 cursor-not-allowed border border-stone-300'
                  }`}
                >
                  <ShoppingCart className="w-4 h-4" />
                  <span>{prod.stockQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}</span>
                </button>

              </div>
            ))}
          </div>
        )}

      </main>

      {/* Cart Drawer Modal */}
      {isCartOpen && (
        <div className="fixed inset-0 z-50 flex justify-end bg-black/50 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white h-full shadow-2xl flex flex-col justify-between p-6 overflow-y-auto">
            
            <div>
              <div className="flex items-center justify-between border-b pb-4 mb-4">
                <div className="flex items-center space-x-2">
                  <ShoppingCart className="w-5 h-5 text-amber-600" />
                  <h2 className="font-bold text-stone-900 text-lg">Your Cart</h2>
                </div>
                <button
                  onClick={() => setIsCartOpen(false)}
                  className="text-stone-400 hover:text-stone-600 text-sm font-bold"
                >
                  Close ✕
                </button>
              </div>

              {cart.length === 0 ? (
                <div className="py-12 text-center text-stone-500 space-y-2">
                  <p className="text-3xl">🛒</p>
                  <p className="font-semibold text-sm">Your cart is empty</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {cart.map((item) => (
                    <div key={item.id} className="p-3 bg-stone-50 rounded-xl border border-stone-200 flex items-center justify-between">
                      <div>
                        <p className="font-bold text-stone-900 text-sm">{item.name}</p>
                        <p className="text-xs text-amber-700 font-semibold">₹{item.price.toFixed(2)}</p>
                      </div>

                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => updateQuantity(item.id, -1)}
                          className="w-7 h-7 rounded-lg bg-stone-200 text-stone-800 font-bold flex items-center justify-center"
                        >
                          <Minus className="w-3.5 h-3.5" />
                        </button>
                        <span className="font-bold text-sm">{item.quantity}</span>
                        <button
                          onClick={() => updateQuantity(item.id, 1)}
                          className="w-7 h-7 rounded-lg bg-amber-600 text-white font-bold flex items-center justify-center"
                        >
                          <Plus className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {cart.length > 0 && (
              <div className="border-t pt-4 space-y-4">
                <div className="flex justify-between items-center text-lg font-black text-stone-900">
                  <span>Subtotal</span>
                  <span className="text-amber-800">₹{cartTotal.toFixed(2)}</span>
                </div>

                <button
                  onClick={() => setIsCheckoutOpen(true)}
                  className="w-full py-3.5 rounded-xl bg-gradient-to-r from-amber-600 to-orange-600 text-white font-bold text-base shadow-lg shadow-orange-600/20"
                >
                  Proceed to Checkout
                </button>
              </div>
            )}

          </div>
        </div>
      )}

      {/* Checkout Modal (Step 1: Address & Order Creation) */}
      {isCheckoutOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-lg w-full shadow-2xl space-y-6">
            <div className="flex items-center justify-between border-b pb-4">
              <div className="flex items-center space-x-2">
                <MapPin className="w-6 h-6 text-amber-600" />
                <h2 className="text-xl font-black text-stone-900">Delivery Address & Checkout</h2>
              </div>
              <button onClick={() => setIsCheckoutOpen(false)} className="text-stone-400 hover:text-stone-600 font-bold">✕</button>
            </div>

            {checkoutError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-xs font-semibold flex items-center space-x-2">
                <AlertTriangle className="w-4 h-4 flex-shrink-0" />
                <span>{checkoutError}</span>
              </div>
            )}

            <form onSubmit={handlePlaceOrder} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-stone-700 mb-1">Address Line</label>
                <input
                  type="text"
                  required
                  value={deliveryAddress.addressLine1}
                  onChange={(e) => setDeliveryAddress({ ...deliveryAddress, addressLine1: e.target.value })}
                  className="w-full px-3 py-2 border rounded-xl text-sm focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-stone-700 mb-1">City</label>
                  <input
                    type="text"
                    required
                    value={deliveryAddress.city}
                    onChange={(e) => setDeliveryAddress({ ...deliveryAddress, city: e.target.value })}
                    className="w-full px-3 py-2 border rounded-xl text-sm focus:ring-2 focus:ring-amber-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-stone-700 mb-1">State</label>
                  <input
                    type="text"
                    required
                    value={deliveryAddress.state}
                    onChange={(e) => setDeliveryAddress({ ...deliveryAddress, state: e.target.value })}
                    className="w-full px-3 py-2 border rounded-xl text-sm focus:ring-2 focus:ring-amber-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-stone-700 mb-1">Pincode</label>
                <input
                  type="text"
                  required
                  value={deliveryAddress.pincode}
                  onChange={(e) => setDeliveryAddress({ ...deliveryAddress, pincode: e.target.value })}
                  className="w-full px-3 py-2 border rounded-xl text-sm focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div className="pt-2 border-t space-y-2 text-xs font-semibold text-stone-600">
                <div className="flex justify-between"><span>Items Subtotal:</span><span>₹{cartTotal.toFixed(2)}</span></div>
                <div className="flex justify-between"><span>Delivery Fee:</span><span>₹40.00</span></div>
                <div className="flex justify-between text-stone-900 font-bold text-sm">
                  <span>Estimated Total:</span>
                  <span className="text-amber-800">₹{(cartTotal + 40).toFixed(2)}</span>
                </div>
              </div>

              <button
                type="submit"
                disabled={isProcessingOrder}
                className="w-full py-3.5 bg-gradient-to-r from-amber-600 to-orange-600 text-white font-bold rounded-xl shadow-lg hover:from-amber-700 hover:to-orange-700 flex items-center justify-center space-x-2"
              >
                {isProcessingOrder ? (
                  <span>Reserving Stock & Placing Order...</span>
                ) : (
                  <>
                    <FileText className="w-4 h-4" />
                    <span>Place Order (Status: PENDING_PAYMENT)</span>
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Payment Modal & Receipt Confirmation (Step 2) */}
      {placedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-md w-full shadow-2xl space-y-6">
            
            {!isPaymentCompleted ? (
              <div className="space-y-4">
                <div className="flex items-center space-x-2 border-b pb-3">
                  <CreditCard className="w-6 h-6 text-amber-600" />
                  <h2 className="text-lg font-black text-stone-900">Execute Order Payment</h2>
                </div>

                <div className="p-3 bg-amber-50 rounded-xl border border-amber-200 text-xs font-semibold text-amber-900">
                  Order ID: <span className="font-mono text-amber-950 font-bold">{placedOrder.id}</span>
                </div>

                <div className="space-y-2">
                  <label className="block text-xs font-bold text-stone-700">Select Mock Payment Method</label>
                  <select
                    value={selectedPaymentMethod}
                    onChange={(e) => setSelectedPaymentMethod(e.target.value)}
                    className="w-full px-3 py-2 border rounded-xl text-sm font-semibold focus:ring-2 focus:ring-amber-500"
                  >
                    <option value="MOCK_UPI">Mock UPI / PhonePe / GPay</option>
                    <option value="MOCK_CARD">Mock Debit / Credit Card</option>
                    <option value="MOCK_NETBANKING">Mock NetBanking</option>
                  </select>
                </div>

                <div className="border-t pt-3 space-y-1 text-xs text-stone-700 font-semibold">
                  <div className="flex justify-between"><span>Subtotal:</span><span>₹{placedOrder.subtotal?.toFixed(2)}</span></div>
                  <div className="flex justify-between"><span>Delivery Fee:</span><span>₹{placedOrder.deliveryFee?.toFixed(2)}</span></div>
                  <div className="flex justify-between text-sm font-black text-stone-900 pt-1 border-t">
                    <span>Amount Payable:</span>
                    <span className="text-amber-800">₹{placedOrder.totalAmount?.toFixed(2)}</span>
                  </div>
                </div>

                <button
                  onClick={handleProcessPayment}
                  disabled={isProcessingOrder}
                  className="w-full py-3.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold rounded-xl shadow-lg hover:from-emerald-700 hover:to-teal-700 flex items-center justify-center space-x-2"
                >
                  {isProcessingOrder ? (
                    <span>Processing Payment...</span>
                  ) : (
                    <>
                      <CheckCircle className="w-5 h-5" />
                      <span>Pay ₹{placedOrder.totalAmount?.toFixed(2)} Now</span>
                    </>
                  )}
                </button>
              </div>
            ) : (
              /* Order Confirmation Receipt */
              <div className="space-y-4 text-center">
                <div className="w-16 h-16 bg-emerald-100 rounded-full flex items-center justify-center mx-auto text-emerald-600">
                  <CheckCircle className="w-10 h-10" />
                </div>

                <div>
                  <h2 className="text-2xl font-black text-stone-900">Order Confirmed!</h2>
                  <p className="text-xs text-emerald-700 font-bold bg-emerald-50 px-3 py-1 rounded-full border border-emerald-200 inline-block mt-1">
                    STATUS: {placedOrder.status}
                  </p>
                </div>

                <div className="p-4 bg-stone-50 rounded-2xl border border-stone-200 text-left text-xs space-y-2">
                  <div className="flex justify-between"><span className="text-stone-500 font-semibold">Order Reference:</span><span className="font-mono font-bold">{placedOrder.id?.substring(0, 18)}...</span></div>
                  <div className="flex justify-between"><span className="text-stone-500 font-semibold">Transaction ID:</span><span className="font-mono font-bold text-amber-800">{placedOrder.payment?.transactionId}</span></div>
                  <div className="flex justify-between"><span className="text-stone-500 font-semibold">Payment Method:</span><span className="font-bold">{placedOrder.payment?.paymentMethod}</span></div>
                  <div className="flex justify-between"><span className="text-stone-500 font-semibold">Total Paid:</span><span className="font-black text-amber-900 text-sm">₹{placedOrder.totalAmount?.toFixed(2)}</span></div>
                </div>

                <div className="p-3 bg-amber-50 rounded-2xl border border-amber-200 text-left text-xs space-y-1">
                  <p className="font-bold text-amber-900 flex items-center space-x-1">
                    <MapPin className="w-3.5 h-3.5 text-amber-700" />
                    <span>Delivery Address Snapshot</span>
                  </p>
                  <p className="text-stone-700 font-medium">
                    {placedOrder.deliveryAddress?.addressLine1}, {placedOrder.deliveryAddress?.city}, {placedOrder.deliveryAddress?.state} - {placedOrder.deliveryAddress?.pincode}
                  </p>
                </div>

                <button
                  onClick={() => setPlacedOrder(null)}
                  className="w-full py-3 bg-stone-900 text-white font-bold text-sm rounded-xl hover:bg-stone-800"
                >
                  Done & Return to Shopping
                </button>
              </div>
            )}

          </div>
        </div>
      )}

    </div>
  );
};

export default ShoppingScreen;
