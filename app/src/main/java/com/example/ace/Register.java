package com.example.ace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ace.user.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Register extends AppCompatActivity {

    private EditText firstName,lastName,contactEmail,DOB,phoneNumber,password,email;
    private Button btn_register,btn_checkbox;
    private CheckBox cb_stuff,cb_user;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firstName = findViewById(R.id.register_firstName);
        lastName = findViewById(R.id.register_LastName);
        contactEmail = findViewById(R.id.register_contactEmail);
        DOB = findViewById(R.id.register_dateOfBirth);
        phoneNumber = findViewById(R.id.register_phone);
        password = findViewById(R.id.register_password);
        btn_register  = findViewById(R.id.btn_register);
        email = findViewById(R.id.register_email);
        btn_checkbox = findViewById(R.id.btn_checkbox);
        cb_stuff = findViewById(R.id.checkbox_stuff);
        cb_user = findViewById(R.id.checkbox_user);

        btn_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((cb_user.isChecked()&&cb_stuff.isChecked())||(!cb_user.isChecked()&&!cb_stuff.isChecked())){
                    Toast.makeText(getApplicationContext(),"Choose choose one",Toast.LENGTH_LONG).show();
                }else if (cb_user.isChecked()){
                    cb_stuff.setVisibility(View.INVISIBLE);
                    cb_user.setVisibility(View.INVISIBLE);
                    btn_checkbox.setVisibility(View.INVISIBLE);
                    firstName.setVisibility(1);
                    lastName.setVisibility(1);
                    contactEmail.setVisibility(1);
                    DOB.setVisibility(1);
                    phoneNumber.setVisibility(1);
                    password.setVisibility(1);
                    email.setVisibility(1);
                    btn_register.setVisibility(1);
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });

        mAuth = FirebaseAuth.getInstance();


    }



    private void signup(){
           String emailStr = email.getText().toString();
           String fName = firstName.getText().toString();
           String lName = lastName.getText().toString();
           String cEmail = contactEmail.getText().toString();
           String dob = DOB.getText().toString();
           String number = phoneNumber.getText().toString();
           String passwordStr = password.getText().toString();

        // Validations for input email and password
        if (TextUtils.isEmpty(emailStr)) {
            Toast.makeText(getApplicationContext(),
                    "Please enter email address!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (TextUtils.isEmpty(fName)) {
            Toast.makeText(getApplicationContext(),
                    "Please enter first name!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (TextUtils.isEmpty(passwordStr)||passwordStr.length()<6) {
            Toast.makeText(getApplicationContext(),
                    "Please enter password!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if(TextUtils.isEmpty(lName)) {
            Toast.makeText(getApplicationContext(),
                    "Please enter last name!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if(TextUtils.isEmpty(cEmail)) {
            Toast.makeText(getApplicationContext(),
                    "Please enter contact email!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if(TextUtils.isEmpty(number) ){
            Toast.makeText(getApplicationContext(),
                    "Please enter your phone number!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if(TextUtils.isEmpty(dob)) {
            Toast.makeText(getApplicationContext(),
                    "Please enter your date of birth!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        //create an email and get the uid
        mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                            //store the information to database
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("User");
                        User user = new User(fName,lName,passwordStr,emailStr,number,dob,cEmail);
                        myRef.child(mAuth.getUid()).setValue(user);
                        }
                        else {

                            // Registration failed
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Registration failed!!"
                                            + " Please try again later",
                                    Toast.LENGTH_LONG)
                                    .show();

                        }
                    }
                });

        }

    }
