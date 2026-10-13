package com.gluedin.ott.presentation.player

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.gluedin.ott.OttApplication
import com.gluedin.ott.databinding.ActivityPlayerBinding
import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.player.PlayerEventHandler
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONTENT_ITEM = "extra_content_item"
    }

    private lateinit var binding: ActivityPlayerBinding

    private val viewModel: PlayerViewModel by viewModels {
        val appContainer = (application as OttApplication).appContainer
        PlayerViewModelFactory(appContainer.logPlayerEventUseCase)
    }

    private var exoPlayer: ExoPlayer? = null
    private var playerEventHandler: PlayerEventHandler? = null
    private var contentItem: ContentItem? = null

    // Playback state restoration for lifecycle transitions
    private var playbackPosition: Long = 0L
    private var playWhenReadyState: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extractIntentData()
        setupViews()
        observeViewModel()
    }

    private fun extractIntentData() {
        @Suppress("DEPRECATION")
        contentItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_CONTENT_ITEM, ContentItem::class.java)
        } else {
            intent.getSerializableExtra(EXTRA_CONTENT_ITEM) as? ContentItem
        }

        contentItem?.let { item ->
            viewModel.setContentTitle(item.title)
            binding.tvPlayerTitle.text = item.title
        }
    }

    private fun setupViews() {
        binding.btnPlayerBack.setOnClickListener {
            finish()
        }

        binding.btnPlayerRetry.setOnClickListener {
            binding.playerErrorOverlay.visibility = View.GONE
            viewModel.clearError()
            initializePlayer()
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

    private fun renderUiState(state: PlayerUiState) {
        // Update live telemetry badge
        binding.tvTelemetryEventBadge.text = state.latestEventDisplay

        // Buffering indicator
        binding.playerBufferingProgress.visibility =
            if (state.isBuffering) View.VISIBLE else View.GONE

        // Error display
        if (state.errorMessage != null) {
            binding.playerErrorOverlay.visibility = View.VISIBLE
            binding.tvPlayerErrorMessage.text = state.errorMessage
        } else {
            binding.playerErrorOverlay.visibility = View.GONE
        }
    }

    private fun initializePlayer() {
        val item = contentItem ?: return
        if (exoPlayer == null) {
            val player = ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(10_000)
                .setSeekForwardIncrementMs(10_000)
                .build()

            // Construct separated PlayerEventHandler following assignment architecture:
            // Player -> Player Event Handler -> ViewModel -> Analytics Repository
            val eventHandler = PlayerEventHandler(
                contentId = item.id,
                contentTitle = item.title,
                onEvent = { event ->
                    viewModel.onPlayerEventReceived(event)
                }
            )

            player.addListener(eventHandler)
            playerEventHandler = eventHandler

            val mediaItem = MediaItem.fromUri(item.videoUrl)
            player.setMediaItem(mediaItem)
            player.playWhenReady = playWhenReadyState
            player.seekTo(playbackPosition)
            player.prepare()

            binding.playerView.player = player
            exoPlayer = player
        }
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            playbackPosition = player.currentPosition
            playWhenReadyState = player.playWhenReady
            playerEventHandler?.let { handler ->
                player.removeListener(handler)
            }
            player.release()
            binding.playerView.player = null
        }
        exoPlayer = null
        playerEventHandler = null
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || exoPlayer == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}
