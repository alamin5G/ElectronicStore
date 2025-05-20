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

# Unadjusted Function Point Contribution Analysis

## Table 5-1: Transaction Functions Detailed Analysis

### External Inputs (EIs)

| # | Transaction Function | Type | FTRs | DETs | Complexity | UFP |
|---|---------------------|------|------|------|------------|-----|
| 1 | Customer Registration | EI | 1 (User) | ~12 (name, email, pass, phone, address fields) | Average | 4 |
| 2 | Customer Login | EI | 1 (User) | ~3 (email, pass, remember_me) | Low | 3 |
| 3 | Customer Profile Update | EI | 1 (User) | ~10 (name, phone, gender - excluding address, password) | Average | 4 |
| 4 | Add New Address | EI | 2 (Address, User) | ~8 (street, city, zip, country, type, is_default) | Average | 4 |
| 5 | Edit Address | EI | 1 (Address) | ~8 (as above) | Average | 4 |
| 6 | Delete Address | EI | 1 (Address) | ~1 (address_id) | Low | 3 |
| 7 | Add Product to Cart | EI | 2 (CartItem, Product) | ~4 (product_id, quantity, user_id/session_id) | Low | 3 |
| 8 | Update Cart Item Quantity | EI | 1 (CartItem) | ~2 (cart_item_id, new_quantity) | Low | 3 |
| 9 | Remove Cart Item | EI | 1 (CartItem) | ~1 (cart_item_id) | Low | 3 |
| 10 | Place Order | EI | 4 (Order, OrderItem, Product, User) | ~25 (shipping details, items, payment choice) | High | 6 |
| 11 | Submit Payment Details | EI | 1 (Order) | ~3 (order_id, trx_id, payment_method_confirm) | Low | 3 |
| 12 | Admin: Add New Product | EI | 4 (Product, Category, Brand, Warranty) | ~20 (name, desc, price, stock, etc.) | High | 6 |
| 13 | Admin: Edit Product | EI | 4 (Product, Cat, Brand, War) | ~20 (as above + product_id) | High | 6 |
| 14 | Admin: Delete Product | EI | 1 (Product) | ~1 (product_id) | Low | 3 |
| 15 | Admin: Add New Category | EI | 1 (Category) | ~3 (name, desc, parent_cat_id) | Low | 3 |
| 16 | Admin: Edit Category | EI | 1 (Category) | ~3 (as above + cat_id) | Low | 3 |
| 17 | Admin: Delete Category | EI | 1 (Category) | ~1 (cat_id) | Low | 3 |
| 18 | Admin: Add New Brand | EI | 1 (Brand) | ~3 (name, logo_path, desc) | Low | 3 |
| 19 | Admin: Edit Brand | EI | 1 (Brand) | ~3 (as above + brand_id) | Low | 3 |
| 20 | Admin: Delete Brand | EI | 1 (Brand) | ~1 (brand_id) | Low | 3 |
| 21 | Admin: Add New Warranty | EI | 1 (Warranty) | ~3 (type, duration_months, details) | Low | 3 |
| 22 | Admin: Edit Warranty | EI | 1 (Warranty) | ~3 (as above + warranty_id) | Low | 3 |
| 23 | Admin: Delete Warranty | EI | 1 (Warranty) | ~1 (warranty_id) | Low | 3 |
| 24 | Admin: Update Order Status | EI | 1 (Order) | ~2 (order_id, new_status) | Low | 3 |
| 25 | Admin: Update Payment Status | EI | 1 (Order) | ~3 (order_id, payment_status, notes) | Low | 3 |
| 26 | Admin: Toggle User Status | EI | 1 (User) | ~2 (user_id, new_status) | Low | 3 |
| 27 | Admin: Update User Roles | EI | 2 (User, Role) | ~2 (user_id, role_id) | Low | 3 |
| 28 | Admin: Handle Contact Message | EI | 1 (ContactMessage) | ~3 (message_id, reply_text, status) | Low | 3 |
| 29 | Change Password | EI | 1 (User) | ~3 (old_pass, new_pass, confirm_pass) | Low | 3 |
| | **Subtotal for EIs** | | | | | **100** |

### External Outputs (EOs)

| # | Transaction Function | Type | FTRs | DETs | Complexity | UFP |
|---|---------------------|------|------|------|------------|-----|
| 30 | Display Order Confirmation | EO | 4 (Order, OI, User, Product) | ~25 (order details, items, customer info) | Average | 5 |
| 31 | Display Order History List | EO | 2 (Order, User) | ~10 (order_id, date, total, status per order) | Average | 5 |
| 32 | Display Detailed Order | EO | 4 (Order, OI, Prod, User) | ~30 (full order details) | Average | 5 |
| 33 | Display User Profile | EO | 2 (User, Address) | ~15 (profile fields, address fields) | Average | 5 |
| 34 | Display Product Listing | EO | 4 (Prod, Cat, Brand, War) | ~25 (product details, filters, pagination) | Average | 5 |
| 35 | Display Product Details | EO | 4 (Prod, Cat, Brand, War) | ~30 (specs, images, related items) | High | 7 |
| 36 | Display Shopping Cart | EO | 2 (CartItem, Product) | ~15 (item details, quantity, price, total) | Average | 5 |
| 37 | Admin: Dashboard Statistics | EO | 4 (Order, User, Prod, Cat) | ~20 (counts, sums, recent items) | Average | 5 |
| 38 | Admin: List of Products | EO | 1 (Product) | ~15 (id, name, price, stock, category per product) | Average | 5 |
| 39 | Admin: List of Categories | EO | 1 (Category) | ~5 (id, name, product_count per category) | Low | 4 |
| 40 | Admin: List of Brands | EO | 1 (Brand) | ~4 (id, name, logo per brand) | Low | 4 |
| 41 | Admin: List of Warranties | EO | 1 (Warranty) | ~4 (id, type, duration per warranty) | Low | 4 |
| 42 | Admin: List of Orders | EO | 1 (Order) | ~15 (id, customer, date, total, status per order) | Average | 5 |
| 43 | Admin: List of Users | EO | 1 (User) | ~10 (id, name, email, status per user) | Average | 5 |
| 44 | Email: Order Confirmation | EO | 3 (Order, User, OI) | ~20 (templated email with order data) | Average | 5 |
| 45 | Email: Registration | EO | 1 (User) | ~10 (templated email with user data) | Average | 5 |
| | **Subtotal for EOs** | | | | | **74** |

### External Inquiries (EQs)

| # | Transaction Function | Type | FTRs | DETs | Complexity | UFP |
|---|---------------------|------|------|------|------------|-----|
| 46 | Search Products | EQ | 3 (Prod, Cat, Brand) | ~8 (search term, filters → product list) | Average | 4 |
| 47 | View Contact Page | EQ | 0-1 | ~5 (static info or basic fields) | Low | 3 |
| 48 | View About Page | EQ | 0-1 | ~5 (static info) | Low | 3 |
| 49 | Admin: View User Details | EQ | 2 (User, Address) | ~20 (all user fields and addresses) | Average | 4 |
| 50 | Admin: View Product Edit Form | EQ | 4 (Prod, Cat, Brand, War) | ~25 (all product fields in form) | High | 6 |
| 51 | Admin: View Order Details | EQ | 4 (Order, OI, User, Prod) | ~30 (all order data for display) | High | 6 |
| | **Subtotal for EQs** | | | | | **26** |

### Summary

| Function Type | Count | UFP |
|---------------|-------|-----|
| External Inputs (EI) | 29 | 100 |
| External Outputs (EO) | 16 | 74 |
| External Inquiries (EQ) | 6 | 26 |
| **TOTAL** | **51** | **200** |

*Note: FTR = File Type Referenced, DET = Data Element Type, UFP = Unadjusted Function Points*


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