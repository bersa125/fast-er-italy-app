package com.presentation_layer.fasteritaly.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import com.example.fasteritaly.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.interactor.SearchOnMapInteractor;
import com.presentation_layer.fasteritaly.presenter.SearchOnMapPresenter;
import com.presentation_layer.fasteritaly.view.SearchOnMapView;
import com.presentation_layer.fasteritaly.view.SettingsView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class SearchOnMapActivity extends AppCompatActivity implements SearchOnMapView {

    private String address_selected;
    private Spinner addr_spinner;
    private String[] spinner_content;
    private TextView toolbar_text;
    private AppBarLayout barLayout;
    //Tabbed view components
    private ViewPager view_pager;
    private TabLayout tabs;
    //Snackbar for address add
    private Snackbar address_lack;
    private Snackbar no_provider;
    private Snackbar noInternet;

    private AlertDialog alert;

    private List<WeakReference<Fragment>> fragList;
    private Location currentLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private BroadcastReceiver internetSignalReceiver;

    private SearchOnMapPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_on_map);

        addr_spinner = findViewById(R.id.adresses_spinner);
        toolbar_text = findViewById(R.id.title_tabbed_page);
        view_pager = findViewById(R.id.view_pager);
        barLayout=findViewById(R.id.appbar_layout_search_on_map);
        tabs = findViewById(R.id.tabs);
        address_lack = Snackbar.make(findViewById(R.id.view_pager), R.string.no_addresses_found, Snackbar.LENGTH_INDEFINITE);
        no_provider = Snackbar.make(findViewById(R.id.view_pager), R.string.no_gps_active, Snackbar.LENGTH_INDEFINITE);
        if (fragList == null)
            fragList = new ArrayList<>();
        spinner_content = new String[]{};
        address_selected= CommonAccessData.getInstance().getAddressInSearch();



        //Hides statusbar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        presenter = new SearchOnMapPresenter(this, new SearchOnMapInteractor());
        address_lack.setAction(R.string.set_an_address, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Launch the account activity to set addresses
                presenter.onActionRequest(CALL_ADD_NEW_ADDRESS, null);
            }
        });
        noInternet= Snackbar.make(view_pager, R.string.no_internet, Snackbar.LENGTH_INDEFINITE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                if (!SmartLocation.with(getBaseContext()).location().state().isAnyProviderAvailable()) {
                    if (!no_provider.isShown())
                        no_provider.show();
                } else {
                    if (no_provider.isShown())
                        no_provider.dismiss();
                }
                presenter.updateChildFragmentsOnPosition(addr_spinner.getSelectedItemPosition());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    locationManager.removeUpdates(locationListener);
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 700, locationListener);
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    locationManager.removeUpdates(locationListener);
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 700, locationListener);
                }
            }
        };

        presenter.onCreate();

    }



    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume(addr_spinner.getSelectedItemPosition());
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
        super.onBackPressed();
    }



    @Override
    public void setCorrectView(boolean searchType, boolean isAddressUsed) {
        //Title construction with colored text
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String pin = "\u25CF ";
        String titleshown="";
        SpannableString preamble = new SpannableString(pin);
        if (!searchType) {
            if (isAddressUsed) {
                titleshown=getString(R.string.tabbed_addr_bar_text_ER);
                preamble.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blueviolet,null)), 0, pin.length(), 0);
                addr_spinner.setVisibility(View.VISIBLE);
                //barLayout.setBackgroundColor(getResources().getColor(R.color.blueviolet,null));
                //tabs.setBackgroundColor(getResources().getColor(R.color.blueviolet,null));
                presenter.onSetAddresses(getResources().getString(R.string.no_addresses_found), getResources().getString(R.string.choose_an_address), spinner_content);
            } else {
                titleshown=getString(R.string.tabbed_normal_bar_text_ER);
                preamble.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.crimson,null)), 0, pin.length(), 0);
                addr_spinner.setVisibility(View.GONE);
                //barLayout.setBackgroundColor(getResources().getColor(R.color.crimson,null));
                //tabs.setBackgroundColor(getResources().getColor(R.color.crimson,null));
                if (address_lack.isShown()) {
                    address_lack.dismiss();
                }
            }
        } else {
            if (isAddressUsed) {
                titleshown=getString(R.string.tabbed_addr_bar_text_Drugs);
                preamble.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.lightseagreen,null)), 0, pin.length(), 0);
                addr_spinner.setVisibility(View.VISIBLE);
                //barLayout.setBackgroundColor(getResources().getColor(R.color.lightseagreen,null));
                //tabs.setBackgroundColor(getResources().getColor(R.color.lightseagreen,null));
                presenter.onSetAddresses(getResources().getString(R.string.no_addresses_found), getResources().getString(R.string.choose_an_address), spinner_content);
            } else {
                titleshown=getString(R.string.tabbed_normal_bar_text_Drugs);
                preamble.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.darkorange,null)), 0, pin.length(), 0);
                addr_spinner.setVisibility(View.GONE);
                //barLayout.setBackgroundColor(getResources().getColor(R.color.darkorange,null));
                //tabs.setBackgroundColor(getResources().getColor(R.color.darkorange,null));
                if (address_lack.isShown()) {
                    address_lack.dismiss();
                }
            }
        }
        preamble.setSpan(new RelativeSizeSpan(1.3f), 0,1, 0);
        builder.append(preamble);
        SpannableString title = new SpannableString(titleshown);
        title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white,null)), 0, getString(R.string.title_activity_history).length(), 0);
        builder.append(title);
        toolbar_text.setText(builder);
    }

    @Override
    public void setRegisteredAddresses(String[] addresses) {
        spinner_content = addresses;
        String address_previously_selected=CommonAccessData.getInstance().getAddressInSearch();

        ArrayAdapter<String> AddressesArray = new ArrayAdapter<>(this, R.layout.address_spinner_item_layout, addresses);
        AddressesArray.setDropDownViewResource(R.layout.address_spinner_dropdown_item_layout);

        addr_spinner.setAdapter(AddressesArray);
        addr_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    address_selected = (String) addr_spinner.getSelectedItem();
                    CommonAccessData.getInstance().setAddressInSearch(address_selected);
                    presenter.updateChildFragmentsOnPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        try {
            addr_spinner.setSelection(getIndex(addr_spinner, address_previously_selected));
            presenter.updateChildFragmentsOnPosition(getIndex(addr_spinner,address_previously_selected));
            CommonAccessData.getInstance().setAddressInSearch(address_previously_selected);
            address_selected=address_previously_selected;
        }catch(Exception e){
            address_selected=null;
            CommonAccessData.getInstance().setAddressInSearch(address_selected);
        }
        if (AddressesArray.getCount() == 1) {
            address_lack.show();
        }
    }

    @Override
    public void setTabbedViewAdapter(final int[] Tabs) {
        final Context mContext = this;
        view_pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return OnSearchMap_Map_Fragment.newInstance();
                } else {
                    return OnSearchMap_List_Fragment.newInstance();
                }
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return mContext.getResources().getString(Tabs[position]);
            }

            @Override
            public int getCount() {
                return Tabs.length;
            }
        });
        tabs.setupWithViewPager(view_pager);
    }

    @Override
    public void launchDetailsActivity(Bundle object) {
        Intent intent=new Intent(this,PlaceInfoActivity.class);
        intent.putExtra("PLACE",object);
        startActivity(intent);
    }

    @Override
    public void launchGoogleMapsIntent(Bundle object) {
        try {
            if (object.getString("CURRENT_ADDRESS") != null) {
                if (!object.getString("CURRENT_ADDRESS").contains("null") && !object.getString("CURRENT_ADDRESS").contains("Null") && !object.getString("CURRENT_ADDRESS").contains("Unnamed")) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + URLEncoder.encode(object.getString("CURRENT_ADDRESS"), "utf-8") + "&destination=" + URLEncoder.encode(object.getString("ADDRESS"), "utf-8")+(this.getOnAddress() ? "" : "&dir_action=navigate")));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                } else {
                    if (object.get("CURRENT_LAT") != null && object.get("CURRENT_LONG") != null) {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + object.getDouble("CURRENT_LAT") + "," + object.getDouble("CURRENT_LONG") + "&destination=" + URLEncoder.encode(object.getString("ADDRESS", "utf-8")+(this.getOnAddress() ? "" : "&dir_action=navigate"))));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                    }
                }
            } else {
                if (object.get("CURRENT_LAT") != null && object.get("CURRENT_LONG") != null) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + object.getDouble("CURRENT_LAT") + "," + object.getDouble("CURRENT_LONG") + "&destination=" + URLEncoder.encode(object.getString("ADDRESS", "utf-8")+(this.getOnAddress() ? "" : "&dir_action=navigate"))));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_maps), Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void launchAddressActivity() {
        Intent intent=new Intent(this,SettingsActivity.class);
        intent.putExtra("PAGE", SettingsView.ADDRESSES);
        startActivity(intent);
    }

    @Override
    public void launchChooseActionDialog(final Bundle object) {
        //Toast.makeText(this,"OK",Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_map_choice, null);
        ((Button) dialogView.findViewById(R.id.details_dialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                presenter.onActionRequest(CALL_LOCATION_INFORMATIONS, object);
            }
        });
        ((Button) dialogView.findViewById(R.id.maps_navigation_dialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                presenter.onActionRequest(CALL_GOOGLE_MAPS_INTENT, object);
            }
        });
        builder.setView(dialogView);
        builder.setCancelable(true);
        alert = builder.create();
        alert.show();

    }

    @Override
    public void setSelectedItem(Bundle object) {
        presenter.updateChildFragmentsOnSelectedPoint(object);
    }


    @Override
    public String getCurrentAddress() {
        if (addr_spinner.getSelectedItemPosition() > 0)
            return addr_spinner.getSelectedItem().toString();
        else
            return null;
    }

    @Override
    public void launchGPSRecognition() {
        if (!this.getOnAddress()) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            currentLocation = SmartLocation.with(getBaseContext()).location().getLastLocation();
            SmartLocation.with(getBaseContext()).location().oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            currentLocation = location;
                            if (!SmartLocation.with(getBaseContext()).location().state().isAnyProviderAvailable()) {
                                if (!no_provider.isShown())
                                    no_provider.show();
                            } else {
                                if (no_provider.isShown())
                                    no_provider.dismiss();
                            }
                            presenter.updateChildFragmentsOnPosition(addr_spinner.getSelectedItemPosition());
                            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }else {
                                if(SmartLocation.with(getBaseContext()).location().state().isGpsAvailable()){
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 700, locationListener);
                                }else{
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 700, locationListener);
                                }
                            }
                        }
                    });
            if (!SmartLocation.with(getBaseContext()).location().state().isAnyProviderAvailable()) {
                if (!no_provider.isShown())
                    no_provider.show();
            } else {
                if (no_provider.isShown())
                    no_provider.dismiss();
            }


        }
    }

    @Override
    public void stopGPSRecognition() {
        if(!this.getOnAddress()) {
            SmartLocation.with(getBaseContext()).location().continuous().stop();
            try {
                locationManager.removeUpdates(locationListener);
            }catch(Exception e){}
        }
    }

    @Override
    public Location getCurrentGPSPosition() {
        return currentLocation;
    }

    @Override
    public boolean getType() {
        return getIntent().getExtras().getBoolean("SEARCH_TYPE");
    }

    @Override
    public boolean getOnAddress() {
        return getIntent().getExtras().getBoolean("ON_ADDRESS");
    }

    @Override
    public Object getContext() {
        return this;
    }

    @Override
    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted

            if(!this.getOnAddress()){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this,R.string.fuctions_not_available_message,Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this,R.string.activate_gps_message,Toast.LENGTH_LONG).show();
                }
                this.onBackPressed();
            }
        }

    }

    @Override
    public void checkInternetFunctionality() {
        //Setup the internet checker
        if(internetSignalReceiver==null) {
            internetSignalReceiver= new BroadcastReceiver() {
                private boolean firstPassed=false;
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
            registerReceiver(internetSignalReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())){//Connection not Active
            try {
                noInternet.dismiss();
            }catch(Exception e){}
            Toast.makeText(getBaseContext(),getString(R.string.fuctions_not_available_message),Toast.LENGTH_LONG).show();
            onBackPressed();
        }
    }

    @Override
    public void stopcheckInternetFunctionality() {
        try {
            unregisterReceiver(internetSignalReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public void requestCheckForNearAddresses(boolean type, LatLng coordinates, String address) {
        presenter.onChildRequest(type,coordinates,address);
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
                ret.add(f);
            }
        }
        return ret;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_FOREGROUND_SERVICE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    presenter.launchService();
                }
                break;
            }
        }
    }

    private int getIndex(Spinner spinner, String myString){
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }


}