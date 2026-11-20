# ============================================================
#  collect-diar.ps1 : DIAGNOSTIC EN LECTURE SEULE
#  Ne modifie rien. Cree uniquement le fichier diag-diar.txt
#  A placer dans DIAR_Projet (a cote de DIAR et backend-diar)
#  ou dans le dossier DIAR.
#  Lancer :
#    powershell -ExecutionPolicy Bypass -File .\collect-diar.ps1
# ============================================================

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
    Write-Host "Place ce script dans DIAR_Projet (a cote de DIAR et backend-diar)." -ForegroundColor Red
    exit 1
}
$javaAndroid = Join-Path $android "app\src\main\java"
$javaBack    = Join-Path $back "src\main\java"
Write-Host "Android : $android" -ForegroundColor Cyan
Write-Host "Backend : $back" -ForegroundColor Cyan

$sb = New-Object System.Text.StringBuilder

function Titre($t) {
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("################################################################")
    [void]$sb.AppendLine("## $t")
    [void]$sb.AppendLine("################################################################")
}

function Dump-Fichier($chemin) {
    if ($chemin -and (Test-Path $chemin)) {
        Titre ("FICHIER : " + $chemin.Replace($base, "."))
        [void]$sb.AppendLine([System.IO.File]::ReadAllText($chemin, $utf8))
    }
}

function Dump-Nom($racine, $nom) {
    $f = Get-ChildItem -Path $racine -Recurse -Filter $nom -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($f) {
        Dump-Fichier $f.FullName
    } else {
        Titre "FICHIER : $nom"
        [void]$sb.AppendLine("(introuvable)")
    }
}

# ---------- A. Parcours etudiant cote Android ----------
$ecrans = @(
    "interface1.java", "Interface4.java", "Interfacecreercompteetudiant.java",
    "Interfaceverification.java", "Interface5.java", "Interface6.java",
    "Interface7.java", "Interface12.java", "Interfaceprofilecoloc.java",
    "BaseActivity.java", "Config.java"
)
foreach ($e in $ecrans) { Dump-Nom $javaAndroid $e }

# ---------- B. Navigation et donnees transmises entre ecrans ----------
Titre "ANDROID : navigation et extras (startActivity, new Intent, getStringExtra, putExtra, SharedPreferences)"
Get-ChildItem -Path $javaAndroid -Recurse -Filter *.java |
    Select-String -Pattern "startActivity|new Intent|getStringExtra|putExtra|SharedPreferences|getIntent" |
    ForEach-Object { [void]$sb.AppendLine(("{0}:{1}: {2}" -f $_.Filename, $_.LineNumber, $_.Line.Trim())) }

# ---------- C. Layout de connexion + gradle ----------
Dump-Nom (Join-Path $android "app\src\main\res\layout") "interface3.xml"
foreach ($g in @("app\build.gradle", "app\build.gradle.kts")) {
    Dump-Fichier (Join-Path $android $g)
}

# ---------- D. Backend ----------
Titre "BACKEND : arborescence src\main\java"
Get-ChildItem -Path $javaBack -Recurse -Filter *.java |
    ForEach-Object { [void]$sb.AppendLine($_.FullName.Replace($back, ".")) }

Titre "BACKEND : tous les endpoints (Mapping)"
Get-ChildItem -Path $javaBack -Recurse -Filter *.java |
    Select-String -Pattern "@(Get|Post|Put|Delete|Patch|Request)Mapping" |
    ForEach-Object { [void]$sb.AppendLine(("{0}:{1}: {2}" -f $_.Filename, $_.LineNumber, $_.Line.Trim())) }

Titre "BACKEND : securite / CORS (SecurityFilterChain, CorsConfiguration, WebMvcConfigurer, CrossOrigin, csrf)"
$sec = Get-ChildItem -Path $javaBack -Recurse -Filter *.java |
    Select-String -Pattern "SecurityFilterChain|CorsConfiguration|WebMvcConfigurer|CrossOrigin|csrf|EnableWebSecurity|PasswordEncoder|BCrypt"
if ($sec) {
    $sec | ForEach-Object { [void]$sb.AppendLine(("{0}:{1}: {2}" -f $_.Filename, $_.LineNumber, $_.Line.Trim())) }
} else {
    [void]$sb.AppendLine("(aucune configuration de securite trouvee)")
}

Dump-Nom $javaBack "EtudiantController.java"
Dump-Fichier (Join-Path $back "pom.xml")

# NB : application.properties n'est volontairement PAS inclus (mot de passe BDD)

# ---------- Ecriture du fichier ----------
$out = Join-Path $base "diag-diar.txt"
[System.IO.File]::WriteAllText($out, $sb.ToString(), $utf8)

$ko = [math]::Round((Get-Item $out).Length / 1KB)
Write-Host ""
Write-Host "Termine : $out ($ko Ko)" -ForegroundColor Green
Write-Host "Envoie-moi ce fichier (joins-le dans la conversation)." -ForegroundColor Cyan
