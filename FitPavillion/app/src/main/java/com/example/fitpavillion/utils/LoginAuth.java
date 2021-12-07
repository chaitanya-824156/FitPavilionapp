package com.example.fitpavillion.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fitpavillion.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executor;

public class LoginAuth implements Executor {
    private static final String TAG = "LoginAuth";
    private static LoginAuth mInstance;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    public LoginAuth() {
        getAuth();
    }

    public static LoginAuth getInstance() {
        if (mInstance == null) mInstance = new LoginAuth();
        return mInstance;
    }

//    public GoogleSignInClient getGoogleClient(){
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken( getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//    }

    public FirebaseAuth getAuth() {
        if (this.mAuth == null) mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }

    public void createAccount(String email, String password, Callback<FirebaseUser> callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this::execute, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            callback.result(user);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            callback.result(null);
                        }
                    }
                });
    }

    public void signIn(String email, String password, Callback<FirebaseUser> callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this::execute, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            callback.result(user);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            callback.result(null);
                        }
                    }
                });
    }


    public void firebaseAuthWithGoogle(String idToken, Callback<FirebaseUser> callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this::execute, new OnCompleteListener<AuthResult>() {
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

    public void signOut(Context context) {
        SharedPref.getInstance(context).clearData();
        mAuth.signOut();
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
