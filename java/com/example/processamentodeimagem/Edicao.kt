package com.example.processamentodeimagem


import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.processamentodeimagem.databinding.ActivityEdicaoBinding
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.io.OutputStream
import java.util.Objects

import android.graphics.Color


class Edicao : AppCompatActivity() {

    private lateinit var binding : ActivityEdicaoBinding

    private lateinit var imageView : ImageView
    private lateinit var btnSalvar : Button
    private lateinit var btnCinza : Button
    private lateinit var btnBordas : Button
    private lateinit var btnSep : Button
    private lateinit var btnNeg : Button
    private lateinit var btnBrilho : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edicao)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView = findViewById(R.id.imageView)
        btnSalvar = findViewById(R.id.btnSave)
        btnBrilho = findViewById(R.id.btnBriContr)
        btnSep = findViewById(R.id.btnSep)
        btnNeg = findViewById(R.id.btnNeg)
        btnCinza = findViewById(R.id.btnCinza)
        btnBordas = findViewById(R.id.btnBordas)

        // Recebe o caminho absoluto do arquivo da imagem da Intent
        val imagePath = intent.getStringExtra("Path")
        // Cria um File a partir do caminho do arquivo
        val imageFile = File(imagePath)
        // Carrega a imagem no ImageView
        imageView.setImageURI(Uri.fromFile(imageFile))


        btnSalvar.setOnClickListener {
            salvar()
        }

        btnCinza.setOnClickListener {
            processarImagemEmTonsDeCinza()
        }

        btnSep.setOnClickListener {
           processarImagemParaSepia()
        }

        btnNeg.setOnClickListener {
            processarImagemParaNegativo()
        }

        btnBrilho.setOnClickListener {
            ajustarBrilhoEContraste(1.2f, 1.5f)
        }

        btnBordas.setOnClickListener {
            processarImagemParaDetecaoDeBordas(100)
        }

    }

    fun salvar() {

        // pegando a imagem do imageView
        val bitmap = imageView.drawable.toBitmap()

        // salvando a imagem na galeria
        saveImageToGallery(bitmap)

    }

    private fun saveImageToGallery(bitmap: Bitmap?){

        val fos: OutputStream

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                val resolver = contentResolver
                val contentValues =  ContentValues()

                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image"+".jpg")
                contentValues.put(MediaStore. MediaColumns.MIME_TYPE, "image/jpg")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+ File.separator+"TestFolder")

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri)!!)!!
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                Objects.requireNonNull<OutputStream?>(fos)
                Toast.makeText(this, "Imagem salva na galeria", Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception){
            Toast.makeText(this, "Não foi possível salvar a imagem", Toast.LENGTH_SHORT).show()
        }
    }

    fun processarImagemEmTonsDeCinza() {
        // Obtenha o Bitmap do ImageView
        val bitmapOriginal: Bitmap = (imageView.drawable).toBitmap()

        // Obtenha largura e altura da imagem
        val largura = bitmapOriginal.width
        val altura = bitmapOriginal.height

        // Crie um novo bitmap para armazenar a imagem em tons de cinza
        val bitmapTonsDeCinza = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)

        // Itere sobre cada pixel da imagem
        for (x in 0 until largura) {
            for (y in 0 until altura) {
                // Obtenha o valor de cor do pixel
                val pixel = bitmapOriginal.getPixel(x, y)

                // Extraia os componentes de cor (R, G, B)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Calcule a média dos componentes de cor
                val tomDeCinza = (r + g + b) / 3

                // Crie uma cor em tons de cinza com o mesmo valor para cada componente
                val corTonsDeCinza = Color.rgb(tomDeCinza, tomDeCinza, tomDeCinza)

                // Defina a nova cor do pixel no Bitmap
                bitmapTonsDeCinza.setPixel(x, y, corTonsDeCinza)
            }
        }

        // Defina o bitmap processado no ImageView
        imageView.setImageBitmap(bitmapTonsDeCinza)
    }

    fun processarImagemParaNegativo() {
        // Obtenha o Bitmap do ImageView
        val bitmapOriginal: Bitmap = (imageView.drawable).toBitmap()

        // Obtenha largura e altura da imagem
        val largura = bitmapOriginal.width
        val altura = bitmapOriginal.height

        // Crie um novo bitmap para armazenar a imagem em negativo
        val bitmapNegativo = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)

        // Itere sobre cada pixel da imagem
        for (x in 0 until largura) {
            for (y in 0 until altura) {
                // Obtenha o valor de cor do pixel
                val pixel = bitmapOriginal.getPixel(x, y)

                // Calcule o negativo de cada componente de cor (R, G, B)
                val r = 255 - Color.red(pixel)
                val g = 255 - Color.green(pixel)
                val b = 255 - Color.blue(pixel)

                // Crie uma nova cor usando os valores negativos
                val corNegativa = Color.rgb(r, g, b)

                // Defina a nova cor do pixel no Bitmap
                bitmapNegativo.setPixel(x, y, corNegativa)
            }
        }

        // Atualize o ImageView com o Bitmap processado
        imageView.setImageBitmap(bitmapNegativo)
    }

    fun ajustarBrilhoEContraste(brilho: Float, contraste: Float) {
        // Obtenha o Bitmap do ImageView
        val bitmapOriginal: Bitmap = (imageView.drawable).toBitmap()

        // Obtenha largura e altura da imagem
        val largura = bitmapOriginal.width
        val altura = bitmapOriginal.height

        // Crie um novo bitmap para armazenar a imagem com os ajustes de brilho e contraste
        val bitmapProcessado = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)

        // Itere sobre cada pixel da imagem
        for (x in 0 until largura) {
            for (y in 0 until altura) {
                // Obtenha o valor de cor do pixel
                val pixel = bitmapOriginal.getPixel(x, y)

                // Extraia os componentes de cor (R, G, B)
                var r = Color.red(pixel)
                var g = Color.green(pixel)
                var b = Color.blue(pixel)

                // Ajustar o brilho
                r = (r * brilho).toInt()
                g = (g * brilho).toInt()
                b = (b * brilho).toInt()

                // Ajustar o contraste
                r = ((r - 128) * contraste + 128).toInt()
                g = ((g - 128) * contraste + 128).toInt()
                b = ((b - 128) * contraste + 128).toInt()

                // Certifique-se de que os valores finais estejam dentro do intervalo de 0 a 255
                r = if (r > 255) 255 else if (r < 0) 0 else r
                g = if (g > 255) 255 else if (g < 0) 0 else g
                b = if (b > 255) 255 else if (b < 0) 0 else b

                // Defina a nova cor do pixel no Bitmap processado
                bitmapProcessado.setPixel(x, y, Color.rgb(r, g, b))
            }
        }

        // Atualize o ImageView com o Bitmap processado
        imageView.setImageBitmap(bitmapProcessado)
    }

    fun processarImagemParaDetecaoDeBordas(limiar: Int) {
        // Obtenha o Bitmap do ImageView
        val bitmapOriginal: Bitmap = (imageView.drawable).toBitmap()

        // Obtenha largura e altura da imagem
        val largura = bitmapOriginal.width
        val altura = bitmapOriginal.height

        // Crie uma cópia do bitmap original para armazenar o resultado do processamento
        val bitmapProcessado = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)

        // Itere sobre cada pixel da imagem, excluindo a borda de 1 pixel para evitar indexação fora dos limites
        for (x in 1 until largura - 1) {
            for (y in 1 until altura - 1) {
                // Calcule as intensidades de borda horizontal e vertical usando o operador Sobel
                val gx = (-1 * Color.red(bitmapOriginal.getPixel(x - 1, y - 1))) + (-2 * Color.red(bitmapOriginal.getPixel(x - 1, y))) + (-1 * Color.red(bitmapOriginal.getPixel(x - 1, y + 1))) +
                        (1 * Color.red(bitmapOriginal.getPixel(x + 1, y - 1))) + (2 * Color.red(bitmapOriginal.getPixel(x + 1, y))) + (1 * Color.red(bitmapOriginal.getPixel(x + 1, y + 1)))

                val gy = (-1 * Color.red(bitmapOriginal.getPixel(x - 1, y - 1))) + (-2 * Color.red(bitmapOriginal.getPixel(x, y - 1))) + (-1 * Color.red(bitmapOriginal.getPixel(x + 1, y - 1))) +
                        (1 * Color.red(bitmapOriginal.getPixel(x - 1, y + 1))) + (2 * Color.red(bitmapOriginal.getPixel(x, y + 1))) + (1 * Color.red(bitmapOriginal.getPixel(x + 1, y + 1)))

                // Calcule a intensidade total da borda
                val intensidadeBorda = Math.sqrt((gx * gx + gy * gy).toDouble()).toFloat()

                // Defina o novo valor de cor do pixel baseado na intensidade da borda calculada
                val corPixel = if (intensidadeBorda > limiar) Color.WHITE else Color.BLACK
                bitmapProcessado.setPixel(x, y, corPixel)
            }
        }

        // Atualize o ImageView com o Bitmap processado
        imageView.setImageBitmap(bitmapProcessado)
    }

    fun processarImagemParaSepia() {
        // Obtenha o Bitmap do ImageView
        val originalBitmap: Bitmap = (imageView.drawable).toBitmap()

        // Obtenha largura e altura da imagem
        val largura = originalBitmap.width
        val altura = originalBitmap.height

        // Criar um novo Bitmap com as mesmas dimensões que o original
        val bitmapProcessado = Bitmap.createBitmap(largura, altura, originalBitmap.config)

        // Iterar sobre cada pixel da imagem
        for (x in 0 until largura) {
            for (y in 0 until altura) {
                // Obter o valor de cor do pixel
                val pixel = originalBitmap.getPixel(x, y)

                // Extrair os componentes de cor (R, G, B)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Aplicar transformação para sépia
                val novoR = (0.393 * r + 0.769 * g + 0.189 * b).toInt().coerceIn(0, 255)
                val novoG = (0.349 * r + 0.686 * g + 0.168 * b).toInt().coerceIn(0, 255)
                val novoB = (0.272 * r + 0.534 * g + 0.131 * b).toInt().coerceIn(0, 255)

                // Definir a nova cor do pixel no Bitmap processado
                bitmapProcessado.setPixel(x, y, Color.rgb(novoR, novoG, novoB))
            }
        }

        // Atualizar o ImageView com o Bitmap processado
        imageView.setImageBitmap(bitmapProcessado)
    }




}