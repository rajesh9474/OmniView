package com.example.core.parser

object MarkdownParser {

    fun parse(rawText: String): ParsedMarkdown {
        val lines = rawText.lines()
        val blocks = mutableListOf<MarkdownBlock>()

        var inCodeBlock = false
        var codeBlockLang = ""
        val codeBuffer = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    blocks.add(MarkdownBlock.CodeBlock(codeBlockLang, codeBuffer.toString().trimEnd()))
                    codeBuffer.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                    codeBlockLang = trimmed.removePrefix("```").trim()
                }
                continue
            }

            if (inCodeBlock) {
                codeBuffer.append(line).append("\n")
                continue
            }

            if (trimmed.isEmpty()) {
                continue
            }

            when {
                trimmed.startsWith("### ") -> {
                    blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ")))
                }
                trimmed.startsWith("## ") -> {
                    blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ")))
                }
                trimmed.startsWith("# ") -> {
                    blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ")))
                }
                trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                    blocks.add(MarkdownBlock.Divider)
                }
                trimmed.startsWith("> ") -> {
                    blocks.add(MarkdownBlock.BlockQuote(trimmed.removePrefix("> ")))
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2)))
                }
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val content = trimmed.substringAfter(". ")
                    blocks.add(MarkdownBlock.BulletItem(content))
                }
                else -> {
                    blocks.add(MarkdownBlock.Paragraph(line))
                }
            }
        }

        if (inCodeBlock && codeBuffer.isNotEmpty()) {
            blocks.add(MarkdownBlock.CodeBlock(codeBlockLang, codeBuffer.toString()))
        }

        return ParsedMarkdown(rawText = rawText, blocks = blocks)
    }
}
