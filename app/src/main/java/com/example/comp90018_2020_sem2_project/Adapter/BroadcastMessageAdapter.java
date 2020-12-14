package com.example.comp90018_2020_sem2_project.Adapter;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.comp90018_2020_sem2_project.R;

import java.io.IOException;
import java.util.List;

public class BroadcastMessageAdapter extends RecyclerView.Adapter<BroadcastMessageAdapter.ViewHolder> {

        private List<String> mData;
        private List<String> stringUrl;
        private LayoutInflater mInflater;
        private MediaPlayer mMediaplayer;

        // data is passed into the constructor
        public BroadcastMessageAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
            mMediaplayer = new MediaPlayer();
            mMediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        }

        public void addData(List<String> users,List<String> stringUrl){
            this.mData = users;
            this.stringUrl = stringUrl;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = mInflater.inflate(R.layout.recycleview_row_broadcast, parent, false);
                return new ViewHolder(view);
                }

        // binds the data to the TextView in each row
        @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                String username = mData.get(position);
                String url = stringUrl.get(position);
                holder.myTextView.setText(username);
                //holder.playButton.setText(url);
                }

        // total number of rows
        @Override
        public int getItemCount() {
                return mData.size();
                }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView myTextView;
            Button playButton;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.userName);
                playButton = itemView.findViewById(R.id.playVoice);
                playButton.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                try {
                    mMediaplayer.setDataSource(stringUrl.get(getAdapterPosition()));
                    mMediaplayer.prepareAsync();
                } catch (IOException e) {
                }

                // wait for media player to get prepare
                mMediaplayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        playButton.setBackgroundResource(R.drawable.playing);
                        try{mp.start();}
                        catch (Exception e){
                        }
                    }
                });
                mMediaplayer.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mMediaplayer.reset();
                        playButton.setBackgroundResource(R.drawable.play);
                    }
                } );
            }
        }

}