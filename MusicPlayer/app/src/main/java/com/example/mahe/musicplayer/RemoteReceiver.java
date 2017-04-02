package com.example.mahe.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Created by AMANSAHAY on 01-Apr-17.
 */
public class RemoteReceiver extends BroadcastReceiver
{

    public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                    final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                          if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                             switch (event.getKeyCode()) {
                                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                                                context.startService(new Intent(context, MediaPlayerService.class));
                                                break;
                                    }
                           }
                  }
            }
}
