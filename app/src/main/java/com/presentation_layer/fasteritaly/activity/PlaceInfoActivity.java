package com.presentation_layer.fasteritaly.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fasteritaly.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.presentation_layer.fasteritaly.activity.loading_dialog.Loading_Dialog;
import com.presentation_layer.fasteritaly.interactor.PlaceInfoInteractor;
import com.presentation_layer.fasteritaly.presenter.PlaceInfoPresenter;
import com.presentation_layer.fasteritaly.view.PlaceInfoFragmentView;
import com.presentation_layer.fasteritaly.view.PlaceInfoView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PlaceInfoActivity extends AppCompatActivity implements PlaceInfoView {

    private List<WeakReference<Fragment>> fragList;

    private AppBarLayout appbar_layout;
    private FloatingActionButton vote_button;
    private Toolbar toolbar;

    private ImageView place_photo;
    private TextView address_title;
    private View address_fragment_view;
    private TextView queue_title;
    private View queue_fragment_view;
    private TextView vote_title;
    private View vote_fragment_view;
    private TextView open_title;
    private View open_fragment_view;

    private Snackbar noInternet;
    private BroadcastReceiver internetSignalReceiver;

    private AlertDialog alert;

    private PlaceInfoPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_info);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        appbar_layout=findViewById(R.id.app_bar);
        vote_button=findViewById(R.id.vote_er_button);

        View content=findViewById(R.id.page_content);
        place_photo=findViewById(R.id.place_photo);
        address_title=content.findViewById(R.id.place_address_title);
        address_fragment_view=content.findViewById(R.id.place_address_fragment);
        queue_title=content.findViewById(R.id.er_queue_state_title);
        queue_fragment_view=content.findViewById(R.id.er_queue_state_fragment);
        vote_title=content.findViewById(R.id.er_vote_title);
        vote_fragment_view=content.findViewById(R.id.er_vote_fragment);
        open_title =content.findViewById(R.id.drug_opening_time_title);
        open_fragment_view=content.findViewById(R.id.drug_opening_time_fragment);

        noInternet= Snackbar.make(content, R.string.no_internet, Snackbar.LENGTH_INDEFINITE);

        //Hides statusbar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fragList=new ArrayList<>();
        if(presenter==null){
            presenter=new PlaceInfoPresenter(this,new PlaceInfoInteractor());
        }
        //Automatic configuration
        presenter.onPopulate(getIntent().getBundleExtra("PLACE"));
    }

    @Override
    protected void onResume(){
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause(){
        presenter.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void setTitleAndTheme(Bundle object) {
        // with colors
        if(!getIntent().getBundleExtra("PLACE").getBoolean("TYPE")){//ER

            getSupportActionBar().setTitle(getString(R.string.ER_details_title));
            //appbar_layout.setBackgroundColor(getResources().getColor(R.color.quantum_googred800,null));
        }else{//Pharmacy
            getSupportActionBar().setTitle(R.string.DRUG_details_title);
            //appbar_layout.setBackgroundColor(getResources().getColor(R.color.quantum_yellow800,null));
        }
        //appbar_layout.setBackgroundColor(getResources().getColor(R.color.olivedrab,null));
        //getSupportActionBar().setTitle("TEST");
    }

    @Override
    public void setPlaceImage(Bundle object, boolean isInvisible) {
        ConstraintLayout.LayoutParams params=this.copyParams(place_photo);
        if(isInvisible){
            place_photo.setVisibility(View.INVISIBLE);
            params.height=0;
            place_photo.setLayoutParams(params);
        }else{
            place_photo.setVisibility(View.VISIBLE);
            params.height= LayoutParams.WRAP_CONTENT;
            place_photo.setLayoutParams(params);
            //Bitmap image=(Bitmap) object.get("PLACE_PHOTO");
            //place_photo.setImageBitmap(image);
            Glide.with(this).load(object.getString("PLACE_PHOTO")).into(place_photo);
        }
    }

    @Override
    public void setAddressContent(Bundle object, boolean isInvisible) {
        ConstraintLayout.LayoutParams params1=this.copyParams(address_title);
        ConstraintLayout.LayoutParams params2=this.copyParams(address_fragment_view);
        if(isInvisible){
            params1.height=0;
            params2.height=0;
            address_title.setVisibility(View.INVISIBLE);
            address_title.setLayoutParams(params1);
            address_fragment_view.setVisibility(View.INVISIBLE);
            address_fragment_view.setLayoutParams(params2);
        }else{
            params1.height= LayoutParams.WRAP_CONTENT;
            params2.height= LayoutParams.WRAP_CONTENT;
            address_title.setVisibility(View.VISIBLE);
            address_title.setLayoutParams(params1);
            address_fragment_view.setVisibility(View.VISIBLE);
            address_fragment_view.setLayoutParams(params2);
            Bundle objectNew=new Bundle(object);
            objectNew.putInt("FRAGMENT_TYPE", PlaceInfoFragmentView.TYPE_NAME_ADDRESS);
            getSupportFragmentManager().beginTransaction().replace(address_fragment_view.getId(),PlaceInfoFragment.newInstance(objectNew)).commit();
        }
    }

    @Override
    public void setVotesContent(Bundle object, boolean isInvisible) {
        ConstraintLayout.LayoutParams params1=this.copyParams(vote_title);
        ConstraintLayout.LayoutParams params2=this.copyParams(vote_fragment_view);
        if(isInvisible){
            params1.height=0;
            params2.height=0;
            vote_title.setVisibility(View.INVISIBLE);
            vote_title.setLayoutParams(params1);
            vote_fragment_view.setVisibility(View.INVISIBLE);
            vote_fragment_view.setLayoutParams(params2);
            vote_button.hide();
        }else{
            params1.height= LayoutParams.WRAP_CONTENT;
            params2.height= LayoutParams.WRAP_CONTENT;
            vote_title.setVisibility(View.VISIBLE);
            vote_title.setLayoutParams(params1);
            vote_fragment_view.setVisibility(View.VISIBLE);
            vote_fragment_view.setLayoutParams(params2);
            vote_button.show();
            vote_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onShowSubmissionVote();
                }
            });
            Bundle objectNew=new Bundle(object);
            objectNew.putInt("FRAGMENT_TYPE", PlaceInfoFragmentView.TYPE_ER_GENERAL_VOTES);
            getSupportFragmentManager().beginTransaction().replace(vote_fragment_view.getId(),PlaceInfoFragment.newInstance(objectNew)).commit();
        }
    }

    @Override
    public void setERQueueState(Bundle object, boolean isInvisible) {
        ConstraintLayout.LayoutParams params1=this.copyParams(queue_title);
        ConstraintLayout.LayoutParams params2=this.copyParams(queue_fragment_view);
        if(isInvisible){
            params1.height=0;
            params2.height=0;
            queue_title.setVisibility(View.INVISIBLE);
            queue_title.setLayoutParams(params1);
            queue_fragment_view.setVisibility(View.INVISIBLE);
            queue_fragment_view.setLayoutParams(params2);
        }else{
            params1.height= LayoutParams.WRAP_CONTENT;
            params2.height= LayoutParams.WRAP_CONTENT;
            queue_title.setVisibility(View.VISIBLE);
            queue_title.setLayoutParams(params1);
            queue_fragment_view.setVisibility(View.VISIBLE);
            queue_fragment_view.setLayoutParams(params2);
            Bundle objectNew=new Bundle(object);
            objectNew.putInt("FRAGMENT_TYPE", PlaceInfoFragmentView.TYPE_STATE_ER);
            getSupportFragmentManager().beginTransaction().replace(queue_fragment_view.getId(),PlaceInfoFragment.newInstance(objectNew)).commit();
        }
    }

    @Override
    public void setDrugOpeningState(Bundle object, boolean isInvisible) {
        ConstraintLayout.LayoutParams params1=this.copyParams(open_title);
        ConstraintLayout.LayoutParams params2=this.copyParams(open_fragment_view);
        if(isInvisible){
            params1.height=0;
            params1.topToBottom=queue_fragment_view.getId();
            params2.height=0;
            open_title.setVisibility(View.INVISIBLE);
            open_title.setLayoutParams(params1);
            open_fragment_view.setVisibility(View.INVISIBLE);
            open_fragment_view.setLayoutParams(params2);
        }else{
            params1.height= LayoutParams.WRAP_CONTENT;
            params1.topToBottom=address_fragment_view.getId();
            params2.height= LayoutParams.WRAP_CONTENT;
            open_title.setVisibility(View.VISIBLE);
            open_title.setLayoutParams(params1);
            open_fragment_view.setVisibility(View.VISIBLE);
            open_fragment_view.setLayoutParams(params2);
            Bundle objectNew=new Bundle(object);
            objectNew.putInt("FRAGMENT_TYPE", PlaceInfoFragmentView.TYPE_DRUG_OPENING_TIME);
            getSupportFragmentManager().beginTransaction().replace(open_fragment_view.getId(),PlaceInfoFragment.newInstance(objectNew)).commit();
        }
    }

    @Override
    public void requestUpdate(Bundle object) {
        if(!noInternet.isShown()) {
            Loading_Dialog.getInstance().showDialog(this,getString(R.string.while_modify));
            presenter.onRequestUpdate(object);
        }
    }

    @Override
    public void showVoteInsertionDialog() {
        if(!noInternet.isShown()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_vote_choice, null);
            final RatingBar rate_wait=(RatingBar) dialogView.findViewById(R.id.ratingBar_wait);
            final RatingBar rate_struct=(RatingBar) dialogView.findViewById(R.id.ratingBar_structure);
            final RatingBar rate_service=(RatingBar) dialogView.findViewById(R.id.ratingBar_service);
            final Activity activity=this;
            ((Button) dialogView.findViewById(R.id.submit_vote_button)).setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                    alert.dismiss();
                    if(!noInternet.isShown()) {
                        Loading_Dialog.getInstance().showDialog(activity,getString(R.string.while_create));
                        presenter.onSubmitVote(getIntent().getBundleExtra("PLACE"), new int[]{(int) rate_wait.getRating(), (int) rate_struct.getRating(), (int) rate_service.getRating()});
                    }
                }
            });
            builder.setView(dialogView);
            builder.setCancelable(true);
            alert = builder.create();
            alert.show();
        }

    }

    @Override
    public void ShowUserVoteSubmissionMessageResult(int result) {
        Loading_Dialog.getInstance().dismissDialog();
        switch (result){
            case PlaceInfoView.ALL_DONE:
                Toast.makeText(this,getString(R.string.vote_submitted),Toast.LENGTH_LONG).show();
                break;
            case PlaceInfoView.ERROR:
                Toast.makeText(this,getString(R.string.error_submission),Toast.LENGTH_LONG).show();
                break;
            case PlaceInfoView.REJECTED:
                Toast.makeText(this,getString(R.string.vote_rejected),Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onAttachFragment (Fragment fragment) {
        super.onAttachFragment(fragment);
        if(fragList==null)
            fragList = new ArrayList<>();
        fragList.add(new WeakReference(fragment));
    }

    @Override
    public List<Object> getActiveFragments() {
        ArrayList<Object> ret = new ArrayList<>();
        for(WeakReference<Fragment> ref : fragList) {
            Fragment f = ref.get();
            if(f != null) {
                if(f.isVisible()) {
                    ret.add(f);
                }
            }
        }
        return ret;
    }

    @Override
    public boolean getDetailsType() {
        return getIntent().getBundleExtra("PLACE").getBoolean("TYPE");
    }

    @Override
    public Context getContext() {
        return this;
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

    private ConstraintLayout.LayoutParams copyParams(View v){
        return new ConstraintLayout.LayoutParams((ConstraintLayout.LayoutParams) v.getLayoutParams());
    }
}
