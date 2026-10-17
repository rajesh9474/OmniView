package com.example.core.sample

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.model.FileItem
import com.example.data.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SampleFilesGenerator {

    suspend fun generateAllSamples(context: Context): List<FileItem> = withContext(Dispatchers.IO) {
        val samplesDir = File(context.filesDir, "samples").apply { mkdirs() }
        val sampleList = mutableListOf<FileItem>()

        // 1. PDF Document
        val pdfFile = File(samplesDir, "Annual_Report_2026.pdf")
        if (!pdfFile.exists() || pdfFile.length() == 0L) {
            generateSamplePdf(pdfFile)
        }
        sampleList.add(createSampleItem(pdfFile, "Annual_Report_2026.pdf", FileType.PDF))

        // 2. Word DOCX
        val docxFile = File(samplesDir, "Project_Proposal.docx")
        if (!docxFile.exists() || docxFile.length() == 0L) {
            generateSampleDocx(docxFile)
        }
        sampleList.add(createSampleItem(docxFile, "Project_Proposal.docx", FileType.DOCX))

        // 3. Excel XLSX
        val xlsxFile = File(samplesDir, "Quarterly_Budget.xlsx")
        if (!xlsxFile.exists() || xlsxFile.length() == 0L) {
            generateSampleXlsx(xlsxFile)
        }
        sampleList.add(createSampleItem(xlsxFile, "Quarterly_Budget.xlsx", FileType.XLSX))

        // 4. PowerPoint PPTX
        val pptxFile = File(samplesDir, "Tech_Innovation_Deck.pptx")
        if (!pptxFile.exists() || pptxFile.length() == 0L) {
            generateSamplePptx(pptxFile)
        }
        sampleList.add(createSampleItem(pptxFile, "Tech_Innovation_Deck.pptx", FileType.PPTX))

        // 5. Markdown
        val mdFile = File(samplesDir, "Architecture_Documentation.md")
        if (!mdFile.exists() || mdFile.length() == 0L) {
            mdFile.writeText(sampleMarkdownContent)
        }
        sampleList.add(createSampleItem(mdFile, "Architecture_Documentation.md", FileType.MARKDOWN))

        // 6. CSV Spreadsheet
        val csvFile = File(samplesDir, "Global_Sales_Data.csv")
        if (!csvFile.exists() || csvFile.length() == 0L) {
            csvFile.writeText(sampleCsvContent)
        }
        sampleList.add(createSampleItem(csvFile, "Global_Sales_Data.csv", FileType.CSV))

        // 7. JSON Data
        val jsonFile = File(samplesDir, "Cloud_Config.json")
        if (!jsonFile.exists() || jsonFile.length() == 0L) {
            jsonFile.writeText(sampleJsonContent)
        }
        sampleList.add(createSampleItem(jsonFile, "Cloud_Config.json", FileType.JSON))

        // 8. XML Document
        val xmlFile = File(samplesDir, "System_Configuration.xml")
        if (!xmlFile.exists() || xmlFile.length() == 0L) {
            xmlFile.writeText(sampleXmlContent)
        }
        sampleList.add(createSampleItem(xmlFile, "System_Configuration.xml", FileType.XML))

        // 9. HTML Report
        val htmlFile = File(samplesDir, "Executive_Dashboard.html")
        if (!htmlFile.exists() || htmlFile.length() == 0L) {
            htmlFile.writeText(sampleHtmlContent)
        }
        sampleList.add(createSampleItem(htmlFile, "Executive_Dashboard.html", FileType.HTML))

        // 10. Rich Text RTF
        val rtfFile = File(samplesDir, "Official_Release_Memo.rtf")
        if (!rtfFile.exists() || rtfFile.length() == 0L) {
            rtfFile.writeText(sampleRtfContent)
        }
        sampleList.add(createSampleItem(rtfFile, "Official_Release_Memo.rtf", FileType.RTF))

        // 11. OpenDocument Text ODT
        val odtFile = File(samplesDir, "OpenDoc_Report.odt")
        if (!odtFile.exists() || odtFile.length() == 0L) {
            generateSampleOdfZip(odtFile, "text", sampleOdtXmlContent)
        }
        sampleList.add(createSampleItem(odtFile, "OpenDoc_Report.odt", FileType.ODT))

        // 12. OpenDocument Spreadsheet ODS
        val odsFile = File(samplesDir, "Warehouse_Inventory.ods")
        if (!odsFile.exists() || odsFile.length() == 0L) {
            generateSampleOdfZip(odsFile, "spreadsheet", sampleOdsXmlContent)
        }
        sampleList.add(createSampleItem(odsFile, "Warehouse_Inventory.ods", FileType.ODS))

        // 13. Plain Text
        val txtFile = File(samplesDir, "Server_Access_Logs.txt")
        if (!txtFile.exists() || txtFile.length() == 0L) {
            txtFile.writeText(sampleTxtContent)
        }
        sampleList.add(createSampleItem(txtFile, "Server_Access_Logs.txt", FileType.TXT))

        sampleList
    }

    private fun createSampleItem(file: File, name: String, type: FileType): FileItem {
        return FileItem(
            id = file.absolutePath.hashCode().toLong(),
            name = name,
            uriString = file.toURI().toString(),
            fileType = type,
            sizeBytes = file.length(),
            lastOpenedTimestamp = System.currentTimeMillis(),
            isFavorite = false,
            isSample = true,
            sampleAssetKey = file.name
        )
    }

    private fun generateSamplePdf(file: File) {
        val document = PdfDocument()

        // Page 1: Executive Overview
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page1 = document.startPage(pageInfo1)
        val canvas1 = page1.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(30, 58, 138)
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 13f
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 12f
            isAntiAlias = true
        }

        val accentBarPaint = Paint().apply {
            color = Color.rgb(14, 165, 233)
        }

        // Header
        canvas1.drawRect(50f, 40f, 545f, 44f, accentBarPaint)
        canvas1.drawText("OMNIVIEW ENTERPRISE 2026", 50f, 80f, titlePaint)
        canvas1.drawText("Comprehensive Universal File Architecture & Document Intelligence Report", 50f, 105f, subtitlePaint)

        // Section 1
        canvas1.drawText("1. Executive Summary", 50f, 160f, Paint().apply {
            color = Color.rgb(15, 23, 42); textSize = 16f; isFakeBoldText = true; isAntiAlias = true
        })

        val summaryLines = listOf(
            "This report validates the multi-format rendering engine implemented in OmniView.",
            "The application delivers seamless, zero-latency document viewing across office formats,",
            "including PDF, Microsoft Office (Word, Excel, PowerPoint), OpenDocument standards, and",
            "modern data interchanges such as Markdown, JSON, XML, and CSV.",
            "",
            "Key Architectural Benchmarks:",
            "  * Native Android PdfRenderer pipeline with on-demand hardware acceleration",
            "  * Stream-based XML parsing to minimize memory footprint on large documents",
            "  * Safe, local-first sandbox model preventing unauthorized script execution",
            "  * Comprehensive offline caching and responsive Compose presentation"
        )

        var y = 190f
        for (line in summaryLines) {
            canvas1.drawText(line, 50f, y, bodyPaint)
            y += 22f
        }

        // Metric Card Background
        canvas1.drawRect(50f, 390f, 545f, 490f, Paint().apply { color = Color.rgb(241, 245, 249) })
        canvas1.drawText("System Performance Metrics", 70f, 420f, Paint().apply {
            color = Color.rgb(15, 23, 42); textSize = 14f; isFakeBoldText = true; isAntiAlias = true
        })
        canvas1.drawText("Average File Load: 18ms    |    RAM Footprint: < 24MB    |    Render Fidelity: 100%", 70f, 455f, bodyPaint)

        // Page Number
        canvas1.drawText("Page 1 of 2", 270f, 800f, subtitlePaint)
        document.finishPage(page1)

        // Page 2: Format Compatibility
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = document.startPage(pageInfo2)
        val canvas2 = page2.canvas

        canvas2.drawText("2. Multi-Format Compatibility Matrix", 50f, 80f, Paint().apply {
            color = Color.rgb(15, 23, 42); textSize = 18f; isFakeBoldText = true; isAntiAlias = true
        })
        canvas2.drawText("Tested and verified against enterprise file standards:", 50f, 110f, subtitlePaint)

        val formatList = listOf(
            "PDF (ISO 32000-1) - Complete page navigation, zoom, and vector text rendering",
            "DOCX / DOC - Paragraph styles, hierarchy headings, and formatted data tables",
            "XLSX / XLS - Multi-tab workbooks, column letter headers, and numeric cell formatting",
            "PPTX / PPT - Slide deck carousel, presenter notes, and bullet presentation",
            "Markdown - CommonMark compliant AST renderer with styled code blocks",
            "CSV / TSV - Auto-delimiter detection with interactive grid layout",
            "JSON & XML - Syntax highlighting with collapsible tree structures",
            "HTML & RTF - Secure sandboxed preview with responsive styling"
        )

        var y2 = 150f
        for (item in formatList) {
            canvas2.drawCircle(60f, y2 - 4f, 4f, accentBarPaint)
            canvas2.drawText(item, 75f, y2, bodyPaint)
            y2 += 36f
        }

        canvas2.drawText("Page 2 of 2", 270f, 800f, subtitlePaint)
        document.finishPage(page2)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
    }

    private fun generateSampleDocx(file: File) {
        val documentXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body>
                <w:p>
                  <w:pPr><w:pStyle w:val="Title"/></w:pPr>
                  <w:r><w:rPr><w:b/></w:rPr><w:t>Project Horizon: Strategic Architecture</w:t></w:r>
                </w:p>
                <w:p>
                  <w:pPr><w:pStyle w:val="Heading1"/></w:pPr>
                  <w:r><w:rPr><w:b/></w:rPr><w:t>1. Project Overview &amp; Mission</w:t></w:r>
                </w:p>
                <w:p>
                  <w:r><w:t>Project Horizon represents our next-generation software foundation, built to deliver universal document processing with unmatched security, reliability, and speed.</w:t></w:r>
                </w:p>
                <w:p>
                  <w:pPr><w:pStyle w:val="Heading2"/></w:pPr>
                  <w:r><w:rPr><w:b/></w:rPr><w:t>2. Key Objectives</w:t></w:r>
                </w:p>
                <w:p>
                  <w:pPr><w:numPr/></w:pPr>
                  <w:r><w:t>Deliver zero-install document preview across all major desktop and mobile platforms.</w:t></w:r>
                </w:p>
                <w:p>
                  <w:pPr><w:numPr/></w:pPr>
                  <w:r><w:t>Maintain a strictly local-first processing boundary to eliminate data exfiltration risks.</w:t></w:r>
                </w:p>
                <w:p>
                  <w:pPr><w:numPr/></w:pPr>
                  <w:r><w:t>Provide full keyboard navigation and accessible touch targets adhering to Material 3 standards.</w:t></w:r>
                </w:p>
                <w:tbl>
                  <w:tr>
                    <w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>Milestone</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>Target Date</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>Status</w:t></w:r></w:p></w:tc>
                  </w:tr>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>Alpha Prototype</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Q1 2026</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Completed</w:t></w:r></w:p></w:tc>
                  </w:tr>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>Beta Release</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Q2 2026</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Active Testing</w:t></w:r></w:p></w:tc>
                  </w:tr>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>Enterprise Rollout</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Q4 2026</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Scheduled</w:t></w:r></w:p></w:tc>
                  </w:tr>
                </w:tbl>
              </w:body>
            </w:document>
        """.trimIndent()

        ZipOutputStream(FileOutputStream(file)).use { zos ->
            zos.putNextEntry(ZipEntry("word/document.xml"))
            zos.write(documentXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    }

    private fun generateSampleXlsx(file: File) {
        val sharedStringsXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="15" uniqueCount="15">
              <si><t>Department</t></si>
              <si><t>Q1 Budget</t></si>
              <si><t>Q2 Budget</t></si>
              <si><t>Q3 Budget</t></si>
              <si><t>Total Spend</t></si>
              <si><t>Engineering</t></si>
              <si><t>Product Design</t></si>
              <si><t>Global Marketing</t></si>
              <si><t>Customer Success</t></si>
              <si><t>Information Security</t></si>
              <si><t>Total</t></si>
              <si><t>Category</t></si>
              <si><t>Vendor</t></si>
              <si><t>Allocated</t></si>
              <si><t>Notes</t></si>
            </sst>
        """.trimIndent()

        val workbookXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <sheets>
                <sheet name="Department Budgets" sheetId="1" r:id="rId1" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"/>
                <sheet name="Vendor Allocations" sheetId="2" r:id="rId2" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"/>
              </sheets>
            </workbook>
        """.trimIndent()

        val sheet1Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <sheetData>
                <row r="1">
                  <c r="A1" t="s"><v>0</v></c>
                  <c r="B1" t="s"><v>1</v></c>
                  <c r="C1" t="s"><v>2</v></c>
                  <c r="D1" t="s"><v>3</v></c>
                  <c r="E1" t="s"><v>4</v></c>
                </row>
                <row r="2">
                  <c r="A2" t="s"><v>5</v></c>
                  <c r="B2"><v>$450,000</v></c>
                  <c r="C2"><v>$480,000</v></c>
                  <c r="D2"><v>$510,000</v></c>
                  <c r="E2"><v>$1,440,000</v></c>
                </row>
                <row r="3">
                  <c r="A3" t="s"><v>6</v></c>
                  <c r="B3"><v>$120,000</v></c>
                  <c r="C3"><v>$135,000</v></c>
                  <c r="D3"><v>$140,000</v></c>
                  <c r="E3"><v>$395,000</v></c>
                </row>
                <row r="4">
                  <c r="A4" t="s"><v>7</v></c>
                  <c r="B4"><v>$250,000</v></c>
                  <c r="C4"><v>$310,000</v></c>
                  <c r="D4"><v>$280,000</v></c>
                  <c r="E4"><v>$840,000</v></c>
                </row>
                <row r="5">
                  <c r="A5" t="s"><v>8</v></c>
                  <c r="B5"><v>$95,000</v></c>
                  <c r="C5"><v>$105,000</v></c>
                  <c r="D5"><v>$115,000</v></c>
                  <c r="E5"><v>$315,000</v></c>
                </row>
                <row r="6">
                  <c r="A6" t="s"><v>9</v></c>
                  <c r="B6"><v>$180,000</v></c>
                  <c r="C6"><v>$190,000</v></c>
                  <c r="D6"><v>$200,000</v></c>
                  <c r="E6"><v>$570,000</v></c>
                </row>
              </sheetData>
            </worksheet>
        """.trimIndent()

        val sheet2Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <sheetData>
                <row r="1">
                  <c r="A1" t="s"><v>11</v></c>
                  <c r="B1" t="s"><v>12</v></c>
                  <c r="C1" t="s"><v>13</v></c>
                  <c r="D1" t="s"><v>14</v></c>
                </row>
                <row r="2">
                  <c r="A2"><v>Cloud Compute</v></c>
                  <c r="B2"><v>Google Cloud Platform</v></c>
                  <c r="C2"><v>$85,000</v></c>
                  <c r="D2"><v>Core infrastructure hosting</v></c>
                </row>
                <row r="3">
                  <c r="A3"><v>Database Storage</v></c>
                  <c r="B3"><v>Enterprise Store</v></c>
                  <c r="C3"><v>$35,000</v></c>
                  <c r="D3"><v>High-availability replica nodes</v></c>
                </row>
              </sheetData>
            </worksheet>
        """.trimIndent()

        ZipOutputStream(FileOutputStream(file)).use { zos ->
            zos.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
            zos.write(sharedStringsXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("xl/workbook.xml"))
            zos.write(workbookXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zos.write(sheet1Xml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("xl/worksheets/sheet2.xml"))
            zos.write(sheet2Xml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    }

    private fun generateSamplePptx(file: File) {
        val slide1Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
              <p:cSld>
                <p:spTree>
                  <p:sp>
                    <p:nvSpPr><p:cNvPr id="2" name="Title 1"/><p:nvPr><p:ph type="ctrTitle"/></p:nvPr></p:nvSpPr>
                    <p:txBody>
                      <a:p><a:r><a:t>OmniView: Next-Gen File Platform</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                  <p:sp>
                    <p:nvSpPr><p:cNvPr id="3" name="Subtitle 2"/><p:nvPr><p:ph type="subTitle"/></p:nvPr></p:nvSpPr>
                    <p:txBody>
                      <a:p><a:r><a:t>Unified document viewing, multi-format rendering, and complete data privacy.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>Executive Strategy &amp; Innovation Briefing - 2026</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                </p:spTree>
              </p:cSld>
            </p:sld>
        """.trimIndent()

        val slide2Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
              <p:cSld>
                <p:spTree>
                  <p:sp>
                    <p:nvSpPr><p:cNvPr id="4" name="Title 1"/><p:nvPr><p:ph type="title"/></p:nvPr></p:nvSpPr>
                    <p:txBody>
                      <a:p><a:r><a:t>Core Value Propositions</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                  <p:sp>
                    <p:nvSpPr><p:cNvPr id="5" name="Content 2"/></p:nvPr>
                    <p:txBody>
                      <a:p><a:r><a:t>All-In-One Universal Compatibility: 17+ file formats supported in a single app.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>Zero Cloud Leakage: 100% on-device parsing ensures strict enterprise confidentiality.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>Instant File Opening: Streaming memory architecture opens large files in milliseconds.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>Rich Navigation: Interactive zoom, slide thumbnails, and global content search.</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                </p:spTree>
              </p:cSld>
            </p:sld>
        """.trimIndent()

        val slide3Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
              <p:cSld>
                <p:spTree>
                  <p:sp>
                    <p:nvSpPr><p:cNvPr id="6" name="Title 1"/><p:nvPr><p:ph type="title"/></p:nvPr></p:nvSpPr>
                    <p:txBody>
                      <a:p><a:r><a:t>Roadmap &amp; Milestones</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                  <p:sp>
                    <p:nvSpPr><p:cNvPr id="7" name="Content 2"/></p:nvPr>
                    <p:txBody>
                      <a:p><a:r><a:t>Phase 1: Foundation Architecture &amp; Room Database Persistence</a:t></a:r></a:p>
                      <a:p><a:r><a:t>Phase 2: Full Document Scaffolding with Zoom &amp; Rotation Tools</a:t></a:r></a:p>
                      <a:p><a:r><a:t>Phase 3: Deep File Associations &amp; System Sharing Integration</a:t></a:r></a:p>
                      <a:p><a:r><a:t>Phase 4: Global Enterprise Release &amp; Accessibility Certification</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                </p:spTree>
              </p:cSld>
            </p:sld>
        """.trimIndent()

        ZipOutputStream(FileOutputStream(file)).use { zos ->
            zos.putNextEntry(ZipEntry("ppt/slides/slide1.xml"))
            zos.write(slide1Xml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("ppt/slides/slide2.xml"))
            zos.write(slide2Xml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("ppt/slides/slide3.xml"))
            zos.write(slide3Xml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    }

    private fun generateSampleOdfZip(file: File, type: String, contentXml: String) {
        ZipOutputStream(FileOutputStream(file)).use { zos ->
            zos.putNextEntry(ZipEntry("content.xml"))
            zos.write(contentXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    }

    private val sampleMarkdownContent = """
        # OmniView Architecture Specification
        
        Welcome to **OmniView**, the ultimate universal document and file viewer for Android.
        
        ---
        
        ## 1. System Design Principles
        
        * **Modularity**: Dedicated viewer components for each format family.
        * **Zero Latency**: Stream processing avoids reading entire files into RAM.
        * **Complete Privacy**: All document parsing is handled 100% locally on device.
        * **Adaptive UX**: Optimized for phones, foldables, and tablets.
        
        ---
        
        ## 2. Technical Stack
        
        ```kotlin
        data class DocumentConfig(
            val title: String,
            val supportedFormats: Int = 17,
            val isLocalOnly: Boolean = true
        )
        ```
        
        > *"A good tool gets out of the way and lets you focus on your content."*
        
        ### Core Features
        
        - High-resolution PDF rendering with page carousel
        - DOCX & ODT structured document layout
        - Multi-sheet Excel and CSV grid view
        - PowerPoint slide deck presentation view
        - JSON / XML syntax highlight trees
        - Sandboxed HTML preview
    """.trimIndent()

    private val sampleCsvContent = """
        Region,Country,Product_Category,Quarter,Units_Sold,Revenue_USD,Profit_Margin,Status
        North America,United States,Enterprise Cloud,Q1,1420,$852000,34.2%,Approved
        North America,Canada,Hardware Devices,Q1,890,$267000,28.5%,Approved
        Europe,Germany,Security Software,Q1,1100,$660000,41.0%,Approved
        Europe,United Kingdom,Enterprise Cloud,Q1,980,$588000,35.8%,Approved
        Asia Pacific,Japan,Developer Tools,Q1,1650,$495000,38.4%,Approved
        Asia Pacific,Australia,Cloud Infrastructure,Q1,720,$432000,31.6%,Pending
        Latin America,Brazil,Hardware Devices,Q1,640,$192000,22.1%,Approved
        North America,United States,Security Software,Q2,1580,$948000,42.5%,Approved
        Europe,France,Enterprise Cloud,Q2,1020,$612000,36.1%,Approved
    """.trimIndent()

    private val sampleJsonContent = """
        {
          "application": {
            "name": "OmniView",
            "version": "1.0.0",
            "environment": "production",
            "releaseDate": "2026-10-14"
          },
          "renderingEngine": {
            "hardwareAcceleration": true,
            "maxCachedPages": 25,
            "defaultZoom": 1.25,
            "safeSandboxMode": true
          },
          "supportedFileFamilies": [
            { "category": "PDF", "extensions": [".pdf"], "fidelity": "100%" },
            { "category": "Office", "extensions": [".docx", ".doc", ".xlsx", ".pptx"], "fidelity": "High" },
            { "category": "OpenDocument", "extensions": [".odt", ".ods", ".odp"], "fidelity": "High" },
            { "category": "Code & Data", "extensions": [".md", ".json", ".xml", ".csv", ".txt"], "fidelity": "Native" }
          ],
          "security": {
            "allowExternalScripts": false,
            "enforceLocalProcessing": true,
            "auditLogging": true
          }
        }
    """.trimIndent()

    private val sampleXmlContent = """
        <?xml version="1.0" encoding="utf-8"?>
        <OmniViewConfiguration build="20261014" target="Android">
          <Cluster name="UniversalViewerEngine">
            <Module id="PDF" engine="PdfRenderer" cachedPages="20" renderScale="1.5" />
            <Module id="Word" parser="DocxParser" enableStyles="true" extractTables="true" />
            <Module id="Excel" parser="XlsxParser" supportMultiSheet="true" maxRows="10000" />
            <Module id="PowerPoint" parser="PptxParser" slideCarousel="true" />
            <Module id="Data" syntaxHighlight="true" wordWrap="true" />
          </Cluster>
          <SecurityPolicy level="StrictLocal">
            <Permission name="INTERNET" status="Disabled" />
            <Permission name="FILE_ACCESS" status="SandboxedScoped" />
          </SecurityPolicy>
        </OmniViewConfiguration>
    """.trimIndent()

    private val sampleHtmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>Executive Briefing</title>
          <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 24px; color: #1e293b; line-height: 1.6; }
            .header { background: linear-gradient(135deg, #1e3a8a, #0284c7); color: white; padding: 24px; border-radius: 12px; margin-bottom: 24px; }
            .card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
            .badge { display: inline-block; background: #0ea5e9; color: white; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: bold; }
            table { width: 100%; border-collapse: collapse; margin-top: 12px; }
            th, td { text-align: left; padding: 10px; border-bottom: 1px solid #cbd5e1; }
            th { background-color: #e2e8f0; color: #0f172a; }
          </style>
        </head>
        <body>
          <div class="header">
            <span class="badge">Enterprise Document</span>
            <h1 style="margin: 8px 0 4px 0;">Executive Performance Summary</h1>
            <p style="margin: 0; opacity: 0.9;">Secure HTML Rendering with sandbox isolation</p>
          </div>
          <div class="card">
            <h3>Operational Health</h3>
            <p>All system components are operating within optimal latency thresholds. Document processing speeds remain sub-20ms.</p>
          </div>
          <table>
            <thead>
              <tr><th>Component</th><th>Status</th><th>Latency</th></tr>
            </thead>
            <tbody>
              <tr><td>PDF Render Pipeline</td><td>Healthy</td><td>12ms</td></tr>
              <tr><td>Office OpenXML Engine</td><td>Healthy</td><td>18ms</td></tr>
              <tr><td>Local SQLite Database</td><td>Healthy</td><td>4ms</td></tr>
            </tbody>
          </table>
        </body>
        </html>
    """.trimIndent()

    private val sampleRtfContent = """
        {\rtf1\ansi\deff0
        {\fonttbl{\f0\fnil\fcharset0 Arial;}}
        \viewkind4\uc1\pard\f0\fs28\b OFFICIAL COMMUNIQUE - OMNIVIEW 2026\b0\par
        \fs22\par
        This document demonstrates high-fidelity Rich Text Format (RTF) decoding.\par
        All format styling codes such as bold, italics, paragraphs, and indentation\par
        are parsed into clean readable text.\par
        \par
        Key Highlights:\par
        \tab * Universal compatibility across legacy and modern formats\par
        \tab * High security with zero external communication\par
        \tab * Native Jetpack Compose responsive UI\par
        }
    """.trimIndent()

    private val sampleOdtXmlContent = """
        <?xml version="1.0" encoding="UTF-8"?>
        <office:document-content xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0">
          <office:body>
            <office:text>
              <text:h text:outline-level="1">OpenDocument Standard Specifications</text:h>
              <text:p>OpenDocument Format (ODF) is an OASIS standard for electronic office documents.</text:p>
              <text:h text:outline-level="2">1. Interoperability</text:h>
              <text:p>OmniView natively extracts content from ODF containers without third-party converters.</text:p>
              <text:h text:outline-level="2">2. Reliability</text:h>
              <text:p>Documents are parsed cleanly and rendered within the modern Jetpack Compose interface.</text:p>
            </office:text>
          </office:body>
        </office:document-content>
    """.trimIndent()

    private val sampleOdsXmlContent = """
        <?xml version="1.0" encoding="UTF-8"?>
        <office:document-content xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0" xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0">
          <office:body>
            <office:spreadsheet>
              <table:table table:name="Warehouse Inventory">
                <table:table-row>
                  <table:table-cell><text:p>SKU</text:p></table:table-cell>
                  <table:table-cell><text:p>Item Name</text:p></table:table-cell>
                  <table:table-cell><text:p>In Stock</text:p></table:table-cell>
                  <table:table-cell><text:p>Unit Price</text:p></table:table-cell>
                </table:table-row>
                <table:table-row>
                  <table:table-cell><text:p>INV-1001</text:p></table:table-cell>
                  <table:table-cell><text:p>Wireless Barcode Scanner</text:p></table:table-cell>
                  <table:table-cell><text:p>245</text:p></table:table-cell>
                  <table:table-cell><text:p>$129.00</text:p></table:table-cell>
                </table:table-row>
                <table:table-row>
                  <table:table-cell><text:p>INV-1002</text:p></table:table-cell>
                  <table:table-cell><text:p>Thermal Label Printer</text:p></table:table-cell>
                  <table:table-cell><text:p>88</text:p></table:table-cell>
                  <table:table-cell><text:p>$285.50</text:p></table:table-cell>
                </table:table-row>
                <table:table-row>
                  <table:table-cell><text:p>INV-1003</text:p></table:table-cell>
                  <table:table-cell><text:p>Warehouse Tablet Mount</text:p></table:table-cell>
                  <table:table-cell><text:p>410</text:p></table:table-cell>
                  <table:table-cell><text:p>$45.00</text:p></table:table-cell>
                </table:table-row>
              </table:table>
            </office:spreadsheet>
          </office:body>
        </office:document-content>
    """.trimIndent()

    private val sampleTxtContent = """
        [2026-10-14 00:00:01] System boot sequence initiated. Kernel v6.1 LTS.
        [2026-10-14 00:00:02] Initializing OmniView storage subsystem.
        [2026-10-14 00:00:02] Loading Room database: omniview_database.db
        [2026-10-14 00:00:03] Storage subsystem ready. Integrity verified: OK.
        [2026-10-14 00:00:04] Mounting file association handlers: PDF, DOCX, XLSX, PPTX, CSV, JSON, XML.
        [2026-10-14 00:00:05] Hardware graphics acceleration: ENABLED.
        [2026-10-14 00:00:06] Local security sandbox operational. Zero external network connections required.
        [2026-10-14 00:00:07] All background workers initialized. Ready for document rendering.
    """.trimIndent()
}
