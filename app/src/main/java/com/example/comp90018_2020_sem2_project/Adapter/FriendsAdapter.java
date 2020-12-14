package com.example.comp90018_2020_sem2_project.Adapter;

import com.example.comp90018_2020_sem2_project.dataClass.ExternalUser;
import com.example.comp90018_2020_sem2_project.R;
import com.example.comp90018_2020_sem2_project.dataClass.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.RecyclerViewHolder> {

    private ArrayList<User> friends;
    private ArrayList<User> itemscopy;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference();
    Query query = databaseReference.child("Users");
    private String hostID;
    Context context;

    public FriendsAdapter(ArrayList<User> friends,String hostID,Context context) {
        this.friends = friends;
        this.itemscopy = new ArrayList<>(friends);
        this.hostID = hostID;
        this.context = context;
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.recycler_friend;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new RecyclerViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        User user = this.friends.get(position);
        holder.usernameView.setText(user.getName());
        holder.emailView.setText(user.getEmail());
        holder.avatarView.setImageResource(user.getDrawable());
        holder.followButton.setVisibility(View.VISIBLE);
        if (user.getLinkedInUsername() != null ){
            holder.linkedInView.setVisibility(View.VISIBLE);

        } else {
            holder.linkedInView.setVisibility(View.GONE);
        }
        for (User f : itemscopy){
           if(f.getEmail().equals(user.getEmail())){
                holder.followButton.setVisibility(View.GONE);
                break;
            }
        }



    }

    /*
    Gets the size of the friends list
     */
    @Override
    public int getItemCount() {
        return friends.size();
    }

    /*
    Filter the friends tree
     */
    public void filter(String text, ArrayList<User> allUsers) {
        friends.clear();
        if(text.isEmpty()){
            friends.addAll(itemscopy);
        } else{
            text = text.toLowerCase();
            for(User item: allUsers){

                if(item.getEmail().toLowerCase().contains(text)){
                    friends.add(item);
                    for(User f : itemscopy){

                        if(f.getEmail().equals(item.getEmail())){

                           friends.remove(item);
                           break;
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView usernameView;
        private TextView emailView;
        private ImageView avatarView;
        private ImageView linkedInView;
        private Context context;
        private Button followButton;

        public RecyclerViewHolder(@NonNull View itemView, Context activityContext) {
            super(itemView);
            this.context = activityContext;
            usernameView = itemView.findViewById(R.id.usernameView);
            emailView = itemView.findViewById(R.id.emailView);
            avatarView = itemView.findViewById(R.id.avatarView);
            linkedInView = itemView.findViewById(R.id.linkedInView);
            followButton = itemView.findViewById(R.id.follow_button);

            linkedInView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    String linkedInURL = "https://www.linkedin.com/in/" + friends.get(getAdapterPosition()).getLinkedInUsername();
                    Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedInURL));
                    browse.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(browse);

                }
            });

            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                findUserId(friends.get(getAdapterPosition()).getEmail());
                }
            });
        }
    }


    /*
    Finds the userid for a given email
     */
    private void findUserId(final String email){
        final String[] userId = {"invalid user"};
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String emailget = (String) ds.child("email").getValue();
                    if (email.equals(emailget)){
                        userId[0] = ds.getKey();
                        break;
                    }
                }

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(userId[0])){
                            if(!snapshot.child(hostID).child("contacts").hasChild(userId[0])){
                                ExternalUser currentUser = new ExternalUser(snapshot.child(userId[0]));
                                databaseReference.child("Users").child(hostID).child("contacts").child(userId[0]).setValue(currentUser.getUserName());
                                Toast.makeText(context,"Friend addded",Toast.LENGTH_SHORT).show();
                            }
                            else{
                            }
                        }
                        else{
                            Toast.makeText(context,"No user found",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}