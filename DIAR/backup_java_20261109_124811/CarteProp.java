
package com.example.diar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.events.MapEventsReceiver;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CarteProp extends AppCompatActivity {

    private MapView mapView;
    private Marker marker;
    private GeoPoint selectedPoint;

    private Button btnValider;
    private Button btnRecherche;
    private Button btnMaPosition;
    private EditText edtRecherche;

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();

        Configuration.getInstance().load(
                ctx,
                PreferenceManager.getDefaultSharedPreferences(ctx)
        );

        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.carte_prop);

        mapView = findViewById(R.id.mapView);
        btnValider = findViewById(R.id.btnValiderLocalisation);
        btnRecherche = findViewById(R.id.btnRechercheLieu);
        edtRecherche = findViewById(R.id.edtRechercheLieu);
        btnMaPosition = findViewById(R.id.btnMaPosition);

        if (mapView == null || btnValider == null
                || btnRecherche == null || edtRecherche == null
                || btnMaPosition == null) {

            Toast.makeText(
                    this,
                    "Erreur : vérifiez les identifiants dans carte_prop.xml",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        OnlineTileSourceBase tileSource = new OnlineTileSourceBase(
                "OpenStreetMap France",
                0,
                19,
                256,
                ".png",
                new String[]{
                        "https://a.tile.openstreetmap.fr/osmfr/",
                        "https://b.tile.openstreetmap.fr/osmfr/",
                        "https://c.tile.openstreetmap.fr/osmfr/"
                }
        ) {
            @Override
            public String getTileURLString(long tileIndex) {
                return getBaseUrl()
                        + MapTileIndex.getZoom(tileIndex) + "/"
                        + MapTileIndex.getX(tileIndex) + "/"
                        + MapTileIndex.getY(tileIndex) + ".png";
            }
        };

        mapView.setTileSource(tileSource);
        mapView.setMultiTouchControls(true);

        GeoPoint centreInitial = new GeoPoint(36.8065, 10.1815);
        selectedPoint = centreInitial;

        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(centreInitial);

        marker = new Marker(mapView);
        marker.setPosition(centreInitial);
        marker.setTitle("Tunis");
        mapView.getOverlays().add(marker);

        MapEventsReceiver receiver = new MapEventsReceiver() {

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint point) {
                if (point != null) {
                    choisirPosition(point, "Logement sélectionné");
                }
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint point) {
                return false;
            }
        };

        mapView.getOverlays().add(new MapEventsOverlay(receiver));

        btnRecherche.setOnClickListener(v -> rechercherLieu());
        btnMaPosition.setOnClickListener(v -> obtenirPositionGPS());
        btnValider.setOnClickListener(v -> validerPosition());
    }

    private void choisirPosition(GeoPoint point, String titre) {

        if (point == null || marker == null || mapView == null) {
            return;
        }

        selectedPoint = point;

        marker.setPosition(point);
        marker.setTitle(titre);

        mapView.getController().animateTo(point);
        mapView.invalidate();

        Toast.makeText(
                this,
                "Position sélectionnée",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void rechercherLieu() {

        String query = edtRecherche.getText().toString().trim();

        if (query.isEmpty()) {
            edtRecherche.setError("Entrez une ville ou un quartier");
            return;
        }

        if (!Geocoder.isPresent()) {
            Toast.makeText(
                    this,
                    "Service de recherche indisponible",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(
                        CarteProp.this,
                        Locale.FRANCE
                );

                List<Address> addresses =
                        geocoder.getFromLocationName(
                                query + ", Tunisie",
                                1
                        );

                runOnUiThread(() -> {

                    if (addresses != null && !addresses.isEmpty()) {

                        Address address = addresses.get(0);

                        GeoPoint point = new GeoPoint(
                                address.getLatitude(),
                                address.getLongitude()
                        );

                        String titre = address.getAddressLine(0);

                        choisirPosition(
                                point,
                                titre != null ? titre : query
                        );

                        mapView.getController().setZoom(16.0);

                    } else {
                        Toast.makeText(
                                CarteProp.this,
                                "Lieu introuvable",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                CarteProp.this,
                                "Erreur du service de recherche",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        }).start();
    }

    private void obtenirPositionGPS() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );

            return;
        }

        try {
            LocationManager locationManager =
                    (LocationManager) getSystemService(
                            Context.LOCATION_SERVICE
                    );

            Location location = null;

            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER
                );

                if (location == null) {
                    location = locationManager.getLastKnownLocation(
                            LocationManager.NETWORK_PROVIDER
                    );
                }
            }

            if (location != null) {

                GeoPoint point = new GeoPoint(
                        location.getLatitude(),
                        location.getLongitude()
                );

                choisirPosition(point, "Ma position GPS");
                mapView.getController().setZoom(16.0);

            } else {
                Toast.makeText(
                        this,
                        "Position GPS indisponible. Activez le GPS.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (SecurityException | IllegalArgumentException e) {
            Toast.makeText(
                    this,
                    "Impossible de récupérer la position GPS",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void validerPosition() {

        if (selectedPoint == null) {
            Toast.makeText(
                    this,
                    "Veuillez choisir une position",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent data = new Intent();

        data.putExtra("latitude", selectedPoint.getLatitude());
        data.putExtra("longitude", selectedPoint.getLongitude());

        setResult(RESULT_OK, data);

        finish();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                obtenirPositionGPS();

            } else {
                Toast.makeText(
                        this,
                        "Permission de localisation refusée",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            mapView.onDetach();
        }

        super.onDestroy();
    }
}
