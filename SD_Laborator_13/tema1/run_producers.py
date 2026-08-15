from kafka import KafkaProducer
from pynput.mouse import Controller
import time
import json
import random
import threading

MOUSE_TOPIC        = "topic_mouse"
RANDOM_PAIRS_TOPIC = "topic_random_pairs"
DURATION           = 10    # secunde
INTERVAL           = 0.1   # 100ms intre mesaje

def mouse_producer_thread():
    producer = KafkaProducer(
        bootstrap_servers="localhost:9092",
        value_serializer=lambda v: json.dumps(v).encode("utf-8")
    )
    mouse = Controller()
    print(f"[MouseProducer] START → topic: '{MOUSE_TOPIC}'")
    start = time.time()

    while time.time() - start < DURATION:
        x, y = mouse.position
        data = {"x": x, "y": y, "timestamp": round(time.time(), 3)}
        producer.send(MOUSE_TOPIC, value=data)
        print(f"  [Mouse]  x={x:6}, y={y:6}")
        time.sleep(INTERVAL)

    producer.flush()
    producer.close()
    print("[MouseProducer] STOP")

def random_pair_producer_thread():
    producer = KafkaProducer(
        bootstrap_servers="localhost:9092",
        value_serializer=lambda v: json.dumps(v).encode("utf-8")
    )
    print(f"[RandomPairProducer] START → topic: '{RANDOM_PAIRS_TOPIC}'")
    start = time.time()

    while time.time() - start < DURATION:
        pair = {
            "a": round(random.uniform(0, 1000), 4),
            "b": round(random.uniform(0, 1000), 4),
            "timestamp": round(time.time(), 3)
        }
        producer.send(RANDOM_PAIRS_TOPIC, value=pair)
        print(f"  [Random] a={pair['a']:10.4f}, b={pair['b']:10.4f}")
        time.sleep(INTERVAL)

    producer.flush()
    producer.close()
    print("[RandomPairProducer] STOP")


if __name__ == "__main__":
    print("=" * 55)
    print("  Misca mouse-ul aleatoriu pe ecran timp de 10 secunde!")
    print("=" * 55)
    time.sleep(2)

    t1 = threading.Thread(target=mouse_producer_thread)
    t2 = threading.Thread(target=random_pair_producer_thread)

    t1.start()
    t2.start()

    t1.join()
    t2.join()

    print("\nAmbii producatori au terminat.")
    print(f"  Mesaje trimise in '{MOUSE_TOPIC}':        ~{int(DURATION / INTERVAL)}")
    print(f"  Mesaje trimise in '{RANDOM_PAIRS_TOPIC}': ~{int(DURATION / INTERVAL)}")