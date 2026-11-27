# SmartShop Workspace Rule (Antigravity)

Always read root `AGENTS.md` before editing code. Treat it as the shared project contract for all agents.

SmartShop is a Java 21 / Spring Boot / Spring Data JPA backend, ReactJS + Vite frontend using Tailwind CSS and shadcn/ui, and MySQL-compatible multi-vendor e-commerce MVP. Follow the approved schema v2.1 and locked business decisions in `docs/`.

Do not invent or silently rename database tables/columns/enums. Inspect the approved SQL DDL before creating Entities or queries. Do not change business rules, add MVP scope, or modify an applied Flyway migration without explicit user approval.

Critical invariants:
- One parent Order per checkout; one Seller Order per Store.
- COD only in MVP; one Payment per Seller Order in current schema.
- Stock is per Product Variant; use conditional atomic decrement and rollback on insufficient stock.
- Customer cancellation only for PENDING and only their own parent Order.
- Seller cancellation only own Store's PENDING/CONFIRMED Seller Order before shipment.
- Restore stock only after successful conditional cancellation, in the same transaction.
- Keep order money snapshots unchanged after partial cancellation.
- Seller Order status is business lifecycle; Shipment status is logistics.
- Order parent status is derived by OrderService from non-cancelled Seller Orders.
- `ddl-auto=none`, Flyway for schema changes, `Long` for signed BIGINT, `BigDecimal` for money, `EnumType.STRING`.

Implement incrementally, test actual behavior, report unverified items honestly, and update progress documentation. Root `AGENTS.md` is the detailed source for all rules.


Frontend rules:
- Use Tailwind CSS + shadcn/ui; do not add Bootstrap or another UI framework without approval.
- Approved supporting packages: Lucide React, React Router, Axios, TanStack Query, React Hook Form, Zod and `@hookform/resolvers`.
- UI UX Pro Max is an Agent Skill, not a runtime dependency.
- Inspect existing package/configuration before installation or upgrade.
- Prefer one primary Agent in traditional Agent Mode; browser automation plugins are optional and need a concrete use case.
- Follow the Design System and keep Mock Data separate from real API clients.
