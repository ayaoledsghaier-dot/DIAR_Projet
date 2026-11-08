
package com.example.diar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AjouterAnnonce extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.1.22:8083";
    private static final int MAX_PHOTOS = 10;

    private EditText etTitre, etDescription, etPrix;
    private EditText etNombrePersonnes;

    private ImageButton btnUploadPhoto;
    private Button btnNext;

    private final ArrayList<Uri> photosUris = new ArrayList<>();

    private double latitude = 0;
    private double longitude = 0;

    // Sélection de plusieurs photos
    private final ActivityResultLauncher<String> photosLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetMultipleContents(),
                    uris -> {
                        if (uris != null && !uris.isEmpty()) {
                            photosUris.clear();

                            int nombre = Math.min(uris.size(), MAX_PHOTOS);

                            for (int i = 0; i < nombre; i++) {
                                photosUris.add(uris.get(i));
                            }

                            btnUploadPhoto.setImageURI(photosUris.get(0));

                            Toast.makeText(
                                    this,
                                    photosUris.size() + " photo(s) sélectionnée(s)",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    // Résultat de la carte
    private final ActivityResultLauncher<Intent> carteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            latitude = result.getData()
                                    .getDoubleExtra("latitude", 0);

                            longitude = result.getData()
                                    .getDoubleExtra("longitude", 0);

                            if (latitude == 0 && longitude == 0) {
                                Toast.makeText(
                                        this,
                                        "Position invalide",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            enregistrerAnnonce();

                        } else {
                            Toast.makeText(
                                    this,
                                    "Sélection de position annulée",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface19);

        initialiserVues();

        btnUploadPhoto.setOnClickListener(v ->
                photosLauncher.launch("image/*")
        );

        btnNext.setOnClickListener(v -> {

            if (!validerFormulaire()) {
                return;
            }

            Intent intent = new Intent(
                    AjouterAnnonce.this,
                    CarteProp.class
            );

            carteLauncher.launch(intent);
        });
    }

    // Initialisation des vues
    private void initialiserVues() {

        etTitre = findViewById(R.id.etTitre);
        etDescription = findViewById(R.id.etDescription);
        etPrix = findViewById(R.id.etPrix);
        etNombrePersonnes = findViewById(R.id.etNombrePersonnes);

        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnNext = findViewById(R.id.btnNext);
    }

    // Validation du formulaire
    private boolean validerFormulaire() {

        String titre = etTitre.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String prixText = etPrix.getText().toString().trim();
        String personnesText =
                etNombrePersonnes.getText().toString().trim();

        if (photosUris.isEmpty()) {
            Toast.makeText(
                    this,
                    "Veuillez choisir au moins une photo",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (titre.isEmpty()) {
            etTitre.setError("Entrez le titre du logement");
            etTitre.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError("Entrez la description");
            etDescription.requestFocus();
            return false;
        }

        if (prixText.isEmpty()) {
            etPrix.setError("Entrez le prix");
            etPrix.requestFocus();
            return false;
        }

        if (personnesText.isEmpty()) {
            etNombrePersonnes.setError("Entrez le nombre de personnes");
            etNombrePersonnes.requestFocus();
            return false;
        }

        try {
            double prix = Double.parseDouble(prixText);
            int personnes = Integer.parseInt(personnesText);

            if (prix <= 0) {
                etPrix.setError("Le prix doit être supérieur à 0");
                etPrix.requestFocus();
                return false;
            }

            if (personnes < 1 || personnes > 100) {
                etNombrePersonnes.setError(
                        "Le nombre doit être entre 1 et 100"
                );
                etNombrePersonnes.requestFocus();
                return false;
            }

        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Vérifiez le prix et le nombre de personnes",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        return true;
    }

    // Conversion photo vers Base64
    private String convertirPhotoEnBase64(Uri uri) throws Exception {

        InputStream inputStream =
                getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new Exception("Impossible de lire la photo");
        }

        Bitmap bitmap;

        try {
            bitmap = BitmapFactory.decodeStream(inputStream);
        } finally {
            inputStream.close();
        }

        if (bitmap == null) {
            throw new Exception("Photo invalide");
        }

        int largeur = bitmap.getWidth();
        int hauteur = bitmap.getHeight();

        int maxDimension = 1000;

        if (largeur > maxDimension || hauteur > maxDimension) {

            float ratio = Math.min(
                    (float) maxDimension / largeur,
                    (float) maxDimension / hauteur
            );

            bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    Math.round(largeur * ratio),
                    Math.round(hauteur * ratio),
                    true
            );
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output);

        return Base64.encodeToString(
                output.toByteArray(),
                Base64.NO_WRAP
        );
    }

    // Enregistrement de l'annonce
    private void enregistrerAnnonce() {

        final String titre = etTitre.getText().toString().trim();
        final String description =
                etDescription.getText().toString().trim();
        final String prixText = etPrix.getText().toString().trim();
        final String personnesText =
                etNombrePersonnes.getText().toString().trim();

        final double annonceLatitude = latitude;
        final double annonceLongitude = longitude;

        final ArrayList<Uri> photosAEnvoyer =
                new ArrayList<>(photosUris);

        btnNext.setEnabled(false);
        btnNext.setText("Enregistrement...");

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {
                JSONObject annonce = new JSONObject();

                annonce.put("titre", titre);
                annonce.put("description", description);
                annonce.put("prix", Double.parseDouble(prixText));
                annonce.put(
                        "nombrePersonnes",
                        Integer.parseInt(personnesText)
                );
                annonce.put("latitude", annonceLatitude);
                annonce.put("longitude", annonceLongitude);

                JSONArray photosArray = new JSONArray();

                for (Uri uri : photosAEnvoyer) {
                    photosArray.put(convertirPhotoEnBase64(uri));
                }

                annonce.put("photos", photosArray);

                URL url = new URL(BASE_URL + "/api/annonces");

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );
                connection.setDoOutput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);

                byte[] data = annonce.toString().getBytes("UTF-8");

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(data);
                    output.flush();
                }

                int code = connection.getResponseCode();

                if (code >= 200 && code < 300) {

                    runOnUiThread(() -> {
                        Toast.makeText(
                                AjouterAnnonce.this,
                                "Annonce enregistrée avec succès !",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent intent = new Intent(
                                AjouterAnnonce.this,
                                AccueilProprietaire.class
                        );

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });

                } else {

                    InputStream errorStream = connection.getErrorStream();
                    StringBuilder erreur = new StringBuilder();

                    if (errorStream != null) {
                        byte[] buffer = new byte[1024];
                        int length;

                        while ((length = errorStream.read(buffer)) != -1) {
                            erreur.append(
                                    new String(buffer, 0, length, "UTF-8")
                            );
                        }

                        errorStream.close();
                    }

                    String message =
                            "Erreur serveur HTTP " + code + " : " + erreur;

                    runOnUiThread(() ->
                            Toast.makeText(
                                    AjouterAnnonce.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                AjouterAnnonce.this,
                                "Erreur : " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }

                runOnUiThread(() -> {
                    btnNext.setEnabled(true);
                    btnNext.setText("Continuer");
                });
            }

        }).start();
    }
}
