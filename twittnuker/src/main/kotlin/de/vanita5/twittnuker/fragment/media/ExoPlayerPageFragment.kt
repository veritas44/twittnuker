/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.fragment.media

import android.accounts.AccountManager
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.HttpDataSource
import kotlinx.android.synthetic.main.layout_media_viewer_exo_player_view.*
import kotlinx.android.synthetic.main.layout_media_viewer_video_overlay.*
import org.mariotaku.mediaviewer.library.MediaViewerFragment
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment
import org.mariotaku.restfu.RestRequest
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.MultiValueMap
import org.mariotaku.restfu.oauth.OAuthAuthorization
import org.mariotaku.restfu.oauth.OAuthEndpoint
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_POSITION
import de.vanita5.twittnuker.extension.model.getAuthorization
import de.vanita5.twittnuker.fragment.iface.IBaseFragment
import de.vanita5.twittnuker.fragment.media.VideoPageFragment.Companion.EXTRA_PAUSED_BY_USER
import de.vanita5.twittnuker.fragment.media.VideoPageFragment.Companion.EXTRA_PLAY_AUDIO
import de.vanita5.twittnuker.fragment.media.VideoPageFragment.Companion.SUPPORTED_VIDEO_TYPES
import de.vanita5.twittnuker.fragment.media.VideoPageFragment.Companion.accountKey
import de.vanita5.twittnuker.fragment.media.VideoPageFragment.Companion.isControlDisabled
import de.vanita5.twittnuker.fragment.media.VideoPageFragment.Companion.isLoopEnabled
import de.vanita5.twittnuker.fragment.media.VideoPageFragment.Companion.isMutedByDefault
import de.vanita5.twittnuker.fragment.media.VideoPageFragment.Companion.media
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import de.vanita5.twittnuker.util.media.TwidereMediaDownloader
import javax.inject.Inject


/**
 * Successor of `VideoPageFragment`, backed by `ExoPlayer`
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class ExoPlayerPageFragment : MediaViewerFragment(), IBaseFragment<ExoPlayerPageFragment> {

    @Inject
    internal lateinit var dataSourceFactory: DataSource.Factory

    @Inject
    internal lateinit var extractorsFactory: ExtractorsFactory

    private lateinit var mainHandler: Handler

    private var playAudio: Boolean = false
    private var pausedByUser: Boolean = false
    private var playbackCompleted: Boolean = false
    private var positionBackup: Long = -1L

    private val playerListener = object : ExoPlayer.EventListener {
        override fun onLoadingChanged(isLoading: Boolean) {

        }

        override fun onPlayerError(error: ExoPlaybackException) {

        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {
                    showProgress(true, 0f)
                }
                ExoPlayer.STATE_ENDED -> {
                    playbackCompleted = true
                    positionBackup = -1L

                    // Reset position
                    playerView.player?.let { player ->
                        player.seekTo(0)
                        player.playWhenReady = false
                    }

                    hideProgress()
                }
                ExoPlayer.STATE_READY -> {
                    playbackCompleted = playWhenReady
                    hideProgress()
                }
                ExoPlayer.STATE_IDLE -> {
                    hideProgress()
                }
            }
        }

        override fun onPositionDiscontinuity() {
        }

        override fun onTimelineChanged(timeline: Timeline, manifest: Any?) {
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainHandler = Handler()


        if (savedInstanceState != null) {
            positionBackup = savedInstanceState.getLong(EXTRA_POSITION)
            pausedByUser = savedInstanceState.getBoolean(EXTRA_PAUSED_BY_USER)
            playAudio = savedInstanceState.getBoolean(EXTRA_PLAY_AUDIO)
        } else {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Play audio by default if ringer mode on
            playAudio = !isMutedByDefault && am.ringerMode == AudioManager.RINGER_MODE_NORMAL
        }

        volumeButton.setOnClickListener {
            this.playAudio = !this.playAudio
            updateVolume()
        }
        playerView.useController = !isControlDisabled
        updateVolume()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            releasePlayer()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (activity != null && !isDetached) {
            if (isVisibleToUser) {
                initializePlayer()
            } else {
                releasePlayer()
            }
        }
    }

    override fun onCreateMediaView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_media_viewer_exo_player_view, parent, false)
    }

    override fun fitSystemWindows(insets: Rect) {
        val lp = videoControl.layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            lp.bottomMargin = insets.bottom
            lp.leftMargin = insets.left
            lp.rightMargin = insets.right
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(EXTRA_POSITION, positionBackup)
        outState.putBoolean(EXTRA_PAUSED_BY_USER, pausedByUser)
        outState.putBoolean(EXTRA_PLAY_AUDIO, playAudio)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        requestFitSystemWindows()
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (ExoPlayerPageFragment) -> Unit) {
        // No-op
    }

    override fun isMediaLoaded(): Boolean {
        val player = playerView.player ?: return false
        return player.playbackState != ExoPlayer.STATE_IDLE
    }

    override fun isMediaLoading(): Boolean {
        val player = playerView.player ?: return false
        return player.isLoading
    }

    private fun releasePlayer() {
        val player = playerView.player ?: return
        positionBackup = player.currentPosition
        pausedByUser = !player.playWhenReady
        player.removeListener(playerListener)
        player.release()
        playerView.player = null
    }

    private fun initializePlayer() {
        if (playerView.player != null) return
        playerView.player = run {
            val bandwidthMeter = DefaultBandwidthMeter()
            val videoTrackSelectionFactory = AdaptiveVideoTrackSelection.Factory(bandwidthMeter)
            val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            val player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, DefaultLoadControl())
            if (positionBackup >= 0) {
                player.seekTo(positionBackup)
            }
            player.playWhenReady = !pausedByUser
            player.addListener(playerListener)
            return@run player
        }

        val uri = media?.getDownloadUri() ?: return
        val am = AccountManager.get(context)
        val factory = AuthDelegatingDataSourceFactory(uri, accountKey, am, dataSourceFactory)
        val uriSource = ExtractorMediaSource(uri, factory, extractorsFactory, null, null)
        if (isLoopEnabled) {
            playerView.player.prepare(LoopingMediaSource(uriSource))
        } else {
            playerView.player.prepare(uriSource)
        }
        updateVolume()
    }

    private fun updateVolume() {
        volumeButton.setImageResource(if (playAudio) R.drawable.ic_action_speaker_max else R.drawable.ic_action_speaker_muted)
        val player = playerView.player ?: return
        if (playAudio) {
            player.volume = 1f
        } else {
            player.volume = 0f
        }
    }

    fun ParcelableMedia.getDownloadUri(): Uri? {
        val bestVideoUrlAndType = VideoPageFragment.getBestVideoUrlAndType(this, SUPPORTED_VIDEO_TYPES)
        if (bestVideoUrlAndType != null && bestVideoUrlAndType.first != null) {
            return Uri.parse(bestVideoUrlAndType.first)
        }
        return arguments.getParcelable<Uri>(SubsampleImageViewerFragment.EXTRA_MEDIA_URI)
    }

    class AuthDelegatingDataSourceFactory(
            val uri: Uri,
            val accountKey: UserKey,
            val am: AccountManager,
            val delegate: DataSource.Factory
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource {
            val source = delegate.createDataSource()
            if (source is HttpDataSource) {
                setAuthorizationHeader(source)
            }
            return source
        }

        private fun setAuthorizationHeader(dataSource: HttpDataSource) {
            val account = AccountUtils.getAccountDetails(am, accountKey, true) ?: return
            val modifiedUri = TwidereMediaDownloader.getReplacedUri(uri, account.credentials.api_url_format) ?: uri
            if (TwidereMediaDownloader.isAuthRequired(account, uri)) {
                val auth = account.credentials.getAuthorization()
                val endpoint: Endpoint
                if (auth is OAuthAuthorization) {
                    endpoint = OAuthEndpoint(TwidereMediaDownloader.getEndpoint(modifiedUri),
                            TwidereMediaDownloader.getEndpoint(uri))
                } else {
                    endpoint = Endpoint(TwidereMediaDownloader.getEndpoint(modifiedUri))
                }
                val queries = MultiValueMap<String>()
                for (name in uri.queryParameterNames) {
                    for (value in uri.getQueryParameters(name)) {
                        queries.add(name, value)
                    }
                }
                val info = RestRequest("GET", false, uri.path, null, queries, null, null, null, null)
                dataSource.setRequestProperty("Authorization", auth.getHeader(endpoint, info))
            }

        }
    }

}