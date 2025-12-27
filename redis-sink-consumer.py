#!/usr/bin/env python3

import json
import redis
from kafka import KafkaConsumer
import os

# Redis connection
redis_client = redis.Redis(host='localhost', port=6379, decode_responses=True)

# Kafka consumer
consumer = KafkaConsumer(
    'booking-events',
    bootstrap_servers=['localhost:9092'],
    value_deserializer=lambda m: json.loads(m.decode('utf-8')),
    auto_offset_reset='earliest',
    enable_auto_commit=True,
    group_id='redis-sink-group'
)

print("Redis Sink Consumer started. Listening for booking events...")
print("Press Ctrl+C to stop")

try:
    for message in consumer:
        event = message.value
        print(f"Received event: {event}")
        
        # Handle different event types
        if 'op' in event:
            op = event['op']
            
            if op == 'c' or op == 'u':  # Create or Update
                # Extract the 'after' data
                after = event.get('after', {})
                if after:
                    booking_id = after.get('id')
                    if booking_id:
                        # Store in Redis with key: booking:{id}
                        key = f"booking:{booking_id}"
                        # Convert to JSON string
                        value = json.dumps(after)
                        redis_client.set(key, value)
                        print(f"✓ Stored booking {booking_id} in Redis")
            
            elif op == 'd':  # Delete
                # Extract the 'before' data for delete
                before = event.get('before', {})
                if before:
                    booking_id = before.get('id')
                    if booking_id:
                        key = f"booking:{booking_id}"
                        redis_client.delete(key)
                        print(f"✓ Deleted booking {booking_id} from Redis")
        else:
            # Direct data (if unwrap transform is working)
            booking_id = event.get('id')
            if booking_id:
                key = f"booking:{booking_id}"
                value = json.dumps(event)
                redis_client.set(key, value)
                print(f"✓ Stored booking {booking_id} in Redis")

except KeyboardInterrupt:
    print("\nShutting down consumer...")
finally:
    consumer.close()

