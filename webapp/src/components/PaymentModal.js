import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCreditCard, faMoneyBillWave } from '@fortawesome/free-solid-svg-icons';
import './PaymentModal.css';

const PaymentModal = ({ totalPrice, selectedPaymentMethod, setSelectedPaymentMethod, onCancel, onAccept, isPaymentConfirmation }) => {
  const getPaymentIcon = () => {
    if (selectedPaymentMethod === 'CARD') {
      return <FontAwesomeIcon icon={faCreditCard} />;
    } else if (selectedPaymentMethod === 'CASH') {
      return <FontAwesomeIcon icon={faMoneyBillWave} />;
    }
    return null;
  };

  return (
    <div className="payment-modal">
      <div className="payment-modal-content">
        {isPaymentConfirmation ? (
          <>
            <h2>Payment confirmation</h2>
            <div className="total-price">Total: ${totalPrice.toFixed(2)}</div>
            <div className="payment-method-confirmation">
              {getPaymentIcon()} <span>{selectedPaymentMethod}</span>
            </div>
          </>
        ) : (
          <>
            <h2>Choose Payment Method</h2>
            <div className="payment-methods">
              <PaymentMethod
                method="CARD"
                selectedPaymentMethod={selectedPaymentMethod}
                setSelectedPaymentMethod={setSelectedPaymentMethod}
                icon={faCreditCard}
                label="Card"
              />
              <PaymentMethod
                method="CASH"
                selectedPaymentMethod={selectedPaymentMethod}
                setSelectedPaymentMethod={setSelectedPaymentMethod}
                icon={faMoneyBillWave}
                label="Cash"
              />
            </div>
            <div className="total-price">Total: ${totalPrice.toFixed(2)}</div>
          </>
        )}
        <div className="modal-buttons">
          <button className="cancel-button" onClick={onCancel}>Cancel</button>
          <button className="accept-button" onClick={onAccept} disabled={!selectedPaymentMethod}>Accept</button>
        </div>
      </div>
    </div>
  );
};

const PaymentMethod = ({ method, selectedPaymentMethod, setSelectedPaymentMethod, icon, label }) => (
  <label className={`payment-method ${selectedPaymentMethod === method ? 'selected' : ''}`}>
    <input
      type="radio"
      value={method}
      checked={selectedPaymentMethod === method}
      onChange={(e) => setSelectedPaymentMethod(e.target.value)}
    />
    <FontAwesomeIcon icon={icon} className="payment-icon" />
    {label}
  </label>
);

export default PaymentModal;