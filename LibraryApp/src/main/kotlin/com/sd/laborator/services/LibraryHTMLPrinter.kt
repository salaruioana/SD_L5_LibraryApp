package com.sd.laborator.services

import com.sd.laborator.interfaces.HTMLPrinter
import com.sd.laborator.model.Book
import org.springframework.stereotype.Service

@Service
class LibraryHTMLPrinter: HTMLPrinter {
    override fun printHTML(books: Set<Book>): String {
        var content = "<html><head><title>Biblioteca</title></head><body><table>"
        books.forEach {
            content += "<tr><td>${it.name}</td><td>${it.author}</td><td>${it.publisher}</td></tr>"
        }
        content += "</table></body></html>"
        return content
    }
}