package com.socialcodia.socialshopia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    //UI
    private EditText inputEmail, inputPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    //Firebase

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //UI init
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        //Firebase Init
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private void validateData()
    {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty())
        {
            inputEmail.setError("Enter Email Address");
            inputEmail.requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            inputEmail.setError("Enter Valid Email Address");
            inputEmail.requestFocus();
        }
        else if (password.isEmpty())
        {
            inputPassword.setError("Enter Password");
            inputPassword.requestFocus();
        }
        else if (password.length()<6)
        {
            inputPassword.setError("Password is less than 6 digit");
            inputPassword.requestFocus();
        }
        else
        {
            doLogin(email,password);
        }

    }

    private void doLogin(String email, String password)
    {
        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    sendToHome();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    btnLogin.setEnabled(true);
                    inputPassword.setError("Wrong Password");
                    inputPassword.requestFocus();
                }
                else if (e instanceof FirebaseAuthInvalidUserException)
                {
                    btnLogin.setEnabled(true);
                    inputEmail.setError("Email Not Registered");
                    inputEmail.requestFocus();
                }
                else
                {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Oops! Something went wrong. "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToHome()
    {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }
}
