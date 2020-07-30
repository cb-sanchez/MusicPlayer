package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;




public class PlayerActivity extends AppCompatActivity {

    String artist;
    String songTitle;

    private ImageView playbackControl;
    private ImageView musicIcon;
    private TextView artistSongInfo;

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks=new MediaBrowserCompat.ConnectionCallback(){
        @Override

        public void onConnected(){
            super.onConnected();

            //get token to connect to mediaSession

            MediaSessionCompat.Token token=mediaBrowser.getSessionToken();



            try {
                //retrieve the controller of the mediaSession

                mediaController=new MediaControllerCompat(PlayerActivity.this,token);

                //link the controller to the activity

                MediaControllerCompat.setMediaController(PlayerActivity.this,mediaController);


                //prepare the controller that will allow control the player

                mediaController.getTransportControls().prepare();




                //call to method linking UI controls with mediaController

                buildTransportControls();

            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }
    };


    //Add functionality to UI controls such that they communicate with mediaController,
    // which relays the appropriate action to take to the mediaSession that controls the player

    //UI -> Service
    private void buildTransportControls(){



        playbackControl=(ImageView) findViewById(R.id.playbackControl);

        playbackControl.setImageResource(R.drawable.play);

        //Attach listener

        playbackControl.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                int currentStatePlayback = mediaController.getPlaybackState().getState(); //<-- Only want to do that when player is Playing, otherwise exception


                switch(currentStatePlayback){

                    case PlaybackStateCompat.STATE_PLAYING: mediaController.getTransportControls().pause();break;

                    case PlaybackStateCompat.STATE_PAUSED: mediaController.getTransportControls().play();break;

                }

            }

        });


        // Register  controllerCallback to stay in sync with mediaSession ie. UI <- Service functionality of controller

        mediaController.registerCallback(controllerCallback);

    }

    //UI <- Service    functionality of controller :Reaction of UI when player changes state (change image button)
    private MediaControllerCompat.Callback controllerCallback=new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);



            switch(state.getState()){

                case PlaybackStateCompat.STATE_PLAYING: playbackControl.setImageResource(R.drawable.media_control_pause);break;//the action that can take place next is play, so display it

                case PlaybackStateCompat.STATE_PAUSED: playbackControl.setImageResource(R.drawable.media_control_play);break; //the action that can take place next is pause, so display it

                case PlaybackStateCompat.STATE_STOPPED: playbackControl.setImageResource(R.drawable.stop); mediaController.getTransportControls().stop();break;

                case PlaybackStateCompat.STATE_NONE: playbackControl.setImageResource(R.drawable.stop);break;

            }


        }
    };

    ///Done with MediaSession details ///
    ///Onwards to Activity lifecycle///


    @Override
    public void onStart(){              //For this activity to fetch mediaSession token
        super.onStart();

        if (!mediaBrowser.isConnected()){
            mediaBrowser.connect();     //if connection to service successful, will call OnConnect of MediaBrowserCompat.ConnectionCallback

        }

    }








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


        //packing all info on song to play passed down from mainActivity into a bundle, to be passed to service


        Intent intent = getIntent();
        long songId=intent.getLongExtra("songId",0);
         artist=intent.getStringExtra("artist");
         songTitle=intent.getStringExtra("songTitle");

        Bundle passSongId=new Bundle();
        passSongId.putLong("songId",songId);
        passSongId.putString("artist",artist);
        passSongId.putString("songTitle",songTitle);

        //Create UI components: music icon and info about song title and artist

        musicIcon=findViewById(R.id.musicIcon);

        musicIcon.setImageResource(R.drawable.music);


        artistSongInfo=findViewById(R.id.artistSongInfo);


        artistSongInfo.setText(songTitle+"\n"+artist);
        artistSongInfo.setGravity(Gravity.CENTER);

        // Create MediaBrowserServiceCompat : Connection to Service established here. So pass id of song clicked on in recycler view

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(PlayerActivity.this, PlaybackService.class),connectionCallbacks,passSongId);

    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        //Release Controller

        if (mediaController!=null ) {                                      // only give up control of media session if state stopped

            mediaController.unregisterCallback(controllerCallback);       //stop   UI<-mediaSession communication

        }

        mediaBrowser.disconnect();

    }
}
