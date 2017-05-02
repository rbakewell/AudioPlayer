package com.example.russ.AudioPlayer;


import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;


// Helper class to store the record for one song
class MediaData
{
    // _id is the primary key, _data is the path name to the music file, and _display_name has a song name

    String _id, _data, _display_name, TITLE, ALBUM, ARTIST;
    int duration;
    MediaData(String id, String data, String display_name, int dur, String title, String album, String artist)
    {
        _id=id;
        _data = data;
        _display_name=display_name;
        duration = dur;
        TITLE = title;
        ALBUM = album;
        ARTIST = artist;

    }
    public String toString()
    {
        return _id+":"+_display_name+ ": "+ duration+" " + ": " + TITLE + ": "+  _data;
    }
}

public class MyAudioService extends Service implements MediaPlayer.OnCompletionListener {

    MediaData[] theMedia = null; // Storage for all of our songs
    String[] projection = null;
    String album="",artist="";
    String cmd ="start";
    private MediaPlayer mediaPlayer = null;
    String tracks;
    int songNum = 0,skTo = 0;
    String songName;
    String Title;
    String Album;
    String Artist;
    String whr = "is_music=1";


    public static final String MY_NEXT_SONG_BROADCAST = "edu.wccnet.russ.MyNextSongBroadcast";

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNextSong(); // Go to the next song
    }

    private void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void playFirstSong() {
        playNextSong();
    }

    private void playNextSong() {
        tracks = String.valueOf(theMedia.length);
        Log.d("MyA", "tracks" + tracks);
        //songNum = (int) (Math.random() * theMedia.length);
        songNum++;
        if(songNum > theMedia.length-1){
            songNum = 0;
        }
        playSong();
        Log.d("MyB", "tracksB" + tracks);
        //songNum++;
    }

    private void playPreviousSong() {
        tracks = String.valueOf(theMedia.length);
        Log.d("MyA", "tracks" + tracks);
        songNum--;
        if(songNum < 0){
            songNum = theMedia.length - 1;
        }
        playSong();
       //songNum--;
    }

    private void pauseSong() {
        mediaPlayer.pause();
    }

    private void resumeSong() {
        mediaPlayer.start();
    }

    private void seekTo(int skTo) {
        mediaPlayer.seekTo(skTo);
    }

    private void playSong() {
        stopSong(); // good to stop the last song if necessary before starting a new song

        songName = theMedia[songNum]._display_name;
        Title = theMedia[songNum].TITLE;
        Album = theMedia[songNum].ALBUM;
        Artist = theMedia[songNum].ARTIST;
        Uri uri = Uri.parse("file://" + theMedia[songNum]._data);
        mediaPlayer = MediaPlayer.create(this, uri);
        Log.d("Mine", "Uri=" + uri);

        if (mediaPlayer != null) {

            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);

            int duration = mediaPlayer.getDuration();
            Toast.makeText(this, "(" + songNum + ")" + Title + " : " + duration, Toast.LENGTH_SHORT).show();

            Intent myIntent = new Intent(MY_NEXT_SONG_BROADCAST);
            myIntent.putExtra("songName", songName);
            myIntent.putExtra("songNum", songNum);
            myIntent.putExtra("duration", duration);
            myIntent.putExtra("TITLE", Title);
            myIntent.putExtra("ALBUM", Album);
            myIntent.putExtra("ARTIST", Artist);
            myIntent.putExtra("Num", tracks);
            Log.d("MyA intent", "tracks" + tracks);
            sendBroadcast(myIntent);
        }
    }

    private void logToast(String s) {
        Log.d("Mine", s);
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public void onCreate() {
        logToast("onCreate of MyAudioService");
        projection = new String[]{"_id", "_data", "_display_name", "duration", "TITLE", "ALBUM", "ARTIST"};
        theMedia = getMedia("is_music=1");
    }

    @Override
    public void onDestroy() {
        stopSong(); // Very important to free up resources
        logToast("MyAudioService onDestroy");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            Log.d("Mine", "Why are we getting a null Intent in onStartCommand????");
            return Service.START_NOT_STICKY;
        }
        cmd = intent.getStringExtra("cmd");
        if(!intent.getStringExtra("artist").equals(artist) || !intent.getStringExtra("album").equals(album)){
            songNum =-1;
        }
        skTo = intent.getIntExtra("seekTo", 0);
        artist = intent.getStringExtra("artist");
        album = intent.getStringExtra("album");
        Log.d("in it now", album +" ? "+ artist);
        if(album != null && !album.isEmpty()){
            whr = "IS_RINGTONE=0 AND is_music=1 AND IS_NOTIFICATION=0 AND ALBUM=\"" +album+"\"";
            Log.d("start new", "album"+album);
            theMedia = getMedia(whr);
        }

        Log.d("Mine Audio", "onStartCommand: " + cmd);

        if ("play".equals(cmd)) {

            playFirstSong();
        } else if ("next".equals(cmd)) {
            playNextSong();
        } else if ("previous".equals(cmd)) {
            playPreviousSong();

        } else if ("pause".equals(cmd)) {
            pauseSong();

        }else if ("resume".equals(cmd)) {
            resumeSong();
        } else if ("stop".equals(cmd)) {
            stopSong();
        } else if ("stopService".equals(cmd)) {
            stopSong(); // You should free up resources.
            // Scary, but see what happens if you comment this out and
            // stop the Service.
            stopSelf();
        } else if ("artist".equals(cmd)) {
            projection = new String[]{"_id", "_data", "ARTIST"};
            theMedia = getMedia(" DISTINCT " + " AND " + "ARTIST");
            Log.d("russ", " audio artist go");

        } else if ("album".equals(cmd)) {
            getMedia("ALBUM");
        } else if ("seekTo".equals(cmd)&&mediaPlayer!=null) {
            seekTo(skTo);

        }

        return Service.START_NOT_STICKY;
    }



    MediaData[] getMedia(String whr) {
        MediaData[] media = null;

        final Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //final Uri mediaUri = MediaStore.Audio.AlbumColumns.EXTERNAL_CONTENT_URI;
        //String[] projection={"_id","_data","_display_name", "duration", "TITLE", "ALBUM", "ARTIST"};
        Cursor cursor = getContentResolver().query(mediaUri, projection, whr, null, null);  //"is_music=1"

        int rows = cursor.getCount();
        media = new MediaData[rows];
        int count = 0;


        while (cursor.moveToNext()) {
            media[count] = new MediaData(cursor.getString(0),
                    cursor.getString(1), // data
                    cursor.getString(2), // _display_name
                    cursor.getInt(3),    // duration
                    cursor.getString(4), // TITLE
                    cursor.getString(5), // ALBUM
                    cursor.getString(6));// ARTIST
            count += 1;
        }


        cursor.close();


        return media;

    }

} // End of Service
