package in.ashprog.nosono;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BackgroundPlayer.createChannel(getApplicationContext());

        if (MusicService.getMediaPlayer() != null && MusicService.getMediaPlayer().isPlaying())
            goToStart();
        else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToStart();
                }
            }, 1000);
        }
    }

    public void goToStart() {
        startActivity(new Intent(MainActivity.this, StartActivity.class));
        finish();
    }
}