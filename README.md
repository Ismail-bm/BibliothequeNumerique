# Digital Library Management System

This project is a Java desktop application developed in Eclipse for managing a digital library. It follows a layered architecture (Presentation, Service, DAO, and Model) to ensure clean code organization and maintainability.

The application allows users to browse and manage digital documents, organize them into categories, create reading lists, borrow and return documents, leave comments, and receive recommendations. Administrators can manage users, documents, and library resources through an intuitive interface.

## Features

- User authentication and management
- Document management (add, edit, delete, search)
- Category management
- Borrowing and returning documents
- Reading list management
- Shared reading lists
- Comment system
- Book recommendations
- Penalty management for overdue returns
- Database integration using JDBC and MySQL

## Technologies Used

- Java
- Eclipse IDE
- JDBC
- MySQL
- Object-Oriented Programming (OOP)
- DAO Design Pattern
- MVC / Layered Architecture

## Project Structure

- `src/` – Java source code
- `dao/` – Database access layer
- `model/` – Data model classes
- `service/` – Business logic
- `presentation/` – User interface
- `utils/` – Utility classes
- `lib/` – External libraries

## Getting Started

1. Clone the repository.
2. Import the project into Eclipse.
3. Add the required JAR files (if not already included).
4. Configure the MySQL database connection.
5. Run the application.
