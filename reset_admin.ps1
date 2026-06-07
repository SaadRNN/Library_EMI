param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Nom,

    [Parameter(Mandatory = $true, Position = 1)]
    [string]$Prenom,

    [Parameter(Mandatory = $false, Position = 2)]
    [string]$Email = "",

    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$DbName = "library_db",
    [string]$DbUser = "root",
    [string]$DbPass = "1234",

    [string]$BcryptJar = "lib\jbcrypt-0.4.jar",
    [string]$MysqlExe = "mysql"
)

$ErrorActionPreference = "Stop"

function Normalize-Name {
    param([string]$Value)
    return ($Value.Trim().ToLower() -replace "\s+", "_")
}

function Sql-Escape {
    param([string]$Value)

    if ($null -eq $Value) {
        return ""
    }

    return $Value.Replace("\", "\\").Replace("'", "''")
}

function Convert-SecureStringToPlainText {
    param([System.Security.SecureString]$SecureString)

    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureString)

    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

function Check-Command {
    param([string]$CommandName, [string]$InstallMessage)

    $cmd = Get-Command $CommandName -ErrorAction SilentlyContinue

    if (-not $cmd -and -not (Test-Path $CommandName)) {
        throw $InstallMessage
    }
}

function Invoke-MySqlRaw {
    param([string]$Query)

    $output = & $MysqlExe `
        -h $DbHost `
        -P $DbPort `
        -u $DbUser `
        --default-character-set=utf8mb4 `
        -N `
        -B `
        -e $Query 2>&1

    if ($LASTEXITCODE -ne 0) {
        throw "Erreur MySQL : $($output | Out-String)"
    }

    return (($output | Out-String).Trim())
}

function Invoke-MySqlScript {
    param([string]$Sql)

    $output = $Sql | & $MysqlExe `
        -h $DbHost `
        -P $DbPort `
        -u $DbUser `
        --default-character-set=utf8mb4 `
        $DbName 2>&1

    if ($LASTEXITCODE -ne 0) {
        throw "Erreur MySQL : $($output | Out-String)"
    }

    return ($output | Out-String)
}

try {
    Write-Host "Vérification de l'environnement..." -ForegroundColor Cyan

    Check-Command "java" "Erreur : java n'est pas installé ou n'est pas dans le PATH."
    Check-Command "javac" "Erreur : javac n'est pas installé. Installe un JDK, pas seulement un JRE."
    Check-Command $MysqlExe "Erreur : mysql.exe n'est pas installé ou n'est pas dans le PATH."

    if (-not (Test-Path $BcryptJar)) {
        throw @"
Erreur : fichier jBCrypt introuvable : $BcryptJar

Il faut le vrai fichier :
  lib\jbcrypt-0.4.jar

Pas :
  jbcrypt-0.4-javadoc.jar
  jbcrypt-0.4-sources.jar
"@
    }

    $NomNorm = Normalize-Name $Nom
    $PrenomNorm = Normalize-Name $Prenom
    $Username = "$PrenomNorm.$NomNorm"

    if ([string]::IsNullOrWhiteSpace($Email)) {
        $Email = "$Username@gmail.com"
    }

    Write-Host ""
    Write-Host "Admin ciblé :" -ForegroundColor Yellow
    Write-Host "  Nom      : $Nom"
    Write-Host "  Prénom   : $Prenom"
    Write-Host "  Username : $Username"
    Write-Host "  Email    : $Email"
    Write-Host ""

    $SecurePass1 = Read-Host "Nouveau mot de passe admin" -AsSecureString
    $SecurePass2 = Read-Host "Confirmer le mot de passe" -AsSecureString

    $PlainPass1 = Convert-SecureStringToPlainText $SecurePass1
    $PlainPass2 = Convert-SecureStringToPlainText $SecurePass2

    if ($PlainPass1 -ne $PlainPass2) {
        throw "Erreur : les deux mots de passe sont différents."
    }

    if ($PlainPass1.Length -lt 8) {
        throw "Erreur : le mot de passe doit contenir au moins 8 caractères."
    }

    if ($PlainPass1 -notmatch "[A-Z]") {
        throw "Erreur : le mot de passe doit contenir au moins une majuscule."
    }

    if ($PlainPass1 -notmatch "[a-z]") {
        throw "Erreur : le mot de passe doit contenir au moins une minuscule."
    }

    if ($PlainPass1 -notmatch "[0-9]") {
        throw "Erreur : le mot de passe doit contenir au moins un chiffre."
    }

    if ($PlainPass1 -notmatch "[^a-zA-Z0-9]") {
        throw "Erreur : le mot de passe doit contenir au moins un caractère spécial."
    }

    Write-Host ""
    Write-Host "Génération du hash BCrypt..." -ForegroundColor Cyan

    $TempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("reset_admin_" + [Guid]::NewGuid().ToString())
    New-Item -ItemType Directory -Force -Path $TempDir | Out-Null

    $JavaFile = Join-Path $TempDir "HashPassword.java"

    $JavaSource = @'
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.mindrot.jbcrypt.BCrypt;

public class HashPassword {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String password = reader.readLine();

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password vide.");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.print(hash);
    }
}
'@

    Set-Content -Path $JavaFile -Value $JavaSource -Encoding UTF8

    & javac -cp $BcryptJar $JavaFile

    if ($LASTEXITCODE -ne 0) {
        throw "Erreur : compilation Java échouée."
    }

    $ClassPath = "$TempDir;$BcryptJar"

    $HashOutput = ($PlainPass1 + "`n") | & java -cp $ClassPath HashPassword

    if ($LASTEXITCODE -ne 0) {
        throw "Erreur : génération BCrypt échouée."
    }

    $PasswordHash = (($HashOutput | Out-String).Trim())

    $PlainPass1 = $null
    $PlainPass2 = $null

    Write-Host "Hash BCrypt généré avec succès." -ForegroundColor Green

    $OldMysqlPwd = $env:MYSQL_PWD
    $env:MYSQL_PWD = $DbPass

    try {
        Write-Host ""
        Write-Host "Vérification de la structure de la table users..." -ForegroundColor Cyan

        $DbNameSql = Sql-Escape $DbName

        $HasNomQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='$DbNameSql' AND TABLE_NAME='users' AND COLUMN_NAME='nom';"
        $HasPrenomQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='$DbNameSql' AND TABLE_NAME='users' AND COLUMN_NAME='prenom';"

        $HasNom = Invoke-MySqlRaw $HasNomQuery
        $HasPrenom = Invoke-MySqlRaw $HasPrenomQuery

        $NomSql = Sql-Escape $Nom
        $PrenomSql = Sql-Escape $Prenom
        $UsernameSql = Sql-Escape $Username
        $EmailSql = Sql-Escape $Email
        $HashSql = Sql-Escape $PasswordHash

        if ($HasNom -eq "1" -and $HasPrenom -eq "1") {
            Write-Host "Colonnes nom/prenom détectées : suppression par nom + prénom." -ForegroundColor Yellow

            $DeleteCondition = "role='ADMIN' AND nom='$NomSql' AND prenom='$PrenomSql'"
            $InsertColumns = "username, email, password, role, nom, prenom"
            $InsertValues = "'$UsernameSql', '$EmailSql', '$HashSql', 'ADMIN', '$NomSql', '$PrenomSql'"
        }
        else {
            Write-Host "Colonnes nom/prenom non détectées : suppression par username." -ForegroundColor Yellow
            Write-Host "Convention utilisée : username = prenom.nom"

            $DeleteCondition = "role='ADMIN' AND username='$UsernameSql'"
            $InsertColumns = "username, email, password, role"
            $InsertValues = "'$UsernameSql', '$EmailSql', '$HashSql', 'ADMIN'"
        }

        Write-Host ""
        $Confirm = Read-Host "Confirmer la suppression puis réinsertion de cet admin ? Tape oui"

        if ($Confirm.ToLower() -ne "oui") {
            Write-Host "Opération annulée." -ForegroundColor Yellow
            exit 0
        }

        $SqlScript = @"
START TRANSACTION;

DELETE FROM users
WHERE $DeleteCondition;

SELECT ROW_COUNT() AS admins_supprimes;

INSERT INTO users ($InsertColumns)
VALUES ($InsertValues);

SELECT id, username, email, role
FROM users
WHERE username = '$UsernameSql';

COMMIT;
"@

        Write-Host ""
        Write-Host "Exécution SQL..." -ForegroundColor Cyan

        $Result = Invoke-MySqlScript $SqlScript

        Write-Host $Result
        Write-Host ""
        Write-Host "Opération terminée avec succès." -ForegroundColor Green
        Write-Host "Connexion admin :"
        Write-Host "  username     : $Username"
        Write-Host "  mot de passe : celui que tu viens de saisir"
    }
    finally {
        $env:MYSQL_PWD = $OldMysqlPwd
    }
}
catch {
    Write-Host ""
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}
finally {
    if ($TempDir -and (Test-Path $TempDir)) {
        Remove-Item -Recurse -Force $TempDir
    }
}