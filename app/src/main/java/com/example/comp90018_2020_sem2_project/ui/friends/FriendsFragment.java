package com.example.comp90018_2020_sem2_project.ui.friends;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comp90018_2020_sem2_project.Adapter.FriendsAdapter;
import com.example.comp90018_2020_sem2_project.R;
import com.example.comp90018_2020_sem2_project.dataClass.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {

    String hostId;

    // Getting Firebase Instance
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    // Getting Database Reference
    DatabaseReference databaseReference = database.getReference();

    // Add RecyclerView member
    private RecyclerView recyclerView;
    ArrayList<String> userList;
    ArrayList<User> friends;
    ArrayList<User> allUsers;
    FriendsAdapter friendsAdapter;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getActivity().getIntent();
        hostId = i.getStringExtra("message");
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_friends, container, false);

        ///
        SearchView searchView = root.findViewById(R.id.action_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                friendsAdapter.filter(newText, allUsers);
                return false;
            }
        });

        // Add the following lines to create RecyclerView
        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        databaseReference.child("Users").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // It is created a list with the user's friend containing the UID
                userList = new ArrayList<String>();
                Iterable<DataSnapshot> contacts = snapshot.child(hostId).child("contacts").getChildren();
                for (DataSnapshot contact : contacts){
                    userList.add(contact.getKey());

                }

                // Given the list, it is filtered the friends to show
                friends = new ArrayList<User>();
                allUsers = new ArrayList<User>();
                for(DataSnapshot ds: snapshot.getChildren()){
                    User friendsv = ds.getValue(User.class);
                    allUsers.add(friendsv);
                    if (userList.contains(ds.getKey())) {
                        User friend = ds.getValue(User.class);
                        friends.add(friend);
                    }
                }
                friendsAdapter = new FriendsAdapter(friends,hostId,getActivity());
                recyclerView.setAdapter(friendsAdapter);
                friendsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return root;

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}