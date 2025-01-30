import React, { useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faMinus } from '@fortawesome/free-solid-svg-icons';
import { Tooltip, OverlayTrigger } from 'react-bootstrap';

const ProductList = ({ addToCart, products, isCartLocked }) => {
  const [quantities, setQuantities] = useState({});

  const handleQuantityChange = (id, value) => {
    const item = products.find(item => item.id === id);
    const maxQuantity = item ? item.quantity : 1;
    const newQuantity = Math.max(1, Math.min(Number(value), maxQuantity));
    setQuantities({
      ...quantities,
      [id]: newQuantity
    });
  };

  const incrementQuantity = (id) => {
    const item = products.find(item => item.id === id);
    const maxQuantity = item ? item.quantity : 1;
    const currentQuantity = quantities[id] || 1;
    const newQuantity = Math.min(currentQuantity + 1, maxQuantity);
    setQuantities({
      ...quantities,
      [id]: newQuantity
    });
  };

  const decrementQuantity = (id) => {
    const currentQuantity = quantities[id] || 1;
    const newQuantity = Math.max(currentQuantity - 1, 1);
    setQuantities({
      ...quantities,
      [id]: newQuantity
    });
  };

  const renderTooltip = (props) => (
    <Tooltip id="button-tooltip" {...props}>
      {props.message}
    </Tooltip>
  );

  return (
    <div className="product-list-container">
      <h1 className="fixed-header">Products</h1>
      <div className="product-list">
        {products.map((item) => (
          <div key={item.id} className="product-item">
            <img src={item.image} alt={item.name} />
            <h2>{item.name}</h2>
            <p>${item.price}</p>
            <p className="availability">Available: {item.quantity}</p>
            <div className="quantity-container">
              <OverlayTrigger
                placement="top"
                overlay={renderTooltip({ message: 'Decrease quantity' })}
              >
                <button onClick={() => decrementQuantity(item.id)} disabled={quantities[item.id] <= 1} className="quantity-button">
                  <FontAwesomeIcon icon={faMinus} />
                </button>
              </OverlayTrigger>
              <input
                type="number"
                min="1"
                max={item.quantity}
                value={quantities[item.id] || 1}
                onChange={(e) => handleQuantityChange(item.id, parseInt(e.target.value))}
                className="quantity-input"
              />
              <OverlayTrigger
                placement="top"
                overlay={renderTooltip({ message: 'Increase quantity' })}
              >
                <button onClick={() => incrementQuantity(item.id)} disabled={quantities[item.id] >= item.quantity} className="quantity-button">
                  <FontAwesomeIcon icon={faPlus} />
                </button>
              </OverlayTrigger>
            </div>
            <button onClick={() => addToCart(item, quantities[item.id] || 1)} className="add-to-cart-button" disabled={isCartLocked}>
              Add to Cart
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ProductList;