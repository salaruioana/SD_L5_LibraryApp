package com.sd.laborator.services

import com.sd.laborator.interfaces.XMLPrinter
import com.sd.laborator.model.Book
import org.springframework.stereotype.Service

@Service
class LibraryXMLPrinter: XMLPrinter {
    override fun printXML(books: Set<Book>): String {
        var content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<books>\n"
        books.forEach {
            content += "  <book>\n" +
                    "    <title>${it.name}</title>\n" +
                    "    <author>${it.author}</author>\n" +
                    "    <publisher>${it.publisher}</publisher>\n" +
                    "  </book>\n"
        }
        content += "</books>"
        return content
    }
}