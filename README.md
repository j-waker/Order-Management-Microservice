**Order Management Service**

A Spring Boot microservice for managing a product catalog and processing orders with integrated caching and data pagination.

---


**How to Run the Service**

  _Prerequisites:_ Java 21 or higher.

  _Build:_
  
    ./mvnw clean install

  _Run:_
  
    ./mvnw spring-boot:run

  _Access Swagger UI:_
  
    http://localhost:8080/swagger-ui/index.html
  Note:
  
  - default login "**user**"
  
  - default password generated on startup, check logs for a line starting with "**Using generated security password:**"

  _Access Auto-Generated Docs:_

    http://localhost:8080/v3/api-docs


---

**How to Run Tests**

  All Tests:
    
    ./mvnw test

  Unit Tests: 
    
    ./mvnw test -Dgroups="unit"

  Integration Tests: 
  
    ./mvnw test -Dgroups="integration"

  Note: The test suite uses an in-memory H2 database and does not require external setup.


---

**Assumptions & Design Notes**

Domain Logic

  - Multiple Quantities: Supported via an OrderItem join entity. Products are submitted via json response with "id" and "quantity" fields.

  - Identity: Orders use UUIDs rather than incremental IDs.

Performance & Scaling

  - Caching: Implemented on high-traffic endpoints. Caches are evicted on data modification to ensure consistency.

  - Pagination: All listing endpoints require Pageable parameters to prevent memory exhaustion with large datasets.

Resilience

  - Concurrency: The service is designed to simultaneous order requests. Order entities implement Persistable to ensure safe inserts with manually assigned UUIDs.

  - Transactional Integrity: All order operations are wrapped in @Transactional boundaries to prevent partial data writes.

Security

  - Stateless: The API uses Basic Authentication and does not maintain server-side sessions.
