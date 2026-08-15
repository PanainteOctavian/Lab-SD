from kafka import KafkaProducer, KafkaConsumer
from random import randint
from uuid import uuid4

class DutchBidder:
    def __init__(self, price_topic, result_topic):
        super().__init__()
        self.price_topic = price_topic
        self.result_topic = result_topic

        self.accept_producer = KafkaProducer()

        # consumatorul pentru pretul propus de auctioneer
        self.price_consumer = KafkaConsumer(
            self.price_topic,
            auto_offset_reset="latest",
            consumer_timeout_ms=15000
        )

        # consumatorul pentru rezultat
        self.result_consumer = KafkaConsumer(
            self.result_topic,
            auto_offset_reset="earliest"
        )

        self.my_id = uuid4()

    def bid(self):
        for msg in self.price_consumer:
            current_price = None
            for header in msg.headers:
                if header[0] == "price":
                    current_price = int.from_bytes(header[1], 'big')

            if current_price is None:
                continue

            print("[{}] Pretul curent propus de Auctioneer: {}".format(self.my_id, current_price))

            # 33,3% sansa sa accepte pretul
            accept = randint(0, 2) == 1

            if accept:
                print("[{}] Accept pretul de {}!".format(self.my_id, current_price))
                accept_message = bytearray("accept", encoding="utf-8")
                accept_headers = [
                    ("price", current_price.to_bytes(2, byteorder='big')),
                    ("identity", bytes("Bidder {}".format(self.my_id), encoding="utf-8"))
                ]
                self.accept_producer.send(
                    topic=self.price_topic + "_accepturi",
                    value=accept_message,
                    headers=accept_headers
                )
                self.accept_producer.flush()
                self.accept_producer.close()
                self.price_consumer.close()
                return
            else:
                print("[{}] Refuz pretul de {}, astept urmatorul...".format(self.my_id, current_price))

        print("[{}] Licitatia s-a incheiat, nu am acceptat niciun pret.".format(self.my_id))
        self.accept_producer.close()

    def get_winner(self):
        print("Astept rezultatul licitatiei...")
        result = next(self.result_consumer)

        for header in result.headers:
            if header[0] == "identity":
                identity = str(header[1], encoding="utf-8")

        if identity == "Bidder {}".format(self.my_id):
            print("[{}] Am castigat!!!".format(self.my_id))
        else:
            print("[{}] Am pierdut...".format(self.my_id))

        self.result_consumer.close()

    def run(self):
        self.bid()
        self.get_winner()


if __name__ == '__main__':
    bidder = DutchBidder(
        price_topic="topic_pret_olandez",
        result_topic="topic_rezultat"
    )
    bidder.run()