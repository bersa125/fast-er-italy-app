package com.business_logic.fasteritaly.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.example.fasteritaly.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.model.fasteritaly.Hospital;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.model.fasteritaly.singleton_and_helpers.ModelBundleAdapter;
import com.presentation_layer.fasteritaly.activity.PlaceInfoActivity;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class Tracking_Service extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int ID = 1;

    private RemoteViews expandedView;
    private RemoteViews collapsedView;
    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private LocationListener locationListener;
    private Location currentLocation;
    private LocationManager locationManager;
    private boolean active;
    private AlertDialog alert;

    @Override
    public void onCreate() {
        active = true;
        expandedView = new RemoteViews(getPackageName(), R.layout.notification_big_layout);
        collapsedView = new RemoteViews(getPackageName(), R.layout.notification_small_layout);
        expandedView.setViewVisibility(R.id.notification_vote, View.GONE);
        expandedView.setViewVisibility(R.id.right_button, View.GONE);
        expandedView.setTextViewText(R.id.left_button, getString(R.string.stop));
        Intent intent = new Intent(getApplicationContext(), Tracking_Service.class);
        intent.setAction("STOP");
        expandedView.setOnClickPendingIntent(R.id.left_button, PendingIntent.getService(getApplicationContext(), 0, intent, 0));
        expandedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        collapsedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        builder = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent intent = new Intent(getApplicationContext(), Tracking_Service.class);
                intent.setAction("UPDATE_NOTIFICATION");
                intent.putExtra("LOC", location);
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, locationListener);
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    locationManager.removeUpdates(locationListener);
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 100, locationListener);
                }
            }
        };
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() == null) {
            createNotificationChannel();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location currentloc = SmartLocation.with(getBaseContext()).location().getLastLocation();
            currentLocation = currentloc;
            SmartLocation.with(getBaseContext()).location().oneFix().start(new OnLocationUpdatedListener() {
                @Override
                public void onLocationUpdated(Location location) {
                    currentLocation = location;
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    } else {
                        if (SmartLocation.with(getBaseContext()).location().state().isGpsAvailable()) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, locationListener);
                        } else {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 100, locationListener);
                        }
                    }
                }
            });
            final Notification notification = builder.build();
            startForeground(ID, notification);
            Intent intent1 = new Intent(getApplicationContext(), Tracking_Service.class);
            intent1.setAction("UPDATE_NOTIFICATION");
            intent1.putExtra("LOC", currentLocation);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent1, 0);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else {
            if (intent.getAction().equals("STOP")) {
                SmartLocation.with(getBaseContext()).location().continuous().stop();
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (Exception e) {
                }
                stopSelf();
            } else {
                if (intent.getAction().equals("VOTE")) {
                    if (intent.getExtras().getBundle("EXTRAS")!=null) {
                        Intent intent1 = new Intent(this, PlaceInfoActivity.class);
                        intent1.putExtra("PLACE", ModelBundleAdapter.getBundleRepresentation(CommonAccessData.getInstance().getHospitalByName((intent.getExtras().getBundle("EXTRAS").getString("HOSPITAL")+intent.getExtras().getBundle("EXTRAS").getString("ADDRESS")))) );
                        startActivity(intent1);
                        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        getBaseContext().sendBroadcast(it);
                    }
                } else {
                    if (intent.getAction().equals("UPDATE_NOTIFICATION")) {
                        if (!(intent.getExtras().get("LOC")).equals(currentLocation)) {
                            currentLocation = (Location) intent.getExtras().get("LOC");
                            LatLng coordinates = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            boolean reached = false;
                            String h_name = null;
                            String h_address = null;
                            for (Hospital h : CommonAccessData.getInstance().getRegisteredHospitals()) {
                                if (SphericalUtil.computeDistanceBetween(coordinates, h.getAddress().getCoordinates()) <= 700) {
                                    reached = true;
                                    h_name = h.getPlaceName();
                                    h_address = h.getAddress().getAddress();
                                    //break;
                                }
                            }
                            CommonAccessData.getInstance().closeGetRealm();
                            //System.err.println(CommonAccessData.getInstance().getRegisteredHospitals().size()+" "+reached);
                            if (reached) {
                                expandedView.setViewVisibility(R.id.notification_vote, View.VISIBLE);
                                expandedView.setViewVisibility(R.id.right_button, View.VISIBLE);
                                expandedView.setTextViewText(R.id.left_button, getString(R.string.call_vote));
                                Intent intent1 = new Intent(getApplicationContext(), Tracking_Service.class);
                                Bundle extras=new Bundle();
                                extras.putString("HOSPITAL", h_name);
                                extras.putString("ADDRESS", h_address);
                                intent1.putExtra("EXTRAS",extras);
                                intent1.setAction("VOTE");
                                expandedView.setOnClickPendingIntent(R.id.left_button, PendingIntent.getService(getApplicationContext(), 0, intent1, 0));
                                Intent intent2 = new Intent(getApplicationContext(), Tracking_Service.class);
                                intent2.setAction("RESET");
                                expandedView.setOnClickPendingIntent(R.id.right_button, PendingIntent.getService(getApplicationContext(), 0, intent2, 0));
                                SpannableStringBuilder span_builder = new SpannableStringBuilder();
                                SpannableString preamble = new SpannableString(getString(R.string.Name_details_title) + "\n");
                                preamble.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, (getString(R.string.Name_details_title) + "\n").length(), 0);
                                span_builder.append(preamble);
                                SpannableString title = new SpannableString(h_name);
                                title.setSpan(new StyleSpan(Typeface.NORMAL), 0, h_name.length(), 0);
                                span_builder.append(title);
                                expandedView.setTextViewText(R.id.hospital_name, span_builder);
                                span_builder = new SpannableStringBuilder();
                                preamble = new SpannableString(getString(R.string.address_details_title) + "\n");
                                preamble.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, (getString(R.string.address_details_title) + "\n").length(), 0);
                                span_builder.append(preamble);
                                title = new SpannableString(h_address);
                                title.setSpan(new StyleSpan(Typeface.NORMAL), 0, h_address.length(), 0);
                                span_builder.append(title);
                                expandedView.setTextViewText(R.id.hospital_address, span_builder);
                                expandedView.setTextViewText(R.id.content_text, getString(R.string.er_reached));
                                collapsedView.setTextViewText(R.id.content_text, getString(R.string.er_reached));
                                manager.notify(ID, builder.build());
                            } else {
                                expandedView.setViewVisibility(R.id.notification_vote, View.GONE);
                                expandedView.setViewVisibility(R.id.right_button, View.GONE);
                                expandedView.setTextViewText(R.id.left_button, getString(R.string.stop));
                                expandedView.setTextViewText(R.id.content_text, getString(R.string.no_er_reached));
                                collapsedView.setTextViewText(R.id.content_text, getString(R.string.no_er_reached));
                                Intent intent1 = new Intent(getApplicationContext(), Tracking_Service.class);
                                intent1.setAction("STOP");
                                expandedView.setOnClickPendingIntent(R.id.left_button, PendingIntent.getService(getApplicationContext(), 0, intent1, 0));
                                expandedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
                                collapsedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
                                manager.notify(ID, builder.build());
                            }
                        }
                    } else {
                        if (intent.getAction().equals("RESET")) {
                            expandedView.setViewVisibility(R.id.notification_vote, View.GONE);
                            expandedView.setViewVisibility(R.id.right_button, View.GONE);
                            expandedView.setTextViewText(R.id.left_button, getString(R.string.stop));
                            expandedView.setTextViewText(R.id.content_text, getString(R.string.no_er_reached));
                            collapsedView.setTextViewText(R.id.content_text, getString(R.string.no_er_reached));
                            Intent intent1 = new Intent(getApplicationContext(), Tracking_Service.class);
                            intent1.setAction("STOP");
                            expandedView.setOnClickPendingIntent(R.id.left_button, PendingIntent.getService(getApplicationContext(), 0, intent1, 0));
                            expandedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
                            collapsedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
                            manager.notify(ID, builder.build());
                        }
                    }
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (Exception e) {
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
