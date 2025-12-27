#!/bin/bash

echo "Setting up CDC with Debezium..."

# Start Docker containers
echo "Starting Docker containers..."
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 30

# Wait for Kafka Connect to be ready
echo "Waiting for Kafka Connect to be ready..."
until curl -f http://localhost:8083/connectors 2>/dev/null; do
  echo "Waiting for Kafka Connect..."
  sleep 5
done

echo "Kafka Connect is ready!"

# Install Redis Sink Connector
echo "Installing Redis Sink Connector..."
curl -X POST http://localhost:8083/connector-plugins \
  -H "Content-Type: application/json" \
  -d '{
    "class": "com.github.jcustenborder.kafka.connect.redis.RedisSinkConnector"
  }' 2>/dev/null || echo "Redis connector plugin installation (if needed)..."

# Create Debezium MySQL connector
echo "Creating Debezium MySQL connector..."
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium-mysql-connector.json

sleep 5

# Check connector status
echo "Checking connector status..."
curl -s http://localhost:8083/connectors/booking-mysql-connector/status | jq '.'

echo ""
echo "Setup complete!"
echo ""
echo "Note: For Redis sink, we'll use a simple Kafka consumer approach"
echo "since the Redis sink connector requires additional setup."
echo "Please run the redis-sink-consumer.py script to sync data to Redis."

