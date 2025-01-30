# Distributed E-commerce System with Event-Driven Architecture
This project is a **distributed e-commerce system** implemented using an **event-driven architecture**, featuring multiple services collaborating seamlessly to handle orders, payments, and inventory management. It is designed to demonstrate concepts such as microservices, event sourcing, change data capture (CDC), and scalability using tools like Kafka, PostgreSQL, Debezium, and more.
## Key Features
1. **Microservice Architecture**:
    - Each service is independent and manages its own domain-specific logic and database.
    - Services include:
        - **Order Service**: Handles order creation and management.
        - **Payment Service**: Processes payments and handles payment state tracking.
        - **Inventory Service**: Manages product inventory and updates availability.

2. **Event-Driven Communication**:
    - Services communicate asynchronously via **Apache Kafka** topics.
    - The **Outbox pattern** is implemented to ensure reliable publishing of events.

3. **Change Data Capture (CDC)**:
    - Uses **Debezium** to monitor and react to changes in the database tables of each service.
    - Debezium captures database changes and publishes them as Kafka events.

4. **PostgreSQL Backends**:
    - Each microservice has an independent PostgreSQL database for data persistence.
    - Dockerized databases with custom configurations, initialization scripts, and health checks.

5. **Web Frontend**:
    - A **ReactJS** frontend provides a user-friendly interface to interact with the system.
    - The frontend integrates APIs from different microservices to manage products, orders, and payments.

6. **Infrastructure and Configuration with Docker Compose**:
    - Fully containerized application using Docker Compose.
    - Services include Kafka, Kafka UI, Debezium, PostgreSQL databases, and the microservices themselves.

7. **Debezium UI**:
    - A web UI for monitoring Debezium Connectors and managing CDC configurations.

8. **Kafka UI**:
    - A web UI for inspecting Kafka topics and the state of messages in the system.

## Project Structure
- `order-service/`: Microservice to handle orders.
- `payment-service/`: Microservice to handle payments.
- `inventory-service/`: Microservice to handle inventory management.
- `webapp/`: React-based frontend for the e-commerce system.
- `debezium-connectors/`: Pre-configured Debezium connector definitions for each service.
- **Infrastructure**:
    - **Kafka** for message brokering.
    - **Debezium** for database change data capture.
    - **PostgreSQL** databases for service persistence.

## Running the Project
To set up and run the project locally:
1. Install **Docker** and **Docker Compose** on your system.
2. Clone the repository.
3. Start the application stack using Docker Compose (it will take about 3-5 minutes):
``` bash
   docker-compose up
```
1. Access the following components:
    - **Frontend**: [http://localhost](http://localhost)
    - **Kafka Topics UI**: [http://localhost:8092](http://localhost:8092)
    - **Debezium UI**: [http://localhost:8089](http://localhost:8089)
    - Services:
        - Order Service: [http://localhost:8086](http://localhost:8086)
        - Payment Service: [http://localhost:8085](http://localhost:8085)
        - Inventory Service: [http://localhost:8084](http://localhost:8084)

## Postman Collection

To explore and test the APIs, use the Postman collection. You can either download it directly or import it using the "Run in Postman" button below.

- [Download Postman Collection](https://github.com/Metelyoff/micro-ecommerce/blob/main/Micro%20E-commerce.postman_collection.json)

[<img src="https://run.pstmn.io/button.svg" alt="Run In Postman" style="width: 128px; height: 32px;">](https://god.gw.postman.com/run-collection/24504602-96dafe1f-3aae-4d9f-8901-df77df17cf2e?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D24504602-96dafe1f-3aae-4d9f-8901-df77df17cf2e%26entityType%3Dcollection%26workspaceId%3D77be33e6-4bcf-4494-a300-c4a8816ee6d2)

## Technologies Used
- **Backend**: Java, Spring Boot, Spring Data JPA
- **Frontend**: React, Bootstrap
- **SSE**: Server side events to notify frontend
- **Messaging**: Apache Kafka
- **Database**: PostgreSQL
- **CDC**: Debezium
- **Containerization**: Docker Compose
- **Monitoring**: Kafka UI, Debezium UI

## Future Enhancements
- Implement CI/CD pipelines for seamless deployment.
- Add more comprehensive testing for scalability and fault-tolerance.
- Publish reusable Outbox dependencies to a public Maven repository.
- Extend the system with additional services like Shipping and Notifications.

## Contributions
Contributions via pull requests are welcome! Please follow the repository's contributing guidelines.
## License
This project is licensed under the MIT License.
