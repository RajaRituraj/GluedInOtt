package com.gluedin.ott.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.gluedin.ott.OttApplication
import com.gluedin.ott.R
import com.gluedin.ott.databinding.ActivityHomeBinding
import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.domain.model.HomeFeed
import com.gluedin.ott.presentation.base.UiState
import com.gluedin.ott.presentation.detail.ContentDetailActivity
import com.gluedin.ott.presentation.home.adapter.ContentRailsAdapter
import com.gluedin.ott.presentation.player.PlayerActivity
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val viewModel: HomeViewModel by viewModels {
        val appContainer = (application as OttApplication).appContainer
        HomeViewModelFactory(appContainer.getHomeFeedUseCase)
    }

    private val railsAdapter by lazy {
        ContentRailsAdapter { clickedItem ->
            navigateToDetail(clickedItem)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvContentRails.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = railsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderUiState(state)
                }
            }
        }
    }

    private fun renderUiState(state: UiState<HomeFeed>) {
        when (state) {
            is UiState.Loading -> {
                binding.loadingLayout.visibility = View.VISIBLE
                binding.contentScrollView.visibility = View.GONE
                binding.errorLayout.visibility = View.GONE
                binding.emptyLayout.visibility = View.GONE
            }
            is UiState.Success -> {
                binding.loadingLayout.visibility = View.GONE
                binding.errorLayout.visibility = View.GONE
                binding.emptyLayout.visibility = View.GONE
                binding.contentScrollView.visibility = View.VISIBLE

                bindHeroBanner(state.data.heroBanner)
                railsAdapter.submitList(state.data.rails)
            }
            is UiState.Error -> {
                binding.loadingLayout.visibility = View.GONE
                binding.contentScrollView.visibility = View.GONE
                binding.emptyLayout.visibility = View.GONE
                binding.errorLayout.visibility = View.VISIBLE
                binding.tvErrorMessage.text = state.message
            }
            is UiState.Empty -> {
                binding.loadingLayout.visibility = View.GONE
                binding.contentScrollView.visibility = View.GONE
                binding.errorLayout.visibility = View.GONE
                binding.emptyLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun bindHeroBanner(hero: ContentItem) {
        binding.tvHeroTitle.text = hero.title
        binding.tvHeroDescription.text = hero.description

        if (!hero.badge.isNullOrBlank()) {
            binding.tvHeroBadge.visibility = View.VISIBLE
            binding.tvHeroBadge.text = hero.badge
        } else {
            binding.tvHeroBadge.visibility = View.GONE
        }

        val metaParts = listOfNotNull(
            hero.releaseYear.takeIf { it.isNotBlank() },
            hero.duration.takeIf { it.isNotBlank() },
            hero.rating.takeIf { it.isNotBlank() }?.let { "★ $it" },
            hero.genre.takeIf { !it.isNullOrBlank() }
        )
        binding.tvHeroMeta.text = metaParts.joinToString(" • ")

        binding.ivHeroBanner.load(hero.bannerUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder_hero)
            error(R.drawable.placeholder_hero)
        }

        binding.btnHeroPlay.setOnClickListener {
            navigateToPlayer(hero)
        }

        binding.btnHeroDetails.setOnClickListener {
            navigateToDetail(hero)
        }
    }

    private fun navigateToDetail(item: ContentItem) {
        val intent = Intent(this, ContentDetailActivity::class.java).apply {
            putExtra(ContentDetailActivity.EXTRA_CONTENT_ITEM, item)
        }
        startActivity(intent)
    }

    private fun navigateToPlayer(item: ContentItem) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_CONTENT_ITEM, item)
        }
        startActivity(intent)
    }
}
