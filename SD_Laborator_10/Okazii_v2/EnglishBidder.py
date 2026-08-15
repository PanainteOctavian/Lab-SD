from kafka import KafkaProducer, KafkaConsumer
from random import randint
from uuid import uuid4

class Bidder:
    def __init__(self, bids_topic, result_topic):
        super().__init__()
        self.bids_topic = bids_topic
        self.result_topic = result_topic

        # producatorul pentru oferte de licitatie
        self.bid_producer = KafkaProducer()

        self.bids_consumer = KafkaConsumer(
            self.bids_topic,
            auto_offset_reset="latest",  # cea mai noua oferta
            consumer_timeout_ms=15000    # maxim 15 sec pana la incheiere
        )

        # consumatorul pentru rezultatul licitatiei
        self.result_consumer = KafkaConsumer(
            self.result_topic,
            auto_offset_reset="earliest"  # mesajele se preiau de la
            # cel mai vechi la cel mai recent
        )

        self.my_id = uuid4()
        self.current_bid = 0  # pretul curent global
        self.my_bid = 0  # oferta curenta a acestui bidder

    def send_bid(self, amount):
        print("[{}] Licitez: {}".format(self.my_id, amount))
        bid_message = bytearray("licitez", encoding="utf-8")
        bid_headers = [
            ("amount", amount.to_bytes(2, byteorder='big')),
            ("identity", bytes("Bidder {}".format(self.my_id), encoding="utf-8"))
        ]
        self.bid_producer.send(topic=self.bids_topic, value=bid_message, headers=bid_headers)
        self.bid_producer.flush()

    def bid(self):
        self.my_bid = randint(1000, 5000)
        self.send_bid(self.my_bid)

        for msg in self.bids_consumer:
            for header in msg.headers:
                if header[0] == "amount":
                    self.current_bid = int.from_bytes(header[1], 'big')
                if header[0] == "identity":
                    sender = str(header[1], encoding="utf-8")

            if sender == "Bidder {}".format(self.my_id):
                continue

            print("[{}] Pretul curent e: {}".format(self.my_id, self.current_bid))

            if self.current_bid > self.my_bid and self.current_bid < 9990:
                chance = randint(0, 1) == 1  # 50% sansa sa supralicitam
                if chance:
                    self.my_bid = self.current_bid + 10
                    self.send_bid(self.my_bid)
                else:
                    print("[{}] Nu mai licitez.".format(self.my_id))
            else:
                print("[{}] Sunt deja cel mai mare, astept...".format(self.my_id))

        print("[{}] Licitatia s-a incheiat, astept rezultatul...".format(self.my_id))
        self.bid_producer.flush()
        self.bid_producer.close()

    def get_winner(self):
        # se asteapta raspunsul licitatiei
        print("Astept rezultatul licitatiei...")
        result = next(self.result_consumer)

        # se verifica identitatea castigatorului
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
    bidder = Bidder(
        bids_topic="topic_oferte",
        result_topic="topic_rezultat"
    )
    bidder.run()
