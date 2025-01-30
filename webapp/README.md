# My React App

This is a simple one-page application built with React that allows users to view products, add them to a cart, and confirm their orders. The application also displays the current order status, providing a seamless shopping experience.

## Features

- Display a list of products with their names and prices.
- Add products to the cart.
- View items in the cart with their quantities.
- Confirm the order and view the order status.
- Order statuses include: CREATED, RESERVED, PENDING_FOR_PAY, PAID, COMPLETED, FAILED, and CANCELLED.

## Installation

1. Clone the repository:
   ```
   git clone <repository-url>
   ```

2. Navigate to the project directory:
   ```
   cd webapp
   ```

3. Install the dependencies:
   ```
   npm install
   ```

## Usage

To start the application, run:
```
npm start
```
This will launch the app in your default web browser.

## Components

- **ProductList**: Fetches and displays all available products.
- **Cart**: Displays items added to the cart and includes a "Confirm" button.
- **OrderStatus**: Shows the current status of the order.

## Styling

The application is styled using CSS, ensuring a visually appealing layout for the product list, cart, and order status.

## License

This project is licensed under the MIT License.