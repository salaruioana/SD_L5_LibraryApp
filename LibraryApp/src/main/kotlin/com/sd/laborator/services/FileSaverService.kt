package com.sd.laborator.services

import com.sd.laborator.interfaces.FileSaver
import org.springframework.stereotype.Service
import java.io.File

@Service
class FileSaverService:FileSaver {
    override fun saveToFile(content: String, format: String, filename: String ) {
        val extension = when (format.lowercase()) {
            "json" -> "json"
            "html" -> "html"
            "text", "raw" -> "txt"
            "xml" -> "xml"
            else -> "txt"
        }
        val file = File("$filename.$extension")
        file.writeText(content)
        println("Fișierul a fost salvat cu succes: ${file.absolutePath}")
    }
}