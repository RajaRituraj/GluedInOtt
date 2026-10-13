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
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.gluedin.ott.OttApplication
import com.gluedin.ott.databinding.ActivityPlayerBinding
import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.player.PlayerEventHandler
import kotlinx.coroutines.launch
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.util.Rational
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

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
            getPlayerTitleView()?.text = item.title
        }
    }

    private fun getPlayerTitleView(): android.widget.TextView? =
        binding.playerView.findViewById(com.gluedin.ott.R.id.tvPlayerTitle)

    private fun getTelemetryBadgeView(): android.widget.TextView? =
        binding.playerView.findViewById(com.gluedin.ott.R.id.tvTelemetryEventBadge)

    private fun getPlayerBackButton(): View? =
        binding.playerView.findViewById(com.gluedin.ott.R.id.btnPlayerBack)

    private fun setupViews() {
        getPlayerBackButton()?.setOnClickListener {
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
        getTelemetryBadgeView()?.text = state.latestEventDisplay

        // Content Title
        if (state.contentTitle.isNotBlank()) {
            getPlayerTitleView()?.text = state.contentTitle
        }

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

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val item = contentItem ?: return
        if (exoPlayer == null) {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 GluedInOtt/1.0")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(15_000)

            val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

            val player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .setSeekBackIncrementMs(10_000)
                .setSeekForwardIncrementMs(10_000)
                .build()

            // Setup player event callbacks
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

            setupCustomControls(player)
        }
    }

    private fun setupCustomControls(player: ExoPlayer) {
        val playBtn = binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_play)
        val pauseBtn = binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_pause)
        val rewBtn = binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_rew)
        val ffwdBtn = binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_ffwd)

        getPlayerBackButton()?.setOnClickListener {
            finish()
        }
        contentItem?.let {
            getPlayerTitleView()?.text = it.title
        }

        updatePlayPauseButtons(player.isPlaying)

        playBtn?.setOnClickListener {
            player.play()
            updatePlayPauseButtons(true)
        }

        pauseBtn?.setOnClickListener {
            player.pause()
            updatePlayPauseButtons(false)
        }

        rewBtn?.setOnClickListener {
            player.seekBack()
        }

        ffwdBtn?.setOnClickListener {
            player.seekForward()
        }

        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayPauseButtons(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    updatePlayPauseButtons(false)
                }
            }
        })
    }

    private fun updatePlayPauseButtons(isPlaying: Boolean) {
        val playBtn = binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_play)
        val pauseBtn = binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_pause)
        playBtn?.visibility = if (isPlaying) View.GONE else View.VISIBLE
        pauseBtn?.visibility = if (isPlaying) View.VISIBLE else View.GONE
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

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        binding.playerView.useController = !isInPictureInPictureMode
    }
}
