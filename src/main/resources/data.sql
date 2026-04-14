-- ─────────────────────────────────────────────────────────────────────────────
-- Seed data for local H2 development
-- Orders: 1 PENDING (1 item), 1 PROCESSING (2 items), 1 DELIVERED (3 items)
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO orders (order_id, customer_name, phone_no, address, status, order_created_at, order_modified_at)
VALUES
    ('a1b2c3d4-0001-0000-0000-000000000001',
     'Alice Johnson', '9876543210', '12 Maple Avenue, Springfield',
     'PENDING', '2026-04-10 09:00:00', '2026-04-10 09:00:00'),

    ('a1b2c3d4-0002-0000-0000-000000000002',
     'Bob Smith', '8765432109', '34 Oak Street, Shelbyville',
     'PROCESSING', '2026-04-11 14:00:00', '2026-04-12 08:30:00'),

    ('a1b2c3d4-0003-0000-0000-000000000003',
     'Carol White', '7654321098', '56 Pine Road, Capital City',
     'DELIVERED', '2026-04-08 10:00:00', '2026-04-13 17:00:00');

-- Order 1 — PENDING: 1 item
INSERT INTO order_items (order_id, product_id, product_name, quantity, price)
VALUES
    ('a1b2c3d4-0001-0000-0000-000000000001', 'p1q2r3s4-0001-0000-0000-000000000001','PROD-NOTEBOOK-A4', 2, 14.99);

-- Order 2 — PROCESSING: 2 items
INSERT INTO order_items (order_id, product_id, product_name,quantity, price)
VALUES
    ('a1b2c3d4-0002-0000-0000-000000000002', 'p1q2r3s4-0001-0000-0000-000000000002', 'PROD-WIRELESS-MOUSE', 1, 49.99),
    ('a1b2c3d4-0002-0000-0000-000000000002', 'p1q2r3s4-0001-0000-0000-000000000003', 'PROD-USB-HUB-4PORT', 1, 24.99);

-- Order 3 — DELIVERED: 3 items
INSERT INTO order_items (order_id, product_id, product_name, quantity, price)
VALUES
    ('a1b2c3d4-0003-0000-0000-000000000003', 'p1q2r3s4-0001-0000-0000-000000000004', 'PROD-MECHANICAL-KB', 1, 129.99),
    ('a1b2c3d4-0003-0000-0000-000000000003', 'p1q2r3s4-0001-0000-0000-000000000005', 'PROD-MONITOR-27IN',  1, 349.99),
    ('a1b2c3d4-0003-0000-0000-000000000003', 'p1q2r3s4-0001-0000-0000-000000000006', 'PROD-HDMI-CABLE-2M', 3, 9.99);
