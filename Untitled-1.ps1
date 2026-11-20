$body = @{ email = "sghaier@gmail.com"; motDePasse = "gugkhkh123" } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "http://127.0.0.1:8083/api/etudiants/connexion" -Method Post -ContentType "application/json" -Body $body
} catch {
    "HTTP " + $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}