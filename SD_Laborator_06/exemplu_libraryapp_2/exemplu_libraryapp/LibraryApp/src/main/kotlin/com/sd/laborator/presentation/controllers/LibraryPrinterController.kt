package com.sd.laborator.presentation.controllers

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
            "addBook" -> _libraryDAOService.addBook(book!!)
            "getBooks" -> _libraryDAOService.getBooks()
            "findAllByName" -> _libraryDAOService.findAllByName(name!!)
            "findAllByAuthor" -> _libraryDAOService.findAllByAuthor(author!!)
            "findAllByPublisher" -> _libraryDAOService.findAllByPublisher(publisher!!)
            "updateBook" -> _libraryDAOService.updateBook(book!!)
            "deleteBook" -> _libraryDAOService.deleteBook(name!!)
            else -> null
        }
        println("Result: $result")
        if (result != null) sendMessage(result.toString())
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