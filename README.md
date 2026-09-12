# Settlement Funding Assignment

Spring Boot service that selects the combination of settlement instructions with the maximum total expected fee without exceeding the available settlement balance.

## Technology

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Gradle
- Docker Compose

## Requirements

- Java 21 or newer
- Docker Desktop

## Database Setup

Start PostgreSQL with Docker Compose:

```powershell
docker compose up -d
```

The default database configuration is:

```text
Database: kursi
Username: kursi
Password: kursi
Host: localhost
Port: 5432
```

Flyway automatically applies SQL migrations from `src/main/resources/db/migration/` when the application starts. Hibernate validates the migrated schema with `spring.jpa.hibernate.ddl-auto=validate`.

Stop PostgreSQL without deleting data:

```powershell
docker compose down
```

Delete the PostgreSQL container and local volume:

```powershell
docker compose down -v
```

The `-v` option deletes local database data.

## Build And Run

Run the tests:

```powershell
.\gradlew.bat clean test
```

Build the executable JAR:

```powershell
.\gradlew.bat clean bootJar
```

The JAR is created at:

```text
build/libs/settlement-funding-0.0.1-SNAPSHOT.jar
```

Run the application:

```powershell
java -jar .\build\libs\settlement-funding-0.0.1-SNAPSHOT.jar
```

The API is available at `http://localhost:8080`.

Database settings can be overridden with environment variables:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/kursi"
$env:DB_USERNAME="kursi"
$env:DB_PASSWORD="kursi"
```

## API

## Postman collection

I personally had issues testing app endpoints via `curl`, so there is Postman Collection json included in repository root for convinience in testing, filename: `postman_collection_v*.json`

---

### 1. Fund Settlement

`POST /api/v1/settlement/fund`

This endpoint accepts candidate instructions, selects the combination with the maximum total fee, persists the funding run, and returns the selected instructions.

PowerShell request:

```powershell
$body = @'
{
	"availableSettlementBalance": 20000,
	"candidateInstructions": [
		{
			"instructionReference": "INS-2001",
			"instructionAmount": 7000,
			"expectedFee": 150
		},
		{
			"instructionReference": "INS-2002",
			"instructionAmount": 9000,
			"expectedFee": 210
		},
		{
			"instructionReference": "INS-2003",
			"instructionAmount": 4000,
			"expectedFee": 90
		},
		{
			"instructionReference": "INS-2004",
			"instructionAmount": 6000,
			"expectedFee": 130
		}
	]
}
'@

Invoke-RestMethod `
	-Uri "http://localhost:8080/api/v1/settlement/fund" `
	-Method Post `
	-ContentType "application/json" `
	-Body $body
```

Successful response: `201 Created`

```json
{
	"requestId": "f1a2b3c4-d5e6-7890-abcd-123456789000",
	"selectedInstructions": [
		{
			"instructionReference": "INS-2001",
			"instructionAmount": 7000,
			"expectedFee": 150
		},
		{
			"instructionReference": "INS-2002",
			"instructionAmount": 9000,
			"expectedFee": 210
		},
		{
			"instructionReference": "INS-2003",
			"instructionAmount": 4000,
			"expectedFee": 90
		}
	],
	"totalSettlementConsumed": 20000,
	"totalExpectedFee": 450,
	"createdAt": "2026-09-13T09:00:00Z"
}
```

The response also includes a `Location` header containing the URL of the created funding request.

If no instruction fits, the request is still persisted and the current API returns `200 OK` with an empty selection and zero totals:

```json
{
	"requestId": "f1a2b3c4-d5e6-7890-abcd-123456789000",
	"selectedInstructions": [],
	"totalSettlementConsumed": 0,
	"totalExpectedFee": 0,
	"createdAt": "2026-09-13T09:00:00Z"
}
```

### 2. Get One Funding Request

`GET /api/v1/settlement/{requestId}`

```powershell
curl.exe -i `
	"http://localhost:8080/api/v1/settlement/f1a2b3c4-d5e6-7890-abcd-123456789000"
```

Successful response: `200 OK`

```json
{
	"requestId": "f1a2b3c4-d5e6-7890-abcd-123456789000",
	"selectedInstructions": [
		{
			"instructionReference": "INS-2001",
			"instructionAmount": 7000,
			"expectedFee": 150
		}
	],
	"totalSettlementConsumed": 7000,
	"totalExpectedFee": 150,
	"createdAt": "2026-09-13T09:00:00Z"
}
```

If the UUID does not exist, the endpoint returns `404 Not Found`:

```json
{
	"error": "Funding request not found: f1a2b3c4-d5e6-7890-abcd-123456789000"
}
```

### 3. List Funding Requests

`GET /api/v1/settlement?page=0&size=20`

```powershell
curl.exe -i `
	"http://localhost:8080/api/v1/settlement?page=0&size=20"
```

Successful response: `200 OK`

```json
{
	"content": [
		{
			"requestId": "f1a2b3c4-d5e6-7890-abcd-123456789000",
			"selectedInstructions": [
				{
					"instructionReference": "INS-2001",
					"instructionAmount": 7000,
					"expectedFee": 150
				}
			],
			"discardedInstructions": [
				{
					"instructionReference": "INS-2002",
					"instructionAmount": 9000,
					"expectedFee": 210
				}
			],
			"totalSettlementConsumed": 7000,
			"totalExpectedFee": 150,
			"createdAt": "2026-09-13T09:00:00Z"
		}
	],
	"totalElements": 1,
	"totalPages": 1,
	"size": 20,
	"number": 0,
	"first": true,
	"last": true
}
```

Results are ordered newest first. Use `page` and `size` query parameters to navigate through the audit history.

## Validation

Invalid input returns `400 Bad Request`. The request must contain a non-negative available balance and at least one candidate instruction. Each instruction must have:

- a non-blank reference
- a positive instruction amount
- a positive expected fee

Example invalid request:

```powershell
curl.exe -i -X POST `
	"http://localhost:8080/api/v1/settlement/fund" `
	-H "Content-Type: application/json" `
	--data-raw '{"availableSettlementBalance":20000,"candidateInstructions":[]}'
```

## Database Design

The schema has two tables.

### `funding_requests`

Stores one funding run and its calculated totals:

- `request_id`: UUID primary key
- `available_settlement_balance`: balance available for selection
- `total_settlement_consumed`: amount consumed by selected instructions
- `total_expected_fee`: fee earned from selected instructions
- `created_at`: creation timestamp

Check constraints ensure that balances and totals are non-negative and that consumed balance does not exceed the available balance.

### `funding_instructions`

Stores the candidate instructions belonging to a funding request:

- `id`: generated primary key
- `request_id`: foreign key to `funding_requests`
- `instruction_reference`: human-readable instruction label
- `instruction_amount`: full amount consumed if selected
- `expected_fee`: fee earned if selected
- `is_selected`: selection result persisted for audit purposes
- `instruction_order`: original request order

The foreign key uses `ON DELETE CASCADE`, so instructions are removed when their funding request is removed. The `(request_id, instruction_order)` unique constraint preserves one unique input position per request.

## Indexes

The `funding_requests(created_at DESC)` index supports the paginated audit query, which returns funding runs newest first.

The composite `funding_instructions(request_id, is_selected, instruction_order)` index supports loading instructions for a request, grouping them by selection state, and preserving their original input order.

## Architecture

```text
Controller
		-> Service
				-> FundingAlgorithm
				-> FundingRequestRepository
						-> FundingRequest
								-> FundingInstruction
```

- Controllers handle HTTP requests and responses.
- DTOs define API input and output.
- Services contain business logic and transaction boundaries.
- The algorithm solves the 0/1 knapsack selection problem.
- The repository handles persistence.
- Flyway manages versioned database migrations.

## Tests

Run all tests:

```powershell
.\gradlew.bat clean test
```

The test report is generated at:

```text
build/reports/tests/test/index.html
```

The tests cover algorithm behavior, funding selection, empty selections, total calculations, selected/discarded audit instructions, validation errors, missing funding requests, and all three API endpoints.
