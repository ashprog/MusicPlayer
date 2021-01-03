package in.ashprog.nosono;

import android.app.Activity;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;

class SongRepository {

    public static ArrayList<SongData> getAllSongs(Activity activity) {
        ArrayList<SongData> songsList = new ArrayList<>();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        Cursor cursor = activity.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        while (cursor.moveToNext()) {
            songsList.add(new SongData(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), false));
        }

        return songsList;
    }
}
