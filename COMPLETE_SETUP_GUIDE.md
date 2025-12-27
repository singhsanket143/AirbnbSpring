# Complete CDC Setup Guide - MySQL to Redis with Debezium

This guide explains the complete setup of Change Data Capture (CDC) using Debezium to replicate MySQL booking records to Redis.

## Architecture Overview

```
MySQL 8.0 (Docker) 
  ↓ (binlog)
Debezium Connector (Kafka Connect)
  ↓
Kafka Topic (booking-events)
  ↓
Redis Consumer (Python script)
  ↓
Redis (Docker)
```

## Prerequisites

- Docker and Docker Compose installed
- Python 3.x with pip
- Ports available: 3306, 6379, 9092, 8083, 2181

## Step-by-Step Setup

### Step 1: Create Docker Compose Configuration

**File: `docker-compose.yml`**

This file defines all services:
- **MySQL 8.0**: Database with binlog enabled (required for CDC)
- **Redis 7**: Cache/database for replicated data
- **Zookeeper**: Required for Kafka
- **Kafka**: Message broker for CDC events
- **Kafka Connect**: Runs Debezium connectors

Key MySQL configuration:
- `log-bin=mysql-bin`: Enables binary logging
- `binlog-format=row`: Required for row-level changes
- `binlog-row-image=full`: Captures full row data
- `gtid-mode=ON`: Global Transaction ID for replication

### Step 2: Create Debezium Connector Configuration

**File: `debezium-mysql-connector.json`**

This configures the Debezium MySQL connector:
- **database.hostname**: `mysql` (Docker service name)
- **database.include.list**: `airbnbspringdemo` (your database)
- **table.include.list**: `airbnbspringdemo.booking` (table to monitor)
- **topic.prefix**: `airbnbspringdemo` (Kafka topic prefix)
- **transforms**: Routes events to `booking-events` topic
- **snapshot.mode**: `initial` (captures existing data on first run)

### Step 3: Create Redis Sink Consumer

**File: `redis-sink-consumer.py`**

Python script that:
- Consumes messages from Kafka topic `booking-events`
- Parses Debezium change events (INSERT, UPDATE, DELETE)
- Writes data to Redis with key format: `booking:{id}`
- Handles all operation types (create, update, delete)

### Step 4: Update Spring Boot Configuration

**File: `src/main/resources/application.properties`**

Updated to connect to Docker MySQL and Redis:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/airbnbspringdemo
spring.datasource.username=root
spring.datasource.password=rootpassword

spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### Step 5: Create Setup Scripts

**File: `install-debezium-connector.sh`**
- Downloads Debezium MySQL connector plugin
- Installs it in Kafka Connect container
- Restarts Kafka Connect to load the connector

**File: `setup-docker.sh`**
- Starts all Docker services
- Waits for services to be ready
- Installs Debezium connector
- Creates the Debezium connector

**File: `create-booking-table.sql`**
- SQL script to create the booking table
- Matches the Booking entity structure

## Complete Setup Process

### 1. Start All Services

```bash
docker-compose up -d
```

Wait 30-60 seconds for all services to start.

### 2. Verify Services

```bash
# Check all containers
docker-compose ps

# Verify MySQL
docker exec mysql-cdc mysqladmin ping -h localhost -u root -prootpassword

# Verify Redis
docker exec redis-cdc redis-cli ping

# Verify Kafka Connect
curl http://localhost:8083/connectors
```

### 3. Install Debezium Connector

```bash
./install-debezium-connector.sh
```

This script:
1. Waits for Kafka Connect to be ready
2. Downloads Debezium MySQL connector (version 2.5.4)
3. Extracts it to `/kafka/connect` in the container
4. Restarts Kafka Connect to load the connector

### 4. Create the Booking Table

```bash
docker exec -i mysql-cdc mysql -uroot -prootpassword airbnbspringdemo < create-booking-table.sql
```

Or manually:
```sql
CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    airbnb_id BIGINT NOT NULL,
    total_price DOUBLE NOT NULL,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    idempotency_key VARCHAR(255) UNIQUE,
    check_in_date DATE,
    check_out_date DATE
);
```

### 5. Create Debezium Connector

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium-mysql-connector.json
```

Verify it's running:
```bash
curl -s http://localhost:8083/connectors/booking-mysql-connector/status | jq '.'
```

Should show:
- `connector.state`: "RUNNING"
- `tasks[0].state`: "RUNNING"

### 6. Install Python Dependencies

```bash
pip3 install -r requirements.txt
```

This installs:
- `kafka-python`: For consuming Kafka messages
- `redis`: For writing to Redis

### 7. Start Redis Consumer

**Important**: Keep this running in a separate terminal!

```bash
python3 redis-sink-consumer.py
```

You should see:
```
Redis Sink Consumer started. Listening for booking events...
Press Ctrl+C to stop
```

## Testing the Setup

### Test Insert

**Connect to MySQL:**
```bash
mysql -h 127.0.0.1 -P 3306 -u root -prootpassword airbnbspringdemo
```

**Insert a booking:**
```sql
INSERT INTO booking (user_id, airbnb_id, total_price, booking_status, idempotency_key, check_in_date, check_out_date)
VALUES (1, 100, 500.00, 'PENDING', 'test-001', '2024-12-26', '2024-12-30');
```

**Verify in Redis Consumer Terminal:**
Should show: `✓ Stored booking 1 in Redis`

**Check Redis:**
```bash
redis-cli
GET booking:1
```

### Test Update

```sql
UPDATE booking SET booking_status = 'CONFIRMED', total_price = 550.00 WHERE id = 1;
```

Check Redis - data should be updated.

### Test Delete

```sql
DELETE FROM booking WHERE id = 1;
```

Redis consumer should show: `✓ Deleted booking 1 from Redis`

## How It Works

### 1. MySQL Binlog
- MySQL writes all changes to binary log (binlog)
- Debezium reads this binlog in real-time
- Captures INSERT, UPDATE, DELETE operations

### 2. Debezium Connector
- Connects to MySQL and reads binlog
- Transforms changes into Debezium change events
- Publishes events to Kafka topic

### 3. Kafka Topic
- Events are published to `booking-events` topic
- Kafka stores events for consumption
- Multiple consumers can read the same events

### 4. Redis Consumer
- Python script consumes from Kafka topic
- Parses Debezium event structure:
  - `op: 'c'` = Create (INSERT)
  - `op: 'u'` = Update
  - `op: 'd'` = Delete
- Extracts `after` data for create/update
- Extracts `before` data for delete

### 5. Redis Storage
- Data stored as JSON strings
- Key format: `booking:{id}`
- Value: JSON representation of booking record

## Key Configuration Details

### MySQL Binlog Settings (in docker-compose.yml)
```yaml
command: 
  - --server-id=1
  - --log-bin=mysql-bin
  - --binlog-format=row
  - --binlog-row-image=full
  - --gtid-mode=ON
  - --enforce-gtid-consistency=ON
```

**Why these settings?**
- `log-bin`: Enables binary logging (required for CDC)
- `binlog-format=row`: Captures row-level changes (not statement-level)
- `binlog-row-image=full`: Captures complete row data (before and after)
- `gtid-mode`: Enables Global Transaction IDs for better replication tracking

### Debezium Connector Settings

**Important configurations:**
- `snapshot.mode=initial`: Captures existing data on first run
- `transforms.unwrap`: Extracts the actual data from Debezium envelope
- `transforms.route`: Routes to `booking-events` topic
- `schema.history.internal.kafka.*`: Stores schema changes in Kafka

### Redis Consumer Logic

The consumer handles three operation types:

1. **Create/Update** (`op: 'c'` or `op: 'u'`):
   - Extracts `after` field (new row data)
   - Stores in Redis: `SET booking:{id} {json_data}`

2. **Delete** (`op: 'd'`):
   - Extracts `before` field (deleted row data)
   - Removes from Redis: `DEL booking:{id}`

## Troubleshooting

### Connector Shows FAILED

1. **Check MySQL is running:**
   ```bash
   docker ps | grep mysql
   ```

2. **Check connector logs:**
   ```bash
   docker logs kafka-connect | tail -50
   ```

3. **Restart connector:**
   ```bash
   curl -X DELETE http://localhost:8083/connectors/booking-mysql-connector
   curl -X POST http://localhost:8083/connectors \
     -H "Content-Type: application/json" \
     -d @debezium-mysql-connector.json
   ```

### No Data in Redis

1. **Check Redis consumer is running:**
   ```bash
   ps aux | grep redis-sink-consumer
   ```

2. **Check Kafka topic has messages:**
   ```bash
   docker exec kafka kafka-console-consumer \
     --bootstrap-server localhost:9092 \
     --topic booking-events \
     --from-beginning \
     --max-messages 5
   ```

3. **Check connector status:**
   ```bash
   curl -s http://localhost:8083/connectors/booking-mysql-connector/status | jq '.'
   ```

### MySQL Connection Issues

**Use `127.0.0.1` instead of `localhost`:**
```bash
mysql -h 127.0.0.1 -P 3306 -u root -prootpassword airbnbspringdemo
```

Or use Docker exec:
```bash
docker exec -it mysql-cdc mysql -uroot -prootpassword airbnbspringdemo
```

## Cleanup

To stop all services:
```bash
docker-compose down
```

To remove all data (volumes):
```bash
docker-compose down -v
```

## Summary

**What was done:**
1. ✅ Created Docker Compose with MySQL 8.0, Redis, Kafka, Zookeeper, Kafka Connect
2. ✅ Configured MySQL with binlog enabled for CDC
3. ✅ Created Debezium connector configuration
4. ✅ Created Python Redis consumer script
5. ✅ Updated Spring Boot to use Docker MySQL/Redis
6. ✅ Created setup and installation scripts
7. ✅ Created booking table
8. ✅ Verified end-to-end CDC pipeline

**Result:**
- All booking INSERT/UPDATE/DELETE operations in MySQL are automatically replicated to Redis
- Real-time synchronization via Kafka
- No application code changes needed - CDC works at database level

## Quick Reference

**Start everything:**
```bash
docker-compose up -d
./install-debezium-connector.sh
curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d @debezium-mysql-connector.json
python3 redis-sink-consumer.py  # In separate terminal
```

**Test:**
```bash
mysql -h 127.0.0.1 -P 3306 -u root -prootpassword airbnbspringdemo
# Insert/update/delete records
# Check Redis: redis-cli GET booking:1
```

**Stop:**
```bash
docker-compose down
```

