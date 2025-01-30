import React from 'react';

const OrderStatus = ({ status }) => {
  return (
    <div className="order-status">
      <p>Order Status: {status}</p>
    </div>
  );
};

export default OrderStatus;