/*
 * This file is a part of the Yandex Advertising Network
 *
 * Version for Android (C) 2021 YANDEX
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at https://legal.yandex.com/partner_ch/
 */
package com.yandex.instream.sample.player.ad;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.yandex.instream.sample.player.SamplePlayer;
import com.yandex.mobile.ads.instream.model.MediaFile;
import com.yandex.mobile.ads.instream.player.ad.InstreamAdPlayer;
import com.yandex.mobile.ads.instream.player.ad.InstreamAdPlayerListener;
import com.yandex.mobile.ads.video.playback.model.VideoAd;

public class SampleInstreamAdPlayer implements InstreamAdPlayer, SamplePlayer {

    @NonNull
    private final PlayerView mExoPlayerView;

    @NonNull
    private final SimpleExoPlayer mAdPlayer;

    @Nullable
    private InstreamAdPlayerListener mAdPlayerListener;

    private VideoAd mVideoAd;

    public SampleInstreamAdPlayer(@NonNull final PlayerView exoplayerView) {
        mExoPlayerView = exoplayerView;

        mAdPlayer = new SimpleExoPlayer.Builder(exoplayerView.getContext()).build();
        mAdPlayer.addListener(new AdPlayerEventListener());
    }

    @Override
    public boolean isPlaying() {
        return mAdPlayer.isPlaying();
    }

    @Override
    public void onPause() {
        pause();
    }

    @Override
    public void onResume() {
        resume();
    }

    public void onDestroy() {
        mAdPlayer.release();
    }

    @Override
    public void prepareAd(@NonNull final VideoAd videoAd) {
        mVideoAd = videoAd;

        final MediaFile mediaFile = videoAd.getMediaFile();
        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mExoPlayerView.getContext(), "ad player");
        final Uri adMediaUri = Uri.parse(mediaFile.getUrl());
        final MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(adMediaUri);

        mAdPlayer.setPlayWhenReady(false);
        mAdPlayer.prepare(videoSource, true, true);
    }

    @Override
    public void playAd() {
        mExoPlayerView.setPlayer(mAdPlayer);

        mExoPlayerView.setUseController(false);
        mAdPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pauseAd() {
        pause();
    }

    @Override
    public void resumeAd() {
        resume();
    }

    @Override
    public void stopAd() {
        mAdPlayer.setPlayWhenReady(false);

        if (mAdPlayerListener != null) {
            mAdPlayerListener.onAdStopped(mVideoAd);
        }
    }

    @Override
    public void setVolume(final float v) {
        mAdPlayer.setVolume(v);
    }

    @Override
    public void release() {
        mAdPlayer.release();
    }

    @Override
    public void setInstreamAdPlayerListener(@Nullable final InstreamAdPlayerListener instreamAdPlayerListener) {
        mAdPlayerListener = instreamAdPlayerListener;
    }

    @Override
    public long getAdDuration() {
        return mAdPlayer.getDuration();
    }

    @Override
    public long getAdPosition() {
        return mAdPlayer.getCurrentPosition();
    }

    @Override
    public boolean isPlayingAd() {
        return mAdPlayer.isPlaying();
    }

    private void pause() {
        if (mAdPlayer.isPlaying()) {
            mAdPlayer.setPlayWhenReady(false);
        }
    }

    private void resume() {
        if (mAdPlayer.isPlaying() == false) {
            mAdPlayer.setPlayWhenReady(true);
        }
    }

    private class AdPlayerEventListener implements Player.EventListener {

        private boolean mAdStarted;

        @Override
        public void onIsPlayingChanged(final boolean isPlaying) {
            if (isPlaying) {
                onResumePlayback();
            } else {
                onPausePlayback();
            }
        }

        private void onResumePlayback() {
            if (mAdPlayerListener != null) {
                if (mAdStarted) {
                    mAdPlayerListener.onAdResumed(mVideoAd);
                } else {
                    mAdPlayerListener.onAdStarted(mVideoAd);
                }
            }

            mAdStarted = true;
        }

        private void onPausePlayback() {
            if (mAdPlayerListener != null) {
                mAdPlayerListener.onAdPaused(mVideoAd);
            }
        }

        @Override
        public void onPlaybackStateChanged(final int playbackState) {
            if (playbackState == Player.STATE_READY) {
                onReadyState();
            } else if (playbackState == Player.STATE_ENDED) {
                onEndedState();
            }
        }

        private void onReadyState() {
            if (mAdPlayerListener != null) {
                mAdPlayerListener.onAdPrepared(mVideoAd);
            }
        }

        private void onEndedState() {
            mAdStarted = false;
            if (mAdPlayerListener != null) {
                mAdPlayerListener.onAdCompleted(mVideoAd);
            }
        }

        @Override
        public void onPlayerError(final ExoPlaybackException error) {
            mAdStarted = false;
            if (mAdPlayerListener != null) {
                mAdPlayerListener.onError(mVideoAd);
            }
        }
    }
}
