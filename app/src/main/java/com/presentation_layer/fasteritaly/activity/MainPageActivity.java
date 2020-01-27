package com.presentation_layer.fasteritaly.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.fasteritaly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.interactor.MainPageInteractor;
import com.presentation_layer.fasteritaly.presenter.MainPagePresenter;
import com.presentation_layer.fasteritaly.view.MainPageView;

import java.util.Map;

public class MainPageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainPageView {

    private MainPagePresenter presenter;
    //maintain the actual state of the user login
    private int log_status= RESULT_CANCELED;
    //Authenticator
    private FirebaseAuth mAuth;

    private BroadcastReceiver internetSignalReceiver;
    private Snackbar noGPS;
    private Snackbar noInternet;

    private boolean RequestFirst;
    private int RqCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.home);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        findViewById(R.id.progressBar_layout).setVisibility(View.INVISIBLE);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.app_name,R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //Hides statusbar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        noGPS= Snackbar.make(findViewById(R.id.toolbar), R.string.no_gps_permission, Snackbar.LENGTH_INDEFINITE);
        noInternet= Snackbar.make(findViewById(R.id.toolbar), R.string.no_internet, Snackbar.LENGTH_INDEFINITE);
        RequestFirst=true;
        RqCounter=0;
        presenter=new MainPagePresenter(this,new MainPageInteractor());
        presenter.onCreate();
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        //Takes the user's metadata
        presenter.onStart(currentUser);
        if(currentUser!=null) {
            if (!currentUser.isEmailVerified()) {
                /* Send Verification Email */
                currentUser.sendEmailVerification()
                        .addOnCompleteListener(this, new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                /* Check Success */
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.mail_not_verified) + " " + currentUser.getEmail(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Failed To Send Verification Email!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        presenter.onScreenChange();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //findViewById(R.id.progressBar_layout).setVisibility(View.VISIBLE);
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            presenter.onScreenChange();
        } else {
            presenter.onCloseupRequest();
        }
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.account_drawer) {
            presenter.onActivityLaunchRequest(ADDRESSES_ACTIVITY);
        } else if (id == R.id.settings_drawer) {
            presenter.onActivityLaunchRequest(SETTINGS_ACTIVITY);
        } else if (id == R.id.history_drawer) {
            presenter.onActivityLaunchRequest(HISTORY_ACTIVITY);
        } else if (id == R.id.disconnect_drawer) {
            presenter.onDisconnectRequest(log_status);
        } else if (id == R.id.quit_drawer){
            presenter.onExitRequest();
        }
        presenter.onScreenChange();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_LOGIN) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            presenter.onStart(currentUser);
        }
    }

    @Override
    public void launchActivity(int i) {
        switch(i){
            case HISTORY_ACTIVITY:
                startActivity(presenter.onIntentCreation(new Intent(this, HistoryActivity.class),i));
                break;
            case ER_NEXT_ME_ACTIVITY:
                startActivity(presenter.onIntentCreation(new Intent(this, SearchOnMapActivity.class),i));
                break;
            case ER_NEXT_ADDR_ACTIVITY:
                startActivity(presenter.onIntentCreation(new Intent(this, SearchOnMapActivity.class),i));
                break;
            case DRUG_NEXT_ME_ACTIVITY:
                startActivity(presenter.onIntentCreation(new Intent(this, SearchOnMapActivity.class),i));
                break;
            case DRUG_NEXT_ADDR_ACTIVITY:
                startActivity(presenter.onIntentCreation(new Intent(this, SearchOnMapActivity.class),i));
                break;
            case SETTINGS_ACTIVITY:
                startActivity(presenter.onIntentCreation(new Intent(this,SettingsActivity.class),i));
                break;
            case ADDRESSES_ACTIVITY:
                startActivity(presenter.onIntentCreation(new Intent(this ,SettingsActivity.class),i));
                break;
            case LOGIN_ACTIVITY:
                startActivityForResult(presenter.onIntentCreation(new Intent(this ,SplashLoginActivity.class),i), REQUEST_LOGIN);
                break;
            case SYSTEM_SETTINGS_PERMISSIONS_APPLICATION:
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                Toast.makeText(getBaseContext(), R.string.activate_permissions_toast,Toast.LENGTH_LONG).show();
                startActivity(presenter.onIntentCreation(intent,i));
                break;
        }
    }

    @Override
    public void closeApp() {
        moveTaskToBack(true);
    }

    @Override
    public void exitApp() {
        finishAndRemoveTask();
    }

    @Override
    public void disconnect() {
        presenter.onDisconnectRequest(this.log_status);
    }

    @Override
    public void changeAspect() {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.getSupportActionBar().hide();

        } else {
            this.getSupportActionBar().show();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void updateDrawerName(String name) {
        ((TextView) ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.main_header_drawer_textName)).setText(name);
    }

    @Override
    public void updateDrawerMail(String mail) {
        ((TextView) ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.main_header_drawer_textMail)).setText(mail);
    }

    @Override
    public void updateDrawerPhoto(Uri photo) {
        try {
            if(photo==null) throw  new RuntimeException("No photo");
            Glide.with(this).load(photo).into(((ImageView) ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.main_header_drawer_profileImageView)));
        } catch (Exception e) {
            ((ImageView) ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.main_header_drawer_profileImageView)).setImageResource(R.drawable.red_cross_basic_account_photo);
        }
    }

    @Override
    public void unlockScreenLoading(String message) {
        findViewById(R.id.progressBar_layout).setVisibility(View.INVISIBLE);
        if(message!=null && CommonAccessData.getInstance().isAuthHappened()){
            Toast t =Toast.makeText(this,getString(R.string.error_settings1)+message+getString(R.string.error_settings2),Toast.LENGTH_LONG);
            t.show();
        }
    }

    @Override
    public void checkPermissions() {
        //permission
        final AppCompatActivity T = this;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // Permission is not granted
            if (RequestFirst || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    noGPS.setAction(R.string.add_permission, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(T,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.FOREGROUND_SERVICE},
                                    REQUEST_PERMISSION_FINE_LOCATION);
                        }
                    });
                }else{
                    noGPS.setAction(R.string.add_permission, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(T,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSION_FINE_LOCATION);
                        }
                    });
                }
                if(RqCounter>=4)
                    RequestFirst = false;
                RqCounter++;
                noGPS.show();
            } else {
                // No explanation needed; request the permission
                noGPS.setAction(R.string.add_permission, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        presenter.onPermissionSettingsCall();
                    }
                });
                noGPS.show();
            }
        }
    }

    @Override
    public void checkInternetFunctionality() {
        //Setup the internet checker
        if(internetSignalReceiver==null) {
            internetSignalReceiver= new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager connectivityManager
                            = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {//Connection not Active
                        noInternet.show();
                    }else{
                        noInternet.dismiss();
                    }
                }
            };
            try {
                registerReceiver(internetSignalReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }catch(Exception e){}
        }

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())){//Connection not Active
            noInternet.show();
        }
    }

    @Override
    public void stopcheckInternetFunctionality() {
        try {
            unregisterReceiver(internetSignalReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getActivity() {
        return this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    noGPS.dismiss();
                } else {
                    // denied
                    noGPS.show();
                }
                break;
            }
        }
    }

    @Override
    public Intent addExtraToIntent(Map<String, Boolean> values, Intent intent) {
        for(String key :values.keySet()){
            intent.putExtra(key,values.get(key));
        }
        return intent;
    }
}
