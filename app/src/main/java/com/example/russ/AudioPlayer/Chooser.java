package com.example.russ.AudioPlayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by russ on 12/9/2016.
 */

public class Chooser extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    final Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    final Uri geneUri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;

    ListView showit;
    TextView artist_album=null;
    ArrayList music = new ArrayList<>();
    ArrayAdapter<String> arrayListAdapter;
    String[] projection = null;
    String whr ="",album="",artist="";
    String lclick ="";
    long album_ID;
    //Uri albumArtUri;
    String thisArt;
    boolean art = false;
    int[] button_resources={com.example.russ.AudioPlayer.R.id.goback, com.example.russ.AudioPlayer.R.id.bArtist, com.example.russ.AudioPlayer.R.id.bAlbum, com.example.russ.AudioPlayer.R.id.mtype};
    // MainActivity.MyReceiver myReceiver=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.russ.AudioPlayer.R.layout.artist_album);  // Set up click listeners for all the buttons

        for (int i=0; i < button_resources.length; i++)
        {
            Button b = (Button)findViewById(button_resources[i]);
            b.setOnClickListener(this);
        }
        artist_album = (TextView) findViewById(com.example.russ.AudioPlayer.R.id.artist_album);

        Intent receivedIntent = getIntent();
        String start = receivedIntent.getStringExtra("start");
        showit = (ListView) findViewById(com.example.russ.AudioPlayer.R.id.showit);

        arrayListAdapter=
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, music);

        showit.setAdapter(arrayListAdapter);
        //artist_album.getText();
    }

    @Override
    public void onClick(View v) {
        int index = v.getId();
        //Button pButton = (Button) findViewById(R.id.pause);
        //String cmd = null;
        artist_album.setText("");
        switch (index) {

            case com.example.russ.AudioPlayer.R.id.bArtist:
                //projection = new String[]{"_id", "_data", "_display_name", "duration", "TITLE", "ALBUM", "ARTIST"};
                projection = new String[]{"DISTINCT ARTIST"};

                whr = "IS_RINGTONE=0 AND is_music=1 AND IS_NOTIFICATION=0";
                selector(mediaUri);
                lclick = "artist";
                Log.d("russ", "artist go");
                album="";
                artist="";

                break;

            case com.example.russ.AudioPlayer.R.id.bAlbum:
                projection = new String[]{"DISTINCT ALBUM"};
                whr = "IS_RINGTONE=0 AND is_music=1 AND IS_NOTIFICATION=0";
                selector(mediaUri);
                lclick = "album";
                Log.d("russ", "album go");
                album="";
                artist="";

                break;
            case com.example.russ.AudioPlayer.R.id.mtype:
                projection = new String[]{"*"};
                whr = ""; //"IS_RINGTONE=0 AND is_music=1 AND IS_NOTIFICATION=0";
                selector(mediaUri);
                break;

            case com.example.russ.AudioPlayer.R.id.goback:
                art = true;
                //whr = "ALBUM=" +album+"\"";
                getArt();
                Log.d("Mine", "Chooser intent start=" + album + " : "+ artist+" : "+thisArt+" : end");
                Intent outData = new Intent();
                outData.putExtra("arist", artist);
                outData.putExtra("album", album);
                outData.putExtra("album_art", thisArt);

                setResult(Activity.RESULT_OK, outData);
                // The default result is Activity.RESULT_CANCELED ... SetResult to this value if you want
                // tell the caller to forget it.
                Log.d("M go", "M "+ music);
                Log.d("Mine", "Chooser intent end=" + album + " : "+ artist);
                finish();

                break;
        }



    }
    public void getArt(){
        //whr = "ALBUM=" +album+"\"";
        whr = "ALBUM=\"" +album+"\"";


          //  album_ID = art_cur.getLong(art_cur.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            art = false;
       // Uri sArtworkUri = Uri
               // .parse("content://media/external/audio/albumart");
      // */
        //albumArtUri = ContentUris.withAppendedId(sArtworkUri, album_ID);
//String au = (String)albumArtUri;
        ContentResolver musicResolve = getContentResolver();
        Uri smusicUri = android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor music =musicResolve.query(smusicUri,null         //should use where clause(_ID==albumid)
                ,whr, null, null);



        music.moveToFirst();            //i put only one song in my external storage to keep things simple
        int x=music.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM_ART);
        Log.d("thisArt", "int " + x+" what");
        thisArt = music.getString(x);
Log.d("thisAt","string "+music.getString(x)+" what");



    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView text = (TextView) view;
        String str =text.getText() + " pos="+i + " id=" +l;
        Log.d("Mine", "onItemClick: "+str);
        switch(lclick){
            case "artist":
                String[] words = str.split(" :");
                artist =words[0];
                projection = new String[]{"DISTINCT ALBUM", "ARTIST"};
                whr = "IS_RINGTONE=0 AND is_music=1 AND IS_NOTIFICATION=0 AND ARTIST=\"" +artist+"\"";
                Log.d(" art", "a " +whr);
                selector(mediaUri);
                artist_album.setText(artist + " : " + album);
                lclick = "album";
                Log.d("lclick", "album go");
                break;
            case "album":
                words = str.split(" :");
                album = words[0];
                whr = "IS_RINGTONE=0 AND is_music=1 AND IS_NOTIFICATION=0 AND ALBUM=\"" +album+"\"";
                projection = new String[]{  "TITLE"};
                selector(mediaUri);
                artist_album.setText(artist + " : " + album);
                lclick = "";
                Log.d("lclick", "album go "+artist + " : " + album);
                break;
        }

    }


    public void selector(Uri u){
        Cursor cursor = getContentResolver().query(u, projection, whr, null, null);  //"is_music=1"  "IS_NOTIFICATION=0"


            music = new ArrayList<>();
            int count = 0;

        music.add(cursor.getColumnName(0));
            while (cursor.moveToNext()) {
                int col = cursor.getColumnCount();
                String row = "";

                for (int i = 0; i < col; i++) {
                    cursor.getColumnNames();
                    if(cursor.getString(i) != null) {
                        row += cursor.getString(i).toString() + " : ";

                    }
                }
                music.add(row);

                count += 1;
            }
            arrayListAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, music);
            showit.setAdapter(arrayListAdapter);
            showit.setOnItemClickListener(this);

        cursor.close();
    }
}
