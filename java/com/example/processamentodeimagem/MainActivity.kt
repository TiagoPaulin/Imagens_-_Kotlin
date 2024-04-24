package com.example.processamentodeimagem

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.processamentodeimagem.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    // Declaração de variáveis
    private lateinit var binding : ActivityMainBinding
    private var imageCapture : ImageCapture? = null
    private lateinit var outputDirectory : File
    private lateinit var cameraExecutor : ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflar o layout usando View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração do diretório de saída para salvar as imagens capturadas
        outputDirectory = getOutputDirectory()
        // ExecutorService para executar operações da câmera em uma única thread
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Verifica se todas as permissões necessárias estão concedidas
        if(allPermissionsGranted()) {
            // Inicia a câmera se todas as permissões estiverem concedidas
            startCamera()
        } else {
            // Solicita permissões caso contrário
            ActivityCompat.requestPermissions(this,
                Constants.REQUIRED_PERMISSIONS, Constants.REQUEST_CODE_PERMISSIONS)
        }

        // Configura o listener do botão para capturar uma foto
        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }

    }

    // Função para obter o diretório de saída para salvar as imagens capturadas
    private fun getOutputDirectory() : File{
        // Obtém ou cria o diretório de mídia externa
        val mediaDir = externalMediaDirs.firstOrNull()?.let{mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }

        // Retorna o diretório de mídia ou o diretório de arquivos internos se não disponível
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir

    }

    // Função para capturar uma foto
    private fun takePhoto() {
        // Verifica se a captura de imagem está disponível
        val imageCapture = imageCapture?: return
        // Cria um arquivo para salvar a foto capturada
        val photoFIle = File(outputDirectory,
            SimpleDateFormat(Constants.FILE_NAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())+".jpg")

        // Configura as opções de saída para a captura de imagem
        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFIle).build()

        // Captura a imagem
        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object :  ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults){
                    // Exibe uma mensagem quando a foto é salva com sucesso
                    val savedUri = Uri.fromFile(photoFIle)
                    val msg = "Foto salva"
                    Toast.makeText(this@MainActivity, "${msg} ${savedUri}", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@MainActivity, Edicao::class.java)
                    intent.putExtra("Path", photoFIle.absolutePath)
                    startActivity(intent)

                }

                override fun onError(exception: ImageCaptureException) {
                    // Manipula erros durante a captura de imagem
                    Log.e(Constants.TAG, "onError: ${exception.message}", exception)
                }

            }
        )
    }

    // Função para iniciar a câmera
    private fun startCamera() {
        // Obtém uma instância do provedor de câmera
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configura a visualização da câmera
            val preview = Preview.Builder()
                .build()
                .also { mPreview ->
                    mPreview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Configura a captura de imagem
            imageCapture = ImageCapture.Builder()
                .build()

            // Seleciona a câmera padrão (traseira)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Desvincula todas as câmeras previamente ligadas e vincula as novas configurações
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (e : Exception) {
                // Manipula exceções ao abrir a câmera
                Log.d(Constants.TAG, "falha ao abrir a camera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Função para lidar com as solicitações de permissão
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            // Verifica se todas as permissões necessárias foram concedidas após a solicitação
            if (allPermissionsGranted()){
                startCamera()
            } else {
                // Fecha a atividade se as permissões não forem concedidas
                Toast.makeText(this,
                    "Permissões negadas pelo usuário",
                    Toast.LENGTH_SHORT).show()
                finish()
            }

        }

    }

    // Função para verificar se todas as permissões necessárias foram concedidas
    private fun allPermissionsGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }

    // Função para encerrar o ExecutorService quando a atividade é destruída
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}
