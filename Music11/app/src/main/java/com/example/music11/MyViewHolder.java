package com.example.music11;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    TextView songview,albumview;


    RelativeLayout mycardview;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageview);
        songview = itemView.findViewById(R.id.song_name);
        albumview = itemView.findViewById(R.id.album_name);
        mycardview = itemView.findViewById(R.id.cardview);

    }
}