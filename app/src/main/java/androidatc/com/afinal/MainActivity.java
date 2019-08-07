package androidatc.com.afinal;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    private AppCompatSeekBar seekBar;
    private Button playButton;//play/pause button
    private TextView timeStart, timeEnd;

    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;
    private MusicAdapter musicAdapter;// adapter for recyclerView
    private List<music> musicList;
    private int mPosition = -1;//current position

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            timeStart.setText(parseDate(mediaPlayer.getCurrentPosition()));
            mHandler.sendMessageDelayed(Message.obtain(), 1000);
            return true;
        }
    });



    //cursor get -- ms
    private String parseDate(int time) {
        time = time / 1000;
        int min = time / 60;
        int second = time % 60;
        return min + ":" + second;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //must have this, it will send request to ask for write external storage
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        setComponents();
        Connector.getDatabase();
        queryMusicFromDataBase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryMusicFromDataBase();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, AddMusicActivity.class);
        startActivity(intent);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void setComponents(){
        timeStart = findViewById(R.id.time_start);
        timeEnd = findViewById(R.id.time_end);
        playButton = findViewById(R.id.music_play);
        Button lastB = findViewById(R.id.last_music);
        Button nextB = findViewById(R.id.next_music);
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());// jump to where seekbar points to
                mHandler.sendMessageDelayed(Message.obtain(), 1000);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(musicList.size() != 0) {
                    if (mediaPlayer == null) {
                        changeMusic(0);
                        mPosition = 0;
                    } else {
                        startOrPause();
                    }
                }
            }
        });

        nextB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicList.size() != 0)changeMusic(++mPosition);
            }
        });

        lastB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(musicList.size() != 0)changeMusic(--mPosition);

            }
        });

        // recyclerView
        final RecyclerView musicListView = findViewById(R.id.music_list);
        musicList = new ArrayList<>();
        musicListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        musicAdapter = new MusicAdapter(musicList);
        musicListView.setAdapter(musicAdapter);
        musicAdapter.setSelected(-1);
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                mPosition = position;
                changeMusic(position);
            }
        });

        musicAdapter.setOnLongItemClickListener(new MusicAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(View v, final int position) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Remove？")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                musicList.get(position).delete();
                                musicList.remove(position);
                                musicAdapter.notifyDataSetChanged();
                            }
                        })
                        .create()
                        .show();
            }
        });

    }

    private void queryMusicFromDataBase() {
        musicList.clear();
        musicList.addAll(DataSupport.findAll(music.class));
        musicAdapter.notifyDataSetChanged();
    }

    private void playMusic(music myMusic){
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                changeMusic(++mPosition);
                            }
                });
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(myMusic.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            timeEnd.setText(parseDate(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
            mHandler.sendMessageDelayed(Message.obtain(), 1000);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void changeMusic(int position){
        if (position < 0) {
            mPosition = musicList.size() - 1;
            playMusic(musicList.get(mPosition));
        } else if (position > musicList.size() - 1) {
            mPosition = 0;
            playMusic(musicList.get(0));
        } else {
            playMusic(musicList.get(position));
            mPosition = position;
        }

        musicAdapter.setSelected(mPosition);

        musicAdapter.notifyDataSetChanged();
        playButton.setBackgroundResource(R.drawable.ic_playing);
    }

    private  void startOrPause(){
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playButton.setBackgroundResource(R.drawable.ic_pause);

        } else {
            mediaPlayer.start();
            playButton.setBackgroundResource(R.drawable.ic_playing);
        }
    }
}
