import pika
from retry import retry


class LibraryRabbitMq:
    config = {
        'host': '0.0.0.0',
        'port': 5672,
        'username': 'octavian',
        'password': 'octavian',
        'exchange': 'librarylab6app.direct',
        'routing_key': 'librarylab6app.routingkey1',
        'queue': 'librarylab6app.queue'
    }
    credentials = pika.PlainCredentials(config['username'], config['password'])
    parameters = (pika.ConnectionParameters(host=config['host']),
                  pika.ConnectionParameters(port=config['port']),
                  pika.ConnectionParameters(credentials=credentials))

    def on_received_message(self, blocking_channel, deliver, properties,
                            message):
        result = message.decode('utf-8')
        blocking_channel.confirm_delivery()
        try:
            print(result)
        except Exception:
            print("wrong data format")
        finally:
            blocking_channel.stop_consuming()

    @retry(pika.exceptions.AMQPConnectionError, delay=5, jitter=(1, 3))
    def receive_message(self):
        # automatically close the connection
        with pika.BlockingConnection(self.parameters) as connection:
            # automatically close the channel
            with connection.channel() as channel:
                channel.basic_consume(self.config['queue'],
                                      self.on_received_message)
                try:
                    channel.start_consuming()
                # Don't recover connections closed by server
                except pika.exceptions.ConnectionClosedByBroker:
                    print("Connection closed by broker.")
                # Don't recover on channel errors
                except pika.exceptions.AMQPChannelError:
                    print("AMQP Channel Error")
                # Don't recover from KeyboardInterrupt
                except KeyboardInterrupt:
                    print("Application closed.")

    def send_message(self, message):
        # automatically close the connection
        with pika.BlockingConnection(self.parameters) as connection:
            # automatically close the channel
            with connection.channel() as channel:
                self.clear_queue(channel)
                channel.basic_publish(exchange=self.config['exchange'],
                                      routing_key=self.config['routing_key'],
                                      body=message)

    def clear_queue(self, channel):
        channel.queue_purge(self.config['queue'])


def print_menu():
    print('\n--- Library Manager CLI ---')
    print('0 --> Exit program')
    print('1 --> Print all books')
    print('2 --> Add book')
    print('3 --> Find book by Name')    
    print('4 --> Find book by Author')
    print('5 --> Find book by Publisher')
    print('6 --> Update book')
    print('7 --> Delete book')
    return input("Option = ")


if __name__ == '__main__':
    rabbit_mq = LibraryRabbitMq()
    
    rabbit_mq.send_message("createLibraryTable~")   
    rabbit_mq.send_message("createCacheTable~")    

    while True:
        option = print_menu()
        
        if option == '0':
            break
            
        elif option == '1':
            rabbit_mq.send_message("getBooks~")
            rabbit_mq.receive_message()
            
        elif option == '2':
            author = input("Author name: ")
            text = input("Text: ")
            name = input("Book name: ")
            publisher = input("Publisher name: ")            
            rabbit_mq.send_message("addBook~id=-1;author={};text={};name={};publisher={}".format(author, text, name, publisher))

        elif option == '3':
            name = input("Name: ")         
            rabbit_mq.send_message("findAllByName~name={}".format(name))
            rabbit_mq.receive_message()

        elif option == '4':
            author = input("Author: ")         
            rabbit_mq.send_message("findAllByAuthor~author={}".format(author))
            rabbit_mq.receive_message()

        elif option == '5':
            publisher = input("Publisher: ")         
            rabbit_mq.send_message("findAllByPublisher~publisher={}".format(publisher))
            rabbit_mq.receive_message()

        elif option == '6':
            idbook = input("Id: ")
            author = input("Author name: ")
            text = input("Text: ")
            name = input("Book name: ")
            publisher = input("Publisher name: ")            
            rabbit_mq.send_message("updateBook~id={};author={};text={};name={};publisher={}".format(idbook, author, text, name, publisher))

        elif option == '7':
            name = input("Book name: ")
            rabbit_mq.send_message("deleteBook~name={}".format(name))

        else:
            print("Invalid option. Please try again.")
