# Notification Service

Multi-channel notification microservice. Consumes domain events from Kafka (`ticket.*`, `solution.*`, `reward.*`, `user.*`) and dispatches **in-app** (DB row) and **email** (Spring Mail) notifications per user preferences + templates. Also exposes REST for direct notification creation, template CRUD, and preference management.

---

## At a glance
| | |
|---|---|
| **Port** | 8087 |
| **Database** | postgres-notification (`notification_db`) |
| **Kafka topics (in)** | `ticket.created`, `ticket.assigned`, `ticket.resolved`, `solution.approved`, `reward.badge.awarded`, `user.password-reset` |
| **Kafka topics (out)** | `notification.sent` (audit trail) |
| **Swagger UI (direct)** | http://localhost:8087/swagger-ui.html |
| **Swagger UI (via gateway)** | http://localhost:8080/swagger-ui.html?urls.primaryName=notification-service |
| **OpenAPI JSON** | http://localhost:8087/v3/api-docs |
| **Java** | 21 (Temurin) |
| **Spring Boot** | 3.2.4 |

---

## What it does
- Consumes 6+ Kafka topics → **renders template** → dispatches per user's channel preferences
- Channels:
  - **in-app** — always available (row in `notifications` table polled by frontend)
  - **email** — SMTP via Spring Mail, gated by `app.email.enabled`
  - **websocket** — real-time broadcast (see `/ws/notifications` endpoint)
- Templates are handlebars-like placeholders (`{{userName}}`, `{{ticketId}}`, etc.) stored in `notification_templates`
- Preferences: per-user per-event-type channel selection

---

## API surface

### Notifications (`/api/notifications/**`)
| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/notifications` | JWT | Create a notification (admin/internal use) |
| GET | `/api/notifications` | JWT | List current user's notifications |
| GET | `/api/notifications/users/{userId}` | JWT | List notifications for a specific user (paginated) |
| GET | `/api/notifications/unread` | JWT | List unread only |
| GET | `/api/notifications/{id}` | JWT | Fetch one |
| PUT | `/api/notifications/{id}/read` | JWT | Mark as read |
| PUT | `/api/notifications/mark-all-read` | JWT | Mark all as read |
| DELETE | `/api/notifications/{id}` | JWT | Delete |
| GET | `/api/notifications/count/unread` | JWT | Unread count badge |
| POST | `/api/notifications/broadcast` | JWT + ADMIN | Send to all users |
| GET | `/api/notifications/search` | JWT | Filter by date/type |

### Templates (`/api/notification-templates/**`)
| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/notification-templates` | JWT + ADMIN | Create template |
| GET | `/api/notification-templates` | JWT | List templates |
| GET | `/api/notification-templates/{id}` | JWT | Fetch one |
| PUT | `/api/notification-templates/{id}` | JWT + ADMIN | Update |
| DELETE | `/api/notification-templates/{id}` | JWT + ADMIN | Delete |
| POST | `/api/notification-templates/{id}/render` | JWT | Preview render with sample payload |

### Preferences (`/api/notification-preferences/**`)
| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/notification-preferences` | JWT | Current user's preferences |
| PUT | `/api/notification-preferences` | JWT | Update own preferences |
| POST | `/api/notification-preferences/reset` | JWT | Reset to defaults |

### Internal (`/internal/**`) — service-to-service
| Method | Path | Purpose |
|---|---|---|
| POST | `/internal/notifications` | Trigger notification directly (skip event bus) |
| GET | `/internal/notifications/by-user/{userId}` | Lookup for audit |

Live: **http://localhost:8087/swagger-ui.html**.

---

## Configuration

| Env var | Yaml key | Default | Purpose |
|---|---|---|---|
| `SERVER_PORT` | `server.port` | `8087` | |
| `SPRING_DATASOURCE_URL` | | `jdbc:postgresql://postgres-notification:5432/notification_db` | |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | | `kafka:9092` | |
| `JWT_SECRET` | `jwt.secret` | (shared) | |
| `APP_EMAIL_ENABLED` | `app.email.enabled` | `false` | Gate on SMTP dispatch |
| `SPRING_MAIL_HOST` | `spring.mail.host` | (unset) | SMTP server |
| `SPRING_MAIL_PORT` | `spring.mail.port` | `587` | |
| `SPRING_MAIL_USERNAME` | `spring.mail.username` | (unset) | |
| `SPRING_MAIL_PASSWORD` | `spring.mail.password` | (unset) | |

`app.email.enabled=false` lets the service start without SMTP creds — email dispatch no-ops but in-app + websocket still work.

---

## Build & run
```bash
./services.sh start notification-service
```

## Docker / K8s
- Manifest: `k8s/notification-service.yaml`
- Service: `notification-service`

---

## Troubleshooting

**Startup fails: "JavaMailSender bean missing"**
`EmailServiceImpl` requires a `JavaMailSender` bean. **Do NOT** exclude `MailSenderAutoConfiguration` in yaml. To disable actual sending, set `app.email.enabled: false` — the bean still exists, dispatch just no-ops.

**`GET /api/notifications` returns 500**
The root `NotificationController` only accepts POST at its base path. `GlobalExceptionHandler` maps `HttpRequestMethodNotSupportedException` → 500 (pre-existing quirk). Use the specific GET endpoints (e.g. `/unread`) or POST as appropriate.

**`GET /api/notifications/users/{userId}` returns 500 (`PropertyReferenceException`)**
`@PageableDefault` in `NotificationController` must sort by `deliveredAt`, not `createdAt`.
The `NotificationRecipient` entity has no `createdAt` field — the column is named `deliveredAt`.

**Kafka consumer crash-loops on startup**
The Java `KafkaConfig.java` `consumerFactory()` bean overrides the YAML config. It must use
`ErrorHandlingDeserializer` wrapping `JsonDeserializer`, with `TRUSTED_PACKAGES = "*"`,
`USE_TYPE_INFO_HEADERS = true`, and `VALUE_DEFAULT_TYPE = NotificationEvent.class.getName()`.
Without `ErrorHandlingDeserializer`, a single malformed payload enters an infinite retry loop.

**`reward.points.added` events not producing "Points Earned" notifications (May 2026 fix)**
Root cause: the reward service publishes without type headers (`ADD_TYPE_INFO_HEADERS: false`). The default `kafkaListenerContainerFactory` uses `USE_TYPE_INFO_HEADERS: true` and falls back to `NotificationEvent`. Spring then tries to convert `NotificationEvent → RewardPointsAddedEvent` and fails.

Fix applied: `KafkaConfig` now has a dedicated `rewardPointsContainerFactory` bean:
- `USE_TYPE_INFO_HEADERS: false`
- `VALUE_DEFAULT_TYPE: RewardPointsAddedEvent.class.getName()`

`consumeRewardPointsAdded` in `NotificationEventConsumer` uses `containerFactory = "rewardPointsContainerFactory"` and accepts a typed `RewardPointsAddedEvent` parameter. The local DTO is in `application/dto/event/RewardPointsAddedEvent.java`.

**`reference_id`/`reference_type` NOT NULL violation on Kafka-triggered notifications (May 2026 fix)**
Root cause: `Notification.java` entity had `@Column(nullable = false)` on `referenceId` and `referenceType`, but the `createAndSend()` convenience helper does not set either field.

Fix applied: removed `nullable = false` from both `@Column` annotations. Also applied to the live DB:
```sql
ALTER TABLE notifications ALTER COLUMN reference_id DROP NOT NULL;
ALTER TABLE notifications ALTER COLUMN reference_type DROP NOT NULL;
```

**Events not reaching the service**
Tail logs: `./services.sh logs notification-service`. Look for `"Consumed event type=…"`. If absent, verify Kafka container + consumer group:
```bash
docker exec kafka kafka-consumer-groups --bootstrap-server kafka:9092 \
  --describe --group notification-service-group
```

**Email dispatch hangs**
With `app.email.enabled=true` and invalid SMTP creds, the dispatcher will block until timeout. Either set valid creds or disable email.

---

## Tech stack
- Java 21 (Temurin)
- Spring Boot 3.2.4
- Spring Kafka (consumer)
- Spring Mail (SMTP)
- Spring WebSocket (STOMP for real-time push)
- Spring Data JPA + PostgreSQL 16
- Spring Security + JJWT
- springdoc-openapi 2.6.0
- Lombok 1.18.34
- `com.kva:common-library` 1.0.0
