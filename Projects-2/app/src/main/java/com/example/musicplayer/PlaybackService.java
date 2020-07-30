package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import java.io.IOException;
import java.util.List;

/*


Notification linked to the foreground service disappears
if not used to control the playback before leaving player activity.

*/

public class PlaybackService extends MediaBrowserServiceCompat {

    private long songId;
    private String artist;
    private String songTitle;
    private Uri songToPlay;
    private boolean notificationCreated=false;
    private MediaPlayer player;
    private MediaSessionCompat mSession;
    private PlaybackStateCompat.Builder mBuilder;


    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";


    public void onCreate(){

        super.onCreate();


        //Initializations related to player

        mSession=new MediaSessionCompat(this, "MediaSession");
        //pass a MediaSessionCompat.Callback object that handles callbacks from a media controller (UI->mSession)
        mSession.setCallback(new MediaSessionCallback());

        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS ); //no need

        //session token so client can communicate
        setSessionToken(mSession.getSessionToken());
        mBuilder=new PlaybackStateCompat.Builder();

    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {

        //get the id of the song to play, artist & song name passed in a Bundle from client (PlayerActivity)

         songId=rootHints.getLong("songId");
         songTitle=rootHints.getString("songTitle");
         artist=rootHints.getString("artist");


        player=new MediaPlayer();


        //connect to your MediaSession without browsing
        return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);

    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

        //  Browsing not enabled
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags,int startId){

        MediaButtonReceiver.handleIntent(mSession,intent);
        return super.onStartCommand(intent,flags,startId);
    }






    //callbacks implemented

    private class MediaSessionCallback extends MediaSessionCompat.Callback{

        @Override

        public void onPrepare(){
            player.reset();

            //be on the lookout for the song's end, so that player goes to stopped state
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    mBuilder.setActions(PlaybackStateCompat.ACTION_STOP);
                    mBuilder.setState(PlaybackStateCompat.STATE_STOPPED,player.getCurrentPosition(),1.0f, SystemClock.elapsedRealtime());
                    mSession.setPlaybackState(mBuilder.build());
                }
            });

            songToPlay= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,songId);


            try {
                player.setDataSource(getApplicationContext(),songToPlay);
            } catch (IOException e) {
                e.printStackTrace();
            }


            //Set onPreparedListener for player, informs when prepared

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    //player has song prepared and ready to be played

                    onPlay();

                }
            });


            // prepare the song for playback
            player.prepareAsync();



        }


        @Override
        public void onPlay() {

            super.onPlay();


            //Set media session to active
            mSession.setActive(true);

            //what actions want support while player is playing
            mBuilder.setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP);

            //inform media session that player is playing

            mBuilder.setState(PlaybackStateCompat.STATE_PLAYING, player.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
            mSession.setPlaybackState(mBuilder.build());

            //play music
            player.start();

            //display notification, if not already existing

            if(!notificationCreated){

                createNotification();
            }

        }


        @Override
        public void onPause(){
            super.onPause();

            //pause song
            player.pause();

            //what actions want support while player is paused
            mBuilder.setActions(PlaybackStateCompat.ACTION_PLAY |PlaybackStateCompat.ACTION_STOP );

            //inform media session that player is paused

            mBuilder.setState(PlaybackStateCompat.STATE_PAUSED,player.getCurrentPosition(),1.0f, SystemClock.elapsedRealtime());
            mSession.setPlaybackState(mBuilder.build());

            stopForeground(false);

        }


        @Override
        public void onStop(){
            super.onStop();

            //Stop foreground Service & release ressources

            mSession.setActive(false);

            if(player!=null){
                player.stop();
                player.reset();
                player.release();
                player=null;
            }

            mSession.release();
            stopForeground(true);
        }

    }


    protected void createNotification(){

        notificationCreated=true;

        //create notification channel for foreground service notification

        NotificationChannel simpleServiceNotificationChannel=new NotificationChannel("notificationChannel","notificationChannel", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.createNotificationChannel(simpleServiceNotificationChannel);


        //add to the notification media controls and bitmap


        Bitmap artwork = BitmapFactory.decodeResource(getResources(),R.drawable.music);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notificationChannel");

        builder

                .setContentTitle(songTitle)
                .setContentText(artist)
                .setSubText("Now Playing")
                .setLargeIcon(artwork)
                .setSmallIcon(R.drawable.play)
                .setContentIntent(mSession.getController().getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(PlaybackService.this,
                        PlaybackStateCompat.ACTION_STOP))
                .addAction(new NotificationCompat.Action(R.drawable.ic_pause, "pause",MediaButtonReceiver.buildMediaButtonPendingIntent(PlaybackService.this,PlaybackStateCompat.ACTION_PAUSE)))
                .addAction(new NotificationCompat.Action(R.drawable.ic_play, "pause",MediaButtonReceiver.buildMediaButtonPendingIntent(PlaybackService.this,PlaybackStateCompat.ACTION_PLAY)))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1)
                        .setMediaSession(mSession.getSessionToken())
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(PlaybackService.this,
                                PlaybackStateCompat.ACTION_STOP)));

        startForeground(1, builder.build());


    }


}
