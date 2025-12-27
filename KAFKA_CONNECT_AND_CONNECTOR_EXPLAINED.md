# Kafka Connect and Debezium Connector Explained

## What is Kafka Connect?

**Kafka Connect** is a framework for integrating Kafka with external systems (databases, file systems, message queues, etc.). Think of it as a **universal adapter system** for Kafka.

### Key Concepts:

1. **Source Connectors**: Pull data FROM external systems INTO Kafka
   - Example: Debezium reads from MySQL binlog and writes to Kafka

2. **Sink Connectors**: Push data FROM Kafka TO external systems
   - Example: Writing Kafka messages to a database or file system

3. **REST API**: Kafka Connect exposes a REST API (port 8083) to:
   - Create connectors
   - Check connector status
   - View connector configurations
   - Stop/start connectors

### How Kafka Connect Works:

```
External System (MySQL) 
    ↓
Source Connector (Debezium)
    ↓
Kafka Topic
    ↓
Sink Connector (optional)
    ↓
External System (Redis, Database, etc.)
```

### Why Use Kafka Connect?

- **No Custom Code**: Pre-built connectors for common systems
- **Scalable**: Can run multiple connector instances
- **Fault Tolerant**: Automatically restarts failed connectors
- **Distributed**: Can run across multiple servers
- **RESTful Management**: Easy to configure via REST API

### In Your Setup:

- **Kafka Connect** runs in Docker container `kafka-connect`
- Exposes REST API on port `8083`
- Loads connectors from `/kafka/connect/` directory
- Manages connector lifecycle (start, stop, restart)

---

## What is `debezium-mysql-connector.json`?

This JSON file is a **connector configuration** that tells Kafka Connect:
- **Which connector to use** (Debezium MySQL connector)
- **How to connect** to MySQL
- **What to monitor** (which database/table)
- **Where to send data** (which Kafka topic)
- **How to transform data** (extract, route, format)

### Breaking Down the Configuration:

#### 1. Basic Connector Info

```json
{
  "name": "booking-mysql-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "tasks.max": "1"
  }
}
```

- **name**: Unique identifier for this connector instance
- **connector.class**: Java class that implements the connector (Debezium MySQL connector)
- **tasks.max**: Number of parallel tasks (1 = single-threaded)

#### 2. MySQL Connection Settings

```json
"database.hostname": "mysql",
"database.port": "3306",
"database.user": "root",
"database.password": "rootpassword",
"database.server.id": "184054",
"database.server.name": "airbnbspringdemo",
"database.connectionTimeZone": "UTC"
```

- **hostname**: `mysql` = Docker service name (resolves to MySQL container)
- **port**: Standard MySQL port
- **user/password**: Credentials to connect to MySQL
- **server.id**: Unique ID for this MySQL replica (Debezium acts as a replica)
- **server.name**: Logical name for this MySQL server (used in topic names)
- **connectionTimeZone**: Timezone for date/time handling

#### 3. What to Monitor

```json
"database.include.list": "airbnbspringdemo",
"table.include.list": "airbnbspringdemo.booking"
```

- **database.include.list**: Only monitor this database (filters out others)
- **table.include.list**: Only monitor the `booking` table (filters out other tables)

#### 4. Kafka Settings

```json
"schema.history.internal.kafka.bootstrap.servers": "kafka:29092",
"schema.history.internal.kafka.topic": "schema-changes.booking",
"topic.prefix": "airbnbspringdemo"
```

- **bootstrap.servers**: Kafka broker address (`kafka:29092` = Docker service name)
- **schema.history.topic**: Where to store database schema changes
- **topic.prefix**: Prefix for all Kafka topics created by this connector

#### 5. Snapshot Mode

```json
"snapshot.mode": "initial",
"include.schema.changes": "false"
```

- **snapshot.mode**: `initial` = capture existing data on first run, then stream changes
- **include.schema.changes**: `false` = don't publish schema change events (only data changes)

#### 6. Data Transforms

```json
"transforms": "unwrap,route",
"transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
"transforms.unwrap.drop.tombstones": "false",
"transforms.unwrap.delete.handling.mode": "rewrite",
"transforms.route.type": "org.apache.kafka.connect.transforms.RegexRouter",
"transforms.route.regex": "airbnbspringdemo.airbnbspringdemo.booking",
"transforms.route.replacement": "booking-events"
```

**Transform Chain:**
1. **unwrap**: Extracts the actual data from Debezium's envelope
   - Debezium wraps data in: `{before: {...}, after: {...}, op: 'c', ...}`
   - Unwrap extracts just the `after` field (the actual row data)
   
2. **route**: Changes the Kafka topic name
   - Default topic: `airbnbspringdemo.airbnbspringdemo.booking`
   - Routes to: `booking-events` (simpler name)

#### 7. Data Format

```json
"key.converter": "org.apache.kafka.connect.json.JsonConverter",
"value.converter": "org.apache.kafka.connect.json.JsonConverter",
"key.converter.schemas.enable": "false",
"value.converter.schemas.enable": "false"
```

- **converters**: How to serialize data (JSON format)
- **schemas.enable**: `false` = don't include schema in messages (just data)

---

## How It All Works Together

### Step 1: Create Connector

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium-mysql-connector.json
```

This sends the JSON config to Kafka Connect REST API, which:
1. Validates the configuration
2. Instantiates the Debezium MySQL connector class
3. Starts the connector task
4. Connector connects to MySQL and starts reading binlog

### Step 2: Connector Reads MySQL Binlog

- Debezium connects to MySQL as a replica
- Reads the binary log (binlog) in real-time
- Captures INSERT, UPDATE, DELETE operations
- Transforms them into Debezium change events

### Step 3: Events Published to Kafka

- Each change becomes a message in Kafka topic `booking-events`
- Message format (after unwrap transform):
  ```json
  {
    "id": 1,
    "user_id": 1,
    "airbnb_id": 100,
    "total_price": 500.0,
    "booking_status": "PENDING",
    ...
  }
  ```

### Step 4: Consumers Read from Kafka

- Your `redis-sink-consumer.py` reads from `booking-events` topic
- Processes each message and writes to Redis

---

## Why This Configuration is Needed

### Without the JSON Config:
- ❌ Kafka Connect doesn't know which connector to use
- ❌ Doesn't know how to connect to MySQL
- ❌ Doesn't know what to monitor
- ❌ Doesn't know where to send data

### With the JSON Config:
- ✅ Kafka Connect knows to use Debezium MySQL connector
- ✅ Knows MySQL connection details
- ✅ Knows to monitor only `booking` table
- ✅ Knows to publish to `booking-events` topic
- ✅ Knows how to transform data (unwrap, route)

---

## Key Takeaways

1. **Kafka Connect** = Framework for integrating Kafka with external systems
2. **Connector** = Plugin that implements integration logic (Debezium = MySQL connector)
3. **Configuration JSON** = Instructions for the connector (what to do, how to do it)
4. **REST API** = Way to manage connectors (create, check status, stop)

### Analogy:
- **Kafka Connect** = Universal adapter system
- **Debezium Connector** = Specific adapter for MySQL
- **JSON Config** = Settings/instructions for that adapter
- **REST API** = Remote control for the adapter

---

## Example: Creating a Connector

```bash
# 1. Install connector plugin (one-time)
./install-debezium-connector.sh

# 2. Create connector instance (using JSON config)
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium-mysql-connector.json

# 3. Check status
curl http://localhost:8083/connectors/booking-mysql-connector/status

# 4. View config
curl http://localhost:8083/connectors/booking-mysql-connector/config

# 5. Delete connector
curl -X DELETE http://localhost:8083/connectors/booking-mysql-connector
```

---

## Summary

- **Kafka Connect**: Framework that runs connectors
- **Debezium Connector**: Plugin that reads MySQL binlog
- **JSON Config**: Instructions telling the connector what to do
- **Result**: MySQL changes automatically flow to Kafka topics

