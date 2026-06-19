package com.sd.laborator.components

import com.sd.laborator.interfaces.*
import com.sd.laborator.model.Book
import com.sd.laborator.model.Content
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class LibraryAppComponent @Autowired constructor(
    private val htmlPrinter: HTMLPrinter,
    private val jsonPrinter: JSONPrinter,
    private val rawPrinter: RawPrinter,
    private val xmlPrinter: XMLPrinter ){
    @Autowired
    private lateinit var libraryDAO: LibraryDAO


    @Autowired
    private lateinit var connectionFactory: RabbitMqConnectionFactoryComponent
    private lateinit var amqpTemplate: AmqpTemplate

    @Autowired
    fun initTemplate() {
        this.amqpTemplate = connectionFactory.rabbitTemplate()
    }

    fun sendMessage(msg: String) {
        this.amqpTemplate.convertAndSend(connectionFactory.getExchange(),
                                         connectionFactory.getRoutingKey(),
                                         msg)
    }

    @RabbitListener(queues = ["\${libraryapp.rabbitmq.queue}"])
    fun recieveMessage(msg: String) {
        // the result needs processing
        val processedMsg = (msg.split(",").map { it.toInt().toChar() }).joinToString(separator="")
        try {
            val (function, parameter) = processedMsg.split(":")
            val result: String? = when(function) {
                "print" -> customPrint(parameter)
                "find" -> customFind(parameter)
                "add" ->customAdd(parameter)
                else -> null
            }
            if (result != null) sendMessage(result)
        } catch (e: Exception) {
            println(e)
        }
    }

    fun customPrint(format: String): String {
        return when(format) {
            "html" -> htmlPrinter.printHTML(libraryDAO.getBooks())
            "json" -> jsonPrinter.printJSON(libraryDAO.getBooks())
            "raw" -> rawPrinter.printRaw(libraryDAO.getBooks())
            "xml" -> xmlPrinter.printXML(libraryDAO.getBooks())
            else -> "Not implemented"
        }
    }

    fun customFind(searchParameter: String): String {
        val (field, value) = searchParameter.split("=")
        return when(field) {
            "author" -> this.jsonPrinter.printJSON(this.libraryDAO.findAllByAuthor(value))
            "title" -> this.jsonPrinter.printJSON(this.libraryDAO.findAllByTitle(value))
            "publisher" -> this.jsonPrinter.printJSON(this.libraryDAO.findAllByPublisher(value))
            else -> "Not a valid field"
        }
    }

    fun addBook(book: Book): Boolean {
        return try {
            this.libraryDAO.addBook(book)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun customAdd(bookFields: String): String {
        // Ne așteptăm la formatul: autor;continut;titlu;editura
        val tokens = bookFields.split(";")
        if (tokens.size != 4) {
            return "Error: Format invalid pentru adaugare! Trebuie sa fie autor;continut;titlu;editura"
        }

        val author = tokens[0]
        val text = tokens[1]
        val name = tokens[2]
        val publisher = tokens[3]

        // Presupunând că Book primeste un obiect Content în constructor ca în exemplul standard de laborator:
        // class Book(val content: Content) sau similar. Adaptează constructorul dacă diferă la tine!
        val contentObj = Content(author, text, name, publisher)
        val bookObj = Book(contentObj)

        val success = addBook(bookObj)
        return if (success) {
            "Success: Cartea '$name' a fost adaugata cu succes in biblioteca."
        } else {
            "Error: Nu s-a putut adauga cartea."
        }
    }
}