package com.example.musicplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


/*

Student: Camila Bareiro
ID: 002239068

*/
public class MainActivity extends AppCompatActivity implements SongListAdapter.OnSongListener{

    private ArrayList<Song> songList;
    private SongListAdapter adapter;



    private static final int MY_READ_EXTERNAL_STORAGE_REQUEST=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        songList=new ArrayList<Song>();             //holds song's on device info, for future display purpose


        if (ContextCompat.checkSelfPermission(MainActivity.this,
                READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    READ_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("MusicPlayer needs access to your device storage to retrieve the songs you wish to play")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_EXTERNAL_STORAGE_REQUEST);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{READ_EXTERNAL_STORAGE},MY_READ_EXTERNAL_STORAGE_REQUEST);

            }
        } else {
            // Permission has already been granted

            getDeviceSongInfo();
        }




        }

        public void getDeviceSongInfo(){
            ContentResolver contentResolver=getContentResolver();
            Uri song=android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor=contentResolver.query(song,null,null,null,null);

            if(cursor.moveToFirst()) {       //media not empty

                int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);


                do{

                    long songId = cursor.getLong(idColumn);
                    String songTitle = cursor.getString(titleColumn);
                    String artist=cursor.getString(artistColumn);

                    songList.add(new Song(songId,songTitle,artist));        //add somg to SongList, to later display in recyclerView

                   // display.setText(songTitle + "\n" + songId);

                }while(cursor.moveToNext());                                //as long as songs in device


                //pass songList to recyclerViewAdapter for display

                adapter=new SongListAdapter(this,songList,this);
                final RecyclerView songPreview=findViewById(R.id.songList);

                songPreview.setLayoutManager(new LinearLayoutManager(this));
                songPreview.setAdapter(adapter);




            }


        }


         public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

            super.onRequestPermissionsResult(requestCode,permissions,grantResults);

            switch(requestCode){

                case MY_READ_EXTERNAL_STORAGE_REQUEST: {
                    if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                        getDeviceSongInfo();
                    }
                }
            }

         }


    @Override
    public void onSongClick(int position) {

        //get the id of Song to be played,the artist and songTitle. pass it to PlayerActivity in an intent


       long songId=songList.get(position).getId();
       String artist=songList.get(position).getArtist();
       String songTitle=songList.get(position).getSongTitle();



        Intent intent= new Intent(this, PlayerActivity.class);
        intent.putExtra("songId",songId);
        intent.putExtra("artist",artist);
        intent.putExtra("songTitle",songTitle);




        startActivity(intent);


    }


















}

