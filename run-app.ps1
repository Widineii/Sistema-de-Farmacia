$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$outputDir = Join-Path $projectRoot "out"
$sqliteJar = Join-Path $env:USERPROFILE ".m2\repository\org\xerial\sqlite-jdbc\3.45.3.0\sqlite-jdbc-3.45.3.0.jar"
$slf4jApiJar = Join-Path $env:USERPROFILE ".m2\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar"
$slf4jJar = Join-Path $env:USERPROFILE ".m2\repository\org\slf4j\slf4j-nop\1.7.36\slf4j-nop-1.7.36.jar"

if (!(Test-Path $sqliteJar)) {
    throw "Dependencia nao encontrada: $sqliteJar"
}

if (!(Test-Path $slf4jApiJar)) {
    throw "Dependencia nao encontrada: $slf4jApiJar"
}

if (!(Test-Path $slf4jJar)) {
    throw "Dependencia nao encontrada: $slf4jJar"
}

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$sources = Get-ChildItem -Path (Join-Path $projectRoot "src\main\java") -Recurse -Filter *.java |
    Select-Object -ExpandProperty FullName

$classPath = "$sqliteJar;$slf4jApiJar;$slf4jJar"

& javac -encoding UTF-8 -cp $classPath -d $outputDir $sources

if ($LASTEXITCODE -ne 0) {
    throw "Falha ao compilar o projeto."
}

& java -cp "$outputDir;$classPath" com.farmacia.view.LoginView
