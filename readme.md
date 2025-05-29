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

## 📸 Screenshots
![Home Page](/screenshot/6.png)
![Product Listing](/screenshot/8.png)
![Admin Dashboard](/screenshot/3.png)
![Admin Dashboard](/screenshot/4.png)
![Cart Page](/screenshot/9.png)
![Checkout Page](/screenshot/10.png)
![Select Payment Page](/screenshot/11.png)
![Order Overview](/screenshot/12.png)
![Order Placement details](/screenshot/13.png)
![User Profile](/screenshot/7.png)
![Admin Add Product](/screenshot/5.png)
![Admin User Management](/screenshot/16.png)
![Admin Order Management](/screenshot/14.png)
![Admin Product Management](/screenshot/15.png)
![User Registration](/screenshot/1.png)
![User Login](/screenshot/2.png)
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

| #  | Transaction Function                                  | Type | Est. FTRs           | Est. DETs (Academic MVP) | Revised Complexity | UFP |
|----|-------------------------------------------------------|------|---------------------|-------------------------|--------------------|-----|
|    | **External Inputs (EIs)**                             |      |                     |                         |                    |     |
| 1  | Customer Registration                                | EI   | 1 (User)            | ~8 (core fields)        | Low                | 3   |
| 2  | Customer Login                                       | EI   | 1 (User)            | ~3                      | Low                | 3   |
| 3  | Customer Profile Update (core fields)                | EI   | 1 (User)            | ~6                      | Low                | 3   |
| 4  | Add New Address                                      | EI   | 2 (Address, User)   | ~6                      | Low                | 3   |
| 5  | Edit Address                                         | EI   | 1 (Address)         | ~6                      | Low                | 3   |
| 6  | Delete Address                                       | EI   | 1 (Address)         | ~1                      | Low                | 3   |
| 7  | Add Product to Cart                                  | EI   | 2 (CartItem, Prod)  | ~3                      | Low                | 3   |
| 8  | Update Cart Item Quantity                            | EI   | 1 (CartItem)        | ~2                      | Low                | 3   |
| 9  | Remove Cart Item                                     | EI   | 1 (CartItem)        | ~1                      | Low                | 3   |
| 10 | Place Order (simplified COD)                         | EI   | 3 (Order, OI, Prod) | ~15 (core order details)| Average            | 4   |
| 11 | Submit Payment Details (Manual TrxID)                | EI   | 1 (Order)           | ~2                      | Low                | 3   |
| 12 | Admin: Add New Product (core fields)                 | EI   | 3 (Prod, Cat, Brand)| ~12                     | Average            | 4   |
| 13 | Admin: Edit Product (core fields)                    | EI   | 3 (Prod, Cat, Brand)| ~12                     | Average            | 4   |
| 14 | Admin: Delete Product                                | EI   | 1 (Product)         | ~1                      | Low                | 3   |
| 15 | Admin: Add New Category (simple)                     | EI   | 1 (Category)        | ~2                      | Low                | 3   |
| 16 | Admin: Edit Category (simple)                        | EI   | 1 (Category)        | ~2                      | Low                | 3   |
| 17 | Admin: Delete Category                               | EI   | 1 (Category)        | ~1                      | Low                | 3   |
| 18 | Admin: Add New Brand (simple)                        | EI   | 1 (Brand)           | ~2                      | Low                | 3   |
| 19 | Admin: Edit Brand (simple)                           | EI   | 1 (Brand)           | ~2                      | Low                | 3   |
| 20 | Admin: Delete Brand                                  | EI   | 1 (Brand)           | ~1                      | Low                | 3   |
| 21 | Admin: Add New Warranty (simple)                     | EI   | 1 (Warranty)        | ~2                      | Low                | 3   |
| 22 | Admin: Edit Warranty (simple)                        | EI   | 1 (Warranty)        | ~2                      | Low                | 3   |
| 23 | Admin: Delete Warranty                               | EI   | 1 (Warranty)        | ~1                      | Low                | 3   |
| 24 | Admin: Update Order Status                           | EI   | 1 (Order)           | ~2                      | Low                | 3   |
| 25 | Admin: Update Payment Status                         | EI   | 1 (Order)           | ~2                      | Low                | 3   |
| 26 | Admin: Toggle User Status                            | EI   | 1 (User)            | ~2                      | Low                | 3   |
| 27 | Admin: Update User Roles (basic)                     | EI   | 2 (User, Role)      | ~2                      | Low                | 3   |
| 28 | Admin: Handle Contact Message (view/mark done)       | EI   | 1 (ContactMessage)  | ~2                      | Low                | 3   |
| 29 | Change Password                                      | EI   | 1 (User)            | ~3                      | Low                | 3   |
|    | **Subtotal for EIs**                                 |      |                     |                         |                    | 88  |
|    | **External Outputs (EOs)**                           |      |                     |                         |                    |     |
| 30 | Display Order Confirmation (core details)            | EO   | 3 (Order, OI, User) | ~15                     | Low                | 4   |
| 31 | Display Order History List (basic)                   | EO   | 2 (Order, User)     | ~8                      | Low                | 4   |
| 32 | Display Detailed Order (core details)                | EO   | 3 (Order, OI, Prod) | ~18                     | Low                | 4   |
| 33 | Display User Profile (core details)                  | EO   | 2 (User, Address)   | ~10                     | Low                | 4   |
| 34 | Display Product Listing (basic filters)              | EO   | 3 (Prod, Cat, Brand)| ~15                     | Low                | 4   |
| 35 | Display Product Details (core info)                  | EO   | 3 (Prod, Cat, Brand)| ~20                     | Average            | 5   |
| 36 | Display Shopping Cart (core info)                    | EO   | 2 (CartItem, Prod)  | ~10                     | Low                | 4   |
| 37 | Admin: Dashboard Statistics (key metrics only)       | EO   | 3 (Order, User, Prod)| ~10                    | Low                | 4   |
| 38 | Admin: List of Products (core fields)                | EO   | 1 (Product)         | ~10                     | Low                | 4   |
| 39 | Admin: List of Categories (names only)               | EO   | 1 (Category)        | ~2                      | Low                | 4   |
| 40 | Admin: List of Brands (names only)                   | EO   | 1 (Brand)           | ~2                      | Low                | 4   |
| 41 | Admin: List of Warranties (names only)               | EO   | 1 (Warranty)        | ~2                      | Low                | 4   |
| 42 | Admin: List of Orders (core fields)                  | EO   | 1 (Order)           | ~10                     | Low                | 4   |
| 43 | Admin: List of Users (core fields)                   | EO   | 1 (User)            | ~8                      | Low                | 4   |
| 44 | Email: Order Confirmation (simple template)          | EO   | 2 (Order, User)     | ~10                     | Low                | 4   |
| 45 | Email: Registration (simple template)                | EO   | 1 (User)            | ~5                      | Low                | 4   |
|    | **Subtotal for EOs**                                 |      |                     |                         |                    | 62  |
|    | **External Inquiries (EQs)**                         |      |                     |                         |                    |     |
| 46 | Search Products (basic keyword)                      | EQ   | 1 (Product)         | ~5                      | Low                | 3   |
| 47 | View Contact Page (static)                           | EQ   | 0                   | ~3                      | Low                | 3   |
| 48 | View About Page (static)                             | EQ   | 0                   | ~3                      | Low                | 3   |
| 49 | Admin: View User Details (core info)                 | EQ   | 1 (User)            | ~10                     | Low                | 3   |
| 50 | Admin: View Product Edit Form (core fields)          | EQ   | 1 (Product)         | ~15                     | Low                | 3   |
| 51 | Admin: View Order Details (core info)                | EQ   | 2 (Order, OI)       | ~15                     | Low                | 3   |
|    | **Subtotal for EQs**                                 |      |                     |                         |                    | 18  |
|    | **TOTAL UFP for Transaction Functions**              |      |                     |                         |                    | 168 |

# Unadjusted Function Point Contribution Analysis


| #  | General System Characteristic      | Degree of Influence (DI) |
|----|-----------------------------------|-------------------------|
| 1  | Data Communications               | 3                       |
| 2  | Distributed Data Processing       | 2                       |
| 3  | Performance                       | 4                       |
| 4  | Heavily Used Configuration        | 3                       |
| 5  | Transaction Rate                  | 3                       |
| 6  | Online Data Entry                 | 5                       |
| 7  | End-User Efficiency               | 4                       |
| 8  | Online Update                     | 4                       |
| 9  | Complex Processing                | 3                       |
| 10 | Reusability                       | 3                       |
| 11 | Installation Ease                 | 3                       |
| 12 | Operational Ease                  | 3                       |
| 13 | Multiple Sites                    | 1                       |
| 14 | Facilitate Change                 | 4                       |
|    | **Total Degree of Influence (TDI)** | **49**                |