package com.sd.laborator.interfaces

interface FileSaver {
    fun saveToFile(content: String, format: String, filename: String = "search_result")
}