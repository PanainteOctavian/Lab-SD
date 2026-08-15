import pika
from retry import retry

class RabbitMq:
    config = {
        'host': '0.0.0.0',
        'port': 5672,
        'username': 'octavian',
        'password': 'octavian',
        'exchange': 'libraryapp.direct',
        'routing_key': 'libraryapp.routingkey1',
        'queue': 'libraryapp.queue'
    }

    def __init__(self, ui):
        self.ui = ui
        self._connection = None
        self._channel = None

        credentials = pika.PlainCredentials(self.config['username'],
                                            self.config['password'])
        self.parameters = pika.ConnectionParameters(
            host=self.config['host'],
            port=self.config['port'],
            credentials=credentials
        )

    def on_connection_open(self, connection):
        self._connection = connection
        self._connection.channel(on_open_callback=self.on_channel_open)

    def on_connection_closed(self, connection, reason):
        self._connection.ioloop.stop()

    def on_channel_open(self, channel):
        self._channel = channel
        self._channel.queue_declare(
            queue=self.config['queue'],
            durable=True,
            callback=self.on_queue_declared
        )

    def on_queue_declared(self, frame):
        pass

    def on_message(self, channel, method, properties, body):
        try:
            message = body.decode('utf-8')
            self.ui.set_response(message)
            channel.basic_ack(delivery_tag=method.delivery_tag)
        except Exception as e:
            print(e)
            channel.basic_nack(delivery_tag=method.delivery_tag)

    def on_received_message(self, blocking_channel, deliver, properties,
                            message):
        result = message.decode('utf-8')
        blocking_channel.confirm_delivery()
        try:
            self.ui.set_response(result)
        except Exception as e:
            print(e)
        finally:
            blocking_channel.stop_consuming()

    @retry(pika.exceptions.AMQPConnectionError, delay=5, jitter=(1, 3))
    def receive_message(self):
        self._connection = pika.SelectConnection(
            parameters=self.parameters,
            on_open_callback=self.on_connection_open,
            on_close_callback=self.on_connection_closed
        )

        original_callback = self.on_queue_declared

        def start_consuming(frame):
            original_callback(frame)
            self._channel.basic_consume(
                queue=self.config['queue'],
                on_message_callback=self.on_message
            )

        self.on_queue_declared = start_consuming

        try:
            self._connection.ioloop.start()
        except KeyboardInterrupt:
            print("Application closed.")
            self.stop()

    def send_message(self, message):
        def publish_when_ready(frame):
            self._channel.queue_purge(self.config['queue'])
            self._channel.basic_publish(
                exchange=self.config['exchange'],
                routing_key=self.config['routing_key'],
                body=message
            )
            self._connection.ioloop.call_later(1, self.stop)

        self.on_queue_declared = publish_when_ready

        self._connection = pika.SelectConnection(
            parameters=self.parameters,
            on_open_callback=self.on_connection_open,
            on_close_callback=self.on_connection_closed
        )
        self._connection.ioloop.start()

    def stop(self):
        if self._channel:
            self._channel.close()
        if self._connection:
            self._connection.close()

def clear_queue(self, channel):
        channel.queue_purge(self.config['queue'])
