#!/bin/bash

echo "=== Setting up CDC with Docker MySQL and Redis ==="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "✗ Docker is not running. Please start Docker first."
    exit 1
fi

echo "✓ Docker is running"
echo ""

# Start Docker services
echo "Starting Docker services..."
docker-compose up -d

echo ""
echo "Waiting for services to be ready..."
sleep 30

# Check MySQL
echo "Checking MySQL..."
for i in {1..30}; do
    if docker exec mysql-cdc mysqladmin ping -h localhost -u root -prootpassword > /dev/null 2>&1; then
        echo "✓ MySQL is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "✗ MySQL failed to start"
        exit 1
    fi
    sleep 2
done

# Check Redis
echo "Checking Redis..."
for i in {1..30}; do
    if docker exec redis-cdc redis-cli ping > /dev/null 2>&1; then
        echo "✓ Redis is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "✗ Redis failed to start"
        exit 1
    fi
    sleep 2
done

# Check Kafka Connect
echo "Checking Kafka Connect..."
for i in {1..30}; do
    if curl -f http://localhost:8083/connectors > /dev/null 2>&1; then
        echo "✓ Kafka Connect is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "✗ Kafka Connect failed to start"
        exit 1
    fi
    sleep 2
done

echo ""
echo "Installing Debezium MySQL connector..."
./install-debezium-connector.sh

echo ""
echo "Creating Debezium connector..."
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium-mysql-connector.json > /dev/null 2>&1

sleep 5

# Check connector status
echo ""
echo "Checking connector status..."
CONNECTOR_STATUS=$(curl -s http://localhost:8083/connectors/booking-mysql-connector/status 2>/dev/null)
if [ $? -eq 0 ]; then
    STATE=$(echo "$CONNECTOR_STATUS" | jq -r '.connector.state' 2>/dev/null)
    if [ "$STATE" = "RUNNING" ]; then
        echo "✓ Debezium connector is RUNNING"
    else
        echo "⚠ Debezium connector state: $STATE"
        echo "Check logs: docker logs kafka-connect"
    fi
else
    echo "✗ Failed to create connector"
fi

echo ""
echo "=== Setup Complete! ==="
echo ""
echo "Next steps:"
echo "1. Start Redis consumer: python3 redis-sink-consumer.py"
echo "2. Start Spring Boot: ./gradlew bootRun"
echo "3. Insert/update booking records in MySQL"
echo "4. Check Redis: redis-cli KEYS booking:*"
echo ""
echo "MySQL credentials:"
echo "  Host: localhost:3306"
echo "  User: root"
echo "  Password: rootpassword"
echo ""
echo "Redis:"
echo "  Host: localhost:6379"
echo ""

