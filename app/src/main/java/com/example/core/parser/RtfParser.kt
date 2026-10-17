package com.example.core.parser

import java.io.InputStream

object RtfParser {

    fun parse(inputStream: InputStream): String {
        val rtf = inputStream.bufferedReader(Charsets.UTF_8).readText()
        val output = StringBuilder()
        var i = 0
        var inControlWord = false
        val controlWord = StringBuilder()
        var braceDepth = 0

        while (i < rtf.length) {
            val c = rtf[i]
            when {
                c == '{' -> {
                    braceDepth++
                    i++
                }
                c == '}' -> {
                    braceDepth = (braceDepth - 1).coerceAtLeast(0)
                    i++
                }
                c == '\\' -> {
                    i++
                    if (i < rtf.length && (rtf[i] == '\\' || rtf[i] == '{' || rtf[i] == '}')) {
                        output.append(rtf[i])
                        i++
                    } else if (i < rtf.length && rtf[i] == '\'') {
                        // Hex escaped character \'hh
                        i++
                        if (i + 1 < rtf.length) {
                            val hex = rtf.substring(i, i + 2)
                            try {
                                output.append(hex.toInt(16).toChar())
                            } catch (_: Exception) {}
                            i += 2
                        }
                    } else {
                        // Read control word
                        controlWord.clear()
                        while (i < rtf.length && rtf[i].isLetter()) {
                            controlWord.append(rtf[i])
                            i++
                        }
                        // Optional numeric parameter
                        while (i < rtf.length && (rtf[i].isDigit() || rtf[i] == '-')) {
                            i++
                        }
                        // Optional trailing space
                        if (i < rtf.length && rtf[i] == ' ') {
                            i++
                        }

                        val cw = controlWord.toString().lowercase()
                        if (cw == "par" || cw == "line") {
                            output.append("\n")
                        } else if (cw == "tab") {
                            output.append("\t")
                        }
                    }
                }
                c == '\r' || c == '\n' -> {
                    // RTF line breaks in source are ignored unless \par is specified
                    i++
                }
                else -> {
                    output.append(c)
                    i++
                }
            }
        }
        return output.toString().trim()
    }
}
