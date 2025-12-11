# Spring Boot Microservices With Eureka + LoadBalanced WebClient

## Complete Step-by-Step Guide (Eureka | User Service | Order Service)

---

## 1. Overview

Three Spring Boot applications:

```
eureka-server      → service registry (port 8761)
user-service       → returns user data (port 9000)
order-service      → calls user-service using WebClient (port 8000)
```

Order service will call:

```
http://user-service/api/v1/user/{id}
```

(not `localhost:9000`).

To make that work you need:

* `spring-cloud-starter-loadbalancer`
* `@LoadBalanced` on `WebClient.Builder`

---

## 2. Eureka Server (port 8761)

### Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### Main class

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### application.properties

```
server.port=8761
spring.application.name=eureka-server

eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

Start Eureka and open `http://localhost:8761`.

---

## 3. User Service (port 9000)

### Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### application.properties

```
server.port=9000
spring.application.name=user-service

eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

### DTO — User (`com.tcs.entity.User`)

```java
package com.tcs.entity;

public class User {
    private int id;
    private String name;
    private String email;

    public User() {}
    public User(int id, String name, String email) {
        this.id = id; this.name = name; this.email = email;
    }
    // getters and setters
}
```

### Controller (`com.tcs.controller.UserController`)

```java
@RestController
@RequestMapping("/api/v1")
public class UserController {

    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable int id) {
        return new User(id, "Safan", "safan@abc.com");
    }
}
```

**Test in Postman:**

```
GET http://localhost:9000/api/v1/user/1
```

Response:

```json
{ "id":1, "name":"Safan", "email":"safan@abc.com" }
```

---

## 4. Order Service (port 8000)

### Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

### application.properties

```
server.port=8000
spring.application.name=order-service

eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

### DTOs (`com.tcs.dto.User` and `com.tcs.dto.OrderResponseDto`)

`User` (same fields as user-service):

```java
package com.tcs.dto;

public class User {
    private int id;
    private String name;
    private String email;
    public User() {}
    public User(int id, String name, String email) { this.id=id; this.name=name; this.email=email; }
    // getters and setters
}
```

`OrderResponseDto`:

```java
package com.tcs.dto;

public class OrderResponseDto {
    private int id;
    private String item;
    private double price;
    private int userId;
    private String username;
    private String email;

    public OrderResponseDto() {}
    public OrderResponseDto(int id, String item, double price, int userId, String username, String email) {
        this.id=id; this.item=item; this.price=price; this.userId=userId; this.username=username; this.email=email;
    }
    // getters and setters
}
```

### WebClient Configuration (`com.tcs.config.AppConfig`)

```java
@Configuration
public class AppConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

### Controller (`com.tcs.controller.OrderController`)

```java
@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final WebClient webClient;

    public OrderController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("/order/{id}")
    public Mono<OrderResponseDto> getOrder(@PathVariable int id) {
        return webClient.get()
                .uri("http://user-service/api/v1/user/{id}", id)
                .retrieve()
                .bodyToMono(User.class)
                .map(user -> new OrderResponseDto(
                        10001, "laptop", 45000.8,
                        user.getId(), user.getName(), user.getEmail()
                ));
    }
}
```

**Test in Postman:**

```
GET http://localhost:8000/api/v1/order/1
```

Expected response:

```json
{
  "id":10001,
  "item":"laptop",
  "price":45000.8,
  "userId":1,
  "username":"Safan",
  "email":"safan@abc.com"
}
```

---

## 5. Simple step-by-step: why `http://user-service/...` needs `@LoadBalanced`

Follow these steps mentally when Order service calls `http://user-service/api/v1/user/1`:

1. **Order service asks the WebClient to call `http://user-service/...`.**
   WebClient sees the host name `user-service`.

2. **Because `WebClient.Builder` is annotated with `@LoadBalanced`, Spring intercepts the host name.**
   The LoadBalancer component is active (it exists because you added `spring-cloud-starter-loadbalancer`).

3. **LoadBalancer asks Eureka:** “What instances do you have for `user-service`?”
   Eureka replies with one or more entries, e.g. `192.168.1.5:9000`.

4. **LoadBalancer picks one instance (if multiple, it may round-robin).**
   It replaces `http://user-service` with the chosen instance address `http://192.168.1.5:9000`.

5. **WebClient performs the HTTP call to the real address returned by the LoadBalancer.**
   The call succeeds and the response flows back to Order service.

**Key point:**

* Eureka **only holds** registry info (service names + addresses).
* `@LoadBalanced` + LoadBalancer **does the work** of asking Eureka, picking an instance, and replacing the service name with an actual host:port before making the HTTP call.

---

## 6. Start order of services & testing

1. Start **eureka-server** (`port 8761`).
2. Start **user-service** (`port 9000`) — it will register in Eureka.
3. Start **order-service** (`port 8000`) — it will register in Eureka.

Visit `http://localhost:8761` to confirm both user-service and order-service are registered.

Test endpoints in Postman:

* `GET http://localhost:9000/api/v1/user/1` → user JSON
* `GET http://localhost:8000/api/v1/order/1` → order + user JSON

---

## 7. Quick troubleshooting

* If Order returns `UnknownHostException` for `user-service`, check:

  * `spring-cloud-starter-loadbalancer` is in the order-service `pom.xml`.
  * `@LoadBalanced` annotation is present on the `WebClient.Builder` bean.
  * Both services are registered in Eureka UI (`http://localhost:8761`).
* If user-service not visible in Eureka, check its `application.properties` `defaultZone` URL.

