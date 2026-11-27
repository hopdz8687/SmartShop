# SmartShop – Instructions for AI Coding Agents

These instructions apply to Antigravity, GitHub Copilot, and other coding agents working in this repository.

## 1. Mission and working style

Build **SmartShop**, a Java 21 + Spring Boot + ReactJS + MySQL/MariaDB multi-vendor e-commerce MVP. Follow the project documentation and existing repository conventions. Work incrementally, explain material decisions, and keep documentation synchronized with implemented behavior.

Before coding:
1. Read this file and `README.md`.
2. Read the relevant requirements/workflow/database/API sections.
3. Inspect the actual source tree and existing code. Do not assume files or modules exist.
4. Check the SQL migration/schema before writing JPA entities or queries.

## 2. Source of truth

Use this priority when information conflicts:
1. Approved physical SQL DDL v2.1 for exact database identifiers, types, constraints, indexes, and enum values.
2. Locked business rules in `docs/02-Requirements.md`, `docs/04-Business-Workflows.md`, `docs/06-Database-Design.md`, and `docs/12-Decision-Log.md`.
3. API and Use Case documents.
4. Existing code, which describes only what is currently implemented.

Never invent a database column, table, enum value, relationship, or endpoint. Never silently resolve a contradiction. Report the conflict and ask for a decision if it affects business behavior or schema.

## 3. Locked MVP decisions – do not change without approval

- Architecture: **Modular Monolith + Layered Architecture**.
- Backend: Java 21, Spring Boot, Spring Data JPA/Hibernate, REST API.
- Frontend: ReactJS + Vite, Tailwind CSS, shadcn/ui, Lucide React, React Router, Axios, TanStack Query, React Hook Form + Zod.
- UI UX Pro Max is an agent-side Skill for UI/UX guidance, not a frontend runtime dependency.
- AI workflow: one primary coding Agent in traditional Agent Mode with human review; do not assume Autopilot, Ralph Loop, Multi-Agent, or extra plugins are required.
- Database: MySQL-compatible schema; current development target is MariaDB through XAMPP/phpMyAdmin unless the user specifies otherwise.
- One Seller account owns at most one Store in MVP.
- Multi-vendor checkout creates one parent `orders` row and one `seller_orders` row per Store.
- Each Seller handles only their own Seller Orders.
- MVP payment is COD only; one Payment is associated with each Seller Order in the current schema.
- Persistent cart: one Cart per user. Checkout accepts selected `cartItemIds`; delete only selected cart rows after successful checkout.
- Product variants exist in schema. For MVP, use **flat variants**: `product_variants.variant_name`, SKU, price, and stock. Attribute tables are reserved; do not build attribute-management entities/UI unless explicitly requested.
- Each Product must have at least one active Variant and exactly one default Variant; enforce this in Service because the current database does not fully express this business invariant.
- Product stock belongs to Variant, not Product.
- Order and Order Item keep immutable checkout snapshots.
- Chat MVP uses REST polling; WebSocket/STOMP is future scope.
- AI uses only public SmartShop product catalog data selected by Backend. AI must not invent products/prices/stock and must not execute mutations.
- `spring.jpa.hibernate.ddl-auto=none`; use Flyway migrations.
- Use Java `Long` for signed SQL `BIGINT`; use `BigDecimal` for money; use `EnumType.STRING`.

## 4. Critical order and inventory invariants

### Checkout
- Validate ownership of every selected cart item.
- Re-read current product/variant status, price, and stock on the server. Never trust client-supplied price, seller, or total.
- Group selected cart items by Store.
- Create one parent Order and Seller Orders per Store.
- Persist snapshots: recipient/address on Order; product name, image, variant description, SKU, unit price, quantity, subtotal on Order Item.
- Allocate shipping fee/discount to Seller Orders at checkout; parent totals must equal the sum of child totals.
- Create COD Payment records according to the schema (one per Seller Order).
- Delete only selected cart items, and only after successful order creation, within the same database transaction.
- Any failure must roll back order rows, payments, stock updates, and cart deletions.

### Prevent overselling
Use an atomic conditional stock decrement, conceptually:
```sql
UPDATE product_variants
SET stock_quantity = stock_quantity - :quantity
WHERE id = :variantId
  AND stock_quantity >= :quantity;
```
If affected rows = 0, reject checkout and roll back. Process Variant IDs in ascending order to reduce deadlock risk. Follow actual DDL identifiers and MariaDB compatibility.

### Cancellation
- Customer may cancel a Seller Order only while its status is `PENDING`, and must own the parent Order (`orders.user_id`).
- Seller may cancel only their own Store's Seller Order while status is `PENDING` or `CONFIRMED`, before shipment.
- Use conditional status updates to prevent concurrent duplicate cancellation. Customer condition: `status = 'PENDING'`. Seller condition: `status IN ('PENDING','CONFIRMED')`.
- Restore stock only after the conditional status update succeeds, in the same transaction. A repeated cancellation must never restore stock twice.
- If any step fails, roll back status and stock together.
- Do not modify original monetary snapshots after partial cancellation. Compute the remaining payable amount from non-cancelled Seller Orders.
- If COD has already been marked `PAID`, do not simply mark it `CANCELLED`; a refund workflow is required.

### Status ownership
- `seller_orders.status` is the business lifecycle.
- `shipments.shipment_status` is logistics tracking only.
- Shipment delivery confirmation must be validated by the authorized actor/process; OrderService owns the transition of Seller Order to `DELIVERED`.
- Parent `orders.order_status` is derived centrally by OrderService from non-cancelled Seller Orders. If all children are cancelled, parent is `CANCELLED`. Cancelled children remain visible separately in UI. Do not introduce `PARTIALLY_CANCELLED`.

## 5. Implementation rules

- Keep Controller thin; business rules and transaction boundaries belong in Service.
- Use DTOs for API input/output. Do not expose JPA entities directly.
- Validate request data at the API boundary and enforce authorization in Backend.
- Every Seller query/mutation must be scoped to the authenticated Seller's Store; never trust a client-provided `storeId` as authorization.
- Customer order queries must be scoped to the authenticated user's ID.
- Avoid N+1 queries; choose fetch strategy deliberately.
- Avoid bidirectional JPA relationships unless required. Prefer explicit ownership and simple mappings.
- Do not cascade-delete historical orders, payments, or snapshots.
- Store image files outside MySQL; database stores URL/path and metadata. Never store raw image bytes in ordinary product rows.
- Do not commit secrets, API keys, database passwords, JWT secrets, or `.env` files.
- Keep CORS, JWT, and security configuration explicit and environment-driven.
- Return consistent API error responses; never expose stack traces or secrets.
- Add tests for every business rule, especially authorization, checkout rollback, stock concurrency, cancellation idempotency, and order status aggregation.

## 6. Scope control

Implement MVP first. Do not start or add these without explicit approval:
- Online payment gateway.
- Coupon/promotion engine.
- Carrier API integration.
- WebSocket chat.
- Vector database or embeddings.
- Multi-store ownership per Seller.
- Inventory movement ledger.
- Complex commissions, wallet, livestream, affiliate, auction.

Do not expand the separate EMS practice project. SmartShop is the active project.

## 7. Database and migration safety

- Never use Hibernate schema auto-update.
- Never edit an already-applied Flyway migration.
- Put future schema changes in a new versioned migration.
- Before proposing a schema change, state: reason, affected tables/columns, data migration impact, rollback considerations, and alternative.
- Keep the original importable SQL file separate from Flyway migration if it includes database creation/use commands.
- Flyway migration should target the database configured by Spring Boot and should not contain `CREATE DATABASE` or `USE smartshop`.
- Verify exact MariaDB/MySQL support for SQL features used; do not assume CHECK enforcement is identical across all server versions.

## 8. Frontend and UI implementation rules

- Use the approved frontend stack in `docs/05-System-Architecture.md` and `docs/08-UI-UX-Design.md`.
- Inspect `package.json`, Node/npm versions, Vite/Tailwind configuration and `components.json` before installing or upgrading packages.
- Do not install duplicate packages or add Bootstrap/another UI framework without approval.
- Use shadcn/ui components where appropriate and customize their source in the project; do not assume every component is installed.
- Use Tailwind CSS for styling and responsive behavior. Avoid dynamically constructed class names that Tailwind cannot detect; use complete static class mappings.
- Use React Router for routing, Axios for HTTP, TanStack Query for server state, React Hook Form + Zod for forms/validation, and Lucide React for icons.
- Keep UI UX Pro Max as Agent guidance only; it is not imported by the running website.
- Define/reuse a consistent Design System. New pages must reuse established tokens, layouts and components.
- For UI-first development, use isolated Mock Data/Mock Services until the agreed API Contract is implemented. Do not mix mock data into production API services.
- Backend remains the source of truth for permissions, prices, stock, shipping and order totals.

## 9. Agent workflow and reporting

For each task:
1. State the intended scope and files to inspect/change.
2. Read relevant documentation and inspect the actual repository before acting.
3. Implement the smallest coherent increment.
4. Run relevant tests/build/lint when available.
5. Report changed files, decisions, commands run, test results, and anything not verified.
6. Update `docs/10-Progress-Log.md` and relevant docs when behavior/decisions/status change.
7. Do not claim a test passed unless it was actually run and its result observed.

Use a human-in-the-loop workflow. The user owns product scope, business decisions and acceptance. The Agent may autonomously complete the explicitly scoped task, but must pause for approval before destructive operations, schema/business-rule changes, external side effects, secret handling, broad dependency upgrades or unclear high-impact decisions. Do not keep expanding scope after the acceptance criteria are met.

If information is missing, ask one focused question instead of guessing. If a task can safely proceed without the missing detail, state the assumption explicitly and keep it reversible.
