package in.ashprog.nosono;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BackgroundPlayer {

    private static String CHANNEL_ID = "NOSONO_MUSIC";

    private static MediaPlayer mediaPlayer;
    private static SongData currentSong;
    private static Bitmap ART;
    public static RemoteViews notifView;
    public static NotificationManagerCompat notificationManagerCompat;
    public static NotificationCompat.Builder builder;
    public static Handler mSeekbarUpdateHandler;
    public static Runnable mUpdateSeekbar;

    public static void createChannel(Context ct) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = "Nosono Background Music Player";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = ct.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void createNotification(MediaPlayer mP, SongData cS, Context context) {
        mediaPlayer = mP;
        currentSong = cS;
        ART = currentSong.getART();
        if (ART == null)
            ART = RescaleImage.getRoundedResizedBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon), 60);

        notifView = new RemoteViews(context.getPackageName(), R.layout.notif_layout);
        notifView.setTextViewText(R.id.notif_titleTV, currentSong.getTITLE());
        notifView.setTextViewText(R.id.notif_artistTV, currentSong.getARTIST());
        notifView.setTextViewText(R.id.notif_Song_totalDuration, currentSong.getDURATION());
        notifView.setImageViewBitmap(R.id.notif_artIV, ART);
        // adding action to left button
        Intent pauseplayIntent = new Intent(context, NotificationIntentService.class);
        pauseplayIntent.setAction("notif_pauseplayIV");
        notifView.setOnClickPendingIntent(R.id.notif_pauseplayIV, PendingIntent.getService(context, 0, pauseplayIntent, 0));

        Intent tapIntent = new Intent(context, StartActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setCustomContentView(notifView)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSound(null)
                .setContentIntent(PendingIntent.getActivity(context, 1, tapIntent, 0));

        notificationManagerCompat = NotificationManagerCompat.from(context);

        mSeekbarUpdateHandler = new Handler(Looper.getMainLooper());
        mUpdateSeekbar = new Runnable() {
            @Override
            public void run() {
                notifView.setProgressBar(R.id.notif_songProgress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);
                notifView.setTextViewText(R.id.notif_Song_currentDuration,
                        String.format("%02d:%02d", (int) mediaPlayer.getCurrentPosition() / 1000 / 60 % 60, (int) mediaPlayer.getCurrentPosition() / 1000 % 60));
                notificationManagerCompat.notify(101, builder.build());
                mSeekbarUpdateHandler.postDelayed(this, 1000);
            }
        };
        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
    }

    public static void destroyBgMusicNotif(Context context) {
        if (BackgroundPlayer.mSeekbarUpdateHandler != null)
            BackgroundPlayer.mSeekbarUpdateHandler.removeCallbacks(BackgroundPlayer.mUpdateSeekbar);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(101);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public SongData getCurrentSong() {
        return currentSong;
    }
}
