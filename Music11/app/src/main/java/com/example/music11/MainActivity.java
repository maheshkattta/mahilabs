package com.example.music11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music11.databinding.ActivityMainBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;

    ActivityMainBinding binding;
    ArrayList<Audio> audioList;

    TextView toolsongalbum,toolsongtitle;
    private MediaPlayerService player;

    private  MediaPlayer musicplayer;
    boolean serviceBound = false;

    private ImageView toolsongpic,prevpic,nextpic,playpic,pausepic;
    public int toolpos;

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.music11;",PAUSED_INTENT = "service paused",RESUMED_INTENT="service resumed";
    private static final int REQUEST_CAMERA_LOCATION_PERMISSION = 100;

// ...



// Change to your package name




    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("MEDIA_POSITION_ACTION")) {
                int currentposition = intent.getIntExtra("CURRENT_POSITION", 0);
                int pos1 =   intent.getIntExtra("POSITION", 0);
                boolean play_pause = intent.getBooleanExtra("play_pause",true);

                //fullseekbar.setProgress(currentposition);
                toolsongpic.setImageBitmap(audioList.get(pos1).getCoverpic());
                toolsongtitle.setText(audioList.get(pos1).getTitle());
                toolsongalbum.setText(audioList.get(pos1).getAlbum());
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
        setContentView(R.layout.activity_main);
        requestPermissions();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("MEDIA_POSITION_ACTION");
        Intent intent= getIntent();
        toolpos = intent.getIntExtra("playerpos",5);
        LocalBroadcastManager.getInstance(this).registerReceiver(positionReceiver, intentFilter);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RecyclerView recyclerView = findViewById(R.id.SONG_VIEW);
        ArrayList<Audio> Mysongs = loadAudio();
        ConstraintLayout rel = findViewById(R.id.songcontrol);
        toolsongtitle = findViewById(R.id.song_name);
        toolsongalbum = findViewById(R.id.album_name);
        toolsongpic = findViewById(R.id.imageview);
        pausepic= findViewById(R.id.tool_pause);
        playpic = findViewById(R.id.tool_play_pause);
        prevpic = findViewById(R.id.tool_prev_song);
        nextpic = findViewById(R.id.tool_next_song);



        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if(Mysongs!=null){
        recyclerView.setAdapter(new MyAdapter(getApplicationContext(),Mysongs));

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                        toolsongtitle.setText(audioList.get(position).getTitle());
                        toolsongtitle.setSelected(true);

                        toolsongalbum.setText(audioList.get(position).getAlbum());
                        toolsongalbum.setMovementMethod(new ScrollingMovementMethod());
                        toolsongpic.setImageBitmap(audioList.get(position).getCoverpic());
                        rel.setVisibility(View.VISIBLE);
                        pausepic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pauseAudio(position);
                                pausepic.setVisibility(View.INVISIBLE);
                                playpic.setVisibility(View.VISIBLE);
                            }
                        });
                        playpic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                resumeMedia(position);
                                playpic.setVisibility(View.INVISIBLE);
                                pausepic.setVisibility(View.VISIBLE);
                            }
                        });

                        rel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent myIntent = new Intent(MainActivity.this, PlayerActivity.class);
                                myIntent.putExtra("playerpos",position); //Optional parameters
                                MainActivity.this.startActivity(myIntent);



                            }
                        });


                        playAudio(position);
                    }
                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );}
        else{
            Toast.makeText(getApplicationContext(), "cannot find offline soongs", Toast.LENGTH_SHORT).show();
        }
        prevpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toolpos>0){
                    toolpos = toolpos-1;
                    toolsongpic.setImageBitmap(audioList.get(toolpos).getCoverpic());
                    toolsongtitle.setText(audioList.get(toolpos).getTitle());
                    toolsongalbum.setText(audioList.get(toolpos).getAlbum());
                    pausepic.setVisibility(View.VISIBLE);
                    playpic.setVisibility(View.INVISIBLE);
                    playAudio(toolpos);}
                else{
                    toolpos = audioList.size()-1;                    toolsongpic.setImageBitmap(audioList.get(toolpos).getCoverpic());
                    toolsongtitle.setText(audioList.get(toolpos).getTitle());
                    toolsongalbum.setText(audioList.get(toolpos).getAlbum());
                    pausepic.setVisibility(View.VISIBLE);
                    playpic.setVisibility(View.INVISIBLE);
                    playAudio(toolpos);

                }

            }
        });
        nextpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(toolpos<audioList.size()-1){
                    toolpos = toolpos+1;
                    toolsongpic.setImageBitmap(audioList.get(toolpos).getCoverpic());
                    toolsongtitle.setText(audioList.get(toolpos).getTitle());
                    toolsongalbum.setText(audioList.get(toolpos).getAlbum());
                    pausepic.setVisibility(View.VISIBLE);
                    playpic.setVisibility(View.INVISIBLE);
                    playAudio(toolpos);}
                else{
                    toolpos = 0;
                    toolsongpic.setImageBitmap(audioList.get(toolpos).getCoverpic());
                    toolsongtitle.setText(audioList.get(toolpos).getTitle());
                    toolsongalbum.setText(audioList.get(toolpos).getAlbum());
                    pausepic.setVisibility(View.VISIBLE);
                    playpic.setVisibility(View.INVISIBLE);
                    playAudio(toolpos);

                }
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
        assert cursor != null;
        cursor.close();
        return audioList;
    }
    private Bitmap getMusicFileThumbnail(String filePath) {

        try{

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);

        byte[] artworkBytes = retriever.getEmbeddedPicture();

        if (artworkBytes != null) {
            return BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.length);
        } else {
            // Return a default thumbnail if no artwork is found
            return null;
        }}catch (RuntimeException e){
            Toast.makeText(getApplicationContext(),"hiiii",Toast.LENGTH_SHORT);
            return null;
        }

    }

    //Binding this Client to the AudioPlayer Service
    private final ServiceConnection serviceConnection = new ServiceConnection() {
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
            storage.storeAudioseekpos(String.valueOf(5));

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }  else {

            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

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


        //Store the new audioIndex to SharedPreferences
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeAudioIndex(audioIndex);


        Intent broadcastIntent1 = new Intent(PAUSED_INTENT);
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
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    private boolean arePermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
       if(Build.VERSION.SDK_INT <= 33) {
           ActivityCompat.requestPermissions(this,

                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CAMERA_LOCATION_PERMISSION);
       }else{
           ActivityCompat.requestPermissions(this,

                   new String[]{Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.READ_MEDIA_AUDIO},
                   REQUEST_CAMERA_LOCATION_PERMISSION);

       }

    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        // super.onBackPressed();
    }


}