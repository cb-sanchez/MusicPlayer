package com.example.musicplayer;



public class Song {

    private long id;
    private String songTitle;
    private String artist;

    public Song(long id, String songTitle, String artist){
        this.id=id;
        this.songTitle=songTitle;
        this.artist=artist;
    }

    //getters

    public long getId() { return id; }
    public String getSongTitle(){return songTitle;}
    public String getArtist(){return artist;}

}
