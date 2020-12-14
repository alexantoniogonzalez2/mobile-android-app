package com.example.comp90018_2020_sem2_project.ui.login;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.comp90018_2020_sem2_project.R;
import com.example.comp90018_2020_sem2_project.dataClass.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

import static android.app.AlertDialog.THEME_HOLO_LIGHT;

public class SignUp extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        final Button loginBtn = findViewById(R.id.signInBtn);
        Button signUpBtn = findViewById(R.id.signUpBtn);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Setup the login button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SignUp.this,Login.class);
                startActivity(intent);
                finish();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    /*
    Setup a new account
     */
    public void createAccount(){
        if (!validateForm()) {
            return;
        }
        else {
            EditText email = findViewById(R.id.userEmail);
            EditText password = findViewById(R.id.userPassword);

            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();

            // [START create_user_with_email]
            mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information

                                FirebaseUser user = mAuth.getCurrentUser();

                                //push user info to database
                                String name = user.getEmail().split("@")[0];
                                User u = new User(name, user.getEmail(), "Avatar1","");
                                mDatabase.child("Users").child(user.getUid()).setValue(u);

                                AlertDialog alertDialog = new AlertDialog.Builder(SignUp.this, THEME_HOLO_LIGHT)
                                        .setTitle("Success")
                                        .setMessage("Account created successfully! Please login")
                                        //.setIcon(android.R.drawable.alert_light_frame)
                                        .setPositiveButton("Login", new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                Intent intent = new Intent(SignUp.this, Login.class);
                                                startActivity(intent);
                                                finish();

                                            }
                                        }).show();
                                mAuth.signOut();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(SignUp.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    /*
    Checks if the user's signup input is valid
     */
    private boolean validateForm() {
        boolean valid = true;
        EditText email =findViewById(R.id.userEmail);
        EditText password = findViewById(R.id.userPassword);
        EditText confPassword = findViewById(R.id.userPasswordConfirm);
        String emailText = email.getText().toString() ;
        String passwordText = password.getText().toString() ;
        String confPasswordText = confPassword.getText().toString();

        if(!passwordText.equals(confPasswordText)){
            password.setError("Passwords dont match");
            confPassword.setError("Passwords dont match");
            valid = false;
        }else{
            password.setError(null);
            confPassword.setError(null);
        }

        if (TextUtils.isEmpty(emailText)) {
            email.setError("Required.");
            valid = false;
        } else {
            if (!isValidEmail(emailText)) {
                email.setError("Invalid Email");
                valid = false;
            } else {
                email.setError(null);
            }
        }

        if (TextUtils.isEmpty(passwordText)) {
            password.setError("Required.");
            valid = false;
        } else {
            if(passwordText.length() < 5){
                password.setError("Password should be more than 5 character");
                valid = false;
            }
            else{
                password.setError(null);
            }
        }
        return valid;
    }


    /*
    Checks if the input email is a valid email
     */
    private boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
}