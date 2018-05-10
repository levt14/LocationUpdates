package com.locationupdates;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private LocationListener locationListener;
    private LocationManager locationManager;
    private TextView txtLongitude, txtLatitude, txtLocation;
    private boolean isLocationUpdatesStarted;
    private NotificationManager notificationManager;;
    private String myLocation;


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        } else {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mLocationLisiner);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {

            int grant = grantResults[0];
            if (grant == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mLocationLisiner);

            }
        }
    }

    private LocationListener mLocationLisiner = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            txtLongitude.setText("Longitude: " + Double.toString(location.getLongitude()));
            txtLatitude.setText("Latitude: " + Double.toString(location.getLatitude()));

            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            myLocation = addresses.get(0).getAddressLine(0);

            txtLocation.setText(myLocation);

            makeNotification();

            locationListener = mLocationLisiner;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startLocationUpdates(View view) {

        if(checkIfGPSAvailable()) {
            return;
        }


        if (isLocationUpdatesStarted == false) {
            Toast.makeText(this, "Please wait few moments while updating...", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Location updates started.", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
            isLocationUpdatesStarted = true;
        }
    }

    public void stopLocationUpdates(View view) {

        if(checkIfGPSAvailable()) {
            return;
        }

        if(isLocationUpdatesStarted == true) {
            Toast.makeText(this, "Location updates stopped.", Toast.LENGTH_SHORT).show();
            if(locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
            isLocationUpdatesStarted = false;
        }
    }

    public void makeNotification() {

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        Notification notification = new Notification.Builder(this).
                setSmallIcon(R.drawable.ic_location_on_black_24dp).
                setContentTitle("Your current location:").
                setContentText(myLocation).
                setContentIntent(pendingIntent).
                setSound(alarmSound).
                build();

        //now we need to go to notification manager
        notificationManager.notify(1, notification);

    }

    public boolean checkIfGPSAvailable() {
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}



        if(!gps_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("GPS is disabled in your device. Would you like to enable it?");
            dialog.setCancelable(false);
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.setPositiveButton("Open Location Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });

            dialog.show();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLongitude = findViewById(R.id.txt_longitude);
        txtLatitude = findViewById(R.id.txt_latitude);
        txtLocation = findViewById(R.id.txt_location);

        isLocationUpdatesStarted = false;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }
}
