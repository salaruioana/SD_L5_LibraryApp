package com.sd.laborator.services

import com.sd.laborator.interfaces.JSONPrinter
import com.sd.laborator.model.Book
import org.springframework.stereotype.Service

@Service
class LibraryJSONPrinter: JSONPrinter {
    override fun printJSON(books: Set<Book>): String {
        var content = "[\n"
        books.forEach {
            if (it != books.last())
                content += " {\"Titlu\": \"${it.name}\", \"Autor\":\"${it.author}\", \"Editura\":\"${it.publisher}\"},\n"
            else
                content += " {\"Titlu\": \"${it.name}\", \"Autor\":\"${it.author}\", \"Editura\":\"${it.publisher}\"}\n"
        }
        content += "]\n"
        return content
    }
}