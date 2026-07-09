<p align="center">
  <img src="docs/assets/logo.svg" alt="ClaimsAI Logo" width="80"/>
</p>

<h1 align="center">ClaimsAI</h1>
<p align="center">
  <strong>AI-Powered Insurance Claims Management Platform</strong>
</p>

<p align="center">
  A production-grade, full-stack microservices application built with <b>Java 17</b>, <b>Spring Boot 3</b>, <b>Angular 18</b>, and <b>Claude AI</b> — designed to modernize how insurance companies process, analyze, and manage claims.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk" alt="Java"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3-green?style=flat-square&logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Angular-18-red?style=flat-square&logo=angular" alt="Angular"/>
  <img src="https://img.shields.io/badge/Spring%20AI-Claude-blueviolet?style=flat-square" alt="Spring AI"/>
  <img src="https://img.shields.io/badge/Kafka-Event%20Driven-blue?style=flat-square&logo=apachekafka" alt="Kafka"/>
  <img src="https://img.shields.io/badge/Elasticsearch-Search-yellow?style=flat-square&logo=elasticsearch" alt="Elasticsearch"/>
  <img src="https://img.shields.io/badge/Docker-Containerized-blue?style=flat-square&logo=docker" alt="Docker"/>
  <img src="https://img.shields.io/badge/PostgreSQL-pgvector-blue?style=flat-square&logo=postgresql" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Tests-JUnit%205%20%7C%20Testcontainers%20%7C%20Jasmine-brightgreen?style=flat-square" alt="Tests"/>
</p>

---

## The Problem

Insurance companies process **millions of claims annually**, yet most still rely on:
- Manual claim review and classification — slow, error-prone, expensive
- Keyword-based search that misses relevant historical claims
- No automated fraud detection until it's too late
- Siloed systems where adjusters lack context across policies and claims

**ClaimsAI** solves these problems by bringing **AI, event-driven architecture, and semantic search** into a unified platform — the same patterns used at companies like **Desjardins**, **Intact Financial**, and **Coveo**.

---

## Key Features

| Feature | Description |
|---------|-------------|
| **AI Claims Chat** | Natural language Q&A about claims, policies, and documents powered by Claude AI with RAG pipeline |
| **Fraud Detection** | AI-driven fraud scoring (0.0–1.0) with red flag identification on every new claim |
| **Smart Search** | Coveo-style semantic search with fuzzy matching, relevance ranking across all claims |
| **Real-time Events** | Kafka-driven event pipeline — claim creation triggers AI analysis + search indexing automatically |
| **Claims Lifecycle** | Full workflow: Submit → AI Triage → Adjuster Review → Investigation → Approve/Deny → Settle |
| **Role-Based Access** | Four roles (Policyholder, Adjuster, Underwriter, Admin) with JWT + OAuth2 security |
| **Audit Trail** | MongoDB-backed audit logging for compliance and regulatory requirements |
| **Monitoring** | Prometheus + Grafana dashboards, Elasticsearch + Kibana for log analytics |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                 Angular 18 Frontend (:4200)                   │
│          Dark Glass UI • Material • Lazy Loading • RxJS      │
└──────────────────────────┬───────────────────────────────────┘
                           │ nginx reverse proxy
┌──────────────────────────▼───────────────────────────────────┐
│              Spring Cloud API Gateway (:8080)                 │
│                 JWT Validation • Route Management             │
└───┬────────┬────────┬────────┬────────┬──────────────────────┘
    │        │        │        │        │
┌───▼───┐┌───▼───┐┌───▼───┐┌───▼───┐┌───▼───┐
│ Auth  ││Policy ││Claims ││  AI   ││Search │
│ :8081 ││ :8082 ││ :8083 ││ :8084 ││ :8085 │
│       ││       ││       ││       ││       │
│ JWT   ││ CRUD  ││Kafka  ││ RAG   ││  ES   │
│ BCrypt││ JPA   ││Events ││Claude ││Mongo  │
└───┬───┘└───┬───┘└───┬───┘└───┬───┘└───┬───┘
    │        │        │    ┌───┘        │
    ▼        ▼        ▼    ▼            ▼
┌─────────────────────────────────────────────┐
│  PostgreSQL    │  Apache Kafka  │  MongoDB  │
│  + pgvector    │  (Events)      │  (Audit)  │
│                │                │           │
│  Elasticsearch │  Prometheus    │  Grafana  │
│  (Search)      │  (Metrics)     │  (Dash)   │
└─────────────────────────────────────────────┘
```

---

## Tech Stack

### Backend
| Technology | Purpose |
|-----------|---------|
| Java 17 | Language runtime |
| Spring Boot 3.3 | Microservice framework |
| Spring Cloud Gateway | API Gateway with reactive routing |
| Spring Security | JWT + OAuth2 authentication |
| Spring AI + Claude | LLM integration for chat, summarization, fraud detection |
| Spring Data JPA | ORM with Hibernate |
| Apache Kafka | Event-driven messaging between microservices |
| PostgreSQL + pgvector | Relational DB + vector embeddings for RAG |
| MongoDB | Document store for audit logs |
| Elasticsearch | Full-text semantic search with relevance ranking |
| ONNX Transformers | Local embedding model (all-MiniLM-L6-v2) for vector store |

### Frontend
| Technology | Purpose |
|-----------|---------|
| Angular 18 | SPA framework with standalone components |
| Angular Material | UI component library |
| RxJS | Reactive state management |
| SCSS | Dark glass-morphism theme with animations |

### DevOps
| Technology | Purpose |
|-----------|---------|
| Docker + Docker Compose | One-command containerized deployment |
| nginx | Frontend reverse proxy |
| Prometheus + Grafana | Metrics and monitoring dashboards |
| Kibana | Elasticsearch visualization |
| Multi-stage Docker builds | Optimized container images |

---

## Quick Start

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- [Git](https://git-scm.com/)

### One-Command Setup

```bash
# 1. Clone the repository
git clone https://github.com/Pulkit2306/ClaimsAI.git
cd ClaimsAI

# 2. Start everything (builds + runs all services)
docker-compose up --build

# 3. Seed sample data (in a separate terminal)
bash scripts/seed-data.sh
```

### Open the App

| Service | URL |
|---------|-----|
| **Frontend** | [http://localhost:4200](http://localhost:4200) |
| **API Gateway** | [http://localhost:8080](http://localhost:8080) |
| **Grafana** | [http://localhost:3000](http://localhost:3000) (admin/admin) |
| **Kibana** | [http://localhost:5601](http://localhost:5601) |
| **Prometheus** | [http://localhost:9090](http://localhost:9090) |

### Login Credentials (after seeding)

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@claimsplatform.com` | `password123` |
| Adjuster | `amelie@claimsplatform.com` | `password123` |
| Underwriter | `luc@claimsplatform.com` | `password123` |

---

## Development Setup (Without Docker)

For active development, run infrastructure in Docker and services locally:

```bash
# Start databases and messaging only
docker-compose up postgres mongodb kafka zookeeper elasticsearch -d

# Build all modules
mvn clean install -DskipTests

# Run each service in a separate terminal
cd auth-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd policy-service && mvn spring-boot:run
cd claims-service && mvn spring-boot:run
cd ai-service && mvn spring-boot:run
cd search-service && mvn spring-boot:run

# Frontend
cd frontend && npm install && ng serve
```

---

## Microservices Overview

| Service | Port | Responsibilities |
|---------|------|-----------------|
| **API Gateway** | 8080 | Request routing, JWT validation, CORS, rate limiting |
| **Auth Service** | 8081 | User registration, login, BCrypt hashing, JWT token generation |
| **Policy Service** | 8082 | CRUD for insurance policies and customers (Auto, Home, Health, Life, Commercial, Travel) |
| **Claims Service** | 8083 | Claims lifecycle management, Kafka event publishing on create/update/status change |
| **AI Service** | 8084 | RAG pipeline with pgvector, fraud detection, claim summarization, document Q&A, WebSocket streaming |
| **Search Service** | 8085 | Elasticsearch multi-match search with fuzziness, Kafka consumer for auto-indexing, MongoDB audit trail |

---

## AI Features

ClaimsAI integrates **Spring AI** with the **Claude** language model for three core capabilities:

### 1. Conversational Claims Assistant
Ask natural language questions about claims, policies, and coverage. The RAG pipeline retrieves relevant document chunks from pgvector before generating context-aware responses.

### 2. Automated Fraud Detection
Every new claim triggers a Kafka event consumed by the AI service. Claude analyzes the claim for fraud indicators and returns a risk score (0.0–1.0), red flags, and a recommendation (Approve / Investigate / Deny).

### 3. Claim Summarization
One-click AI-generated summaries for adjusters — extracts key facts, damage assessment, and recommended next steps from claim descriptions and supporting documents.

> **Demo Mode:** The AI features work without an API key using intelligent mock responses. Set `ANTHROPIC_API_KEY` in `.env` for live Claude-powered responses.

---

## Event-Driven Architecture

```
Claims Service                    AI Service              Search Service
     │                                │                        │
     │──── claim.created ────────────▶│ Fraud Analysis         │
     │                                │                        │
     │──── claim.created ────────────────────────────────────▶│ Index to ES
     │                                                         │ Audit to MongoDB
     │──── claim.status.changed ─────────────────────────────▶│ Re-index
     │                                                         │ Audit log
     │──── claim.updated ───────────────────────────────────▶│ Update index
```

Kafka topics: `claim.created`, `claim.updated`, `claim.status.changed`

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `ANTHROPIC_API_KEY` | `demo-mode` | Claude API key for live AI (optional) |
| `POSTGRES_USER` | `postgres` | Database username |
| `POSTGRES_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | (built-in) | JWT signing secret |

Copy `.env.example` to `.env` to customize.

---

## Project Structure

```
claims-management-platform/
├── api-gateway/          # Spring Cloud Gateway
├── auth-service/         # Authentication & JWT
├── policy-service/       # Policy & Customer management
├── claims-service/       # Claims lifecycle + Kafka
├── ai-service/           # RAG, Fraud Detection, Summarization
├── search-service/       # Elasticsearch + MongoDB audit
├── common/               # Shared DTOs, enums, exceptions, JWT util
├── config-server/        # Spring Cloud Config (centralized config)
├── frontend/             # Angular 18 SPA
│   ├── src/app/core/     # Services, guards, interceptors, models
│   ├── src/app/features/ # Auth, Dashboard, Claims, Policies, AI Chat, Search
│   └── src/app/shared/   # Navbar, pipes, shared components
├── scripts/              # Data seeding scripts
├── docker-compose.yml    # One-command full stack deployment
├── Dockerfile.service    # Multi-stage build for Java services
├── prometheus.yml        # Prometheus scrape config
└── init-databases.sql    # PostgreSQL database initialization
```

---

## Screenshots

> The UI features a modern dark glass-morphism design with gradient accents, animated transitions, and responsive layouts.

- **Dashboard** — Real-time claim statistics with trend indicators
- **Claims List** — Sortable table with status pills and fraud score bars
- **Claim Detail** — AI analysis panel with fraud scoring and one-click summarization
- **AI Chat** — ChatGPT-style interface with suggestion chips and typing indicators
- **Smart Search** — Semantic search with relevance-ranked results
- **Policies** — Card grid with type-specific icons and coverage details

---

## Testing

The project includes a full test suite covering unit, integration, and frontend tests.

### Run Java Unit Tests

```bash
# All services at once
mvn test

# Individual service
cd auth-service && mvn test
cd policy-service && mvn test
cd claims-service && mvn test
cd ai-service && mvn test
```

### Run Integration Tests (requires Docker)

Testcontainers spins up real PostgreSQL and Kafka containers automatically — no manual setup needed.

```bash
cd claims-service && mvn test -Dgroups=integration
```

### Run Angular Tests

```bash
cd frontend && ng test
# Headless (CI)
cd frontend && ng test --watch=false --browsers=ChromeHeadless
```

### Test Coverage

| Layer | Framework | What's Covered |
|-------|-----------|----------------|
| Auth Service | JUnit 5 + Mockito | `AuthService` (register, login, role assignment), `JwtUtil` (token generation, validation, expiry) |
| Policy Service | JUnit 5 + Mockito | `PolicyService` (CRUD, soft delete, customer validation) |
| Claims Service | JUnit 5 + Mockito | `ClaimService` (lifecycle, stats aggregation, fraud threshold query) |
| Claims Service | Testcontainers | Full integration against real PostgreSQL + Kafka (create, status update, pagination, adjuster assignment) |
| AI Service | JUnit 5 + Mockito | `RagService` (demo mode routing, session persistence), `FraudDetectionService` (JSON parsing, fallback on AI failure) |
| Angular | Jasmine + Karma | `AuthService`, `ClaimService` (all HTTP endpoints), `ClaimListComponent` (render, pagination, fraud gradient) |

---

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is open source and available under the [MIT License](LICENSE).

---

<p align="center">
  Built with ❤️ by <a href="https://github.com/Pulkit2306">Pulkit</a>
</p>
