package in.ashprog.nosono;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

public class MusicService extends JobIntentService {

    private static MediaPlayer mediaPlayer;
    private static SongData currentSong;
    private Bitmap ART;
    static RemoteViews notifView;
    static NotificationCompat.Builder builder;
    private static WifiManager.WifiLock wifiLock;

    static int JOB_ID = 100;
    static String CHANNEL_ID = "NOSONO_MUSIC";
    static int NOTIFICATION_ID = 101;

    @Override
    public void onCreate() {
        super.onCreate();

        if (currentSong != null) {
            createNotification(this);
            startForeground(NOTIFICATION_ID, builder.build());
        }

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "MyWifiLock");
        wifiLock.acquire();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        try {
            updateProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MusicService.class, JOB_ID, work);
    }

    static void initialize(MediaPlayer mediaPlayer, SongData currentSong) {
        MusicService.mediaPlayer = mediaPlayer;
        MusicService.currentSong = currentSong;
    }

    private void createNotification(Context context) {
        ART = currentSong.getART();
        if (ART == null)
            ART = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);

        notifView = new RemoteViews(context.getPackageName(), R.layout.notif_layout);
        notifView.setTextViewText(R.id.notif_titleTV, currentSong.getTITLE());
        notifView.setTextViewText(R.id.notif_artistTV, currentSong.getARTIST());
        notifView.setTextViewText(R.id.notif_Song_totalDuration, currentSong.getDURATION());
        notifView.setImageViewBitmap(R.id.notif_artIV, ART);
        // adding action to left button
        Intent pauseplayIntent = new Intent(context, NotificationIntentService.class);
        pauseplayIntent.setAction("notif_pauseplay");
        notifView.setOnClickPendingIntent(R.id.notif_pauseplayIV, PendingIntent.getService(context, 0, pauseplayIntent, 0));

        Intent tapIntent = new Intent(context, StartActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setCustomContentView(notifView)
                .setSound(null)
                .setContentIntent(PendingIntent.getActivity(context, 1, tapIntent, 0));
    }

    private void updateProgress() throws Exception {
        while (true) {

            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    notifView.setProgressBar(R.id.notif_songProgress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);
                    notifView.setTextViewText(R.id.notif_Song_currentDuration,
                            String.format("%02d:%02d", (int) mediaPlayer.getCurrentPosition() / 1000 / 60 % 60, (int) mediaPlayer.getCurrentPosition() / 1000 % 60));
                    startForeground(NOTIFICATION_ID, builder.build());
                } else {
                    stopForeground(false);
                }
            } else {
                stopForeground(true);
                break;
            }

            Thread.sleep(1000);
        }
    }

    @Override
    public void onDestroy() {
        releaseMP();
        stopForeground(true);
        wifiLock.release();

        super.onDestroy();
    }

    @Override
    public boolean onStopCurrentWork() {
        releaseMP();
        stopForeground(true);
        wifiLock.release();

        return false;
    }

    static void releaseMP() {
        mediaPlayer = null;
    }

    static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    static SongData getCurrentSong() {
        return currentSong;
    }
}
