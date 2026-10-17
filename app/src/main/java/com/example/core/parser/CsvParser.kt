package com.example.core.parser

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object CsvParser {

    fun parse(inputStream: InputStream): ParsedCsv {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val lines = reader.readLines()
        if (lines.isEmpty()) {
            return ParsedCsv(emptyList(), emptyList())
        }

        // Detect delimiter (comma vs tab vs semicolon)
        val sample = lines.take(3).joinToString("\n")
        val delimiter = when {
            sample.count { it == '\t' } > sample.count { it == ',' } && sample.count { it == '\t' } > sample.count { it == ';' } -> '\t'
            sample.count { it == ';' } > sample.count { it == ',' } -> ';'
            else -> ','
        }

        val allRows = lines.filter { it.isNotBlank() }.map { parseLine(it, delimiter) }
        if (allRows.isEmpty()) {
            return ParsedCsv(emptyList(), emptyList())
        }

        val headers = allRows.first()
        val dataRows = allRows.drop(1)
        return ParsedCsv(headers = headers, rows = dataRows)
    }

    private fun parseLine(line: String, delimiter: Char): List<String> {
        val cells = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    cells.add(current.toString().trim())
                    current.clear()
                }
                else -> {
                    current.append(c)
                }
            }
            i++
        }
        cells.add(current.toString().trim())
        return cells
    }
}
