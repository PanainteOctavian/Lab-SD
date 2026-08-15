from kafka import KafkaProducer
import time
import json
import random

TOPIC = "topic_random_pairs"
DURATION = 10       # secunde - acelasi interval ca si mouse producer
INTERVAL = 0.1      # la fiecare 100ms

def generate_random_pair():
    """Genereaza o pereche de valori aleatoare (float intre 0 si 1000)."""
    return {
        "a": round(random.uniform(0, 1000), 4),
        "b": round(random.uniform(0, 1000), 4),
        "timestamp": time.time()
    }

def main():
    producer = KafkaProducer(
        bootstrap_servers="localhost:9092",
        value_serializer=lambda v: json.dumps(v).encode("utf-8")
    )

    print(f"[RandomPairProducer] Publica perechi aleatoare in '{TOPIC}' timp de {DURATION}s...")
    start = time.time()

    while time.time() - start < DURATION:
        pair = generate_random_pair()
        producer.send(TOPIC, value=pair)
        print(f"  -> trimis: a={pair['a']}, b={pair['b']}")
        time.sleep(INTERVAL)

    producer.flush()
    producer.close()
    print("[RandomPairProducer] Terminat.")

if __name__ == "__main__":
    main()