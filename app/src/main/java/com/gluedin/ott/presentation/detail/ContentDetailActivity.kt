package com.gluedin.ott.presentation.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.gluedin.ott.OttApplication
import com.gluedin.ott.R
import com.gluedin.ott.databinding.ActivityContentDetailBinding
import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.presentation.base.UiState
import com.gluedin.ott.presentation.player.PlayerActivity
import kotlinx.coroutines.launch

class ContentDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONTENT_ITEM = "extra_content_item"
        const val EXTRA_CONTENT_ID = "extra_content_id"
    }

    private lateinit var binding: ActivityContentDetailBinding

    private val viewModel: ContentDetailViewModel by viewModels {
        val appContainer = (application as OttApplication).appContainer
        ContentDetailViewModelFactory(appContainer.getContentDetailsUseCase)
    }

    private var currentItem: ContentItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
        extractIntentData()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnPlayVideo.setOnClickListener {
            currentItem?.let { item ->
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_CONTENT_ITEM, item)
                }
                startActivity(intent)
            }
        }
    }

    private fun extractIntentData() {
        @Suppress("DEPRECATION")
        val item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_CONTENT_ITEM, ContentItem::class.java)
        } else {
            intent.getSerializableExtra(EXTRA_CONTENT_ITEM) as? ContentItem
        }

        if (item != null) {
            currentItem = item
            viewModel.setInitialContent(item)
        } else {
            val contentId = intent.getStringExtra(EXTRA_CONTENT_ID).orEmpty()
            if (contentId.isNotBlank()) {
                viewModel.loadContent(contentId)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is UiState.Success) {
                        bindContent(state.data)
                    }
                }
            }
        }
    }

    private fun bindContent(item: ContentItem) {
        currentItem = item

        binding.tvDetailTitle.text = item.title
        binding.tvDetailDescription.text = item.description

        if (!item.badge.isNullOrBlank()) {
            binding.tvDetailBadge.visibility = View.VISIBLE
            binding.tvDetailBadge.text = item.badge
        } else {
            binding.tvDetailBadge.visibility = View.GONE
        }

        val metaParts = listOfNotNull(
            item.releaseYear.takeIf { it.isNotBlank() },
            item.duration.takeIf { it.isNotBlank() },
            item.rating.takeIf { it.isNotBlank() }?.let { "★ $it" },
            item.genre.takeIf { !it.isNullOrBlank() }
        )
        binding.tvDetailMeta.text = metaParts.joinToString(" • ")

        if (!item.cast.isNullOrBlank()) {
            binding.tvDetailCast.visibility = View.VISIBLE
            binding.tvDetailCast.text = "Cast: ${item.cast}"
        } else {
            binding.tvDetailCast.visibility = View.GONE
        }

        if (!item.director.isNullOrBlank()) {
            binding.tvDetailDirector.visibility = View.VISIBLE
            binding.tvDetailDirector.text = "Director: ${item.director}"
        } else {
            binding.tvDetailDirector.visibility = View.GONE
        }

        val banner = item.bannerUrl.ifBlank { item.thumbnailUrl }
        binding.ivDetailBanner.load(banner) {
            crossfade(true)
            placeholder(R.drawable.placeholder_hero)
            error(R.drawable.placeholder_hero)
        }
    }
}
