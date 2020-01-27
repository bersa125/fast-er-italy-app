package com.presentation_layer.fasteritaly.activity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.content.ContentResolver;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.fasteritaly.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.activity.photo_adapter.CircleBubbleTransformation;
import com.presentation_layer.fasteritaly.interactor.SearchOnMapFragmentInteractor;
import com.presentation_layer.fasteritaly.presenter.SearchOnMapFragmentPresenter;
import com.presentation_layer.fasteritaly.view.SearchOnMapFragmentView;
import com.presentation_layer.fasteritaly.view.SearchOnMapView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OnSearchMap_Map_Fragment extends Fragment implements OnMapReadyCallback, SearchOnMapFragmentView, GoogleMap.OnInfoWindowCloseListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnMarkerClickListener {


    private static final int CENTER_TYPE_POINT = 0;
    private static final int ER_TYPE_POINT=1;
    private static final int DRUG_TYPE_POINT=2;

    private Map<LatLng,Bundle> resultPoints;
    private Map<LatLng,Marker> resultMarkersMap;
    private List<Marker> resultMarkers;
    private LatLng centerCoordinates;
    private String centerAddress;

    private LatLng defaultCenter=new LatLng(42.42322,12.11141);
    private int defaultAnimationSpeed=1600;
    private int defaultCountryzoom=5;
    private int defaultCenterZoom=17;

    private View thisView;
    private View mProgressBar;
    private View mSmallProgressBar;

    private GoogleMap mMap;

    private SearchOnMapFragmentPresenter presenter;


    public OnSearchMap_Map_Fragment(){

    }

    public static OnSearchMap_Map_Fragment newInstance() {
        OnSearchMap_Map_Fragment fragment = new OnSearchMap_Map_Fragment();
        return fragment;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(presenter==null){
            presenter=new SearchOnMapFragmentPresenter(this,new SearchOnMapFragmentInteractor());
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView=inflater.inflate(R.layout.fragment_search_on_map_map, container, false);
        mProgressBar = thisView.findViewById(R.id.map_progressBar);
        mSmallProgressBar = thisView.findViewById(R.id.map_progressBarSmall);
        resultPoints=new HashMap<>();
        resultMarkersMap=new HashMap<>();
        resultMarkers=new ArrayList<>();
        showProgressBar(false);
        //Here all the Configs
        MapView mMapView = thisView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);

        if(presenter==null){
            presenter=new SearchOnMapFragmentPresenter(this,new SearchOnMapFragmentInteractor());
        }
        return thisView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.onDetach();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        hideProgressBar(false);
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);
        //Startup Setup
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultCenter));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(defaultCountryzoom));
    }

    @Override
    public void clearResults() {
        try {
            resultPoints.clear();
            resultMarkersMap.clear();
            resultMarkers.clear();
            if (mMap != null) {
                mMap.clear();
                //Camera Animation
                AnimateCameraOnPoint(defaultCenter, defaultCountryzoom);
            }
        }catch(Exception e){}
    }

    @Override
    public void addResult(Bundle data) {
        LatLng position=new LatLng(data.getDouble("LAT"),data.getDouble("LONG"));
        resultPoints.put(position,data);
        AddMarker(position,data.getBoolean("TYPE")? DRUG_TYPE_POINT:ER_TYPE_POINT);
    }

    @Override
    public void addResults(List<Bundle> data) {
        this.clearResults();
        //Camera animation
        if(centerCoordinates!=null) {//Controllo su chiamate concorrenti
            if(mMap!=null)
                mMap.clear();
            AnimateCameraOnPoint(centerCoordinates, defaultCenterZoom);
            AddMarker(centerCoordinates,CENTER_TYPE_POINT);
        }
        for(Bundle b : data){
            LatLng position=new LatLng(b.getDouble("LAT"),b.getDouble("LONG"));
            resultPoints.put(position,b);
            AddMarker(position,b.getBoolean("TYPE")? DRUG_TYPE_POINT:ER_TYPE_POINT);
        }
    }

    @Override
    public void setCenterPosition(String address) {
        presenter.onCentralLocationSearchRequest(address);
    }

    @Override
    public void setCenterPosition(Location coordinates) {
        presenter.onCentralLocationSearchRequest(coordinates);
    }

    @Override
    public void setCenterPosition(LatLng coordinates, String address) {
        centerCoordinates = coordinates;
        centerAddress = address;
        //Camera animation
        if(centerCoordinates!=null) {//Controllo su chiamate concorrenti
            if(mMap!=null)
                mMap.clear();
            AnimateCameraOnPoint(centerCoordinates, defaultCenterZoom);
            AddMarker(centerCoordinates,CENTER_TYPE_POINT);
        }
        presenter.onCentralLocationSet();

    }

    @Override
    public void onItemSelected(Bundle object) {
        if(object!=null) {
            LatLng coordinates = new LatLng(object.getDouble("LAT"), object.getDouble("LONG"));
            if (resultMarkersMap.get(coordinates) != null) {
                AnimateCameraOnPoint(coordinates, mMap.getCameraPosition().zoom);
                resultMarkersMap.get(coordinates).showInfoWindow();
            }
        }else{
            for(Marker m:resultMarkers){
                m.hideInfoWindow();
            }
            AdjustZoomOnMarkers();
        }
    }

    @Override
    public void showLoadingBar(boolean status,boolean small) {
        if(status){
            showProgressBar(small);
        }else{
            hideProgressBar(small);
        }
    }

    @Override
    public LatLng getCenterCoordinates() {
        return centerCoordinates;
    }

    @Override
    public String getCenterAddress() {
        return centerAddress;
    }

    @Override
    public Object getAttachedView() {
        return this.getActivity();
    }
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
    @Override
    public View getInfoContents(Marker marker) {
        if(marker.getTitle()==null) {
            View markerPopupView = getActivity().getLayoutInflater().inflate(R.layout.info_view_maps_pin, null);

            ConstraintLayout layout=markerPopupView.findViewById(R.id.constraintLayout);
            TextView place = markerPopupView.findViewById(R.id.place_name);
            RatingBar rating=markerPopupView.findViewById(R.id.ratingBar);
            TextView other = markerPopupView.findViewById(R.id.other_info);

            Bundle markerInfo=resultPoints.get(marker.getPosition());
            place.setText(markerInfo.getString("PLACE"));
            if(!markerInfo.getBoolean("TYPE")){//E.R
                rating.setRating(Integer.parseInt(markerInfo.get("AVG_VOTE").toString()));
                other.setText(getString(R.string.people_in_queue)+" "+markerInfo.getString("TOT_WAIT_QUEUE"));
                if(markerInfo.getBoolean("BEST_QUEUE",false)){
                    other.setTextColor(getResources().getColor(R.color.green,null));
                }else if(markerInfo.getBoolean("WORST_QUEUE",false)){
                    other.setTextColor(getResources().getColor(R.color.primary,null));
                }
            }else{//PHARMACY
                //Hide the ratingBar space
                rating.setVisibility(View.INVISIBLE);
                rating.getLayoutParams().height=1;
                other.setText(markerInfo.getString("TIME")+" : "+(markerInfo.getBoolean("OPEN")?getString(R.string.open):getString(R.string.closed)));
                if(markerInfo.getBoolean("OPEN",false)){
                    other.setTextColor(getResources().getColor(R.color.green,null));
                }else{
                    other.setTextColor(getResources().getColor(R.color.primary,null));
                }
            }
            return markerPopupView;
        }
        return null;
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        if(!marker.getPosition().equals(centerCoordinates))
            presenter.onSelectedItem(resultPoints.get(marker.getPosition()));
    }
    private void AdjustZoomOnMarkers(){
        if(!resultMarkers.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : resultMarkers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int width = thisView.getMeasuredWidth();
            int height = thisView.getMeasuredWidth();
            int padding = Math.max((int) (width * 0.10), (int) (height * 0.10)); // offset from edges of the map 10% of screen
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                padding = Math.max((int) (width * 0.35), (int) (height * 0.35)); // offset from edges of the map 35% of screen
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
        }
    }
    private void AnimateCameraOnPoint(LatLng point, float zoom){
        if(mMap!=null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), defaultAnimationSpeed, null);
        }
    }
    private void AddMarker(final LatLng coordinates, int type){
        final MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(coordinates.latitude,coordinates.longitude));
        marker.draggable(false);
        //TODO Change the appeareance of the pins
        if(type==CENTER_TYPE_POINT){
            //marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            Target mTarget = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        try {
                            marker.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                            marker.title(getResources().getString(R.string.your_position));
                            Marker res = mMap.addMarker(marker);
                            resultMarkers.add(res);
                            resultMarkersMap.put(coordinates, res);
                        }catch(Exception e){}
                    }

                    @Override
                    public void onBitmapFailed(Exception ex, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) { }
            };
            if(CommonAccessData.getInstance().getCurrentFirebaseUser()!=null) {
                Uri photo=CommonAccessData.getInstance().getCurrentFirebaseUser().getPhotoUrl();
                try {
                    if(photo==null) throw  new RuntimeException("No photo");
                    Picasso.get()
                            .load(photo)
                            .resize(150,150)
                            .centerCrop()
                            .transform(new CircleBubbleTransformation())
                            .into(mTarget);
                } catch (Exception e) {
                    try{
                        Picasso.get()
                                .load(R.drawable.red_cross_basic_account_photo)
                                .resize(150,150)
                                .centerCrop()
                                .transform(new CircleBubbleTransformation())
                                .into(mTarget);
                    }catch (Exception ed) {
                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        marker.title(getResources().getString(R.string.your_position));
                        Marker res = mMap.addMarker(marker);
                        resultMarkers.add(res);
                        resultMarkersMap.put(coordinates, res);
                    }
                }
            }else {
                try{
                    Picasso.get()
                            .load(R.drawable.red_cross_basic_account_photo)
                            .resize(150,150)
                            .centerCrop()
                            .transform(new CircleBubbleTransformation())
                            .into(mTarget);
                }catch (Exception e) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    marker.title(getResources().getString(R.string.your_position));
                    Marker res = mMap.addMarker(marker);
                    resultMarkers.add(res);
                    resultMarkersMap.put(coordinates, res);
                }
            }
        }else if(type==ER_TYPE_POINT){
            if(((SearchOnMapView)this.getAttachedView()).getOnAddress())
                marker.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_map_cross_address),140,148,false)));
            else
                marker.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_map_cross_1),140,148,false)));
        }else if(type==DRUG_TYPE_POINT){
            if(((SearchOnMapView)this.getAttachedView()).getOnAddress())
                marker.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_map_pharmacy_address),140,148,false)));
            else
                marker.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_map_pharmacy),140,148,false)));
        }
        if(mMap!=null && type!=CENTER_TYPE_POINT) {
            Marker res=mMap.addMarker(marker);
            resultMarkers.add(res);
            resultMarkersMap.put(coordinates,res);
            AdjustZoomOnMarkers();
        }
    }

    private void showProgressBar(boolean small) {
        if(!small)
            mProgressBar.setVisibility(View.VISIBLE);
        else
            mSmallProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(boolean small) {
        if(!small)
            mProgressBar.setVisibility(View.GONE);
        else
            mSmallProgressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng key=new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
        if(this.resultPoints.get(key)!=null){
            presenter.onSetSelectedItem(resultPoints.get(key));
        }
        return false;
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        presenter.onSetSelectedItem(null);
    }
}

