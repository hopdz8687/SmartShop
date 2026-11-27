# GitHub Copilot Instructions – SmartShop

Read the repository root `AGENTS.md` first. It contains the authoritative project rules and locked business invariants.

Before implementing a feature, inspect the relevant docs:
- Requirements: `docs/02-Requirements.md`
- Workflows: `docs/04-Business-Workflows.md`
- Database: `docs/06-Database-Design.md` and the approved SQL migration
- API: `docs/07-API-Documentation.md`
- Tests: `docs/09-Test-Plan.md`

Do not invent schema fields, enum values, relationships, or endpoints. Do not change locked MVP decisions without asking. Use Java `Long` for signed BIGINT, `BigDecimal` for money, `EnumType.STRING`, DTOs, Service-layer transactions, and `spring.jpa.hibernate.ddl-auto=none`.

For every code change, add or update relevant tests. Report exactly what was changed and which build/tests were actually run. Update progress and decision documentation when appropriate. See `AGENTS.md` for checkout, stock, cancellation, authorization, Flyway, AI, and scope rules.


## Frontend stack and AI workflow

Approved frontend stack: ReactJS + Vite, Tailwind CSS, shadcn/ui, Lucide React, React Router, Axios, TanStack Query, React Hook Form + Zod. UI UX Pro Max is an Agent Skill for design guidance, not a website dependency.

Before installing/upgrading anything, inspect `package.json`, Node/npm versions, Vite/Tailwind config and `components.json`. Avoid duplicate packages and unapproved UI frameworks. Use shadcn/ui components selectively and Tailwind for styling/responsive behavior. Keep mock services separate from real Axios API services.

Use one primary Agent in traditional Agent Mode with human review. Do not assume Autopilot, Ralph Loop, Multi-Agent or extra browser MCP plugins are required. Prefer built-in browser tools; add Playwright/MCP only for a concrete testing need. Follow `docs/11-Implementation-Guide.md` and `docs/13-AI-Agent-Workflow.md` for setup, task execution and Frontend-first/API-first sequencing.
