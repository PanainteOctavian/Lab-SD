package com.sd.laborator.presentation.controllers

import com.sd.laborator.business.interfaces.ICachingService
import com.sd.laborator.business.interfaces.ILibraryDAOService
import com.sd.laborator.business.interfaces.ILibraryPrinterService
import com.sd.laborator.models.Book
import com.sd.laborator.models.Content
import com.sd.laborator.presentation.config.RabbitMqComponent
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

@Controller
class LibraryPrinterController {
    @Autowired
    private lateinit var _libraryDAOService: ILibraryDAOService

    @Autowired
    private lateinit var _cachingService: ICachingService

    @Autowired
    private lateinit var _rabbitMqComponent: RabbitMqComponent

    private lateinit var _amqpTemplate: AmqpTemplate

    @Autowired
    fun initTemplate() {
        this._amqpTemplate = _rabbitMqComponent.rabbitTemplate()
    }

    @RabbitListener(queues = ["\${librarylab6app.rabbitmq.queue}"])
    fun receiveMessage(msg: String) {
        val (operation, parameters) = msg.split('~')
        var book: Book? = null
        var author: String? = null
        var name: String? = null
        var publisher: String? = null
        var text: String? = null

        // addBook~id=-1;author={};text={};name={};publisher={}
        if("id=" in parameters) {
            println(parameters)
            val params: List<String> = parameters.split(';')
            try {
                val id = params[0].split('=')[1].toInt()
                val cont = Content(
                    params[1].split('=')[1],
                    params[2].split('=')[1],
                    params[3].split('=')[1],
                    params[4].split('=')[1],
                )
                book = Book(id, cont)
            } catch (e: Exception) {
                print("Error parsing the parameters: ")
                println(params)
                return
            }
        } else if ("author=" in parameters) {
            author = parameters.split('=')[1]
        } else if ("name=" in parameters) {
            name = parameters.split("=")[1]
        } else if ("publisher=" in parameters) {
            publisher = parameters.split("=")[1]
        } else if ("text=" in parameters) {
            text = parameters.split("=")[1]
        }
        println("Parameters: $parameters")
        println("Author: $author")
        println("name: $name")
        println("Publisher: $publisher")
        println("Text: $text")
        val result: Any? = when(operation) {
            "createLibraryTable" -> _libraryDAOService.createLibraryTable()
            "createCacheTable" -> _cachingService.createCacheTable()
            "addBook" -> _libraryDAOService.addBook(book!!)
            "getBooks" -> _libraryDAOService.getBooks()

            "findAllByAuthor" -> {
                val cacheKey = "author=$author"
                //               cheia       fct lambda care se exec daca cacheu n are rezultat
                getCachedOrFetch(cacheKey) { _libraryDAOService.findAllByAuthor(author!!) }
            }
            "findAllByName" -> {
                val cacheKey = "name=$name"
                getCachedOrFetch(cacheKey) { _libraryDAOService.findAllByName(name!!) }
            }
            "findAllByPublisher" -> {
                val cacheKey = "publisher=$publisher"
                getCachedOrFetch(cacheKey) { _libraryDAOService.findAllByPublisher(publisher!!) }
            }

            "updateBook" -> _libraryDAOService.updateBook(book!!)
            "deleteBook" -> _libraryDAOService.deleteBook(name!!)
            else -> null
        }
        println("Result: $result")
        if (result != null) sendMessage(result.toString())
    }

    // getCachedOrFetch("author=Verne", _libraryDAOService.findAllByAuthor("Verne"))
    private fun getCachedOrFetch(cacheKey: String, fetch: () -> Any?): Any? {
        val cached = _cachingService.exists(cacheKey)

        if (cached != null && !_cachingService.isExpired(cacheKey)) {
            println("Cache HIT for: $cacheKey")
            return cached.result
        }

        if (cached != null) {
            println("Cache EXPIRED for: $cacheKey")
            _cachingService.deleteCache(cacheKey)
        } else {
            println("Cache MISS for: $cacheKey")
        }

        val result = fetch() ?: return null
        _cachingService.addToCache(cacheKey, result.toString())
        return result
    }

    private fun sendMessage(msg: String) {
        println("Message to send: $msg")
        this._amqpTemplate.convertAndSend(
            _rabbitMqComponent.getExchange(),
            _rabbitMqComponent.getRoutingKey(),
            msg
        )
    }

}