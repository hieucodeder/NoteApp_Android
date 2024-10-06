package com.example.test_gitlad.View

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfGenerator {

    fun createPdf(context: Context, fileName: String, content: String) {
        // Create a new PdfDocument
        val document = PdfDocument()

        // Create a page description
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()

        // Start a page
        val page = document.startPage(pageInfo)

        // Get the canvas of the page
        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 16f

        // Draw text on the canvas
        canvas.drawText(content, 80f, 50f, paint)

        // Finish the page
        document.finishPage(page)

        // Write the document content to a file
        val file = File(context.getExternalFilesDir(null), fileName)
        try {
            document.writeTo(FileOutputStream(file))
            println("PDF created successfully at ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            // Close the document
            document.close()
        }
    }
}
