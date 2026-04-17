# Notification Service

A comprehensive event-driven notification microservice built with Spring Boot 3.2, featuring Kafka integration, real-time WebSocket notifications, and multi-channel delivery (in-app + email).

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           NOTIFICATION SERVICE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   REST API  │  │  WebSocket  │  │    Kafka    │  │   Internal  │        │
│  │  Controller │  │   /ws/...   │  │  Consumer   │  │    APIs     │        │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
│         │                │                │                │               │
│         └────────────────┴────────────────┴────────────────┘               │
│                                   │                                         │
│                    ┌──────────────┴──────────────┐                         │
│                    │      Application Layer      │                         │
│                    │  ┌────────────────────────┐ │                         │
│                    │  │  NotificationService   │ │                         │
│                    │  │  PreferenceService     │ │                         │
│                    │  │  TemplateService       │ │                         │
│                    │  │  EmailService          │ │                         │
│                    │  └────────────────────────┘ │                         │
│                    └──────────────┬──────────────┘                         │
│                                   │                                         │
│                    ┌──────────────┴──────────────┐                         │
│                    │     Infrastructure Layer    │                         │
│                    │  ┌────────────────────────┐ │                         │
│                    │  │     Repositories       │ │                         │
│                    │  │     Kafka Producer     │ │                         │
│                    │  │     Retry Service      │ │                         │
│                    │  └────────────────────────┘ │                         │
│                    └──────────────┬──────────────┘                         │
│                                   │                                         │
│                    ┌──────────────┴──────────────┐                         │
│                    │        PostgreSQL           │                         │
│                    └─────────────────────────────┘                         │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Features

- **Event-Driven Architecture**: Kafka-based event processing
- **Multi-Channel Delivery**: In-app and email notifications
- **Real-Time Updates**: WebSocket support for instant notifications
- **User Preferences**: Configurable notification preferences per user
- **Template System**: Dynamic notification templates
- **Delivery Tracking**: Complete delivery logs with retry mechanism
- **Role-Based Access**: ENGINEER and ADMIN roles with JWT authentication
- **Statistics & Analytics**: Notification metrics and statistics

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Security** (JWT)
- **Spring Data JPA**
- **Spring Kafka**
- **Spring WebSocket**
- **PostgreSQL**
- **Liquibase** (Database migrations)
- **MapStruct** (Object mapping)
- **Lombok**
- **Swagger/OpenAPI 3**

## API Endpoints

### User APIs (ENGINEER + ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications/users/{userId}` | Get user notifications (paginated) |
| GET | `/api/notifications/{id}` | Get notification by ID |
| PUT | `/api/notifications/{id}/read` | Mark notification as read |
| PUT | `/api/notifications/users/{userId}/read-all` | Mark all as read |
| DELETE | `/api/notifications/{id}` | Delete notification |
| GET | `/api/notifications/users/{userId}/unread-count` | Get unread count |

### Admin APIs (ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/notifications` | Create notification |
| POST | `/api/notifications/send` | Create and send notification |
| POST | `/api/notifications/{id}/send` | Send existing notification |
| GET | `/api/notifications/statistics` | Get statistics |

### Preference APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/preferences/{userId}` | Get user preferences |
| PUT | `/api/preferences/{userId}` | Update preferences |
| POST | `/api/preferences/{userId}` | Create default preferences |

### Template APIs (ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications/templates` | Get all templates |
| GET | `/api/notifications/templates/{id}` | Get template by ID |
| POST | `/api/notifications/templates` | Create template |
| PUT | `/api/notifications/templates/{id}` | Update template |
| DELETE | `/api/notifications/templates/{id}` | Delete template |

### Internal APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/internal/notifications/event` | Process notification event |
| POST | `/internal/notifications/broadcast` | Broadcast notification |

### WebSocket

| Endpoint | Description |
|----------|-------------|
| `/ws/notifications` | WebSocket connection endpoint |
| `/user/{userId}/queue/notifications` | User-specific notifications |
| `/user/{userId}/queue/unread-count` | User unread count updates |
| `/topic/notifications` | Broadcast notifications |

## Kafka Topics

### Consumed Topics

| Topic | Description |
|-------|-------------|
| `ticket.created` | New ticket created |
| `ticket.assigned` | Ticket assigned to user |
| `ticket.resolved` | Ticket resolved |
| `solution.approved` | Solution approved |
| `knowledge.created` | Knowledge article created |
| `reward.points.added` | Reward points added |
| `reward.badge.awarded` | Badge awarded |
| `leaderboard.updated` | Leaderboard updated |

### Published Topics

| Topic | Description |
|-------|-------------|
| `notification.sent` | Notification sent successfully |
| `notification.failed` | Notification delivery failed |

## Database Schema

```sql
-- notifications
CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    reference_id UUID,
    reference_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- notification_recipients
CREATE TABLE notification_recipients (
    recipient_id UUID PRIMARY KEY,
    notification_id UUID REFERENCES notifications,
    user_id UUID NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    delivered_at TIMESTAMP
);

-- notification_preferences
CREATE TABLE notification_preferences (
    preference_id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    email_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    ticket_updates BOOLEAN DEFAULT TRUE,
    solution_updates BOOLEAN DEFAULT TRUE,
    knowledge_updates BOOLEAN DEFAULT TRUE,
    reward_updates BOOLEAN DEFAULT TRUE
);

-- notification_templates
CREATE TABLE notification_templates (
    template_id UUID PRIMARY KEY,
    event_type VARCHAR(100) UNIQUE NOT NULL,
    title_template VARCHAR(255) NOT NULL,
    message_template TEXT NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- notification_delivery_logs
CREATE TABLE notification_delivery_logs (
    delivery_id UUID PRIMARY KEY,
    notification_id UUID REFERENCES notifications,
    user_id UUID NOT NULL,
    delivery_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    attempted_at TIMESTAMP NOT NULL,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0
);

-- notification_activity_logs
CREATE TABLE notification_activity_logs (
    log_id UUID PRIMARY KEY,
    notification_id UUID REFERENCES notifications,
    action VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | notification_db |
| `DB_USERNAME` | Database username | postgres |
| `DB_PASSWORD` | Database password | postgres |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka servers | localhost:9092 |
| `JWT_SECRET` | JWT secret key | (required) |
| `MAIL_HOST` | SMTP host | smtp.gmail.com |
| `MAIL_PORT` | SMTP port | 587 |
| `MAIL_USERNAME` | SMTP username | - |
| `MAIL_PASSWORD` | SMTP password | - |
| `EMAIL_ENABLED` | Enable email sending | false |

## Running Locally

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Apache Kafka 3.x

### Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd notification-service
   ```

2. **Configure database**
   ```bash
   createdb notification_db
   ```

3. **Set environment variables**
   ```bash
   export DB_HOST=localhost
   export DB_PASSWORD=your_password
   export JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-32-characters-long
   export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access Swagger UI**
   ```
   http://localhost:8080/swagger-ui.html
   ```

## Docker

### Build Image

```bash
docker build -t notification-service:latest .
```

### Run Container

```bash
docker run -p 8086:8086 \
  -e DB_HOST=host.docker.internal \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=your-secret \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  notification-service:latest
```

## CI/CD

The project includes GitHub Actions workflows for:

- **Build**: Compile and run tests
- **Test**: Run unit and integration tests with JaCoCo coverage
- **Docker**: Build and push Docker images
- **Deploy**: Deploy to INT, UAT, and PROD environments

### Environments

| Environment | Image Tag | Branch |
|-------------|-----------|--------|
| Integration | `notification-service:int` | develop |
| UAT | `notification-service:uat` | main |
| Production | `notification-service:prod` | main (manual) |

## Testing

### Run Tests

```bash
./mvnw test
```

### Generate Coverage Report

```bash
./mvnw jacoco:report
```

Coverage report available at: `target/site/jacoco/index.html`

**Minimum coverage requirement: 80%**

## Project Structure

```
src/
├── main/
│   ├── java/com/cognizant/notificationservice/
│   │   ├── adapter/web/
│   │   │   ├── controller/          # REST controllers
│   │   │   └── exception/           # Exception handlers
│   │   ├── application/
│   │   │   ├── dto/                 # DTOs (request/response/event)
│   │   │   ├── mapper/              # MapStruct mappers
│   │   │   └── service/             # Service interfaces
│   │   ├── domain/
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── enums/               # Enumerations
│   │   │   └── exception/           # Domain exceptions
│   │   └── infrastructure/
│   │       ├── config/              # Configuration classes
│   │       ├── kafka/               # Kafka consumer/producer
│   │       ├── repository/          # JPA repositories
│   │       ├── security/            # Security configuration
│   │       ├── service/             # Service implementations
│   │       └── websocket/           # WebSocket services
│   └── resources/
│       ├── application.yaml         # Application configuration
│       └── db/changelog/            # Liquibase migrations
└── test/
    └── java/com/cognizant/notificationservice/
        ├── controller/              # Controller tests
        ├── kafka/                   # Kafka tests
        ├── security/                # Security tests
        └── service/                 # Service tests
```

## License

Copyright 2024 Cognizant. All rights reserved.
