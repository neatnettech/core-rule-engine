# Rule Engine Demo

Example Spring Boot application showcasing the core-rule-engine library.

## Prerequisites

- Java 17+
- Maven
- Docker (for MongoDB)

## Quick Start

### 1. Start MongoDB

```bash
docker-compose up -d
```

### 2. Install the library (from project root)

```bash
cd ../..
mvn clean install -DskipTests
```

### 3. Run the demo app

```bash
cd examples/rule-engine-demo
mvn spring-boot:run
```

The app starts at `http://localhost:8080`

---

## API Endpoints

### Initialize Sample Rules

Creates 4 sample rules (2 decision tables, 1 decision tree, 1 validation rule):

```bash
curl -X POST http://localhost:8080/api/rules/init-samples
```

**Response:**
```json
{"status":"Sample rules created successfully"}
```

---

### Evaluate Rules

#### Example 1: Premium Customer Discount (Decision Table)

Premium customer with order > $100 gets 20% discount:

```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "customerType": "premium",
      "orderTotal": 150
    },
    "category": "PRICING",
    "subCategory": "DEFAULT",
    "hitPolicy": "FIRST"
  }'
```

**Response:**
```json
{
  "results": [
    {
      "ruleName": "Premium Customer Discount",
      "matched": true,
      "output": {
        "discountPercent": 20,
        "message": "Premium customer discount applied"
      }
    }
  ]
}
```

#### Example 2: No Match

Regular customer with small order:

```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "customerType": "regular",
      "orderTotal": 50
    },
    "category": "PRICING",
    "subCategory": "DEFAULT",
    "hitPolicy": "FIRST"
  }'
```

**Response:**
```json
{
  "results": [
    {
      "ruleName": "Premium Customer Discount",
      "matched": false,
      "output": {}
    }
  ]
}
```

#### Example 3: Collect All Matches

Use `COLLECT` hit policy to get all matching rules:

```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "customerType": "premium",
      "orderTotal": 250
    },
    "category": "PRICING",
    "subCategory": "DEFAULT",
    "hitPolicy": "COLLECT"
  }'
```

**Response:** (both discount rules match)
```json
{
  "results": [
    {
      "ruleName": "Premium Customer Discount",
      "matched": true,
      "output": {"discountPercent": 20, "message": "Premium customer discount applied"}
    },
    {
      "ruleName": "Standard Customer Discount",
      "matched": true,
      "output": {"discountPercent": 5, "message": "Bulk order discount applied"}
    }
  ]
}
```

#### Example 4: Email Validation

```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "email": "user@example.com"
    },
    "category": "VALIDATION",
    "subCategory": "DEFAULT",
    "hitPolicy": "FIRST"
  }'
```

**Response:**
```json
{
  "results": [
    {
      "ruleName": "Email Required",
      "matched": true,
      "output": {"valid": true}
    }
  ]
}
```

#### Example 5: Invalid Email

```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "email": "invalid-email"
    },
    "category": "VALIDATION",
    "subCategory": "DEFAULT",
    "hitPolicy": "FIRST"
  }'
```

**Response:**
```json
{
  "results": [
    {
      "ruleName": "Email Required",
      "matched": false,
      "output": {}
    }
  ]
}
```

#### Example 6: Loan Approval (Decision Tree)

Approved case (age >= 21, income >= 50000, creditScore >= 700):

```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "age": 30,
      "income": 75000,
      "creditScore": 750
    },
    "category": "WORKFLOW",
    "subCategory": "DEFAULT",
    "hitPolicy": "FIRST"
  }'
```

Rejected (underage):

```bash
curl -X POST http://localhost:8080/api/rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "age": 18,
      "income": 75000,
      "creditScore": 750
    },
    "category": "WORKFLOW",
    "subCategory": "DEFAULT",
    "hitPolicy": "FIRST"
  }'
```

---

### Create Custom Rules

#### Create Decision Table Rule

```bash
curl -X POST http://localhost:8080/api/rules/decision-table \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Free Shipping",
    "description": "Free shipping for orders over $50",
    "category": "PRICING",
    "subCategory": "DEFAULT",
    "conditions": [
      {"expression": "orderTotal >= 50"}
    ],
    "results": {
      "freeShipping": true,
      "message": "Free shipping applied"
    }
  }'
```

#### Create Decision Tree Rule

```bash
curl -X POST http://localhost:8080/api/rules/decision-tree \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Risk Assessment",
    "description": "Transaction risk assessment",
    "category": "WORKFLOW",
    "subCategory": "DEFAULT",
    "rootCondition": {
      "condition": "amount > 10000",
      "trueBranch": {
        "condition": "verified == true",
        "trueBranch": {
          "action": "'\''LOW_RISK'\''"
        },
        "falseBranch": {
          "action": "'\''HIGH_RISK'\''"
        }
      },
      "falseBranch": {
        "action": "'\''LOW_RISK'\''"
      }
    }
  }'
```

---

## Sample Rules Reference

After calling `/api/rules/init-samples`, these rules are created:

| Rule Name | Type | Category | Conditions | Output |
|-----------|------|----------|------------|--------|
| Premium Customer Discount | Decision Table | PRICING | customerType == 'premium' AND orderTotal > 100 | discountPercent: 20 |
| Standard Customer Discount | Decision Table | PRICING | orderTotal > 200 | discountPercent: 5 |
| Loan Approval Process | Decision Tree | WORKFLOW | age/income/creditScore checks | APPROVED/REVIEW/REJECTED |
| Email Required | Decision Table | VALIDATION | email != null AND email.contains('@') | valid: true |

---

## Available Categories

```
DEFAULT, GENERAL, VALIDATION, PRICING, WORKFLOW
```

## Hit Policies

| Policy | Description |
|--------|-------------|
| `FIRST` | Stop on first matching rule (default) |
| `COLLECT` | Evaluate all rules, return all matches |

---

## Performance Endpoints

### Run Benchmark

Run a full benchmark with configurable iterations:

```bash
# Default: 1000 iterations, 100 warmup, single thread
curl -X POST "http://localhost:8080/api/performance/benchmark"

# Custom: 5000 iterations, 500 warmup, 4 threads
curl -X POST "http://localhost:8080/api/performance/benchmark?iterations=5000&warmup=500&parallel=4"
```

**Response:**
```json
{
  "iterations": 1000,
  "threads": 1,
  "totalDurationMs": 45.2,
  "avgMs": 0.045,
  "minMs": 0.02,
  "maxMs": 0.15,
  "p50Ms": 0.04,
  "p95Ms": 0.08,
  "p99Ms": 0.12,
  "throughputPerSecond": 22123.5
}
```

### Cold vs Warm Comparison

Compare first call (cold) vs subsequent calls (warm cache):

```bash
curl -X POST http://localhost:8080/api/performance/cold-vs-warm
```

**Response:**
```json
{
  "firstCallMs": 2.5,
  "avgWarmMs": 0.05,
  "speedupFactor": 50.0,
  "allTimingsMs": [2.5, 0.04, 0.05, 0.04, 0.06, 0.05, 0.04, 0.05, 0.04, 0.05]
}
```

### Single Evaluation with Timing

```bash
curl -X POST http://localhost:8080/api/performance/single \
  -H "Content-Type: application/json" \
  -d '{"customerType": "premium", "orderTotal": 150}'
```

**Response:**
```json
{
  "durationMs": 0.045,
  "rulesEvaluated": 2,
  "matched": true
}
```

---

## Cleanup

```bash
docker-compose down -v
```
