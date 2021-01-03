package in.ashprog.nosono;

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

public class CollapsedMPFragment extends Fragment {

    private MediaPlayer mediaPlayer;
    private SongData currentSong;

    private TextView collapsed_mp_titleTV;
    private ImageView collapsed_mp_pauseplayIV, collapsed_mp_artIV;
    private SeekBar collapsed_mp_songProgress;
    private Handler mHandler;
    private Runnable mRunnable;

    public CollapsedMPFragment() {
        // Required empty public constructor
    }

    public static CollapsedMPFragment instantiate(MediaPlayer mediaPlayer, SongData currentSong) {
        CollapsedMPFragment collapsedMPFragment = new CollapsedMPFragment();
        collapsedMPFragment.mediaPlayer = mediaPlayer;
        collapsedMPFragment.currentSong = currentSong;
        return collapsedMPFragment;
    }

    private void initializeComponents(View view) {
        collapsed_mp_titleTV = view.findViewById(R.id.collapsed_mp_titleTV);
        collapsed_mp_pauseplayIV = view.findViewById(R.id.collapsed_mp_pauseplayIV);
        collapsed_mp_artIV = view.findViewById(R.id.collapsed_mp_artIV);
        collapsed_mp_songProgress = view.findViewById(R.id.collapsed_mp_songProgress);
    }

    public void update(final MediaPlayer mediaPlayer, SongData currentSong) {
        this.mediaPlayer = mediaPlayer;
        this.currentSong = currentSong;

        if (mediaPlayer.isPlaying())
            collapsed_mp_pauseplayIV.setImageResource(R.drawable.ic_baseline_pause_24);
        else collapsed_mp_pauseplayIV.setImageResource(R.drawable.ic_baseline_play_arrow_24);

        collapsed_mp_titleTV.setText(currentSong.getTITLE());
        if (currentSong.getART() != null)
            collapsed_mp_artIV.setImageBitmap(currentSong.getART());
        else
            collapsed_mp_artIV.setImageResource(R.drawable.icon);

        collapsed_mp_songProgress.setMax(mediaPlayer.getDuration());

        if(mHandler != null && mRunnable!=null) mHandler.removeCallbacks(mRunnable);
        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = new Runnable() {
            @Override
            public void run() {
                collapsed_mp_songProgress.setProgress(mediaPlayer.getCurrentPosition());
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mRunnable, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collapsed_mp, container, false);

        initializeComponents(view);

        update(mediaPlayer,currentSong);

        collapsed_mp_pauseplayIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    collapsed_mp_pauseplayIV.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    mHandler.removeCallbacks(mRunnable);
                } else {
                    mediaPlayer.start();
                    collapsed_mp_pauseplayIV.setImageResource(R.drawable.ic_baseline_pause_24);
                    mHandler.postDelayed(mRunnable, 0);
                }
            }
        });

        collapsed_mp_songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
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
}