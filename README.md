# CSV Bulk Import (Spring Boot + PostgreSQL)

Import large CSV files of customers into PostgreSQL **fast and with flat memory use**, and compare it with the
straightforward approach many projects start with.

| | Naive importer | Streaming importer |
|---|---|---|
| Reading the file | whole file in memory (`readAllBytes`) | line by line (`BufferedReader`) |
| Writing rows | JPA `save()` per row, one transaction each | JDBC batches of 1,000 rows |
| Duplicate emails | `SELECT` per row before insert | unique index + `ON CONFLICT DO NOTHING` |
| Bad rows | reported with line number | reported with line number, import continues |
| Memory | grows with file size | constant |

## Benchmark

Run it on your machine (numbers depend on hardware):

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.arguments="--bench.rows=100000"
```

The app generates a test file (≈1% invalid rows, ≈0.5% duplicates), runs both importers on it and logs a table:

| Strategy | Rows | Inserted | Duplicates | Rejected | Time | Rows/s |
|---|---:|---:|---:|---:|---:|---:|
| _paste your result here_ | | | | | | |

## API

```bash
curl -F file=@customers.csv "http://localhost:8080/api/imports?strategy=streaming"
```

Example response for a 4-row file with one duplicate and one bad email:

```json
{
  "strategy": "streaming",
  "totalRows": 4,
  "inserted": 2,
  "duplicates": 1,
  "rejected": 1,
  "errors": [ { "line": 3, "message": "invalid email: broken-email" } ],
  "millis": 12
}
```

CSV format (header required, quoted fields and `""` escapes supported):

```csv
email,full_name,phone,city
an@example.com,"Nguyen, Van An",0901234567,Ha Noi
```

`strategy=naive` runs the baseline importer. A wrong header returns `400` with a Problem Details body.

## Run

Requirements: JDK 17+, Docker.

```bash
docker compose up -d          # PostgreSQL 17 on localhost:5432
./mvnw spring-boot:run        # Flyway creates the table on start
./mvnw verify                 # unit tests + integration tests on a real PostgreSQL (Testcontainers)
```

## Design notes

- **Batch size 1,000** (`app.import.batch-size`): large enough to remove most network round trips, small enough
  that one failing batch is cheap to retry.
- **Each batch commits on its own.** If the import stops halfway, the rows already imported stay, and running the
  same file again only inserts what is missing, because duplicates are skipped by the unique index.
- **Errors are capped** (`app.import.max-reported-errors`, default 100) so a broken file cannot fill the memory
  with error messages; the rest are still counted.
- Emails are lower-cased before insert, so `AN@example.com` and `an@example.com` are the same customer.

## Tech

Java 17 · Spring Boot 4 · Spring MVC · Spring Data JPA · JdbcTemplate · PostgreSQL 17 · Flyway ·
JUnit 5 · AssertJ · Testcontainers · Docker Compose · GitHub Actions
