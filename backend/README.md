# Backend

## Tech Stack

- Spring Boot 3
- MyBatis-Plus
- MySQL 8
- JWT
- Lombok
- Maven
- JDK 21

## Structure

```text
backend
|- src/main/java/com/dormrepair
|  |- common
|  |- config
|  |- security
|  |- auth
|  |- user
|  |- order
|  |- category
|  |- record
|  |- evaluation
|  |- file
|  `- stats
`- src/main/resources
```

## Run

This initial commit is a runnable backend skeleton. Database access is not enabled yet, so MySQL is not required for startup.

1. Ensure `JAVA_HOME` points to JDK 21.
2. Run `mvn spring-boot:run` or start `DormRepairApplication`.
3. Visit `GET /ping`.
