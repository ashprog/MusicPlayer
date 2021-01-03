package in.ashprog.nosono;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public interface OnSongClickListener {
        void onSongClicked(SongData currentSong);
    }

    private Context context;
    private ArrayList<SongData> songs;
    private OnSongClickListener onSongClickListener;

    public void setOnSongClickListener(OnSongClickListener onSongClickListener) {
        this.onSongClickListener = onSongClickListener;
    }

    public SongAdapter(Context context, ArrayList<SongData> songs) {
        this.context = context;
        this.songs = songs;
    }

    public void updateList(ArrayList<SongData> newList) {
        this.songs = newList;
        notifyDataSetChanged();
    }

    public void filterSongs(final String searchString) {
        final ArrayList<SongData> filteredSongs = new ArrayList<>();
        final boolean[] scanningComplete = {false};

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < songs.size(); i++) {
                    SongData tempSong = songs.get(i);
                    if (tempSong.getTITLE().toLowerCase().contains(searchString) || tempSong.getARTIST().toLowerCase().contains(searchString))
                        filteredSongs.add(tempSong);
                }
                scanningComplete[0] = true;
            }
        });

        final Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (scanningComplete[0]) {
                    songs = filteredSongs;
                    notifyDataSetChanged();
                    mHandler.removeCallbacks(this);
                } else {
                    mHandler.postDelayed(this, 0);
                }
            }
        });
    }

    class SongViewHolder extends RecyclerView.ViewHolder {

        ImageView artIV, downloadIV;
        TextView titleTV, artistTV, durationTV;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            artIV = itemView.findViewById(R.id.artIV);
            downloadIV = itemView.findViewById(R.id.downloadIV);
            titleTV = itemView.findViewById(R.id.titleTV);
            artistTV = itemView.findViewById(R.id.artistTV);
            durationTV = itemView.findViewById(R.id.durationTV);
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.songs_list_view, parent, false);

        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongViewHolder holder, final int position) {

        if (songs.get(position).equalTo(StartActivity.currentSong)) {
            holder.titleTV.setTextColor(Color.parseColor("#31e382"));
            holder.titleTV.setTextSize(16);
        } else {
            holder.titleTV.setTextColor(Color.parseColor("#FFFFFF"));
            holder.titleTV.setTextSize(14);
        }

        if (songs.get(position).isFromNet()) holder.downloadIV.setVisibility(View.VISIBLE);
        else holder.downloadIV.setVisibility(View.INVISIBLE);

        holder.titleTV.setText(songs.get(position).getTITLE());
        holder.artistTV.setText(songs.get(position).getARTIST());
        holder.durationTV.setText(songs.get(position).getDURATION());

        Bitmap art = songs.get(position).getART();
        if (art != null)
            holder.artIV.setImageBitmap(art);
        else
            holder.artIV.setImageResource(R.drawable.icon);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSongClickListener.onSongClicked(songs.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
