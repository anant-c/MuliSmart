package com.example.mulismarthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RoomRecViewAdapter extends RecyclerView.Adapter<RoomRecViewAdapter.ViewHolder> {

    private ArrayList<Room> rooms = new ArrayList<>();

    private Context context;

    public RoomRecViewAdapter(Context context){this.context = context;}


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_layout,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtName.setText(rooms.get(position).getName());

        //for showing the images using glide
        Glide.with(context)
                .asBitmap().load(rooms.get(position).getImageUrl()).into(holder.image);

        Glide.with(context)
                .asBitmap().load(rooms.get(position).getImageUrl2()).into(holder.image2);

        //onclick listener on each element
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, rooms.get(position).getName()+ " Selected", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
        notifyDataSetChanged(); // for dynamically changing dataset like names we get from website
    }

    public class ViewHolder extends RecyclerView.ViewHolder{ //responsible for holding view items for every item in our contact list recycler view

        private TextView txtName;
        private CardView parent;

        private ImageView image,image2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            parent = itemView.findViewById(R.id.parent);
            image = itemView.findViewById(R.id.image);
            image2 = itemView.findViewById(R.id.image2);
        }
    }
}
