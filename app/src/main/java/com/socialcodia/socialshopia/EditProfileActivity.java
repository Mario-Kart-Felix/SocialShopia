package com.socialcodia.socialshopia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialcodia.socialshopia.storage.Constants;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    Toolbar toolbar;
    private ImageButton btnBack, btnLocation;
    private ImageView userProfileImage;
    private EditText inputName, inputMobileNumber, inputCity, inputState, inputCountry, inputAddress;
    private Button btnUpdateProfile;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    FirebaseUser mUser;

    String userId,name,mobile,state,city,country,address;
    Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack = findViewById(R.id.btnBack);
        btnLocation = findViewById(R.id.btnLocation);
        userProfileImage = findViewById(R.id.userProfileImage);
        inputName = findViewById(R.id.inputName);
        inputMobileNumber = findViewById(R.id.inputMobileNumber);
        inputCity = findViewById(R.id.inputCity);
        inputState = findViewById(R.id.inputState);
        inputCountry = findViewById(R.id.inputCountry);
        inputAddress = findViewById(R.id.inputAddress);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mUser = mAuth.getCurrentUser();

        if (mUser!=null)
        {
            userId = mUser.getUid();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        getUserData();
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==100 && resultCode==RESULT_OK && data!=null)
        {
            filePath = data.getData();
        }

    }

    private void validateData()
    {
         name = inputName.getText().toString().trim();
         mobile = inputMobileNumber.getText().toString().trim();
         city = inputCity.getText().toString().trim();
         state = inputState.getText().toString().trim();
         country = inputCountry.getText().toString().trim();
         address = inputAddress.getText().toString().trim();

        if (name.isEmpty())
        {
            inputName.setError("Enter Name");
            inputName.requestFocus();
        }
        else if (name.length()<3)
        {
            inputName.setError("Name should be greater than 3 character");
            inputName.requestFocus();
        }
        else if (mobile.isEmpty())
        {
            inputMobileNumber.setError("Enter Mobile Number");
            inputMobileNumber.requestFocus();
        }
        else if (mobile.length()<10 || mobile.length()>13)
        {
                inputMobileNumber.setError("Enter Valid Mobile Number");
                inputMobileNumber.requestFocus();
         }
        else if (filePath!=null)
        {
            uploadImage();
        }
        else
        {
            updateProfile("noImage");
        }

    }

    private void uploadImage()
    {
        btnUpdateProfile.setEnabled(false);
        mStorageRef.child("userProfileImage").child(userId).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageDownloadUrl = uri.toString();
                            updateProfile(imageDownloadUrl);
                        }
                    });
                }
            }
        });
    }

    private void updateProfile(String imageDownloadUrl)
    {
        btnUpdateProfile.setEnabled(false);
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.USER_NAME,name);
        map.put(Constants.USER_MOBILE,mobile);
        map.put(Constants.CITY,city);
        map.put(Constants.STATE,state);
        map.put(Constants.COUNTRY,country);
        map.put(Constants.ADDRESS,address);
        if (imageDownloadUrl.equals("noImage"))
        {

        }
        else
        {
            map.put(Constants.USER_IMAGE,imageDownloadUrl);
        }
        mRef.child(Constants.USERS).child(userId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                btnUpdateProfile.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnUpdateProfile.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserData()
    {
        mRef.child(Constants.USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(Constants.USER_NAME).getValue(String.class);
                String mobile = dataSnapshot.child(Constants.USER_MOBILE).getValue(String.class);
                String city = dataSnapshot.child(Constants.CITY).getValue(String.class);
                String state = dataSnapshot.child(Constants.STATE).getValue(String.class);
                String country = dataSnapshot.child(Constants.COUNTRY).getValue(String.class);
                String image = dataSnapshot.child(Constants.USER_IMAGE).getValue(String.class);
                String address = dataSnapshot.child(Constants.ADDRESS).getValue(String.class);

                inputName.setText(name);
                inputMobileNumber.setText(mobile);
                inputCity.setText(city);
                inputState.setText(state);
                inputCountry.setText(country);
                inputAddress.setText(address);

                try {
                    Picasso.get().load(image).into(userProfileImage);
                }
                catch (Exception e)
                {
                    Picasso.get().load(R.drawable.person_male).into(userProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
