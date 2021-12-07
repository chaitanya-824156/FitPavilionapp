package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.fitpavillion.MainActivity;
import com.example.fitpavillion.R;
import com.example.fitpavillion.constants.CONSTANTS;
import com.example.fitpavillion.utils.Callback;
import com.example.fitpavillion.utils.LoginAuth;
import com.example.fitpavillion.utils.SharedPref;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static android.content.ContentValues.TAG;

public class LoginActivity extends AppCompatActivity {
    private LoginAuth loginAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private AppCompatEditText userName, password;
    private FirebaseAuth mAuth;
    private RadioGroup typeGroup;
    private String type;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginAuth = LoginAuth.getInstance();
        mAuth = loginAuth.getAuth();
        sharedPref = SharedPref.getInstance(this);

        userName = findViewById(R.id.email);
        password = findViewById(R.id.password);
        typeGroup = findViewById(R.id.login_rad_group);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public boolean validateFields() {
        if (userName.getText().toString().trim().equals("")) return false;
        if (password.getText().toString().trim().equals("") || userName.getText().toString().trim().length() < 6)
            return false;
        return true;
    }

    public void signInWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, CONSTANTS.RC_SIGN_IN);
    }

    public void firebaseLogin(View view) {
        if (validateFields()) {
            loginAuth.signIn(userName.getText().toString().trim(), password.getText().toString().trim(), this::updateUI);
        }
    }

    public void firebaseRegister(View view) {
        if (validateFields()) {
            loginAuth.createAccount(userName.getText().toString().trim(), password.getText().toString().trim(), this::updateUI);
        }
    }

    private void getProfileType() {
        int id = typeGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(id);
        type = radioButton.getText().toString();
        sharedPref.setProfileType(type);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == CONSTANTS.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken(), this::updateUI);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    public void firebaseAuthWithGoogle(String idToken, Callback<FirebaseUser> callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            callback.result(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            callback.result(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            getProfileType();
            startActivity(i);
            finish();
        } else {
            sharedPref.setProfileType(null);
        }
    }

}