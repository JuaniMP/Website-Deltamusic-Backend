# Backend - Record & Vinyl Store

This is the backend for a website dedicated to selling records and vinyls. The backend is built with Java and Spring Boot, and is connected to a local MySQL database. It provides all the necessary APIs for the frontend (developed in Angular and TypeScript) to manage products, users, purchases, payments, statistics, and more.

## Main Features

- RESTful API for managing records, vinyls, users, carts, purchases, and payments.
- Integration with a payment gateway for secure transactions.
- Admin endpoints to view statistics, transactions, audits, customers, and purchases.
- Customer endpoints for managing shopping cart and performing purchases.
- Connection to a local MySQL database.
- Authentication and authorization for admin and client roles.
- Still under development: some features and improvements are planned.

## Tech Stack

- **Backend:** Java, Spring Boot
- **Database:** MySQL (local)
- **Frontend:** Angular, TypeScript (see separate repo)

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/JuaniMP/BackEnd.git
   cd BackEnd
   ```
2. Configure the database:

   Create a MySQL database locally and update your `application.properties` (or `application.yml`) with your database credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/YOUR_DATABASE
   spring.datasource.username=YOUR_USER
   spring.datasource.password=YOUR_PASSWORD
   ```

3. Build and run the project:
   ```bash
   ./mvnw spring-boot:run
   ```
   Or, if using Maven directly:
   ```bash
   mvn spring-boot:run
   ```

## Project Structure

- `src/main/java/` - Main source code (controllers, services, models, repositories)
- `src/main/resources/` - Configuration files
- `pom.xml` - Project dependencies and build configuration

## Usage

- The backend exposes a REST API consumed by the Angular frontend.
- Admin users can access analytics, transactions, audits, customers, and purchase management endpoints.
- Clients can browse records/vinyls, manage their shopping cart, and make purchases.

## Contributions

Contributions are welcome! If you want to improve the project, please open an issue or submit a pull request.

## License

This project currently does not have a specified license.

## Author

- [JuaniMP](https://github.com/JuaniMP)
