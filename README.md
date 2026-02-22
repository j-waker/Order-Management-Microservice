**Order Management Service**

A Spring Boot microservice for managing a product catalog and processing orders with integrated caching and data pagination.

---


**How to Run the Service**

  Prerequisites: Java 21 and Maven installed.
  
  Build: Run $mvn clean install.
  
  Run: Run $mvn spring-boot:run.

  Access:

        API: http://localhost:8080/api

        Swagger UI: http://localhost:8080/swagger-ui/index.html

---

**How to Run Tests**

  All Tests: mvn test

  Concurrency Specific: mvn test -Dtest=OrderConcurrencyTest

  Note: The test suite uses an in-memory H2 database and does not require external setup.


---

**Assumptions & Design Notes**

Domain Logic

    Multiple Quantities: Supported via an OrderItem join entity. Users can submit duplicate Product IDs in a single request (e.g., [1, 1, 2]) to increase quantities.

    Identity: Orders use UUIDs rather than incremental IDs.

Performance & Scaling

    Caching: Implemented on high-traffic endpoints. Caches are evicted on data modification to ensure consistency.

    Pagination: All listing endpoints require Pageable parameters to prevent memory exhaustion with large datasets.

Resilience

    Concurrency: The service is designed to simultaneous order requests. Order entities implement Persistable to ensure safe inserts with manually assigned UUIDs.

    Transactional Integrity: All order operations are wrapped in @Transactional boundaries to prevent partial data writes.

Security

    Stateless: The API uses Basic Authentication and does not maintain server-side sessions.
