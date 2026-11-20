# ============================================================
#  fix-urls.ps1 : centralise toutes les URLs de l'app DIAR
#  - sauvegarde les sources
#  - cree Config.java
#  - remplace toutes les URLs http://IP:PORT par Config.BASE_URL
#  - lance adb reverse (si le telephone est branche en USB)
# ============================================================

$root    = "C:\Users\user\DIAR_Projet\DIAR"
$javaDir = Join-Path $root "app\src\main\java\com\example\diar"
$utf8    = New-Object System.Text.UTF8Encoding($false)   # UTF-8 sans BOM

if (-not (Test-Path $javaDir)) {
    Write-Host "Dossier introuvable : $javaDir" -ForegroundColor Red
    exit 1
}

# ---------- 1. Sauvegarde (hors du dossier source) ----------
$backup = Join-Path $root ("backup_java_" + (Get-Date -Format "yyyyMMdd_HHmmss"))
Copy-Item $javaDir $backup -Recurse
Write-Host "[1/4] Sauvegarde creee : $backup" -ForegroundColor Green

# ---------- 2. Creation de Config.java ----------
$configPath = Join-Path $javaDir "Config.java"
$configCode = @'
package com.example.diar;

public class Config {
    // Mode cable USB (adb reverse) : fonctionne sur n'importe quel reseau
    public static final String BASE_URL = "http://localhost:8083";

    // Mode Wi-Fi : commente la ligne du dessus, decommente celle-ci
    // et mets l'IP du PC (commande ipconfig)
    // public static final String BASE_URL = "http://192.168.6.36:8083";
}
'@
[System.IO.File]::WriteAllText($configPath, $configCode, $utf8)
Write-Host "[2/4] Config.java cree" -ForegroundColor Green

# ---------- 3. Remplacement des URLs ----------
$pattern     = '"http://\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}:\d{2,5}'
$replacement = 'Config.BASE_URL + "'
$total = 0

Get-ChildItem -Path $javaDir -Recurse -Filter *.java |
    Where-Object { $_.Name -ne "Config.java" } |
    ForEach-Object {
        $content = [System.IO.File]::ReadAllText($_.FullName, $utf8)
        $found   = [regex]::Matches($content, $pattern).Count
        if ($found -gt 0) {
            $new = [regex]::Replace($content, $pattern, $replacement)
            [System.IO.File]::WriteAllText($_.FullName, $new, $utf8)
            Write-Host ("   {0} : {1} remplacement(s)" -f $_.Name, $found)
            $total += $found
        }
    }
Write-Host "[3/4] $total URL(s) remplacee(s)" -ForegroundColor Green

# ---------- Verification : il ne doit rester aucune IP en dur ----------
$restes = Get-ChildItem -Path $javaDir -Recurse -Filter *.java |
    Where-Object { $_.Name -ne "Config.java" } |
    Select-String -Pattern '\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}'
if ($restes) {
    Write-Host "Attention, il reste des IP en dur :" -ForegroundColor Yellow
    $restes | ForEach-Object { Write-Host ("   {0}:{1}  {2}" -f $_.Filename, $_.LineNumber, $_.Line.Trim()) }
} else {
    Write-Host "   Verification OK : plus aucune IP en dur" -ForegroundColor Green
}

# ---------- 4. adb reverse ----------
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (Test-Path $adb) {
    & $adb devices
    & $adb reverse tcp:8083 tcp:8083
    Write-Host "[4/4] adb reverse execute (voir le resultat ci-dessus)" -ForegroundColor Green
} else {
    Write-Host "[4/4] adb.exe introuvable : $adb" -ForegroundColor Yellow
    Write-Host "      Branche le telephone et lance adb reverse manuellement."
}

Write-Host ""
Write-Host "Termine. Dans Android Studio : Build > Rebuild Project, puis relance l'app." -ForegroundColor Cyan
Write-Host "Pour annuler : recopie le dossier $backup a la place de $javaDir"
