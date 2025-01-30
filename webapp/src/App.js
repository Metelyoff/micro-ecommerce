import React, { useState, useEffect, useRef } from 'react';
import ProductList from './components/ProductList';
import Cart from './components/Cart';
import PaymentModal from './components/PaymentModal';
import './styles.css';

const useFetchProducts = () => {
  const [products, setProducts] = useState([]);
  const fetchCalled = useRef(false);

  useEffect(() => {
    if (!fetchCalled.current) {
      fetchCalled.current = true;
      fetch(`${process.env.REACT_APP_PRODUCTS_API_URL}/items`)
        .then(response => response.json())
        .then(data => setProducts(data));
    }
  }, []);

  return products;
};

const useCart = () => {
  const [cart, setCart] = useState(() => {
    const savedCart = localStorage.getItem('cart');
    return savedCart ? JSON.parse(savedCart) : [];
  });

  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(cart));
  }, [cart]);

  const addToCart = (item, quantity) => {
    const existingItem = cart.find(cartItem => cartItem.id === item.id);
    if (existingItem) {
      const newQuantity = Math.min(existingItem.quantity + quantity, item.quantity);
      setCart(cart.map(cartItem =>
        cartItem.id === item.id ? { ...cartItem, quantity: newQuantity } : cartItem
      ));
    } else {
      const newQuantity = Math.min(quantity, item.quantity);
      setCart([...cart, { ...item, quantity: newQuantity }]);
    }
  };

  const updateQuantity = (id, quantity) => {
    setCart(cart.map(cartItem =>
      cartItem.id === id ? { ...cartItem, quantity: quantity } : cartItem
    ));
  };

  const removeFromCart = (id) => {
    setCart(cart.filter(cartItem => cartItem.id !== id));
  };

  return { cart, addToCart, updateQuantity, removeFromCart, setCart };
};

const useOrderStatus = (setPaymentAmount, setPaymentExpiry, setIsCartLocked, setIsRefreshed, setSelectedPaymentMethod) => {

  const [orderStatuses, setOrderStatuses] = useState(() => {
    const savedStatuses = localStorage.getItem('orderStatuses');
    return savedStatuses ? JSON.parse(savedStatuses) : [];
  });

  // const [eventSource, setEventSource] = useState(null);
  const eventSourceRef = useRef(null);

  // useEffect(() => {
  //   return () => {
  //     if (eventSource) {
  //       eventSource.close();
  //     }
  //   };
  // }, [eventSource]);

  useEffect(() => {
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, []);

  const subscribeToOrderStatus = (orderId) => {
    // if (eventSource) {
    //   eventSource.close();
    // }

    if (eventSourceRef.current) {
      eventSourceRef.current.close(); // Close any previous SSE connections
    }

    const newEventSource = new EventSource(`${process.env.REACT_APP_ORDERS_API_URL}/orders/subscribe/${orderId}`);
    newEventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      setOrderStatuses(prevStatuses => [...prevStatuses, { status: data.status, statusDescription: data.statusDescription, paymentId: data.paymentId }]);
      if (data.status === 'PENDING_FOR_PAY' && data.paymentId) {
        fetch(`${process.env.REACT_APP_PAYMENTS_API_URL}/payments/${data.paymentId}`)
          .then(response => response.json())
          .then(paymentData => {
            setPaymentAmount(paymentData.amount);
            setPaymentExpiry(new Date(paymentData.expiredAt));
          })
          .catch(error => {
            console.error('Error fetching payment data:', error);
          });
      }
      if (['PAID', 'FAILED', 'CANCELLED'].includes(data.status)) {
        newEventSource.close();
        // setEventSource(null);
        newEventSource.close(); // Stop listening if the order is finalized
        eventSourceRef.current = null; // Reset the ref
      }
      if (data.status === 'CREATED') {
        setIsCartLocked(true); // Lock the cart when the order is created
      }
    };

    newEventSource.onerror = () => {
      console.error('Error in SSE connection.');
      newEventSource.close();
      eventSourceRef.current = null; // Reset the ref on error
    };

    eventSourceRef.current = newEventSource;

    localStorage.setItem('currentOrderId', orderId);

    // setEventSource(newEventSource);
  };

  useEffect(() => {
    const savedOrderId = localStorage.getItem('currentOrderId');
    if (savedOrderId) {
      subscribeToOrderStatus(savedOrderId);
      fetch(`${process.env.REACT_APP_ORDERS_API_URL}/orders/${savedOrderId}`)
        .then(response => response.json())
        .then(orderData => {
          fetch(`${process.env.REACT_APP_PAYMENTS_API_URL}/payments/${orderData.paymentId}`)
            .then(response => response.json())
            .then(paymentData => {
              setPaymentAmount(paymentData.amount);
              setPaymentExpiry(new Date(paymentData.expiredAt));
              setSelectedPaymentMethod(paymentData.method);
              setIsRefreshed(true);
            })
            .catch(error => {
              console.error('Error fetching payment data:', error);
            });
        })
        .catch(error => {
          console.error('Error fetching payment data:', error);
        });
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('orderStatuses', JSON.stringify(orderStatuses));
  }, [orderStatuses]);

  return { orderStatuses, subscribeToOrderStatus, setOrderStatuses };
};

const App = () => {
  const products = useFetchProducts();
  const { cart, addToCart, updateQuantity, removeFromCart, setCart } = useCart();
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState(null);
  const [previousPaymentMethod, setPreviousPaymentMethod] = useState(null);
  const [paymentAmount, setPaymentAmount] = useState(0);
  const [paymentExpiry, setPaymentExpiry] = useState(null);
  const [isCartLocked, setIsCartLocked] = useState(false);
  const [isRefreshed, setIsRefreshed] = useState(false);
  const { orderStatuses, subscribeToOrderStatus, setOrderStatuses } = useOrderStatus(setPaymentAmount, setPaymentExpiry, setIsCartLocked, setIsRefreshed, setSelectedPaymentMethod);

  const confirmOrder = () => {
    setPreviousPaymentMethod(selectedPaymentMethod);
    setShowPaymentModal(true);
  };

  const handleAcceptPayment = () => {
    const order = {
      paymentMethod: selectedPaymentMethod,
      items: cart.map(item => ({
        productId: item.id,
        name: item.name,
        price: item.price,
        quantity: item.quantity
      }))
    };

    fetch(`${process.env.REACT_APP_ORDERS_API_URL}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(order)
    })
      .then(response => {
        const location = response.headers.get('Location');
        if (location) {
          const orderId = location.split('/').pop();
          subscribeToOrderStatus(orderId);
          setCart([]);
          localStorage.removeItem('cart');
        } else {
          console.error('Location header is missing');
        }
      })
      .catch(error => console.error('Error:', error));

    setShowPaymentModal(false);
  };

  const handleCancelPayment = () => {
    setSelectedPaymentMethod(previousPaymentMethod);
    setShowPaymentModal(false);
  };

  const createNewOrder = () => {
    setCart([]);
    setOrderStatuses([]);
    setSelectedPaymentMethod(null);
    setPaymentAmount(0);
    setPaymentExpiry(null);
    setIsCartLocked(false); // Unlock the cart when creating a new order
  };
  return (
    <div className="app">
      <div className="left">
        <ProductList addToCart={addToCart} products={products} isCartLocked={isCartLocked} />
      </div>
      <div className="right">
        <Cart
          cart={cart}
          products={products}
          updateQuantity={updateQuantity}
          removeFromCart={removeFromCart}
          confirmOrder={confirmOrder}
          orderStatuses={orderStatuses}
          setOrderStatuses={setOrderStatuses}
          selectedPaymentMethod={selectedPaymentMethod}
          setSelectedPaymentMethod={setSelectedPaymentMethod}
          createNewOrder={createNewOrder}
          paymentAmount={paymentAmount}
          paymentExpiry={paymentExpiry}
          disableAddToCart={isCartLocked}
          isRefreshed={isRefreshed}
          setIsRefreshed={setIsRefreshed}
        />
      </div>
      {showPaymentModal && (
        <PaymentModal
          totalPrice={cart.reduce((total, item) => total + item.price * item.quantity, 0)}
          selectedPaymentMethod={selectedPaymentMethod}
          setSelectedPaymentMethod={setSelectedPaymentMethod}
          onCancel={handleCancelPayment}
          onAccept={handleAcceptPayment}
        />
      )}
    </div>
  );
};

export default App;