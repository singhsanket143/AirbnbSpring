#!/bin/bash

echo "=== CDC Setup Verification ==="
echo ""

# Check Docker containers
echo "1. Checking Docker containers..."
docker-compose ps
echo ""

# Check Kafka Connect
echo "2. Checking Kafka Connect..."
if curl -f http://localhost:8083/connectors 2>/dev/null; then
    echo "✓ Kafka Connect is running"
else
    echo "✗ Kafka Connect is not accessible"
fi
echo ""

# Check Debezium connector
echo "3. Checking Debezium connector..."
CONNECTOR_STATUS=$(curl -s http://localhost:8083/connectors/booking-mysql-connector/status 2>/dev/null)
if [ $? -eq 0 ]; then
    echo "✓ Connector exists"
    echo "$CONNECTOR_STATUS" | grep -q '"state":"RUNNING"' && echo "✓ Connector is RUNNING" || echo "✗ Connector is not RUNNING"
else
    echo "✗ Connector not found"
fi
echo ""

# Check Redis (local instance)
echo "4. Checking Redis (local)..."
if redis-cli ping 2>/dev/null | grep -q "PONG"; then
    echo "✓ Redis is running on localhost:6379"
    KEY_COUNT=$(redis-cli KEYS "booking:*" 2>/dev/null | wc -l | tr -d ' ')
    echo "  Found $KEY_COUNT booking keys in Redis"
else
    echo "✗ Redis is not accessible on localhost:6379"
    echo "  Start Redis: brew services start redis (macOS) or sudo systemctl start redis (Linux)"
fi
echo ""

# Check Kafka topic
echo "5. Checking Kafka topic..."
if docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null | grep -q "booking-events"; then
    echo "✓ booking-events topic exists"
else
    echo "✗ booking-events topic not found"
fi
echo ""

echo "=== Verification Complete ==="

