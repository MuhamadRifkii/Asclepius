package com.dicoding.asclepius.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    private val uCropContract = object : ActivityResultContract<List<Uri>, Uri>() {
        override fun createIntent(context: Context, input: List<Uri>): Intent {
            val inputUri = input[0]
            val outputUri = input[1]

            val uCrop = UCrop.of(inputUri, outputUri)
                .withAspectRatio(5f, 5f)
                .withMaxResultSize(800, 800)

            return uCrop.getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri {
            return UCrop.getOutput(intent!!)!!
        }
    }

    private val cropImage = registerForActivityResult(uCropContract) { croppedUri ->
        croppedUri?.let {
            currentImageUri = it
            showImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
                showLoading(true)
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }
    }

    override fun onResume() {
        showLoading(false)
        super.onResume()
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            cropImage(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun cropImage(imageUri: Uri) {
        val timestamp = System.currentTimeMillis()
        val filename = "cropped_image_$timestamp.jpg"
        val outputUri = Uri.fromFile(File(filesDir, filename))
        val inputOutputList = listOf(imageUri, outputUri)

        cropImage.launch(inputOutputList)
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(imageUri: Uri) {
        val imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    showToast(error)
                    showLoading(false)
                }

                override fun onResults(results: List<Classifications>?) {
                    showLoading(false)
                    if (!results.isNullOrEmpty()) {
                        val topResult = results[0] // Ambil hasil teratas
                        val resultLabel = topResult.categories[0].label
                        val resultScore = topResult.categories[0].score

                        moveToResult(resultLabel, resultScore)
                    } else {
                        showToast(getString(R.string.image_classifier_failed))
                    }
                }
            }
        )
        imageClassifierHelper.classifyStaticImage(imageUri)
    }

    private fun moveToResult(label: String, score: Float) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
        intent.putExtra(ResultActivity.EXTRA_RESULT, label)
        intent.putExtra(ResultActivity.EXTRA_SCORE, score)
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}