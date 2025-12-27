#!/bin/bash

echo "=== Quick CDC Test ==="
echo ""

# Check if services are running
echo "Checking services..."
docker ps | grep -q mysql-cdc && echo "✓ MySQL running" || echo "✗ MySQL not running"
docker ps | grep -q redis-cdc && echo "✓ Redis running" || echo "✗ Redis not running"
docker ps | grep -q kafka-connect && echo "✓ Kafka Connect running" || echo "✗ Kafka Connect not running"

echo ""

# Insert test record
echo "1. Inserting test booking..."
mysql -h 127.0.0.1 -P 3306 -u root -prootpassword airbnbspringdemo -e "
INSERT INTO booking (user_id, airbnb_id, total_price, booking_status, idempotency_key, check_in_date, check_out_date)
VALUES (999, 999, 999.00, 'PENDING', 'quick-test-001', '2024-12-26', '2024-12-30');
" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ Inserted booking ID 999"
else
    echo "✗ Failed to insert booking"
    exit 1
fi

sleep 3

# Check Kafka
echo ""
echo "2. Checking Kafka topic..."
KAFKA_OUTPUT=$(docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic booking-events \
  --from-beginning \
  --max-messages 1 \
  --timeout-ms 10000 2>&1)

if echo "$KAFKA_OUTPUT" | grep -q "999\|user_id\|booking"; then
    echo "✓ Found in Kafka"
else
    echo "⚠ No message in Kafka (may need to wait longer or check connector)"
fi

sleep 2

# Check Redis
echo ""
echo "3. Checking Redis..."
REDIS_VALUE=$(redis-cli -h localhost -p 6379 GET booking:999 2>/dev/null)
if [ -n "$REDIS_VALUE" ]; then
    echo "✓ Found in Redis"
    echo "Value: $REDIS_VALUE" | head -c 100
    echo "..."
else
    echo "⚠ Not in Redis yet (check if redis-sink-consumer.py is running)"
fi

echo ""
echo "=== Test Complete ==="
echo ""
echo "To see full data:"
echo "  redis-cli GET booking:999"
echo ""
echo "To check all bookings:"
echo "  redis-cli KEYS booking:*"

