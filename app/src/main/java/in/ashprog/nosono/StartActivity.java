package in.ashprog.nosono;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {

    private SlidingUpPanelLayout slidingUpPanelLayout;
    private EditText searchEditText;
    private RadioGroup radioGroup;
    private RecyclerView recyclerView;
    private FrameLayout collapsed_mp_container, expanded_mp_container;
    private CollapsedMPFragment collapsedMPFragment;
    private ExpandedMPFragment expandedMPFragment;
    private LinearLayout emptyView;

    private ArrayList<SongData> songs;
    private SongAdapter songAdapter;
    static SongData currentSong = null;
    private static MediaPlayer mediaPlayer;

    private Intent musicServiceIntent;

    public void initialize() {
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        radioGroup = findViewById(R.id.radioGroup);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerView);
        collapsed_mp_container = findViewById(R.id.collapsed_mp_container);
        expanded_mp_container = findViewById(R.id.expanded_mp_container);
        emptyView = findViewById(R.id.emptyView);

        songs = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
        musicServiceIntent = new Intent(getApplicationContext(), MusicService.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initialize();

        setRecyclerView();

        setSlidingUpPanelLayout();

        setSearchTextChangedListener();

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MediaPlayer backgroundMP = MusicService.getMediaPlayer();
        if (backgroundMP != null) {
            mediaPlayer = backgroundMP;
            currentSong = MusicService.getCurrentSong();
            songAdapter.notifyDataSetChanged();

            if (collapsedMPFragment == null)
                addCollapsedMpFragment(CollapsedMPFragment.instantiate(mediaPlayer, currentSong));
            else collapsedMPFragment.update(mediaPlayer, currentSong);

            if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            if (expandedMPFragment == null)
                addExpandedMpFragment(ExpandedMPFragment.instantiate(mediaPlayer, currentSong));
            else expandedMPFragment.update(mediaPlayer, currentSong);
        }
        MusicService.releaseMP();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer.isPlaying()) {
            MusicService.initialize(mediaPlayer, currentSong);
            MusicService.enqueueWork(getApplicationContext(), musicServiceIntent);
        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else
            super.onBackPressed();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void initializeSongs() {
        final boolean[] scanningComplete = {false};
        final LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), "Loading_Dialog");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                songs.addAll(SongRepository.getAllSongs(StartActivity.this));
                scanningComplete[0] = true;
            }
        });

        final Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scanningComplete[0]) {
                    songAdapter.notifyDataSetChanged();
                    loadingDialog.dismiss();
                    mHandler.removeCallbacks(this);
                } else
                    mHandler.postDelayed(this, 0);
            }
        }, 0);
    }

    void addCollapsedMpFragment(Fragment fragment) {
        collapsedMPFragment = (CollapsedMPFragment) fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.collapsed_mp_container, fragment);
        transaction.commit();
    }

    void addExpandedMpFragment(Fragment fragment) {
        expandedMPFragment = (ExpandedMPFragment) fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.expanded_mp_container, fragment);
        transaction.commit();
    }

    public void search(View view) {
        hideKeyboard(view);
        final String searchString = searchEditText.getText().toString().toLowerCase();

        if (searchString.length() > 0) {
            switch (radioGroup.getCheckedRadioButtonId()) {

                case R.id.localRB:
                    songAdapter.filterSongs(searchString);
                    break;

                case R.id.youtubeRB:
                    Toast.makeText(StartActivity.this, "Youtube selected", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.jioRB:
                    Toast.makeText(StartActivity.this, "Jio selected", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initializeSongs();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        } else {
            initializeSongs();
        }
    }

    void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ActivityCompat.getDrawable(this, R.drawable.recyclerview_divider));
        recyclerView.addItemDecoration(divider);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        songAdapter = new SongAdapter(this, songs);
        songAdapter.setOnSongClickListener(new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClicked(final SongData currentSong) {
                StartActivity.currentSong = currentSong;

                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(currentSong.getDATA());
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                            if (collapsedMPFragment == null)
                                addCollapsedMpFragment(CollapsedMPFragment.instantiate(mediaPlayer, currentSong));
                            else collapsedMPFragment.update(mediaPlayer, currentSong);

                            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                    });
                    mediaPlayer.setLooping(true);
                } catch (Exception e) {
                    Toast.makeText(StartActivity.this, "Some error occurred.", Toast.LENGTH_SHORT).show();
                }

                songAdapter.notifyDataSetChanged();
            }
        });
        songAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                emptyView.setVisibility((songAdapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
            }
        });
        recyclerView.setAdapter(songAdapter);
    }

    void setSlidingUpPanelLayout() {
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                collapsed_mp_container.setAlpha(1 - slideOffset);
                expanded_mp_container.setAlpha(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                    collapsed_mp_container.setVisibility(View.VISIBLE);
                    if (previousState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                        if (expandedMPFragment == null)
                            addExpandedMpFragment(ExpandedMPFragment.instantiate(mediaPlayer, currentSong));
                        else expandedMPFragment.update(mediaPlayer, currentSong);
                    else if (previousState == SlidingUpPanelLayout.PanelState.EXPANDED)
                        if (collapsedMPFragment == null)
                            addCollapsedMpFragment(CollapsedMPFragment.instantiate(mediaPlayer, currentSong));
                        else collapsedMPFragment.update(mediaPlayer, currentSong);
                } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    collapsed_mp_container.setVisibility(View.GONE);
                }
            }
        });
    }

    void setSearchTextChangedListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) songAdapter.updateList(songs);
            }
        });
    }

    void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
}