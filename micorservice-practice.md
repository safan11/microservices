

# 🧠 Microservices Design Question 

## 📌 Problem Statement

Design and implement a **ONE-TO-ONE Microservices system** using **Spring Boot and MySQL**.

The system must consist of **two independent microservices**:

* **Employee Microservice**
* **Laptop Microservice**

Each microservice must have its **own database** and must communicate with other services using **Feign Client**.

---

##  Functional Requirements

### 1. Employee Microservice

* Manage employee information.
* Each employee can be assigned **only ONE laptop**.
* Initially, an employee is created **without any laptop assigned**.
* The employee service must store **only the `laptopId`**, not laptop details.

### 2. Laptop Microservice

* Manage laptop information.
* Laptop service must be **independent** and must not be aware of employee service.

---

##  Relationship Rules

* One Employee → One Laptop
* One Laptop → One Employee
* No database joins are allowed.
* No shared database between services.
* Relationship must be maintained **using IDs and REST communication**.

---

##  Technical Requirements

* Use **Spring Boot** for both microservices.
* Use **MySQL** as the database for both services (separate schemas).
* Implement **CRUD operations** for:

  * Employee
  * Laptop
* Use **Feign Client** in Employee Microservice to fetch laptop details.
* Follow RESTful API design principles.

---

##  APIs to Be Designed

### Employee Microservice APIs

1. Create Employee (Laptop ID initially NULL)
2. Update Employee to assign a Laptop
3. Get Employee details
4. Get Employee along with assigned Laptop details (using Feign Client)
5. Update Employee details
6. Delete Employee

---

### Laptop Microservice APIs

1. Create Laptop
2. Get Laptop by ID
3. Get All Laptops
4. Update Laptop details
5. Delete Laptop

---

## 🔄 Expected Flow

1. Create a Laptop using Laptop Microservice.
2. Create an Employee using Employee Microservice (no laptop assigned initially).
3. Assign a Laptop to an Employee using an update API.
4. Fetch Employee details along with Laptop information.

   * Employee Microservice must call Laptop Microservice using **Feign Client**.
5. Return a combined response containing:

   * Employee details
   * Laptop details

---

##  Expected Output (High Level)

When fetching an employee with laptop details, the response should include:

* Employee basic information
* Laptop information retrieved via Feign Client

---

##  Constraints

* Do not use `@OneToOne`, `@ManyToOne`, or any JPA relationship between services.
* Use Feign Clien for fetching related service data.

---

This question tests:

* Microservices design principles
* One-to-one relationship handling
* Feign Client usage
* REST API design
* Database isolation


