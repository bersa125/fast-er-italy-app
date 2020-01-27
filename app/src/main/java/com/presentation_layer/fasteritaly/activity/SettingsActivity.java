package com.presentation_layer.fasteritaly.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.activity.loading_dialog.Loading_Dialog;
import com.presentation_layer.fasteritaly.interactor.SettingsInteractor;
import com.presentation_layer.fasteritaly.presenter.SettingsPresenter;
import com.presentation_layer.fasteritaly.view.SettingsView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SettingsView {

    private List<WeakReference<Fragment>> fragList;

    private Toolbar toolbar;
    private View fragment;
    private int currentFragment;

    private SettingsPresenter presenter;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Set Portrait

        //Setup the back button and the toolbar
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        fragment=findViewById(R.id.fragment_placeholder);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Hides statusbar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        fragList=new LinkedList<>();
        currentFragment=-1;
        presenter=new SettingsPresenter(this,new SettingsInteractor());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentFragment==getIntent().getExtras().getInt("PAGE"))
                    onBackPressed();
                else
                    presenter.changeContent(MAIN);
            }
        });
        presenter.changeContent(getIntent().getExtras().getInt("PAGE"));

        if(getIntent().getExtras().get("PAGE_CURRENT")!=null){
            presenter.changeContent(getIntent().getExtras().getInt("PAGE_CURRENT"));
        }
    }


    @Override
    public void changeCurrentContent(int choice) {//getIntent().getExtras().getInt("PAGE")
        boolean doAnimation=true;
        boolean reverseAnimation=false;

        if(choice!=currentFragment) {
            settingsFragment=null;
            if(currentFragment==-1){
                doAnimation=false;
            }
            currentFragment = choice;
            getIntent().getExtras().putInt("PAGE_CURRENT",currentFragment);
            switch (choice) {
                case SettingsView.MAIN:
                    getSupportActionBar().setTitle(R.string.menu_settings);
                    if(doAnimation){
                        reverseAnimation=true;
                    }
                    break;
                case SettingsView.ADDRESSES:
                    getSupportActionBar().setTitle(R.string.menu_addresses);
                    break;
                case SettingsView.OPTIONS:
                    getSupportActionBar().setTitle(R.string.user_options);
                    break;
            }
        }
        if(settingsFragment==null){
            settingsFragment=SettingsFragment.newInstance(choice);
            if(doAnimation) {
                if(reverseAnimation){
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out).replace(fragment.getId(), settingsFragment).commit();
                }else {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out).replace(fragment.getId(), settingsFragment).commit();
                }
            }else{
                getSupportFragmentManager().beginTransaction().replace(fragment.getId(), settingsFragment).commit();
            }
        }
    }

    @Override
    public void populateAddresses() {
        presenter.onAddressUpdate();
    }

    @Override
    public void populateOptions() {
        presenter.onSettingsUpdate();
    }

    @Override
    public void updateOptions(Bundle options) {
        presenter.onSettingsModification(options);
    }

    @Override
    public void modifyAddress(String newAddress, int position) {
        Loading_Dialog.getInstance().showDialog(this,getString(R.string.while_modify));
        presenter.onAddressModify(newAddress,position);
    }

    @Override
    public void deleteAddress(int pos) {
        Loading_Dialog.getInstance().showDialog(this,getString(R.string.while_delete));
        presenter.onAddressDelete(pos);
    }

    @Override
    public void addAddress(String address) {
        Loading_Dialog.getInstance().showDialog(this,getString(R.string.while_create));
        presenter.onAddressInsertion(address);
    }

    @Override
    public void onAttachFragment (Fragment fragment) {
        super.onAttachFragment(fragment);
        fragList = new ArrayList<>();
        fragList.add(new WeakReference(fragment));
    }


    @Override
    public List<Object> getActiveFragments() {
        ArrayList<Object> ret = new ArrayList<>();
        for(WeakReference<Fragment> ref : fragList) {
            Fragment f = ref.get();
            if(f != null) {
                ret.add(f);
            }
        }
        return ret;
    }

    @Override
    public int getCurrentLoadedFragment() {
        return currentFragment;
    }

    @Override
    public Activity getActivity() {
        return this;
    }


    @Override
    public void showResultMessage(int msg) {
        Loading_Dialog.getInstance().dismissDialog();
        switch(msg){
            case ERROR:
                Toast.makeText(this,getString(R.string.error_submission),Toast.LENGTH_LONG).show();
                break;
            case REJECTED:
                Toast.makeText(this,getString(R.string.address_rejected),Toast.LENGTH_LONG).show();
                break;
            case ALL_DONE:
                Toast.makeText(this,getString(R.string.done),Toast.LENGTH_LONG).show();
                break;
        }
    }

}
