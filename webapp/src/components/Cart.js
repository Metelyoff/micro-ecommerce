import React, { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrash, faPlus, faMinus, faCreditCard, faMoneyBillWave } from '@fortawesome/free-solid-svg-icons';
import { Tooltip, OverlayTrigger, Spinner } from 'react-bootstrap';
import PaymentModal from './PaymentModal';
import './PaymentModal.css';

const Cart = ({ cart, products, updateQuantity, removeFromCart, confirmOrder, orderStatuses, setOrderStatuses, selectedPaymentMethod, setSelectedPaymentMethod, createNewOrder, paymentAmount, paymentExpiry, disableAddToCart, isRefreshed, setIsRefreshed }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [timeLeft, setTimeLeft] = useState(null);
  const [isExpired, setIsExpired] = useState(false);
  const [showLoader, setShowLoader] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);

  const toggleCart = () => {
    setIsExpanded(!isExpanded);
  };

  const getPaymentIcon = () => {
    if (selectedPaymentMethod === 'CARD') {
      return <FontAwesomeIcon icon={faCreditCard} />;
    } else if (selectedPaymentMethod === 'CASH') {
      return <FontAwesomeIcon icon={faMoneyBillWave} />;
    }
    return null;
  };

  const getStatusColor = (status) => {
    switch (status) {
    case 'CREATED':
    case 'RESERVED':
    case 'PENDING_FOR_PAY':
    case 'PAID':
    case 'COMPLETED':
      return 'primary';
    case 'FAILED':
    case 'CANCELLED':
      return 'accent';
    default:
      return '';
    }
  };

  const currentOrder = orderStatuses.length > 0 ? orderStatuses[orderStatuses.length - 1] : null;

  useEffect(() => {
    const savedOrderStatuses = localStorage.getItem('orderStatuses');
    if (savedOrderStatuses) {
      setOrderStatuses(JSON.parse(savedOrderStatuses));
    }
  }, [setOrderStatuses]);

  useEffect(() => {
    if (orderStatuses.length > 0) {
      localStorage.setItem('orderStatuses', JSON.stringify(orderStatuses));
    }
  }, [orderStatuses]);

  useEffect(() => {
    if ((currentOrder && currentOrder.status !== 'PENDING_FOR_PAY')) {
      setShowLoader(true);
    } else {
      setShowLoader(false);
    }
    if (currentOrder && currentOrder.status === 'PENDING_FOR_PAY' && paymentExpiry) {
      const interval = setInterval(() => {
        const now = new Date().getTime();
        const expiryDate = new Date(paymentExpiry);
        const expiryTime = Date.UTC(
          expiryDate.getFullYear(),
          expiryDate.getMonth(),
          expiryDate.getDate(),
          expiryDate.getHours(),
          expiryDate.getMinutes(),
          expiryDate.getSeconds(),
          expiryDate.getMilliseconds()
        );
        const timeDifference = expiryTime - now;
        if (timeDifference <= 0) {
          clearInterval(interval);
          setTimeLeft('Expired');
          setIsExpired(true);
          setShowLoader(true); // Show loader when the timer expires
          setShowPaymentModal(false); // Close the payment modal when the timer expires
          setIsRefreshed(false);
        } else {
          const minutes = Math.floor((timeDifference / 1000 / 60) % 60);
          const seconds = Math.floor((timeDifference / 1000) % 60);
          setTimeLeft(`${minutes}:${seconds < 10 ? '0' : ''}${seconds}`);
          setIsExpired(false); // Ensure isExpired is set to false when the timer is running
        }
      }, 1000);
  
      return () => clearInterval(interval);
    }
  }, [currentOrder, paymentExpiry, isRefreshed]);

  const handleConfirmOrder = () => {
    confirmOrder();
  };

  const handlePay = () => {
    setShowPaymentModal(true);
  };

  const handlePaymentConfirm = () => {
    if (!currentOrder || !currentOrder.paymentId) {
      console.error('Payment ID is missing');
      return;
    }

    setShowPaymentModal(false);
    setShowLoader(true);

    fetch(`${process.env.REACT_APP_PAYMENTS_API_URL}/payments/${currentOrder.paymentId}/pay`, {
      method: 'POST'
    })
      .then(response => response.json())
      .then(data => {
        if (data.status === 'SUCCESS') {
          // Payment was successful, wait for order status update via notifications
          console.log('Payment successful, waiting for order status update...');
        } else {
          setShowLoader(false);
          alert('Payment failed: ' + data.statusDescription);
        }
      })
      .catch(error => {
        setShowLoader(false);
        console.error('Error:', error);
      });
  };

  const renderCartItems = () => {
    if (cart.length === 0) {
      return <p>Your cart is empty</p>;
    }

    return cart.map((item, index) => {
      const product = products.find(product => product.id === item.id);
      const maxQuantity = product ? product.quantity : item.quantity;

      return (
        <div key={index} className="cart-item">
          <div className="cart-item-details">
            <h2>{item.name}</h2>
            <p>${item.price} x {item.quantity}</p>
          </div>
          <div className="quantity-container">
            <button onClick={() => updateQuantity(item.id, Math.max(1, item.quantity - 1))} disabled={item.quantity <= 1 || disableAddToCart} className="quantity-button">
              <FontAwesomeIcon icon={faMinus} />
            </button>
            <input
              type="number"
              min="1"
              max={maxQuantity}
              value={item.quantity.toString()}
              onChange={(e) => updateQuantity(item.id, parseInt(e.target.value))}
              className="quantity-input"
              disabled={disableAddToCart}
            />
            <button onClick={() => updateQuantity(item.id, Math.min(item.quantity + 1, maxQuantity))} disabled={item.quantity >= maxQuantity || disableAddToCart} className="quantity-button">
              <FontAwesomeIcon icon={faPlus} />
            </button>
            <OverlayTrigger
              placement="top"
              overlay={<Tooltip id={'tooltip-top'}>Remove item</Tooltip>}
            >
              <button onClick={() => removeFromCart(item.id)} className="remove-button" disabled={disableAddToCart}>
                <FontAwesomeIcon icon={faTrash} />
              </button>
            </OverlayTrigger>
          </div>
        </div>
      );
    });
  };

  const renderOrderStatuses = () => {
    return orderStatuses.map((status, index) => (
      <div key={index} className={`order-status ${getStatusColor(status.status)}`} style={{ marginBottom: '10px' }}>
        <strong>{status.status}</strong>
        {status.statusDescription && <p style={{ textAlign: 'left' }}>{status.statusDescription}</p>}
      </div>
    ));
  };

  const renderFooter = () => {
    if (currentOrder && currentOrder.status === 'PENDING_FOR_PAY' && (selectedPaymentMethod || isExpired || isRefreshed)) {
      return (
        <>
          <div className="countdown-timer">Time left: {timeLeft}</div>
          <div className="footer-content">
            <div className="total-price">
              {selectedPaymentMethod && <span style={{ paddingRight: '8px' }}>{getPaymentIcon()}</span>}
              Total: ${paymentAmount.toFixed(2)}
            </div>
            <button className="confirm-button pay-button" onClick={handlePay} disabled={isExpired}>Pay</button>
          </div>
        </>
      );
    }

    if (currentOrder && ['PAID', 'FAILED', 'CANCELLED'].includes(currentOrder.status)) {
      return <button className="confirm-button create-new-button" onClick={() => {
        createNewOrder();
        localStorage.removeItem('orderStatuses');
      }}>Create new</button>;
    }

    return (
      <div className="footer-content">
        {cart.length > 0 && (
          <div className="total-price">
            {selectedPaymentMethod && <span style={{ paddingRight: '8px' }}>{getPaymentIcon()}</span>}
            Total: ${cart.reduce((total, item) => total + item.price * item.quantity, 0).toFixed(2)}
          </div>
        )}
        {cart.length > 0 && (
          <button className="confirm-button" onClick={handleConfirmOrder} disabled={cart.length === 0}>Confirm</button>
        )}
      </div>
    );
  };

  return (
    <div className={`cart-container ${isExpanded ? 'expanded' : ''}`}>
      <h1 className="fixed-header">{currentOrder ? 'Order statuses' : 'Cart'}</h1>
      <div className="cart-footer mobile" onClick={toggleCart}>
        <div className="footer-content">
          {currentOrder && ['PAID', 'FAILED', 'CANCELLED'].includes(currentOrder.status) ? (
            <>
              <div className={`order-status-mobile ${getStatusColor(currentOrder.status)}`}>
                <strong>{currentOrder.status}</strong>
              </div>
              <button className="confirm-button create-new-button" onClick={() => {
                createNewOrder();
                localStorage.removeItem('orderStatuses');
              }}>Create new</button>
            </>
          ) : (
            <>
              {!(currentOrder && ['PAID', 'FAILED', 'CANCELLED'].includes(currentOrder.status)) && cart.length > 0 && (
                <div className="total-price">
                  {selectedPaymentMethod && <span style={{ paddingRight: '8px' }}>{getPaymentIcon()}</span>}
                  Total: ${currentOrder && currentOrder.status === 'PENDING_FOR_PAY' ? paymentAmount.toFixed(2) : cart.reduce((total, item) => total + item.price * item.quantity, 0).toFixed(2)}
                </div>
              )}
              {currentOrder && currentOrder.status === 'PENDING_FOR_PAY' && (
                <div className="countdown-timer" style={{ textAlign: 'center' }}>Time left: {timeLeft}</div>
              )}
              {!currentOrder || !['PAID', 'FAILED', 'CANCELLED'].includes(currentOrder.status) ? (
                !currentOrder || currentOrder.status !== 'PENDING_FOR_PAY' ? <div className="title">Cart</div> : null
              ) : null}
              {currentOrder && currentOrder.status === 'PENDING_FOR_PAY' ? (
                <button className="confirm-button pay-button" onClick={handlePay} disabled={isExpired}>
                  Pay
                </button>
              ) : (
                <button className="confirm-button" onClick={handleConfirmOrder} disabled={cart.length === 0}>
                  Confirm
                </button>
              )}
            </>
          )}
        </div>
      </div>
      <div className="cart-list">
        {orderStatuses.length > 0 ? renderOrderStatuses() : renderCartItems()}
        {showLoader && !['PAID', 'FAILED', 'CANCELLED'].includes(currentOrder?.status) && (
          <div className="loader-container">
            <Spinner animation="border" role="status" variant="primary">
              <span className="sr-only">Loading...</span>
            </Spinner>
          </div>
        )}
      </div>
      <div className="cart-footer">
        {renderFooter()}
      </div>

      {showPaymentModal && (
        <PaymentModal
          totalPrice={paymentAmount}
          selectedPaymentMethod={selectedPaymentMethod}
          setSelectedPaymentMethod={setSelectedPaymentMethod}
          onCancel={() => setShowPaymentModal(false)}
          onAccept={handlePaymentConfirm}
          isPaymentConfirmation={true}
        />
      )}
    </div>
  );
};

export default Cart;