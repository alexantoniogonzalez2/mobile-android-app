package com.example.comp90018_2020_sem2_project.ui.account;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.comp90018_2020_sem2_project.R;
import com.example.comp90018_2020_sem2_project.dataClass.User;
import com.example.comp90018_2020_sem2_project.ui.login.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth;
    User user;
    String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getActivity().getIntent();
        userId = i.getStringExtra("message");
        user  = (User) i.getSerializableExtra("User");
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        String Avatar = null;
        if (user != null) {
            Avatar = user.getAvatar();
        }

        final Drawable back1 = getResources().getDrawable(R.drawable.image_blue_border);
        final Drawable back2 = getResources().getDrawable(R.drawable.image_transp_border);

        accountViewModel =
                ViewModelProviders.of(this).get(AccountViewModel.class);
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        final Button changeName = root.findViewById(R.id.changeDName);
        final ImageButton avatar1 = root.findViewById(R.id.Avatar1);
        final ImageButton avatar2 = root.findViewById(R.id.Avatar2);

        final Button logOutBtn = root.findViewById(R.id.logoutBtn);

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        if (Avatar == "Avatar2") {
            avatar2.setBackground(back1);
        }
        else {
            avatar1.setBackground(back1);
        }

        final TextView username = root.findViewById(R.id.editTextPersonName);
        username.setText(user.getName());
        final TextView linkedInUsername = root.findViewById(R.id.editLinkedin);
        linkedInUsername.setText(user.getLinkedInUsername());
        final TextView email = root.findViewById(R.id.userEmail);
        email.setText(user.getEmail());

        changeName.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                user.setName(username.getText().toString());
                // Update display name in database
                Map<String, Object> mHashMap = new HashMap<>();
                mHashMap.put("name",username.getText().toString());
                mDatabase.child("Users").child(userId).updateChildren( mHashMap);

                user.setLinkedInUsername(linkedInUsername.getText().toString());
                // Update LinkedIn username in database
                mHashMap.put("linkedInUsername",linkedInUsername.getText().toString());
                mDatabase.child("Users").child(userId).updateChildren( mHashMap);

            }
        });

        avatar1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                avatar1.setBackground(back1);
                avatar2.setBackground(back2);
                user.setAvatar("Avatar1");

                //update avatar selection
                mDatabase.child("Users").child(userId).child("avatar").setValue("Avatar1");
            }
        });

        avatar2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                avatar2.setBackground(back1);
                avatar1.setBackground(back2);
                user.setAvatar("Avatar2");
                mDatabase.child("Users").child(userId).child("avatar").setValue("Avatar2");
            }
        });

        return root;
    }
}