package in.ashprog.nosono;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import java.util.ArrayList;

class SongData {
    private String ID;
    private String ARTIST;
    private String TITLE;
    private String DATA;
    private String DISPLAY_NAME;
    private long duration;
    private int HOURS, MINUTES, SECONDS;
    private Bitmap ART = null;
    private boolean fromNet;

    public SongData(String ID, String ARTIST, String TITLE, String DATA, String DISPLAY_NAME, String DURATION,boolean fromNEt) {
        this.ID = ID;
        this.ARTIST = ARTIST;
        this.TITLE = TITLE;
        this.DATA = DATA;
        this.DISPLAY_NAME = DISPLAY_NAME;
        this.duration = Long.parseLong(DURATION);
        this.SECONDS = (int) this.duration / 1000 % 60;
        this.MINUTES = (int) this.duration / 1000 / 60 % 60;
        this.HOURS = (int) this.duration / 1000 / 60 / 60;
        this.fromNet = fromNEt;

        byte[] data = null;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(DATA);
            data = mmr.getEmbeddedPicture();
        } catch (Exception e) {
        }
        if (data != null)
            this.ART = RescaleImage.getRoundedResizedBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 60);
    }

    public String getID() {
        return ID;
    }

    public String getARTIST() {
        return ARTIST;
    }

    public String getTITLE() {
        return TITLE;
    }

    public String getDATA() {
        return DATA;
    }

    public String getDISPLAY_NAME() {
        return DISPLAY_NAME;
    }

    public Bitmap getART() {
        return ART;
    }

    public String getDURATION() {
        if (HOURS != 0) return String.format("%02d:%02d:%02d", HOURS, MINUTES, SECONDS);
        else return String.format("%02d:%02d", MINUTES, SECONDS);
    }

    public boolean equalTo(SongData secondSong) {

        if(secondSong == null) return false;

        if(this.DATA.equals(secondSong.DATA)) return true;
        else return false;
    }

    public boolean isFromNet() {
        return fromNet;
    }
}
