from kafka import KafkaConsumer, KafkaProducer

class DutchAuctioneer:
    def __init__(self, price_topic, notify_message_processor_topic):
        super().__init__()
        self.price_topic = price_topic
        self.notify_processor_topic = notify_message_processor_topic

        self.price_producer = KafkaProducer()

        self.accept_consumer = KafkaConsumer(
            self.price_topic + "_accepturi",
            auto_offset_reset="earliest",
            consumer_timeout_ms=10000
        )

        self.notify_processor_producer = KafkaProducer()

        self.starting_price = 9000  # pretul initial
        self.min_price = 1000       # pretul minim
        self.decrease_step = 500    # cu cat scade la fiecare runda
        self.current_price = self.starting_price
        self.winner_identity = None

    def publish_price(self, price):
        print("Auctioneer: Propun pretul de {}".format(price))
        price_message = bytearray("pret_nou", encoding="utf-8")
        price_headers = [
            ("price", price.to_bytes(2, byteorder='big'))
        ]
        self.price_producer.send(
            topic=self.price_topic,
            value=price_message,
            headers=price_headers
        )
        self.price_producer.flush()

    def wait_for_accept(self):
        for msg in self.accept_consumer:
            for header in msg.headers:
                if header[0] == "identity":
                    self.winner_identity = str(header[1], encoding="utf-8")

            print("Auctioneer: {} a acceptat pretul de {}!".format(
                self.winner_identity, self.current_price))
            return True

        return False

    def run_auction(self):
        print("Auctioneer: Incep licitatia olandeza de la pretul {}".format(self.current_price))

        while self.current_price >= self.min_price:
            self.publish_price(self.current_price)

            accepted = self.wait_for_accept()

            if accepted:
                self.finish_auction()
                return

            self.current_price -= self.decrease_step
            print("Auctioneer: Nimeni nu a acceptat, scad pretul la {}".format(self.current_price))

        print("Auctioneer: Pretul minim atins, licitatia se incheie fara castigator!")
        self.finish_auction()

    def finish_auction(self):
        print("Auctioneer: Licitatia s-a incheiat!")
        self.price_producer.close()
        self.accept_consumer.close()

        auction_finished_message = bytearray("incheiat", encoding="utf-8")
        self.notify_processor_producer.send(
            topic=self.notify_processor_topic,
            value=auction_finished_message
        )
        self.notify_processor_producer.flush()
        self.notify_processor_producer.close()

    def run(self):
        self.run_auction()


if __name__ == '__main__':
    auctioneer = DutchAuctioneer(
        price_topic="topic_pret_olandez",
        notify_message_processor_topic="topic_notificare_procesor_mesaje"
    )
    auctioneer.run()
