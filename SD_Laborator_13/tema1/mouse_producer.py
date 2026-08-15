from kafka import KafkaProducer
from pynput.mouse import Controller
import time
import json

TOPIC = "topic_mouse"
DURATION = 10       # secunde
INTERVAL = 0.1      # la fiecare 100ms -> 100 de puncte in 10 secunde

def main():
    producer = KafkaProducer(
        bootstrap_servers="localhost:9092",
        value_serializer=lambda v: json.dumps(v).encode("utf-8")
    )
    mouse = Controller()

    print(f"[MouseProducer] Publica coordonate in '{TOPIC}' timp de {DURATION}s...")
    start = time.time()

    while time.time() - start < DURATION:
        x, y = mouse.position
        data = {"x": x, "y": y, "timestamp": time.time()}
        producer.send(TOPIC, value=data)
        print(f"  -> trimis: x={x}, y={y}")
        time.sleep(INTERVAL)

    producer.flush()
    producer.close()
    print("[MouseProducer] Terminat.")

if __name__ == "__main__":
    main()