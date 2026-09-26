# Short URL Service

A Kotlin/Spring Boot URL shortener targeting Java 25. It uses a database-backed counter with range allocation, so multiple service instances generate unique Base62 codes without coordinating in memory.

## Why it is safe under concurrency

Each server reserves a non-overlapping counter block from PostgreSQL inside a row-locking transaction. It then serves IDs from that block with an `AtomicLong`; the database is touched again only when the block is exhausted. With the default block size of 1,000, 1,000 creates need one serialized counter reservation rather than 1,000.

The numeric ID is encoded as Base62 (`0-9a-zA-Z`) and stored with a unique database constraint. A server crash may leave unused IDs in its block, so gaps are expected, but IDs are never reused or duplicated. Counter-based codes are enumerable; add an authorization layer or a reversible keyed permutation if codes must be unguessable.

## Requirements

- Java 25
- Docker with Docker Compose for the realistic two-server/PostgreSQL setup
- Python 3 only for the optional concurrency script

The included Gradle wrapper downloads the correct Gradle version automatically.

## Quick local start

This starts one instance with an in-memory H2 database:

```bash
./gradlew bootRun
```

H2 is for local development and automated tests only. Do not run multiple service instances with separate H2 databases.

Check health:

```bash
curl http://localhost:8080/actuator/health
```

## API

| Method | Path | Result |
|---|---|---|
| `POST` | `/api/v1/urls` | Create a short URL (`201`) |
| `GET` | `/api/v1/urls/{code}` | Read metadata (`200`) |
| `DELETE` | `/api/v1/urls/{code}` | Delete it (`204`) |
| `GET` | `/{code}` | Redirect to the destination (`302`) |

An expired code returns `410 Gone`; an unknown/deleted code returns `404`. Invalid input uses RFC 9457-style `application/problem+json` responses. The full machine-readable contract is in `docs/openapi.yaml`.

Create a permanent short URL:

```bash
curl -i -X POST http://localhost:8080/api/v1/urls \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/a/long/path?source=demo"}'
```

Example response (the code varies):

```json
{
  "code": "1",
  "shortUrl": "http://localhost:8080/1",
  "longUrl": "https://example.com/a/long/path?source=demo",
  "createdAt": "2026-09-26T19:00:00Z",
  "expiresAt": null
}
```

Create one that expires:

```bash
curl -i -X POST http://localhost:8080/api/v1/urls \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com","expiresAt":"2030-01-01T00:00:00Z"}'
```

Use the `code` returned above:

```bash
curl http://localhost:8080/api/v1/urls/1
curl -i http://localhost:8080/1
curl -i -X DELETE http://localhost:8080/api/v1/urls/1
```

Use `curl -L http://localhost:8080/1` if you want curl to follow the redirect.

## Run automated tests

```bash
./gradlew test
```

The tests cover Base62 boundaries, URL safety rules, the complete create/read/redirect/delete flow, validation and error responses, and 2,000 concurrent IDs issued through two independently constructed generators sharing one database counter.

## Test two running servers

Start PostgreSQL, two identical application instances, and an Nginx load balancer:

```bash
docker compose up --build -d
curl http://localhost:8080/actuator/health
```

Run 2,000 create requests with 100 concurrent workers. It exits nonzero for any HTTP error, missing response, or duplicate code:

```bash
python3 scripts/concurrency_test.py --requests 2000 --concurrency 100
```

Nginx logs show both upstream instances receiving traffic:

```bash
docker compose logs gateway
```

Stop the stack:

```bash
docker compose down
```

Add `-v` only when you intentionally want to delete the PostgreSQL test data.

## Configuration

| Environment variable | Default | Purpose |
|---|---|---|
| `DB_URL` | in-memory H2 URL | JDBC URL; use PostgreSQL in shared environments |
| `DB_USER` | `sa` | Database user |
| `DB_PASSWORD` | empty | Database password |
| `APP_BASE_URL` | `http://localhost:8080` | Public URL used in API responses |
| `COUNTER_BLOCK_SIZE` | `1000` | IDs reserved per database counter transaction |

For production, use PostgreSQL as the single counter/source of truth, TLS at the gateway, authentication and rate limiting on create/delete, real secrets, backups, and monitoring. Consider destination-domain policy or abuse scanning for a public shortener. Redirect reads can be cached later, but cache invalidation must account for deletion and expiry.
