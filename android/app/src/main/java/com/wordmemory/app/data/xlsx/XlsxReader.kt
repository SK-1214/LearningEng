package com.wordmemory.app.data.xlsx

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class XlsxFormatException(message: String) : Exception(message)

/** One worksheet row. [number] is the 1-based row number shown in Excel. */
data class SheetRow(val number: Int, val cells: List<String>) {
    fun cell(index: Int): String = cells.getOrElse(index) { "" }
}

/**
 * Minimal .xlsx reader: an .xlsx file is a zip of XML parts, so the first worksheet is read with
 * the platform XML parser instead of a heavy spreadsheet library (see wiki/tech/android-stack.md).
 *
 * Supports shared strings (including rich text), inline strings, numbers, booleans, and numbers
 * formatted as dates. A date cell is returned as "month-day", because Excel silently turns a
 * typed "4-1" into April 1st.
 */
object XlsxReader {
    private const val MAIN_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
    private const val DOC_REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    private const val PKG_REL_NS = "http://schemas.openxmlformats.org/package/2006/relationships"

    private val BUILT_IN_DATE_FORMATS = (14..22) + (27..36) + (45..47) + (50..58)
    private val EXCEL_EPOCH: LocalDate = LocalDate.of(1899, 12, 30)

    fun readFirstSheet(input: InputStream): List<SheetRow> {
        val parts = unzip(input)
        val workbook = parts["xl/workbook.xml"]?.let(::parseXml)
            ?: throw XlsxFormatException("엑셀(.xlsx) 파일이 아니에요")
        val sheetPath = firstSheetPath(workbook, parts["xl/_rels/workbook.xml.rels"]?.let(::parseXml))
        val sheet = parts[sheetPath]?.let(::parseXml)
            ?: throw XlsxFormatException("엑셀 파일에서 시트를 찾을 수 없어요")
        val sharedStrings = parts["xl/sharedStrings.xml"]?.let { readSharedStrings(parseXml(it)) } ?: emptyList()
        val dateStyles = parts["xl/styles.xml"]?.let { readDateStyles(parseXml(it)) } ?: emptySet()
        return readRows(sheet, sharedStrings, dateStyles)
    }

    private fun unzip(input: InputStream): Map<String, ByteArray> {
        val parts = mutableMapOf<String, ByteArray>()
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val name = entry.name.removePrefix("/")
                if (!entry.isDirectory && (name.endsWith(".xml") || name.endsWith(".rels"))) {
                    parts[name] = zip.readBytes()
                }
            }
        }
        if (parts.isEmpty()) throw XlsxFormatException("엑셀(.xlsx) 파일이 아니에요")
        return parts
    }

    private fun parseXml(bytes: ByteArray): Document {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isExpandEntityReferences = false
            runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
        }
        return factory.newDocumentBuilder().parse(ByteArrayInputStream(bytes))
    }

    private fun firstSheetPath(workbook: Document, rels: Document?): String {
        val fallback = "xl/worksheets/sheet1.xml"
        val sheet = workbook.elements(MAIN_NS, "sheet").firstOrNull() ?: return fallback
        val relId = sheet.getAttributeNS(DOC_REL_NS, "id").ifEmpty { return fallback }
        val target = rels?.elements(PKG_REL_NS, "Relationship")
            ?.firstOrNull { it.getAttribute("Id") == relId }
            ?.getAttribute("Target")
            ?: return fallback
        return if (target.startsWith("/")) target.removePrefix("/") else "xl/$target"
    }

    private fun readSharedStrings(doc: Document): List<String> =
        doc.documentElement.childElements("si").map { si ->
            // Skip <rPh> phonetic runs; they repeat the text as reading hints.
            si.descendants("t").filterNot { it.hasAncestor("rPh", stopAt = si) }.joinToString("") { it.textContent }
        }

    /** Indexes into cellXfs whose number format displays a date. */
    private fun readDateStyles(doc: Document): Set<Int> {
        val customDateFormats = doc.elements(MAIN_NS, "numFmt")
            .filter { isDateFormatCode(it.getAttribute("formatCode")) }
            .mapNotNull { it.getAttribute("numFmtId").toIntOrNull() }
            .toSet()
        val cellXfs = doc.elements(MAIN_NS, "cellXfs").firstOrNull() ?: return emptySet()
        return cellXfs.childElements("xf").withIndex()
            .filter { (_, xf) ->
                val id = xf.getAttribute("numFmtId").toIntOrNull()
                id != null && (id in BUILT_IN_DATE_FORMATS || id in customDateFormats)
            }
            .map { it.index }
            .toSet()
    }

    private fun isDateFormatCode(code: String): Boolean {
        val stripped = code.replace(Regex("\"[^\"]*\"|\\[[^]]*]"), "")
        return stripped.any { it.lowercaseChar() in "dmy" }
    }

    private fun readRows(sheet: Document, sharedStrings: List<String>, dateStyles: Set<Int>): List<SheetRow> {
        var nextRowNumber = 1
        return sheet.elements(MAIN_NS, "row").map { row ->
            val number = row.getAttribute("r").toIntOrNull() ?: nextRowNumber
            nextRowNumber = number + 1
            val cells = mutableListOf<String>()
            for (c in row.childElements("c")) {
                val column = columnIndex(c.getAttribute("r")) ?: cells.size
                while (cells.size < column) cells += ""
                val value = cellValue(c, sharedStrings, dateStyles)
                if (column < cells.size) cells[column] = value else cells += value
            }
            SheetRow(number, cells)
        }
    }

    private fun cellValue(c: Element, sharedStrings: List<String>, dateStyles: Set<Int>): String {
        val v = c.childElements("v").firstOrNull()?.textContent ?: ""
        return when (c.getAttribute("t")) {
            "s" -> v.toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: ""
            "inlineStr" -> c.descendants("t").joinToString("") { it.textContent }
            "b" -> if (v == "1") "TRUE" else "FALSE"
            "str", "e" -> v
            else -> {
                val number = v.toBigDecimalOrNull() ?: return v
                val style = c.getAttribute("s").toIntOrNull()
                if (style != null && style in dateStyles) excelDateToMonthDay(number) else formatNumber(number)
            }
        }
    }

    private fun excelDateToMonthDay(serial: BigDecimal): String {
        val date = EXCEL_EPOCH.plusDays(serial.toLong())
        return "${date.monthValue}-${date.dayOfMonth}"
    }

    private fun formatNumber(n: BigDecimal): String = n.stripTrailingZeros().toPlainString()

    /** "B12" -> 1 (0-based column index). */
    private fun columnIndex(ref: String): Int? {
        val letters = ref.takeWhile { it.isLetter() }.uppercase()
        if (letters.isEmpty()) return null
        return letters.fold(0) { acc, ch -> acc * 26 + (ch - 'A' + 1) } - 1
    }

    private fun Document.elements(ns: String, localName: String): List<Element> =
        getElementsByTagNameNS(ns, localName).let { list -> List(list.length) { list.item(it) as Element } }

    private fun Element.descendants(localName: String): List<Element> =
        getElementsByTagNameNS(MAIN_NS, localName).let { list -> List(list.length) { list.item(it) as Element } }

    private fun Element.childElements(localName: String): List<Element> {
        val result = mutableListOf<Element>()
        var node = firstChild
        while (node != null) {
            if (node is Element && node.localName == localName) result += node
            node = node.nextSibling
        }
        return result
    }

    private fun Element.hasAncestor(localName: String, stopAt: Element): Boolean {
        var node = parentNode
        while (node != null && node != stopAt) {
            if (node is Element && node.localName == localName) return true
            node = node.parentNode
        }
        return false
    }
}
