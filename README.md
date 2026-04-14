# Order API

A Spring Boot REST API for basic e-commerce order processing — create orders, track status, and cancel pending orders.

---

## Prerequisites

- Java 17+
- Gradle (wrapper included — no installation needed)

---

## Build & Run

**Build and run all tests:**
```bash
./gradlew build
```

**Run the application:**
```bash
./gradlew bootRun
```

The server starts on **`http://localhost:8080`**.

---

## Data Store

The application uses an **H2 in-memory database** (no setup required). The schema is created automatically by Hibernate on startup and dropped on shutdown.

| Setting | Value |
|---------|-------|
| JDBC URL | `jdbc:h2:mem:orderdb` |
| Username | `sa` |
| Password | _(empty)_ |

**Tables:**

| Table | Description |
|-------|-------------|
| `orders` | One row per order — id, customer details, status, timestamps |
| `order_items` | One row per line item — FK to `orders`, product id/name, quantity, price |

**H2 Console** (browser-based DB inspector, available while the app is running):
```
http://localhost:8080/h2-console
```

**Seed data** (`data.sql`) pre-loads 3 orders on every startup:

| Order ID | Status | Items |
|----------|--------|-------|
| `a1b2c3d4-0001-...` | `PENDING` | 1 item |
| `a1b2c3d4-0002-...` | `PROCESSING` | 2 items |
| `a1b2c3d4-0003-...` | `DELIVERED` | 3 items |

---

## Order Status

```
PENDING  →  PROCESSING  →  SHIPPED  →  DELIVERED
   ↓
CANCELLED  (only from PENDING)
```

A background scheduler automatically promotes all `PENDING` orders to `PROCESSING` every **5 minutes**.

---

## API Routes

Base path: `/api/v1/orders`

---

### GET `/api/v1/orders`

Returns a paginated list of all orders. Optionally filter by status.

**Query parameters:**

| Parameter | Required | Description |
|-----------|----------|-------------|
| `status` | No | Filter by status: `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED` |
| `page` | No | Page index, zero-based (default: `0`) |
| `size` | No | Page size (default: `10`) |
| `sort` | No | Sort field and direction (default: `orderCreatedAt,desc`) |

**Example:**
```
GET /api/v1/orders?status=PENDING&page=0&size=5
```
---

### GET `/api/v1/orders/{orderId}`

Returns a single order by its ID.

---

### POST `/api/v1/orders`

Creates a new order. Status is set to `PENDING` and the order ID is generated server-side.

**Request body:**
```json
{
  "items": [
    { "productId": "SKU-001", "productName": "Wireless Mouse", "quantity": 1, "price": 49.99 }
  ],
  "customerDetails": {
    "name": "Jane Doe",
    "phoneNo": "8888888888",
    "address": "2 Sample Street, Springfield"
  }
}
```

**Response `201 Created`:** full `OrderResponse` with generated `orderId` and status `PENDING`.

---

### PUT `/api/v1/orders/{orderId}/cancel`

Cancels an order. Only orders in `PENDING` status can be cancelled.

**Response `200 OK`:** full `OrderResponse` with status `CANCELLED`.

---

## Error Response Shape

All error responses follow a consistent structure:

```json
{
  "statusCode": 404,
  "message": "Human-readable description of the error",
  "timestamp": "2026-04-14T10:30:00"
}
```
