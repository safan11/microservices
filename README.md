# SPRING BOOT MICROSERVICES EXAMPLE

Product Service (8081) + Order Service (8082) + Feign Client + MySQL

---

# 1. Why Microservices?

## Problems in Monolithic Architecture

* All features (Product, Order, Payment, Users) are in one big project
* Hard to scale individual modules
* A small change needs redeploying entire application
* If one feature crashes, the whole system crashes
* Teams cannot work independently
* Large codebase becomes slow and complex

## How Microservices Solve This

* Each feature is a separate service
* Each has its own database
* Services can be deployed independently
* Easy to scale only the required module
* Better fault isolation
* Faster development and maintenance

---

# 2. How Services Communicate (Feign Client)

Without Feign:

* You must use RestTemplate
* Build URLs manually
* Convert JSON manually
* Handle errors manually

With Feign:

* Only define an interface
* Feign auto-creates the HTTP communication code

Example:

```java
@FeignClient(name = "product-service", url = "http://localhost:8081")
public interface ProductClient {
    @GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable Long id);
}
```

---

# 3. Folder Structure

## Product Service (8081)

```
product-service
│   pom.xml
│   application.properties
└───src/main/java/com/micro/product
    ├── controller
    │     └── ProductController.java
    ├── entity
    │     └── Product.java
    ├── repo
    │     └── ProductRepository.java
    └── service
          ├── ProductService.java
          └── ProductServiceImpl.java
```

---

## Order Service (8082)

```
order-service
│   pom.xml
│   application.properties
└───src/main/java/com/micro/order
    ├── controller
    │     └── OrderController.java
    ├── entity
    │     └── Order.java
    ├── repo
    │     └── OrderRepository.java
    ├── client
    │     └── ProductClient.java
    ├── dto
    │     └── ProductResponse.java
    └── service
          ├── OrderService.java
          └── OrderServiceImpl.java
```

---

# 4. Architecture Diagram (Text-Based)

```
                      +---------------------+
                      |   Product Service   |
                      |   (Port 8081)       |
                      |  Product DB (MySQL) |
                      +----------+----------+
                                 ^
                                 |
                                 | Feign Client Call
                                 |
                      +----------+----------+
                      |    Order Service    |
                      |    (Port 8082)      |
                      |  Order DB (MySQL)   |
                      +----------+----------+
                                 ^
                                 |
                      Postman / Frontend
```

Explanation:

* Frontend/Postman hits the Order Service
* Order Service calls Product Service using Feign
* Product Service returns product details
* Order Service calculates total and stores order
* Both have separate MySQL databases

---

# 5. PRODUCT SERVICE (8081)

## Product Entity

```java
@Entity
public class Product {

    @Id
    private Long id;
    private String name;
    private Double price;

    // Getters & Setters
}
```

## Product Repository

```java
public interface ProductRepository extends JpaRepository<Product, Long> {}
```

## Product Service Interface

```java
public interface ProductService {
    Product save(Product p);
    List<Product> getAll();
    Product getById(Long id);
    Product update(Long id, Product p);
    void delete(Long id);
}
```

## Product Service Implementation

```java
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;

    public ProductServiceImpl(ProductRepository repo) {
        this.repo = repo;
    }

    @Override
    public Product save(Product product) {
        return repo.save(product);
    }

    @Override
    public List<Product> getAll() {
        return repo.findAll();
    }

    @Override
    public Product getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Product update(Long id, Product updated) {
        Product existing = repo.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        return repo.save(existing);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
```

## Product Controller

```java
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public Product save(@RequestBody Product p) {
        return service.save(p);
    }

    @GetMapping
    public List<Product> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Product getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product p) {
        return service.update(id, p);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Product deleted";
    }
}
```

---

# 6. ORDER SERVICE (8082)

## Order Entity

```java
@Entity
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private Long productId;
    private Integer quantity;
    private Double totalPrice;

    // Getters & Setters
}
```

## Order Repository

```java
public interface OrderRepository extends JpaRepository<Order, Long> {}
```

## Product Response DTO

```java
public class ProductResponse {
    private Long id;
    private String name;
    private Double price;
}
```

## Feign Client

```java
@FeignClient(name = "product-service", url = "http://localhost:8081")
public interface ProductClient {
    @GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable Long id);
}
```

## Order Service Interface

```java
public interface OrderService {
    Order placeOrder(Order order);
    List<Order> getAll();
    Order getById(Long id);
    void delete(Long id);
}
```

## Order Service Implementation

```java
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repo;
    private final ProductClient productClient;

    public OrderServiceImpl(OrderRepository repo, ProductClient productClient) {
        this.repo = repo;
        this.productClient = productClient;
    }

    @Override
    public Order placeOrder(Order order) {
        ProductResponse product = productClient.getProduct(order.getProductId());
        if (product == null) return null;

        double total = order.getQuantity() * product.getPrice();
        order.setTotalPrice(total);

        return repo.save(order);
    }

    @Override
    public List<Order> getAll() {
        return repo.findAll();
    }

    @Override
    public Order getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
```

## Order Controller

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public Order place(@RequestBody Order order) {
        return service.placeOrder(order);
    }

    @GetMapping
    public List<Order> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Order getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Order deleted";
    }
}
```

---

# 7. Postman Testing URLs

## Product Service (8081)

* Add Product
  POST → `http://localhost:8081/products`

* Get All Products
  GET → `http://localhost:8081/products`

* Get Product by ID
  GET → `http://localhost:8081/products/1`

* Update Product
  PUT → `http://localhost:8081/products/1`

* Delete Product
  DELETE → `http://localhost:8081/products/1`

---

## Order Service (8082)

* Place Order
  POST → `http://localhost:8082/orders`

* Get All Orders
  GET → `http://localhost:8082/orders`

* Get Order by ID
  GET → `http://localhost:8082/orders/1`

* Delete Order
  DELETE → `http://localhost:8082/orders/1`

---

# 8. Business Logic Flow

1. Order Service receives productId and quantity
2. It calls Product Service using Feign Client
3. Product Service returns product price
4. Order Service calculates:

   ```
   totalPrice = quantity × price
   ```
5. Order is saved in Order DB
6. Response is returned



Would you like these added?
