package com.omnimargen.omniserv.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.omnimargen.omniserv.domain.model.Client
import com.omnimargen.omniserv.domain.model.Service
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN_LEFT = 40f
        private const val MARGIN_TOP = 60f
        private const val LINE_HEIGHT = 20f
    }

    private val titlePaint = Paint().apply {
        textSize = 18f
        isFakeBoldText = true
        color = android.graphics.Color.BLACK
    }

    private val headerPaint = Paint().apply {
        textSize = 12f
        isFakeBoldText = true
        color = android.graphics.Color.DKGRAY
    }

    private val bodyPaint = Paint().apply {
        textSize = 10f
        color = android.graphics.Color.BLACK
    }

    private val linePaint = Paint().apply {
        color = android.graphics.Color.LTGRAY
        strokeWidth = 1f
    }

    fun generateClientsPdf(clients: List<Client>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var yPos = MARGIN_TOP

        canvas.drawText("Lista de Clientes", MARGIN_LEFT, yPos, titlePaint)
        yPos += LINE_HEIGHT * 1.5f

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generado: $dateStr", MARGIN_LEFT, yPos, bodyPaint)
        yPos += LINE_HEIGHT * 2f

        canvas.drawText("N°", MARGIN_LEFT, yPos, headerPaint)
        canvas.drawText("Nombre", MARGIN_LEFT + 30f, yPos, headerPaint)
        canvas.drawText("Teléfono", MARGIN_LEFT + 250f, yPos, headerPaint)
        canvas.drawText("Dirección", MARGIN_LEFT + 370f, yPos, headerPaint)
        yPos += LINE_HEIGHT
        canvas.drawLine(MARGIN_LEFT, yPos, PAGE_WIDTH - MARGIN_LEFT, yPos, linePaint)
        yPos += LINE_HEIGHT * 0.5f

        clients.forEachIndexed { index, client ->
            if (yPos > PAGE_HEIGHT - MARGIN_LEFT) {
                document.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.pages.size + 1).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                yPos = MARGIN_TOP
            }

            canvas.drawText("${index + 1}", MARGIN_LEFT, yPos, bodyPaint)
            canvas.drawText(client.nombre.take(30), MARGIN_LEFT + 30f, yPos, bodyPaint)
            canvas.drawText(client.telefono, MARGIN_LEFT + 250f, yPos, bodyPaint)
            canvas.drawText(client.direccion.take(25), MARGIN_LEFT + 370f, yPos, bodyPaint)
            yPos += LINE_HEIGHT
        }

        document.finishPage(page)

        val fileName = "Clientes_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(getDocumentsDir(), fileName)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun generateServicesPdf(services: List<Service>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var yPos = MARGIN_TOP

        canvas.drawText("Lista de Servicios", MARGIN_LEFT, yPos, titlePaint)
        yPos += LINE_HEIGHT * 1.5f

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generado: $dateStr", MARGIN_LEFT, yPos, bodyPaint)
        yPos += LINE_HEIGHT * 2f

        canvas.drawText("N°", MARGIN_LEFT, yPos, headerPaint)
        canvas.drawText("Cliente", MARGIN_LEFT + 30f, yPos, headerPaint)
        canvas.drawText("Fecha", MARGIN_LEFT + 200f, yPos, headerPaint)
        canvas.drawText("Tipo", MARGIN_LEFT + 300f, yPos, headerPaint)
        canvas.drawText("Estado", MARGIN_LEFT + 400f, yPos, headerPaint)
        canvas.drawText("Monto", MARGIN_LEFT + 470f, yPos, headerPaint)
        yPos += LINE_HEIGHT
        canvas.drawLine(MARGIN_LEFT, yPos, PAGE_WIDTH - MARGIN_LEFT, yPos, linePaint)
        yPos += LINE_HEIGHT * 0.5f

        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        services.forEachIndexed { index, service ->
            if (yPos > PAGE_HEIGHT - MARGIN_LEFT) {
                document.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.pages.size + 1).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                yPos = MARGIN_TOP
            }

            canvas.drawText("${index + 1}", MARGIN_LEFT, yPos, bodyPaint)
            canvas.drawText(service.clienteNombre.take(20), MARGIN_LEFT + 30f, yPos, bodyPaint)
            canvas.drawText(dateFormat.format(service.fechaServicio), MARGIN_LEFT + 200f, yPos, bodyPaint)
            canvas.drawText(service.tipoServicio.take(15), MARGIN_LEFT + 300f, yPos, bodyPaint)
            canvas.drawText(service.estado.name, MARGIN_LEFT + 400f, yPos, bodyPaint)
            canvas.drawText("$${String.format("%.2f", service.monto)}", MARGIN_LEFT + 470f, yPos, bodyPaint)
            yPos += LINE_HEIGHT

            if (service.notas.isNotBlank()) {
                canvas.drawText("Notas: ${service.notas.take(50)}", MARGIN_LEFT + 30f, yPos, bodyPaint)
                yPos += LINE_HEIGHT
            }
        }

        document.finishPage(page)

        val fileName = "Servicios_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(getDocumentsDir(), fileName)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    private fun getDocumentsDir(): File {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "OmniServ"
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
