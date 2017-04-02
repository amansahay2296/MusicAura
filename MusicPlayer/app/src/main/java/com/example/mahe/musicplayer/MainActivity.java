package com.example.mahe.musicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.ArrayList;
import android.widget.MediaController.MediaPlayerControl;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl
{

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.mahe.musicplayer.PlayNewAudio";
    private MediaPlayerService player;
    boolean serviceBound = false;
    ArrayList<Audio> audioList;


    Intent playerIntent;

    private MusicController controller;


    private boolean paused=false, playbackPaused=false;



    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Songs");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Songs");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Albums");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Albums");
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("Artists");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Artists");
        host.addTab(spec);



//        loadCollapsingImage(imageIndex);
        loadAudio();
        //play the first audio in the ArrayList
        //playAudio(audioIndex);


        initRecyclerView();

       RelativeLayout root = (RelativeLayout) findViewById(R.id.root);

        ViewTreeObserver vto = root.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Call your controller set-up now that the layout is loaded
                setController();
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }

    }

    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {

            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);
             playerIntent = new Intent(this, MediaPlayerService.class);

            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send media with BroadcastReceiver

            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }


    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // Save to audioList
                audioList.add(new Audio(data, title, album, artist));
            }
        }
        cursor.close();
    }


    private void initRecyclerView() {
        if (audioList.size() > 0) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            SongAdapter adapter = new SongAdapter(audioList, getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            //setController();
            recyclerView.addOnItemTouchListener(new CustomTouchListener(this, new onItemClickListener() {
                @Override
                public void onClick(View view, int index) {
                    playAudio(index);

                }

            }));
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menures, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void start() {
        if(playbackPaused==true)
        player.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        player.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(player!=null && serviceBound && player.isPng())
        return player.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(player!=null && serviceBound&& player.isPng())
            return player.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int i) {
            player.seek(i);
    }

    @Override
    public boolean isPlaying() {
        if(player!=null && serviceBound)
        return player.isPng();
        return false;
    }


    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {

        return 0;
    }


    private void setController ()
    {
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                playNext();
                                            }
                                        }, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                playPrev();
                                            }
                                        }

        );


        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.recyclerview));
        controller.setEnabled(true);
        controller.show();

    }

    @Override
    protected void onPause() {
        super.onPause();

        paused=true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    private void playNext(){


        player.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    private void playPrev(){
        player.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

   /* public void songPicked(View view){
        playAudio(Integer.parseInt(view.getTag().toString()));

        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        controller.hide();
       
        }
}
