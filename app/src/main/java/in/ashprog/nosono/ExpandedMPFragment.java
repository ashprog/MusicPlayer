package in.ashprog.nosono;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class ExpandedMPFragment extends Fragment {

    private MediaPlayer mediaPlayer;
    private SongData currentSong;

    private TextView expanded_titleTV, expanded_artistTV, expanded_song_currentDurationTV, expanded_song_totalDuartionTV;
    private ImageView expanded_artIV, expanded_pauseplayIV;
    private SeekBar expanded_song_progress;
    private Handler mHandler;
    private Runnable mRunnable;

    public ExpandedMPFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static ExpandedMPFragment instantiate(MediaPlayer mediaPlayer, SongData currentSong) {
        ExpandedMPFragment expandedMPFragment = new ExpandedMPFragment();
        expandedMPFragment.mediaPlayer = mediaPlayer;
        expandedMPFragment.currentSong = currentSong;
        return expandedMPFragment;
    }

    private void initializeComponents(View view) {
        expanded_titleTV = view.findViewById(R.id.expanded_titleTV);
        expanded_artistTV = view.findViewById(R.id.expanded_artistTV);
        expanded_song_currentDurationTV = view.findViewById(R.id.expanded_song_currentDurationTV);
        expanded_song_totalDuartionTV = view.findViewById(R.id.expanded_song_totalDuartionTV);
        expanded_artIV = view.findViewById(R.id.expanded_artIV);
        expanded_pauseplayIV = view.findViewById(R.id.expanded_pauseplayIV);
        expanded_song_progress = view.findViewById(R.id.expanded_song_progress);
    }

    public void update(final MediaPlayer mediaPlayer, SongData currentSong) {
        this.mediaPlayer = mediaPlayer;
        this.currentSong = currentSong;

        expanded_titleTV.setText(currentSong.getTITLE());
        expanded_artistTV.setText(currentSong.getARTIST());
        expanded_song_totalDuartionTV.setText(currentSong.getDURATION());
        expanded_song_progress.setMax(mediaPlayer.getDuration());

        if (mediaPlayer.isPlaying()) expanded_pauseplayIV.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
        else expanded_pauseplayIV.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);

        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(currentSong.getDATA());
            byte[] data = mmr.getEmbeddedPicture();
            expanded_artIV.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        } catch (Exception e) {
            expanded_artIV.setImageResource(R.drawable.logo);
        }

        if(mHandler != null && mRunnable!=null) mHandler.removeCallbacks(mRunnable);
        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = new Runnable() {
            @Override
            public void run() {
                expanded_song_progress.setProgress(mediaPlayer.getCurrentPosition());
                expanded_song_currentDurationTV.setText(String.format("%02d:%02d", (int) mediaPlayer.getCurrentPosition() / 1000 / 60 % 60, mediaPlayer.getCurrentPosition() / 1000 % 60));
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mRunnable, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expanded_mp, container, false);

        initializeComponents(view);

        update(mediaPlayer,currentSong);

        expanded_pauseplayIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMPStatus(mHandler,mRunnable);
            }
        });

        expanded_song_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    expanded_song_currentDurationTV.setText(String.format("%02d:%02d", progress / 1000 / 60 % 60, progress / 1000 % 60));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    void checkMPStatus(Handler mHandler,Runnable mRunnable) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            expanded_pauseplayIV.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
            mHandler.removeCallbacks(mRunnable);
        } else {
            mediaPlayer.start();
            expanded_pauseplayIV.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            mHandler.postDelayed(mRunnable, 0);
        }
    }
}