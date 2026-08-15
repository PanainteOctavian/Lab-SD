from datetime import datetime
from kafka import KafkaConsumer, KafkaProducer

class DutchMessageProcessor:
    def __init__(self, accept_topic, notify_message_processor_topic, processed_bids_topic):
        super().__init__()
        self.accept_topic = accept_topic
        self.notify_message_processor_topic = notify_message_processor_topic
        self.processed_bids_topic = processed_bids_topic

        self.notify_consumer = KafkaConsumer(
            self.notify_message_processor_topic,
            auto_offset_reset="earliest"
        )

        # citeste accepturile bidderilor
        self.accept_consumer = KafkaConsumer(
            self.accept_topic,
            auto_offset_reset="earliest",
            consumer_timeout_ms=1000
        )

        self.processed_bids_producer = KafkaProducer()

    def get_and_process_messages(self):
        print("Astept notificare de la Auctioneer...")
        auction_end_message = next(self.notify_consumer)
        self.notify_consumer.close()

        if str(auction_end_message.value, encoding="utf-8") == "incheiat":
            print("Licitatie incheiata. Procesez accepturile...")

            # la dutch nu avem duplicate - primul accept e castigatorul
            # luam doar primul mesaj de accept
            first_accept = None
            for msg in self.accept_consumer:
                identity = None
                price = None
                for header in msg.headers:
                    if header[0] == "identity":
                        identity = str(header[1], encoding="utf-8")
                    if header[0] == "price":
                        price = int.from_bytes(header[1], 'big')

                if identity is None:
                    continue  # ignoram mesajele fara identity (preturile de la auctioneer)

                if first_accept is None:
                    first_accept = msg
                    print("Primul accept: {} la pretul {}".format(identity, price))

            self.accept_consumer.close()

            if first_accept is not None:
                new_headers = []
                for header in first_accept.headers:
                    if header[0] == "price":
                        new_headers.append(("amount", header[1])) # redenumit pt bidderprocessor
                    else:
                        new_headers.append(header)

                self.processed_bids_producer.send(
                    topic=self.processed_bids_topic,
                    value=first_accept.value,
                    headers=new_headers
                )
                self.processed_bids_producer.flush()
            else:
                print("Nu a existat niciun accept!")

            self.processed_bids_producer.close()

    def run(self):
        self.get_and_process_messages()


if __name__ == '__main__':
    processor = DutchMessageProcessor(
        accept_topic="topic_pret_olandez_accepturi",
        notify_message_processor_topic="topic_notificare_procesor_mesaje",
        processed_bids_topic="topic_oferte_procesate"
    )
    processor.run()