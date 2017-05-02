package com.example.russ.AudioPlayer;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;



public class MainActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    TextView display=null;
    SeekBar seekBar=null;
    boolean paused = true,playing=false,new_album=false;
    ImageView art = null;
    String artist ="", album ="";
    String album_art;
    int seek =0;


    int[] button_resources={com.example.russ.AudioPlayer.R.id.previous, com.example.russ.AudioPlayer.R.id.next, com.example.russ.AudioPlayer.R.id.play_pause, com.example.russ.AudioPlayer.R.id.stop, com.example.russ.AudioPlayer.R.id.stopService, com.example.russ.AudioPlayer.R.id.chooser};
    MyReceiver myReceiver=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.russ.AudioPlayer.R.layout.activity_main);  // Set up click listeners for all the buttons

        for (int i=0; i < button_resources.length; i++)
        {
            Button b = (Button)findViewById(button_resources[i]);
            b.setOnClickListener(this);
        }
        seekBar =(SeekBar)findViewById(com.example.russ.AudioPlayer.R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(0);
        seekBar.setMax(100);
        display = (TextView) findViewById(com.example.russ.AudioPlayer.R.id.display);
        art = (ImageView) findViewById(com.example.russ.AudioPlayer.R.id.imageView);
    }

    // ...
    public void onClick(View v) {
        int index = v.getId();
        Button pButton=(Button)findViewById(com.example.russ.AudioPlayer.R.id.play_pause);
        String cmd = null;
        switch(index)
        {
            //  case R.id.reslist:
            //    cmd = "play";
            //  Log.d("ply go go", album);
            //break;

            case com.example.russ.AudioPlayer.R.id.next:
                cmd = "next";
                break;
            case com.example.russ.AudioPlayer.R.id.play_pause:
                if(!playing){
                    cmd = "play";
                    playing = true;
                    //new_album = false;
                    paused =false;
                    pButton.setBackgroundResource(android.R.drawable.ic_media_pause);
                } else if(playing){
                    if(!paused){

                            cmd = "pause";
                            paused = true;
                            pButton.setBackgroundResource(android.R.drawable.ic_media_play);

                    } else if(paused){
                        cmd = "resume";
                        paused =false;
                        pButton.setBackgroundResource(android.R.drawable.ic_media_pause);
                    }
                }
                break;

            case com.example.russ.AudioPlayer.R.id.stop:
                cmd = "stop";
                playing = false;
                paused = true;
                pButton.setBackgroundResource(android.R.drawable.ic_media_play);
                break;

            case com.example.russ.AudioPlayer.R.id.stopService:
                cmd = "stopService";
                break;
            // Alternatively, you could replace the above 2 lines with the
            // following 2 lines
            //stopService(myIntent);
            //return;
            case com.example.russ.AudioPlayer.R.id.chooser:
                String go = "go";
                Intent intent = new Intent(MainActivity.this, Chooser.class);
                intent.putExtra("start", go);
                startActivityForResult(intent, 0);
                Log.d("Mine", "chosser go");
                break;
            case com.example.russ.AudioPlayer.R.id.previous:
                cmd = "previous";
                break;
        }
        if (cmd != null)
        {
            send_cmd(cmd);

        }

    }

    public void send_cmd(String cmd){
        Intent myIntent = new Intent(this, MyAudioService.class);
        myIntent.putExtra("seekTo", seek);
        myIntent.putExtra("cmd", cmd);
        myIntent.putExtra("artist", artist);
        myIntent.putExtra("album", album);
        Log.d("intent main now", album +" "+ artist);
        startService(myIntent);
    }

     public void onActivityResult(int whatIsIt, int resCode, Intent data) {
        super.onActivityResult(whatIsIt, resCode, data);
        if (resCode != Activity.RESULT_OK) {
            return;
        }
        Button pButton=(Button)findViewById(com.example.russ.AudioPlayer.R.id.play_pause);
        Log.d("Mine", "data data=" );
        if (whatIsIt == 0) {
            artist = data.getStringExtra("arist");
            album = data.getStringExtra("album");
            album_art = data.getStringExtra("album_art");
            Log.d("Mine", "Word Array=" + album + " "+ artist);

            Bitmap bm= BitmapFactory.decodeFile(album_art);
            Bitmap bmNew;
            if(bm != null)
            {
                int w = art.getWidth() - (art.getWidth()/7) ;
                bmNew= Bitmap.createScaledBitmap(bm, w, w, false); //475
                art.setImageBitmap(bmNew);
            }
            else {
                art.setImageResource(com.example.russ.AudioPlayer.R.drawable.no_image);
            }

            playing = true;
            paused = false;
            send_cmd("play");
            pButton.setBackgroundResource(android.R.drawable.ic_media_pause);

            Log.d("art grab", "path= " +album_art+" end");
           // Log.d("bm size", "W "+bm.getWidth()+" H "+ bm.getHeight());
            Log.d("art size", "W "+art.getWidth()+" H "+ art.getHeight());

        }
    }
    @Override
    public void onResume() {

        if (myReceiver == null)
        {
            IntentFilter filter;
            filter = new IntentFilter(MyAudioService.MY_NEXT_SONG_BROADCAST);
            myReceiver = new MyReceiver();
            registerReceiver(myReceiver, filter);
            Log.d("Mine", "onResume: registering MyReceiver");
        }
        super.onResume();
    }

    @Override
    public void onPause() {

        //If you Don't want to receive broadcasts when you don't own the screen
        //   Then uncomment out the following 2 lines of code
        // unregisterReceiver(myReceiver);
        // myReceiver=null;
        Log.d("Mine", "onPause: UNregistering Receiver");

        super.onPause();
    }
    @Override
    public void onDestroy(){
        // Clean up any resources including ending threads,
        // closing database connections etc.
        super.onDestroy();
        if (myReceiver != null)
            unregisterReceiver(myReceiver);
        Log.d("Mine", "onDestroy");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        Log.d("seek"," i "+i+" b "+b);
        seek = i;
        send_cmd("seekTo");

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    //  *******************  INNER CLASS *******************
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int songNum = intent.getIntExtra("songNum", 0);
            int duration = intent.getIntExtra("duration", -1);
            Log.d("dur","d"+ duration);
            //duration /= 1000.0;
            //String songName = intent.getStringExtra("songName");
            String title = intent.getStringExtra("TITLE");
            String album = intent.getStringExtra("ALBUM");
            String artist = intent.getStringExtra("ARTIST");
            String num = intent.getStringExtra("Num");
            double doS = (double)(duration/1000)/60;
            String doSF = String.format("%.2f",doS);

            String str ="("+(songNum +1)+") " + " duration="+doSF + "\n ARTIST = "+ artist + "\n ALBUM = "+ album + "\n TITLE = "+title+  "\n No. of Tracks = " + num;
            seekBar.setMax(0);
            seekBar.setMax(duration);


            Log.d("seek dur", "dur "+ doSF+" doS "+ doS); //(double)(duration/1000)/60);
            display.setText(str);
        }
    }
}