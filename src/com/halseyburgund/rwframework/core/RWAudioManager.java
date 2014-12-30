package com.halseyburgund.rwframework.core;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * Simple Audio Management
 * <p/>
 * Adapted from npr-android-app
 * https://code.google.com/p/npr-android-app/
 */


public class RWAudioManager {
    private static final String LOG_TAG = RWAudioManager.class.getName();
    private AudioManager audioManager;
    private boolean hasFocus = false;
    private RWOnAudioFocusChangeListener listener;

    public RWAudioManager(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        listener = new RWOnAudioFocusChangeListener(context);
    }


    public boolean getAudioFocus() {
        int rc = audioManager.requestAudioFocus(this.listener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if(rc == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            hasFocus = true;
        }
        return hasFocus;
    }

    public void releaseAudioFocus() {
        audioManager.abandonAudioFocus(listener);
        //presume granted for all we care
        hasFocus = false;
    }

    private class RWOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        private Context context;

        public RWOnAudioFocusChangeListener(Context context) {
            this.context = context;
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.v(LOG_TAG, "Audio focus change.  focusChange = " + focusChange);
            if (hasFocus) {
                if (focusChange > 0) {
                    Log.v(LOG_TAG, "Audio focus gained");
                    //TODO send intent to resume
                } else {
                    Log.v(LOG_TAG, "Audio focus lost");
                    //TODO send intent to pause
                }
            }
        }
    }
}
