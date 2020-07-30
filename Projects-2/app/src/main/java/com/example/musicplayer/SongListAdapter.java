package com.example.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;



public class SongListAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<Song> songList;

    private OnSongListener listener;

    public SongListAdapter(Context context,ArrayList<Song> songList, OnSongListener listener){


        super();
        this.context=context;

        this.songList=new ArrayList<Song>();
        this.songList=songList;

        this.listener=listener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View singleSongView=inflater.inflate(R.layout.song_details_display,parent,false);

        return new songHolder(singleSongView,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        String songTitle=songList.get(position).getSongTitle();
        String artist=songList.get(position).getArtist();
        Long songId=songList.get(position).getId();


        ((songHolder)holder).songDetails.setText(songTitle+"\n"+artist+"\n"+songId);

    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    private class songHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView songDetails;
        private OnSongListener listener;

        public songHolder(@NonNull View itemView,OnSongListener listener) {
            super(itemView);
            songDetails=itemView.findViewById(R.id.songDetails);
            this.listener=listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onSongClick(getAdapterPosition());

            //position of song to be played passed to main activity, which implements OnSongListener
        }
    }



    public interface OnSongListener{

        void onSongClick(int position);
    }

}
