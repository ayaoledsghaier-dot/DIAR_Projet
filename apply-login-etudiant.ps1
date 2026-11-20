# ============================================================
#  apply-login-etudiant.ps1
#  Ajoute la connexion ETUDIANT (backend + Android) et corrige
#  les bugs trouves pendant le diagnostic.
#  A placer dans DIAR_Projet (a cote de DIAR et backend-diar).
#  Lancer :
#    powershell -ExecutionPolicy Bypass -File .\apply-login-etudiant.ps1
#  Chaque fichier modifie est sauvegarde dans backup_apply_<date>.
#  Relancer le script ne casse rien (chaque etape saute si deja faite).
# ============================================================

# ---------- Options : mets $false pour sauter une etape ----------
$CorrigerInterface4   = $true   # carte Proprietaire -> bon ecran d'inscription
$CorrigerVerification = $true   # fin de verification -> Interfaceprofilecoloc (au lieu de boucler sur Interface6)
$MasquerMotDePasse    = $true   # ne plus envoyer motDePasse dans les reponses JSON
$CompilerBackend      = $true   # mvnw compile a la fin pour verifier

$utf8 = New-Object System.Text.UTF8Encoding($false)
$base = $PSScriptRoot

# ---------- Detection des dossiers ----------
if (Test-Path (Join-Path $base "app\src\main\java")) {
    $android = $base
    $back    = Join-Path (Split-Path $base -Parent) "backend-diar"
} elseif (Test-Path (Join-Path $base "DIAR\app\src\main\java")) {
    $android = Join-Path $base "DIAR"
    $back    = Join-Path $base "backend-diar"
} else {
    Write-Host "Projet introuvable depuis : $base" -ForegroundColor Red
    exit 1
}

$pI3   = Join-Path $android "app\src\main\java\com\example\diar\Interface3.java"
$pI4   = Join-Path $android "app\src\main\java\com\example\diar\Interface4.java"
$pVer  = Join-Path $android "app\src\main\java\com\example\diar\Interfaceverification.java"
$pCtrl = Join-Path $back "src\main\java\com\diar\backend_diar\controller\EtudiantController.java"
$pEnt  = Join-Path $back "src\main\java\com\diar\backend_diar\entity\Etudiant.java"

foreach ($p in @($pI3, $pI4, $pVer, $pCtrl, $pEnt)) {
    if (-not (Test-Path $p)) {
        Write-Host "Fichier introuvable : $p" -ForegroundColor Red
        exit 1
    }
}
Write-Host "Android : $android" -ForegroundColor Cyan
Write-Host "Backend : $back" -ForegroundColor Cyan

# ---------- Sauvegarde ----------
$backup = Join-Path $base ("backup_apply_" + (Get-Date -Format "yyyyMMdd_HHmmss"))
New-Item -ItemType Directory -Path $backup | Out-Null
foreach ($p in @($pI3, $pI4, $pVer, $pCtrl, $pEnt)) {
    Copy-Item $p (Join-Path $backup (Split-Path $p -Leaf))
}
Write-Host "Sauvegarde : $backup" -ForegroundColor Green

function Lire($p)         { return [System.IO.File]::ReadAllText($p, $utf8) }
function Ecrire($p, $t)   { [System.IO.File]::WriteAllText($p, $t, $utf8) }
$resume = @()

# ============================================================
# ETAPE 1 : backend - POST /api/etudiants/connexion
# ============================================================
$ctrl = Lire $pCtrl
if ($ctrl -match '@PostMapping\("/connexion"\)') {
    $resume += "[1] EtudiantController : endpoint /connexion deja present (saute)"
} else {
    $methode = @'

    // ==========================================
    // CONNEXION ETUDIANT
    // ==========================================
    @PostMapping("/connexion")
    public ResponseEntity<?> connecter(@RequestBody Map<String, String> donnees) {

        String email = donnees.get("email");
        String motDePasse = donnees.get("motDePasse");

        if (email == null || email.trim().isEmpty()
                || motDePasse == null || motDePasse.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email et mot de passe obligatoires."));
        }

        // L'app enregistre les emails en minuscules a l'inscription
        String emailClean = email.trim().toLowerCase();

        Etudiant etudiant = etudiantRepository.findByEmail(emailClean);

        if (etudiant == null || !motDePasse.equals(etudiant.getMotDePasse())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Email ou mot de passe incorrect."));
        }

        // Prochaine etape de l'inscription a reprendre (ou ACCUEIL si tout est fait)
        String etape;
        if (estVide(etudiant.getGenre()) || estVide(etudiant.getUniversite())
                || estVide(etudiant.getNiveau())) {
            etape = "PROFIL";
        } else if (estVide(etudiant.getTypeLogement())) {
            etape = "PREFERENCES";
        } else if (!"VERIFIE".equals(etudiant.getStatutVerification())) {
            etape = "VERIFICATION";
        } else if (estVide(etudiant.getModeDeVie())) {
            etape = "PROFIL_COLOC";
        } else {
            etape = "ACCUEIL";
        }

        // HashMap (et pas Map.of) car nomComplet peut etre null
        Map<String, Object> reponse = new HashMap<>();
        reponse.put("message", "Connexion r\u00e9ussie");
        reponse.put("role", "ETUDIANT");
        reponse.put("nom", etudiant.getNomComplet() != null ? etudiant.getNomComplet() : "");
        reponse.put("email", etudiant.getEmail());
        reponse.put("etape", etape);

        return ResponseEntity.ok(reponse);
    }

    private static boolean estVide(String s) {
        return s == null || s.trim().isEmpty();
    }
'@
    $idx = $ctrl.LastIndexOf('}')
    $ctrl = $ctrl.Substring(0, $idx) + $methode + "`r`n" + $ctrl.Substring($idx)
    Ecrire $pCtrl $ctrl
    $resume += "[1] EtudiantController : endpoint /connexion AJOUTE"
}

# ============================================================
# ETAPE 2 : Android - Interface3 (connexion double + session + routage)
# ============================================================
$i3 = Lire $pI3
if ($i3.Contains("ecranPourEtape")) {
    $resume += "[2] Interface3 : deja a jour (saute)"
} else {
    $nouveauI3 = @'
package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Interface3 extends AppCompatActivity {

    private static final String TAG = "CONNEXION";

    private static final String URL_PROPRIETAIRE = Config.BASE_URL + "/api/proprietaires/connexion";
    private static final String URL_ETUDIANT = Config.BASE_URL + "/api/etudiants/connexion";

    private EditText etEmail;
    private EditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface3);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(Interface3.this, Interface4.class)));

        btnLogin.setOnClickListener(v -> connecter());
    }

    private void connecter() {
        String email = etEmail.getText().toString().trim();
        String motDePasse = etPassword.getText().toString();

        if (email.isEmpty() || motDePasse.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject donnees = new JSONObject();
        try {
            donnees.put("email", email);
            donnees.put("motDePasse", motDePasse);
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de la pr\u00e9paration des donn\u00e9es.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Erreur JSON", e);
            return;
        }

        btnLogin.setEnabled(false);
        essayerProprietaire(donnees, email);
    }

    // ETAPE 1 : connexion proprietaire
    private void essayerProprietaire(JSONObject donnees, String email) {
        envoyer(URL_PROPRIETAIRE, donnees,
                response -> {
                    btnLogin.setEnabled(true);
                    Log.d(TAG, "Proprietaire OK : " + response);

                    Intent intent = new Intent(Interface3.this, AccueilProprietaire.class);
                    intent.putExtra("nomProprietaire", response.optString("nom", ""));
                    intent.putExtra("emailProprietaire", response.optString("email", email));
                    startActivity(intent);
                    finish();
                },
                error -> {
                    if (statusCode(error) == 401) {
                        // Pas un proprietaire : on tente le compte etudiant
                        essayerEtudiant(donnees, email);
                    } else {
                        afficherErreur(URL_PROPRIETAIRE, error);
                    }
                });
    }

    // ETAPE 2 : connexion etudiant
    private void essayerEtudiant(JSONObject donnees, String email) {
        envoyer(URL_ETUDIANT, donnees,
                response -> {
                    btnLogin.setEnabled(true);
                    Log.d(TAG, "Etudiant OK : " + response);

                    String emailRetour = response.optString("email", email.toLowerCase());
                    String nom = response.optString("nom", "");
                    String etape = response.optString("etape", "ACCUEIL");

                    // Interface5, 6 et 7 lisent l'email dans la session
                    new SessionManager(Interface3.this).sauvegarderSession(emailRetour, nom);

                    Intent intent = new Intent(Interface3.this, ecranPourEtape(etape));
                    intent.putExtra("USER_EMAIL", emailRetour);
                    intent.putExtra("USER_NAME", nom);
                    startActivity(intent);
                    finish();
                },
                error -> afficherErreur(URL_ETUDIANT, error));
    }

    // Reprend l'inscription la ou l'etudiant s'est arrete
    private Class<?> ecranPourEtape(String etape) {
        if ("PROFIL".equals(etape)) return Interface5.class;
        if ("PREFERENCES".equals(etape)) return Interface6.class;
        if ("VERIFICATION".equals(etape)) return Interfaceverification.class;
        if ("PROFIL_COLOC".equals(etape)) return Interfaceprofilecoloc.class;
        return Interface7.class;
    }

    private void envoyer(String url, JSONObject donnees,
                         Response.Listener<JSONObject> onSuccess,
                         Response.ErrorListener onError) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, donnees, onSuccess, onError);

        request.setRetryPolicy(new DefaultRetryPolicy(
                15000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag(TAG);
        requestQueue.add(request);
    }

    private int statusCode(VolleyError error) {
        return error.networkResponse != null ? error.networkResponse.statusCode : -1;
    }

    private void afficherErreur(String url, VolleyError error) {
        btnLogin.setEnabled(true);

        int code = statusCode(error);
        String details = "";
        if (error.networkResponse != null && error.networkResponse.data != null) {
            details = new String(error.networkResponse.data, StandardCharsets.UTF_8);
        }
        Log.e(TAG, "URL : " + url + " | HTTP : " + code + " | R\u00e9ponse : " + details, error);

        String message;
        if (code == 401) {
            message = "Email ou mot de passe incorrect.";
        } else if (code == 400) {
            message = "Donn\u00e9es incorrectes.";
        } else if (code == 404) {
            message = "Route API introuvable.";
        } else if (code == -1) {
            message = "Connexion impossible. V\u00e9rifie le Backend et ADB Reverse.";
        } else {
            message = "Erreur serveur HTTP : " + code;
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}
'@
    Ecrire $pI3 $nouveauI3
    $resume += "[2] Interface3 : REMPLACE (connexion double + session + routage)"
}

# ============================================================
# ETAPE 3 : Interface4 - la carte Proprietaire ouvrait Interface5 (profil etudiant)
# ============================================================
if ($CorrigerInterface4) {
    $i4 = Lire $pI4
    if ($i4.Contains("interface_creer_compte_proprietaire.class")) {
        $resume += "[3] Interface4 : deja corrige (saute)"
    } else {
        $n = ([regex]::Matches($i4, 'Interface5\.class')).Count
        if ($n -eq 1) {
            $i4 = $i4.Replace("Interface5.class", "interface_creer_compte_proprietaire.class")
            Ecrire $pI4 $i4
            $resume += "[3] Interface4 : carte Proprietaire corrigee"
        } else {
            $resume += "[3] Interface4 : ATTENTION $n occurrence(s) de Interface5.class, rien modifie"
        }
    }
} else {
    $resume += "[3] Interface4 : saute (option desactivee)"
}

# ============================================================
# ETAPE 4 : Interfaceverification - succes -> Interfaceprofilecoloc (au lieu de Interface6)
# ============================================================
if ($CorrigerVerification) {
    $ver = Lire $pVer
    if ($ver.Contains("Interfaceverification.this, Interfaceprofilecoloc.class")) {
        $resume += "[4] Interfaceverification : deja corrige (saute)"
    } else {
        $motif = '(?s)Intent intent = new Intent\(Interfaceverification\.this, Interface6\.class\);.*?// intent\.putExtra\("USER_EMAIL", userEmail\);'
        if ([regex]::Matches($ver, $motif).Count -eq 1) {
            $remplacement = 'Intent intent = new Intent(Interfaceverification.this, Interfaceprofilecoloc.class);' + "`r`n" + '                            intent.putExtra("USER_EMAIL", userEmail);'
            $ver = [regex]::Replace($ver, $motif, $remplacement)
            Ecrire $pVer $ver
            $resume += "[4] Interfaceverification : redirection corrigee vers Interfaceprofilecoloc"
        } else {
            $resume += "[4] Interfaceverification : ATTENTION bloc non trouve, rien modifie"
        }
    }
} else {
    $resume += "[4] Interfaceverification : saute (option desactivee)"
}

# ============================================================
# ETAPE 5 : Etudiant.java - ne plus renvoyer motDePasse dans le JSON
# ============================================================
if ($MasquerMotDePasse) {
    $ent = Lire $pEnt
    if ($ent.Contains("Access.WRITE_ONLY")) {
        $resume += "[5] Etudiant : motDePasse deja masque (saute)"
    } elseif ($ent -notmatch 'private String motDePasse;') {
        $resume += "[5] Etudiant : ATTENTION champ motDePasse non trouve, rien modifie"
    } else {
        $ent = $ent.Replace('private String motDePasse;',
            '@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)' + "`r`n" + '    private String motDePasse;')
        $ent = $ent.Replace('import jakarta.persistence.*;',
            'import jakarta.persistence.*;' + "`r`n" + 'import com.fasterxml.jackson.annotation.JsonProperty;')
        Ecrire $pEnt $ent
        $resume += "[5] Etudiant : motDePasse masque dans les reponses JSON (/profil, /compatibles)"
    }
} else {
    $resume += "[5] Etudiant : saute (option desactivee)"
}

# ============================================================
# ETAPE 6 : compilation du backend pour verifier
# ============================================================
if ($CompilerBackend) {
    $mvnw = Join-Path $back "mvnw.cmd"
    if (Test-Path $mvnw) {
        Write-Host ""
        Write-Host "Compilation du backend (quelques secondes)..." -ForegroundColor Cyan
        Push-Location $back
        & $mvnw -q -DskipTests compile
        $code = $LASTEXITCODE
        Pop-Location
        if ($code -eq 0) {
            $resume += "[6] Compilation backend : OK"
        } else {
            $resume += "[6] Compilation backend : ECHEC (voir l'erreur ci-dessus, envoie-la moi)"
        }
    } else {
        $resume += "[6] mvnw.cmd introuvable, compilation sautee"
    }
}

# ---------- Resume ----------
Write-Host ""
Write-Host "================ RESUME ================" -ForegroundColor Cyan
foreach ($l in $resume) {
    if ($l -match "ATTENTION|ECHEC") {
        Write-Host $l -ForegroundColor Yellow
    } else {
        Write-Host $l -ForegroundColor Green
    }
}
Write-Host ""
Write-Host "Ensuite :" -ForegroundColor Cyan
Write-Host "  1. Relance Spring :  cd backend-diar ; .\mvnw spring-boot:run"
Write-Host "  2. Android Studio :  Build > Rebuild Project, puis relance l'app"
Write-Host "  3. Teste la connexion avec ton compte etudiant"
Write-Host "Pour annuler : recopie les fichiers de $backup a leur place."
