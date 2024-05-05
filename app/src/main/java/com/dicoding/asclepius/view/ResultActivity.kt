package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        val label = intent.getStringExtra(EXTRA_RESULT)
        val score = intent.getFloatExtra(EXTRA_SCORE, 0f)

        val imageUri = Uri.parse(imageUriString)
        binding.resultImage.setImageURI(imageUri)

        binding.resultText.text = getString(R.string.result_with_confidence, label, (score * 100).toInt().toString())

    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_SCORE = "extra_score"
    }

}