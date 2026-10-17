# OmniView - Universal File Viewer

**OmniView** is a modern, high-performance, and privacy-focused Android document and file viewer built entirely with Kotlin and Jetpack Compose adhering to Material Design 3 guidelines.

It allows users to open, preview, read, and manage 17+ file formats seamlessly without uploading any documents to external cloud servers.

---

## 1. Supported Formats & Architecture

| Category | Formats | Processing Engine | Key Capabilities |
| :--- | :--- | :--- | :--- |
| **PDF** | `.pdf` | Native `android.graphics.pdf.PdfRenderer` | Page navigation, multi-level zoom, 90° rotation, full-screen, page carousel |
| **Microsoft Word** | `.docx`, `.doc` | OpenXML Stream Parser (`DocxParser`) | Headings (H1-H3), styled text, tables, bullet lists, search highlight |
| **Microsoft Excel** | `.xlsx`, `.xls` | OpenXML Shared-String Engine (`XlsxParser`) | Multi-sheet tabs, column letter headers, row numbers, cell inspector |
| **Microsoft PowerPoint** | `.pptx`, `.ppt` | OpenXML Slide Parser (`PptxParser`) | 16:9 presentation canvas, slide carousel, presentation mode |
| **OpenDocument** | `.odt`, `.ods`, `.odp` | ODF XML Parser (`OdfParser`) | Native text, spreadsheet, and slide presentation rendering |
| **Code & Data** | `.json`, `.xml` | Native Syntax Highlighting Engine | Syntax-aware coloring, line numbers, formatted indentation, copy |
| **Spreadsheets** | `.csv`, `.tsv` | RFC-4180 CSV Engine (`CsvParser`) | Auto-delimiter detection, quote escaping, interactive grid, filter |
| **Web & Rich Text** | `.html`, `.htm`, `.rtf` | Sandboxed Android `WebView` & `RtfParser` | Script-disabled preview, HTML source code toggle, RTF formatting |
| **Markdown** | `.md` | CommonMark AST Parser (`MarkdownParser`) | Headers, styled code blocks, blockquotes, raw toggle |
| **Plain Text** | `.txt`, `.log`, `.conf` | Monospace Text Engine | Line numbers, word-wrap toggle, search term navigation |
| **Images** | `.png`, `.jpg`, `.jpeg`, `.webp`, `.gif`, `.bmp`, `.svg` | Hardware-Accelerated Bitmap & Coil | Pinch-to-zoom, pan gestures, 90° rotation, EXIF metadata |

---

## 2. Core Architecture

```
User File (SAF / Intent / Sample)
       │
       ▼
FileTypeDetector (Extension + MIME + Magic Byte Inspection)
       │
       ▼
ViewerViewModel (State machine: Idle -> Loading -> Ready / Error)
       │
       ▼
ViewerManager / CommonViewerScaffold
 ├── PdfViewerComponent
 ├── WordViewerComponent
 ├── ExcelViewerComponent
 ├── PowerPointViewerComponent
 ├── ImageViewerComponent
 ├── TextViewerComponent
 ├── CsvViewerComponent
 ├── CodeDataViewerComponent
 ├── HtmlViewerComponent
 └── MarkdownViewerComponent
```

### Local Persistence
- **Room Database (`omniview_database`)**:
  - `RecentFileEntity`: tracks file URI, name, type, size, last opened timestamp, favorite flag.
  - Reactive `Flow` queries update the UI automatically without manual polling.

---

## 3. Security Implementation

- **100% On-Device Processing**: No external network requests are made. All parsing is done locally within memory buffers.
- **Zero Macro/Script Execution**: Microsoft Office macro streams and scripts are never evaluated.
- **Sandboxed HTML Rendering**: `WebView` settings enforce:
  - `javaScriptEnabled = false`
  - `allowFileAccess = false`
  - `allowContentAccess = false`
- **Scoped Storage**: Uses standard Android Storage Access Framework (SAF) without requesting broad storage permissions.

---

## 4. Keyboard Shortcuts

OmniView supports hardware keyboard shortcuts:
- `Ctrl + O`: Open file picker
- `Ctrl + F`: Toggle document search
- `Ctrl + W`: Close current viewer and return to file manager
- `Ctrl + +`: Zoom in
- `Ctrl + -`: Zoom out
- `F11`: Toggle immersive full-screen mode

---

## 5. Build & Installation Instructions

### Requirements
- Android SDK 36 (Minimum SDK 24)
- Java 11+
- Gradle 9.1+ with Kotlin 2.2+

### Running the App
```bash
# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Build debug APK
gradle :app:assembleDebug
```
The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
