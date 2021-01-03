package in.ashprog.nosono;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

public class NotificationIntentService extends IntentService {

    public NotificationIntentService() {
        super("Nosono Intent Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getAction().equalsIgnoreCase("notif_pauseplay")) {

            int pausePlayDrawableID;

            MediaPlayer mediaPlayer = MusicService.getMediaPlayer();
            if (mediaPlayer == null) return;

            if (mediaPlayer.isPlaying()) {
                pausePlayDrawableID = R.drawable.ic_baseline_play_arrow_24;
                mediaPlayer.pause();
            } else {
                pausePlayDrawableID = R.drawable.ic_baseline_pause_24;
                mediaPlayer.start();
            }

            MusicService.notifView.setImageViewResource(R.id.notif_pauseplayIV, pausePlayDrawableID);
            startForeground(MusicService.NOTIFICATION_ID, MusicService.builder.build());
        }
    }
}
