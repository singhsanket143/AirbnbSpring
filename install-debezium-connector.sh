#!/bin/bash

echo "Installing Debezium MySQL Connector..."

# Wait for Kafka Connect to be ready
echo "Waiting for Kafka Connect to be ready..."
until curl -f http://localhost:8083/connectors 2>/dev/null; do
  echo "Waiting for Kafka Connect..."
  sleep 5
done

# Check if connector is already installed
if docker exec kafka-connect ls /kafka/connect/debezium-connector-mysql 2>/dev/null | grep -q "debezium-connector-mysql"; then
    echo "✓ Debezium MySQL connector already installed"
    # Check if it's loaded
    if curl -s http://localhost:8083/connector-plugins | jq -e '.[] | select(.class == "io.debezium.connector.mysql.MySqlConnector")' > /dev/null 2>&1; then
        echo "✓ Connector is loaded and available"
        exit 0
    else
        echo "Connector found but not loaded, restarting Kafka Connect..."
        docker restart kafka-connect
        sleep 15
        exit 0
    fi
fi

# Download and install the connector
echo "Downloading Debezium MySQL connector..."
docker exec kafka-connect bash -c "
    cd /kafka/connect && \
    curl -L -o debezium-connector-mysql.tar.gz https://repo1.maven.org/maven2/io/debezium/debezium-connector-mysql/2.5.4.Final/debezium-connector-mysql-2.5.4.Final-plugin.tar.gz && \
    tar -xzf debezium-connector-mysql.tar.gz && \
    rm debezium-connector-mysql.tar.gz
"

if [ $? -eq 0 ]; then
    echo "✓ Connector downloaded and extracted"
    echo "Restarting Kafka Connect to load the connector..."
    docker restart kafka-connect
    sleep 20
    
    # Verify installation
    if curl -s http://localhost:8083/connector-plugins | jq -e '.[] | select(.class == "io.debezium.connector.mysql.MySqlConnector")' > /dev/null 2>&1; then
        echo "✓ Debezium MySQL connector successfully installed and loaded!"
    else
        echo "✗ Connector installed but not yet loaded. Please wait a few more seconds and check again."
    fi
else
    echo "✗ Failed to install connector"
    exit 1
fi

