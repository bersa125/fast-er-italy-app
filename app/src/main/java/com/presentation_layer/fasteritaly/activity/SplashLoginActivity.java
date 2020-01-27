package com.presentation_layer.fasteritaly.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.fasteritaly.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.presentation_layer.fasteritaly.interactor.SplashLoginInteractor;
import com.presentation_layer.fasteritaly.presenter.SplashLoginPresenter;
import com.presentation_layer.fasteritaly.view.SplashLoginView;

import java.util.Arrays;
import java.util.List;

public class SplashLoginActivity extends AppCompatActivity implements SplashLoginView {

    private List<AuthUI.IdpConfig> authProviders = Arrays.asList(
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            //new AuthUI.IdpConfig.FacebookBuilder().build(),
            new AuthUI.IdpConfig.EmailBuilder().build()
            /*new AuthUI.IdpConfig.PhoneBuilder().build()
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            ,
            new AuthUI.IdpConfig.TwitterBuilder().build()*/);

    private SplashLoginPresenter presenter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_login);

        //Hides statusbar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Hides ActionBar
        getSupportActionBar().hide();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        presenter = new SplashLoginPresenter(this, new SplashLoginInteractor());
        presenter.onCreate();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        //System.err.println(requestCode+" "+resultCode+" "+RESULT_OK);
        if (requestCode == REQUEST_SPLASH_LOGIN) {
            if (resultCode == RESULT_OK) {
                final FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null)
                    Toast.makeText(getApplicationContext(), getString(R.string.auth_error), Toast.LENGTH_SHORT).show();
                presenter.onReturnToMain();
            } else {
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (response == null) { // handles BackPressure situation on the AuthUI
                    presenter.onAuthenticationRequest();
                    presenter.onCloseupRequest();
                } else {
                    presenter.onAuthenticationRequest();
                }

            }

        }
    }

    @Override
    public void closeApp() {
        moveTaskToBack(true);
    }

    @Override
    public void callAuthentication() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(authProviders)
                        .setLogo(R.drawable.pin_map_cross_1)
                        .setIsSmartLockEnabled(false)
                        .setTheme(R.style.AppTheme_Dark_ActionBar)
                        .build(),
                REQUEST_SPLASH_LOGIN);
    }

    @Override
    public void revokeConnection() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        presenter.onAuthenticationRequest();
                    }
                });
    }

    @Override
    public void returnToMain(int result) {
        setResult(result);
        finish();
    }
}
