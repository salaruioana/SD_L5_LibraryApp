package com.sd.laborator.services

import com.sd.laborator.interfaces.RawPrinter
import com.sd.laborator.model.Book
import org.springframework.stereotype.Service

@Service
class LibraryRawPrinter: RawPrinter {
    override fun printRaw(books: Set<Book>): String {
        var content = ""
        books.forEach { content += "${it.name}\n${it.author}\n${it.publisher}\n${it.content}\n\n" }
        return content
    }
}