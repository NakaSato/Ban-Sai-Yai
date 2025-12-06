# Ban Sai Yai Savings Group - AI Coding Instructions

## Project Overview

A **financial accounting system for Thai savings groups** with:

- **Backend**: Spring Boot 4.0 (Java 21) + MariaDB + JWT authentication
- **Frontend**: React 18 + TypeScript + Material-UI + Redux Toolkit + Vite

## Architecture Pattern

### Backend (Layered Architecture)

```
Controller (@RestController) → Service (@Service) → Repository (JpaRepository)
     ↓                              ↓                        ↓
   DTOs                    Business Logic              JPA Entities
```

- **Role-based authorization** via `@PreAuthorize` annotations on controller methods
- Four user roles: `PRESIDENT` > `SECRETARY` > `OFFICER` > `MEMBER`
- JWT authentication with refresh tokens, rate limiting on login

### Frontend (RTK Query Pattern)

```
Pages → Redux Store (authSlice/uiSlice) → API Slices (RTK Query) → Backend /api/*
```

- All API calls go through `frontend/src/store/api/*.ts` using RTK Query
- Use `useAppDispatch` and `useAppSelector` from `hooks/redux.ts` (typed hooks)
- API base URL is `/api` (proxied in Vite dev mode)

## Key Conventions

### Backend Java

- **Package structure**: `com.bansaiyai.bansaiyai.{controller,service,repository,entity,dto,security,config}`
- **Entity naming**: Use `@Entity` classes in `/entity/`, all extend `BaseEntity`
- **DTOs**: Request/Response DTOs in `/dto/` - never expose entities directly to API
- **Database migrations**: Flyway in `src/main/resources/db/migration/V*.sql`

### Frontend React/TypeScript

- **Path alias**: Use `@/` to import from `src/` (e.g., `@/components`, `@/store`)
- **Types**: All interfaces/enums defined in `frontend/src/types/index.ts`
- **Constants**: Routes, permissions, API config in `frontend/src/constants/index.ts`
- **Lazy loading**: Page components use `React.lazy()` - see `App.tsx`
- **Form handling**: Use `react-hook-form` with `yup` validation

## Critical Business Rules (Thai Savings Group)

- Loan amount cannot exceed **5x member's share capital**
- Members need **6 months tenure** before loan eligibility
- Maximum **2 active loans** per member
- Loan types: Personal (50K), Business (200K), Emergency (20K), Education (100K), Housing (500K) THB
- See `docs/reference/business-rules.md` for complete rules

## Development Commands

### Backend (from project root)

```bash
./mvnw spring-boot:run              # Start dev server (port 9090)
./mvnw clean package -DskipTests    # Build JAR
./mvnw test                         # Run tests
```

### Frontend (from /frontend)

```bash
bun dev                    # Dev server (port 5173, proxies to :9090)
bun run build              # Production build
bun run test               # Jest tests
bun run test:coverage      # Coverage report (70% threshold)
```

### Docker Deployment

```bash
./deploy.sh                         # Build and deploy with docker-compose
docker-compose up --build -d        # Manual deployment
```

## API Patterns

### Backend Controller Example

```java
@GetMapping
@PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
public ResponseEntity<Page<Entity>> getAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) { ... }
```

### Frontend RTK Query Example

```typescript
// In store/api/xxxApi.ts - inject into apiSlice
export const xxxApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    getItems: builder.query<PaginatedResponse<Item>, FilterParams>({
      query: (params) => ({ url: "/items", params }),
      providesTags: ["Item"],
    }),
  }),
});
```

## Testing Patterns

### Frontend Tests

- Test files: `__tests__/*.test.tsx` alongside components
- Mock setup in `frontend/src/test/setup.ts`
- Use `renderWithProviders()` from `test/utils.tsx` for Redux-connected components

### Backend Tests

- Use `@SpringBootTest` for integration tests
- `@WebMvcTest` for controller-only tests
- H2 database for test profile

## File Structure Reference

| Concern        | Backend Location                    | Frontend Location            |
| -------------- | ----------------------------------- | ---------------------------- |
| API routes     | `/controller/*.java`                | `/store/api/*.ts`            |
| Business logic | `/service/*.java`                   | `/store/slices/*.ts`         |
| Data types     | `/entity/*.java`, `/dto/*.java`     | `/types/index.ts`            |
| Auth           | `/security/*.java`                  | `/store/slices/authSlice.ts` |
| Config         | `/config/*.java`, `application.yml` | `/constants/index.ts`        |
