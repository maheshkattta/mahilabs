package com.example.music11;

import static com.example.music11.MainActivity.Broadcast_PLAY_NEW_AUDIO;
import static com.example.music11.MainActivity.PAUSED_INTENT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import  com.example.music11.MediaPlayerService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {



    private MediaPlayerService player;

    private Handler handler;

    private MediaPlayer musicplayer;


    ArrayList<Audio> audioList;

    public int pos;

    private  int audioListIndex;

    private TextView fullsongtitle,fullsongalbum,endtime,seektime;

    private ImageView fullsongpic,prevpic,nextpic,pausepic,playpic;

    private SeekBar fullseekbar;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.music11;",PAUSED_INTENT = "service paused",RESUMED_INTENT="service resumed",SEEK_POS ="seek";

    boolean serviceBound = false;
    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("MEDIA_POSITION_ACTION")) {
                int currentPosition = intent.getIntExtra("CURRENT_POSITION", 0);
                boolean play_pause = intent.getBooleanExtra("play_pause",true);
                int pos1 =   intent.getIntExtra("POSITION", 0);
                int minutes = (currentPosition / 1000) / 60;
                int seconds = (currentPosition / 1000) % 60;
                //String formattedTime = String.valueOf(minutes)+":"+String.valueOf(seconds);
                int endPosition= audioList.get(pos1).getDuration();
                int endminutes = (endPosition / 1000) / 60;
                int endseconds = (endPosition / 1000) % 60;
                //String formattedTimes = String.valueOf(endminutes)+":"+String.valueOf(endseconds);
                String formattedTime = String.format("%02d:%02d", minutes, seconds);
                String formattedTimes = String.format("%02d:%02d", endminutes, endseconds);
                fullseekbar.setProgress(currentPosition);
                fullsongpic.setImageBitmap(audioList.get(pos1).getCoverpic());
                fullsongtitle.setText(audioList.get(pos1).getTitle());
                fullsongalbum.setText(audioList.get(pos1).getAlbum());
                seektime.setText(formattedTimes);
                endtime.setText(formattedTime);
                if(play_pause){
                    pausepic.setVisibility(View.VISIBLE);
                    playpic.setVisibility(View.INVISIBLE);


                }else {
                    playpic.setVisibility(View.VISIBLE);
                    pausepic.setVisibility(View.INVISIBLE);

                }
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("MEDIA_POSITION_ACTION");
        LocalBroadcastManager.getInstance(this).registerReceiver(positionReceiver, intentFilter);
        Intent playerIntent = new Intent(this, MediaPlayerService.class);
        startService(playerIntent);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        MediaPlayerService player = new MediaPlayerService();

        Intent intent= getIntent();
        pos = intent.getIntExtra("playerpos",5);
        audioList = loadAudio();
        audioListIndex = pos;

        fullsongtitle = findViewById(R.id.activesongtitle);
        fullsongalbum = findViewById(R.id.activesongalbum);
        fullsongpic = findViewById(R.id.imageView);
        endtime = findViewById(R.id.end_time);
        seektime = findViewById(R.id.start_time);
        prevpic = findViewById(R.id.prev_song);
        nextpic  = findViewById(R.id.next_song);
        pausepic = findViewById(R.id.pause_pic);
        playpic = findViewById(R.id.play_pic);
        fullseekbar = findViewById(R.id.progressbar);
        fullsongpic.setImageBitmap(audioList.get(audioListIndex).getCoverpic());
        fullsongtitle.setText(audioList.get(audioListIndex).getTitle());
        fullsongalbum.setText(audioList.get(audioListIndex).getAlbum());
        nextpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(pos<audioList.size()-1){
                    pos = pos+1;
                    fullsongpic.setImageBitmap(audioList.get(pos).getCoverpic());
                    fullsongtitle.setText(audioList.get(pos).getTitle());
                    fullsongalbum.setText(audioList.get(pos).getAlbum());
                    pausepic.setVisibility(View.VISIBLE);
                    playpic.setVisibility(View.INVISIBLE);
                    playAudio(pos);}
                else{
                    pos = 0;
                    fullsongpic.setImageBitmap(audioList.get(pos).getCoverpic());
                    fullsongtitle.setText(audioList.get(pos).getTitle());
                    fullsongalbum.setText(audioList.get(pos).getAlbum());
                    pausepic.setVisibility(View.VISIBLE);
                    playpic.setVisibility(View.INVISIBLE);
                    playAudio(pos);

                }

            }
        });
        prevpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pos>0){
                pos = pos-1;
                fullsongpic.setImageBitmap(audioList.get(pos).getCoverpic());
                fullsongtitle.setText(audioList.get(pos).getTitle());
                fullsongalbum.setText(audioList.get(pos).getAlbum());
                pausepic.setVisibility(View.VISIBLE);
                playpic.setVisibility(View.INVISIBLE);
                playAudio(pos);}
                else{
                    pos = audioList.size()-1;
                    fullsongpic.setImageBitmap(audioList.get(pos).getCoverpic());
                    fullsongtitle.setText(audioList.get(pos).getTitle());
                    fullsongalbum.setText(audioList.get(pos).getAlbum());
                    pausepic.setVisibility(View.VISIBLE);
                    playpic.setVisibility(View.INVISIBLE);
                    playAudio(pos);


                }

            }
        });
        pausepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseAudio(pos);
                pausepic.setVisibility(View.INVISIBLE);
                playpic.setVisibility(View.VISIBLE);
            }
        });
        playpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeMedia(pos);
                playpic.setVisibility(View.INVISIBLE);
                pausepic.setVisibility(View.VISIBLE);
            }
        });
        fullseekbar.setMax(audioList.get(pos).getDuration());
        fullseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){

                   seekAudiopos(progress);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
    private ArrayList<Audio> loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);


        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                Bitmap coverpic =getMusicFileThumbnail(data);
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                // Save to audioList
                audioList.add(new Audio(data, title, album, artist,coverpic,duration));
            }
        }
        cursor.close();
        return audioList;
    }
    private Bitmap getMusicFileThumbnail(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);

        byte[] artworkBytes = retriever.getEmbeddedPicture();

        if (artworkBytes != null) {
            return BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.length);
        } else {
            // Return a default thumbnail if no artwork is found
            return null;
        }
    }
    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }  else {


            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    private void resumeMedia(int audioIndex){

        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeAudioIndex(audioIndex);

        //Service is active
        //Send a broadcast to the service -> RESUME_AUDIO

        Intent broadcastIntent1 = new Intent(RESUMED_INTENT);
        sendBroadcast(broadcastIntent1);

    }

    private void pauseAudio(int audioIndex) {
        //Check is service is active


            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO

            Intent broadcastIntent1 = new Intent(PAUSED_INTENT);
            sendBroadcast(broadcastIntent1);


    }
    private void seekAudiopos(int seekpos) {

        Intent broadcastIntent1 = new Intent(SEEK_POS).putExtra("seekpos",seekpos);
        sendBroadcast(broadcastIntent1);


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(positionReceiver);
        try{
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }}catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"app crashed",Toast.LENGTH_SHORT).show();
        }
    }


}