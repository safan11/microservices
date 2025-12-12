# Spring Boot Microservices Example

Product Service + Order Service + Feign Client + MySQL
Full CRUD + Business Logic (Order Total Calculation)

---

## 1. Overview

This project contains:

1. **Product Service (8081)**

   * Full CRUD
   * Stores product data (id, name, price)
   * Exposes API for Order Service

2. **Order Service (8082)**

   * Full CRUD
   * Calls Product Service using **Feign Client**
   * Calculates total order amount

No Eureka, no API Gateway (you can add later).

---

## 2. Why DTO Is Used

DTO (Data Transfer Object) solves:

| Problem                           | How DTO Helps                   |
| --------------------------------- | ------------------------------- |
| Exposes database directly         | Prevents leaking entity fields  |
| Entity changes break API          | DTO keeps API stable            |
| Sensitive fields                  | DTO contains safe fields        |
| JPA relationships cause recursion | DTO contains required data only |
| Extra data sent unnecessarily     | API becomes lightweight         |

For small beginner examples → DTO **can be skipped**
For real microservices → DTO is **recommended**

---

## 3. What Is Feign Client?

Feign is a **declarative HTTP client** for microservice communication.

You only write an **interface**, Feign auto-creates the implementation.

Example:

```java
@FeignClient(name = "product-service", url = "http://localhost:8081")
public interface ProductClient {
    @GetMapping("/products/{id}")
    ProductDTO getProduct(@PathVariable Long id);
}
```

No need for:

* RestTemplate
* WebClient
* Manual JSON parsing
* Manually writing URLs

---

# PRODUCT SERVICE (PORT 8081)

---

## 1. Product Entity

`Product.java`

```java
package com.micro.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Product {

    @Id
    private Long id;
    private String name;
    private Double price;

    // Getters and Setters
}
```

---

## 2. Product Repository

`ProductRepository.java`

```java
package com.micro.product.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.micro.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

---

## 3. Product Service

`ProductService.java`

```java
package com.micro.product.service;

import com.micro.product.entity.Product;
import com.micro.product.repo.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Product save(Product product) {
        return repo.save(product);
    }

    public List<Product> getAll() {
        return repo.findAll();
    }

    public Product getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Product update(Long id, Product updated) {
        Product existing = repo.findById(id).orElse(null);

        if (existing == null) {
            return null;
        }

        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());

        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
```

---

## 4. Product Controller

`ProductController.java`

```java
package com.micro.product.controller;

import com.micro.product.entity.Product;
import com.micro.product.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

# ORDER SERVICE (PORT 8082)

---

## 1. Order Entity

`Order.java`

```java
package com.micro.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private Long productId;
    private Integer quantity;
    private Double totalPrice;

    // Getters and Setters
}
```

---

## 2. Order Repository

`OrderRepository.java`

```java
package com.micro.order.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.micro.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
```

---

## 3. ProductDTO (for Feign response)

`ProductDTO.java`

```java
package com.micro.order.dto;

public class ProductDTO {
    private Long id;
    private String name;
    private Double price;

    // Getters and Setters
}
```

---

## 4. Feign Client

`ProductClient.java`

```java
package com.micro.order.client;

import com.micro.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", url = "http://localhost:8081")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductDTO getProduct(@PathVariable Long id);
}
```

---

## 5. Order Service (Business Logic Included)

`OrderService.java`

```java
package com.micro.order.service;

import com.micro.order.client.ProductClient;
import com.micro.order.dto.ProductDTO;
import com.micro.order.entity.Order;
import com.micro.order.repo.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repo;
    private final ProductClient productClient;

    public OrderService(OrderRepository repo, ProductClient productClient) {
        this.repo = repo;
        this.productClient = productClient;
    }

    public Order placeOrder(Order order) {

        ProductDTO p = productClient.getProduct(order.getProductId());

        if (p == null) {
            return null;
        }

        double total = order.getQuantity() * p.getPrice();

        order.setTotalPrice(total);

        return repo.save(order);
    }

    public List<Order> getAll() {
        return repo.findAll();
    }

    public Order getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
```

---

## 6. Order Controller

`OrderController.java`

```java
package com.micro.order.controller;

import com.micro.order.entity.Order;
import com.micro.order.service.OrderService;
import com.micro.order.repo.OrderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;
    private final OrderRepository repo;

    public OrderController(OrderService service, OrderRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping
    public Order place(@RequestBody Order order) {
        return service.placeOrder(order);
    }

    @GetMapping
    public List<Order> getAll() {
        return repo.findAll();
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

# BUSINESS LOGIC SUMMARY

When placing an order:

1. Order Service sends productId to Product Service via Feign
2. Product Service returns product price
3. Order Service calculates

   ```
   totalPrice = quantity × price
   ```
4. Saves the order
5. Returns the response

---

# TESTING

---

## 1. Add Product

POST → `http://localhost:8081/products`

```json
{
  "id": 1,
  "name": "Laptop",
  "price": 50000
}
```

---

## 2. Place Order

POST → `http://localhost:8082/orders`

```json
{
  "productId": 1,
  "quantity": 2
}
```

Response:

```json
{
  "id": 1,
  "productId": 1,
  "quantity": 2,
  "totalPrice": 100000
}
```

---


