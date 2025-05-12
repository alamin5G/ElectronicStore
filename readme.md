# Electronic Store E-Commerce Application

An advanced Spring Boot e-commerce platform for electronics retail, featuring a complete shopping experience for customers and a robust admin management system.

![Electronic Store Banner](https://placeholder-for-banner-image.com)

## 🚀 Features

### Customer Features
- **User Authentication**: Secure registration and login system
- **Product Browsing**: Search, filter, and sort products by various criteria
- **Shopping Cart**: Add products, update quantities, and manage your selections
- **Checkout Process**: Multi-step checkout with address selection and payment options
- **Order Management**: View order history and track current orders
- **User Profile**: Update personal information and manage addresses

### Admin Features
- **Dashboard**: Overview of sales, orders, stock levels, and customer activity
- **Product Management**: Add, edit, and delete products with image uploads
- **Category & Brand Management**: Organize products effectively
- **Order Management**: Process orders, update statuses, and handle payments
- **User Management**: View and manage customer accounts and permissions
- **Contact Message Management**: Handle customer inquiries

## 🛠️ Technology Stack

### Backend
- **Java 17+**
- **Spring Boot 3+**: Core framework
- **Spring MVC**: Web layer
- **Spring Data JPA**: Data access with Hibernate ORM
- **Spring Security**: Authentication and authorization
- **Jakarta Validation**: Input validation

### Frontend
- **Thymeleaf**: Server-side template engine
- **Bootstrap 5**: Responsive UI framework
- **HTML5 & CSS3**: Structure and styling
- **JavaScript**: Enhanced interactivity

### Database
- **MySQL**: Persistent storage

### Tools & Libraries
- **Lombok**: Reduces boilerplate code
- **ModelMapper**: Object mapping between entities and DTOs
- **Maven**: Build automation and dependency management

## 📋 Requirements

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.8+ (or use included Maven wrapper)

## 🚀 Getting Started

### Setting up the Database
```sql
CREATE DATABASE electronic_store;
CREATE USER 'store_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON electronic_store.* TO 'store_user'@'localhost';
FLUSH PRIVILEGES;
```

### Configuration
1. Clone the repository:
```bash
git clone https://github.com/alamin5G/electronic-store.git
cd electronic-store
```

2. Configure application.properties:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/electronic_store
spring.datasource.username=store_user
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update

# File Upload Configuration
app.storage.location=./uploads
```

### Running the Application
```bash
./mvnw spring-boot:run
```

The application will be available at http://localhost:8080

### Default Admin Credentials
- Username: `admin@electronicstore.com`
- Password: `admin123`

## 🏗️ Project Structure

```
electronic-store/
├── src/
│   ├── main/
│   │   ├── java/com/goonok/electronicstore/
│   │   │   ├── config/        # Configuration classes
│   │   │   ├── controller/    # MVC controllers
│   │   │   ├── dto/           # Data Transfer Objects
│   │   │   ├── enums/         # Enum classes
│   │   │   ├── exception/     # Custom exceptions
│   │   │   ├── model/         # Entity classes
│   │   │   ├── repository/    # JPA repositories
│   │   │   ├── service/       # Business logic services
│   │   │   │   ├── impl/      # Service implementations
│   │   │   │   ├── interfaces/ # Service interfaces
│   │   │   ├── util/          # Utility classes
│   │   │   ├── ElectronicStoreApplication.java
│   │   ├── resources/
│   │   │   ├── static/        # Static resources (CSS, JS)
│   │   │   ├── templates/     # Thymeleaf templates
│   │   │   ├── application.properties
│   ├── test/                  # Test classes
```

## ✨ Endpoints

The application provides numerous endpoints for both customer and administrative functions. Key ones include:

### Public Endpoints
- `/` - Home page
- `/products` - Product catalog
- `/products/{id}` - Product details
- `/search` - Search results
- `/login` - User login
- `/register` - User registration
- `/about` - About us page
- `/contact` - Contact form

### User Endpoints (Authenticated)
- `/cart` - Shopping cart
- `/checkout/*` - Multi-step checkout process
- `/order/history` - Order history
- `/user/profile` - User profile management
- `/user/addresses` - Address book management

### Admin Endpoints
- `/admin/dashboard` - Admin dashboard
- `/admin/products/*` - Product management
- `/admin/users/*` - User management
- `/admin/orders/*` - Order processing
- `/admin/categories/*` - Category management
- `/admin/brands/*` - Brand management

## 👨‍💻 Development

### Adding a New Product
1. Log in as an admin
2. Navigate to `/admin/products`
3. Click "Add New Product"
4. Fill in the product details and upload an image
5. Submit the form

### Processing Orders
1. Log in as an admin
2. Navigate to `/admin/orders`
3. Click on an order to view details
4. Update the order status or payment information
5. Save changes

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -am 'Add new feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Contact

- Md. Alamin - [LinkedIn](https://linkedin.com/in/alamin5g)
- GitHub: [alamin5G](https://github.com/alamin5G)

## 🙏 Acknowledgements

- Spring Boot and Spring Framework teams
- Bootstrap team for the responsive UI framework
- All contributors who have helped improve this project

---

⭐️ If you find this project helpful, please give it a star on GitHub!
