[CmdletBinding()]
param(
    [string]$JdkHome = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot',
    [string]$SdkRoot = 'C:\Android\sdk',
    [switch]$CheckToolsOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-Native {
    param(
        [Parameter(Mandatory = $true)][string]$File,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )
    & $File @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Native tool failed (exit $LASTEXITCODE): $File"
    }
}

$root = $PSScriptRoot
$jdkBin = Join-Path $JdkHome 'bin'
$tools = Join-Path $SdkRoot 'build-tools\34.0.0'
$androidJar = Join-Path $SdkRoot 'platforms\android-34\android.jar'
$java = Join-Path $jdkBin 'java.exe'
$javac = Join-Path $jdkBin 'javac.exe'
$jar = Join-Path $jdkBin 'jar.exe'
$keytool = Join-Path $jdkBin 'keytool.exe'
$aapt2 = Join-Path $tools 'aapt2.exe'
$d8 = Join-Path $tools 'd8.bat'
$zipalign = Join-Path $tools 'zipalign.exe'
$apksigner = Join-Path $tools 'apksigner.bat'

foreach ($file in @($java, $javac, $jar, $keytool, $aapt2, $d8, $zipalign, $apksigner, $androidJar)) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Required offline build tool is missing: $file"
    }
}

# The SDK batch launchers use JAVA_HOME. Restore the caller's environment afterward.
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
$oldTemp = $env:TEMP
$oldTmp = $env:TMP
try {
    $env:JAVA_HOME = $JdkHome
    $env:PATH = "$jdkBin;$oldPath"

    if ($CheckToolsOnly) {
        Invoke-Native $java @('-version')
        Invoke-Native $javac @('-version')
        Invoke-Native $aapt2 @('version')
        Invoke-Native $d8 @('--version')
        Invoke-Native $apksigner @('version')
        Write-Host 'All required offline tools are available.'
        return
    }

    $manifest = Join-Path $root 'AndroidManifest.xml'
    $resources = Join-Path $root 'res'
    $sourceDir = Join-Path $root 'src'
    $mainActivity = Join-Path $sourceDir 'org\mehmet\litetv\MainActivity.java'
    foreach ($file in @($manifest, $mainActivity)) {
        if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
            throw "Required source file is missing: $file"
        }
    }
    if (-not (Test-Path -LiteralPath $resources -PathType Container)) {
        throw "Resource directory is missing: $resources"
    }

    # A fresh work directory prevents removed classes/resources entering later builds.
    # No source tree or pre-existing work directory is deleted by this script.
    $out = Join-Path $root 'out'
    $work = Join-Path $out ('build\' + [Guid]::NewGuid().ToString('N'))
    $generated = Join-Path $work 'generated'
    $classes = Join-Path $work 'classes'
    $dex = Join-Path $work 'dex'
    $temp = Join-Path $work 'tmp'
    $keys = Join-Path $root 'keys'
    foreach ($directory in @($out, $work, $generated, $classes, $dex, $temp, $keys)) {
        New-Item -ItemType Directory -Path $directory -Force | Out-Null
    }
    $env:TEMP = $temp
    $env:TMP = $temp

    $compiledResources = Join-Path $work 'resources.zip'
    $resourceApk = Join-Path $work 'resources.apk'
    $classJar = Join-Path $work 'classes.jar'
    $unsigned = Join-Path $work 'LiteTV-unsigned.apk'
    $aligned = Join-Path $work 'LiteTV-aligned.apk'
    $signed = Join-Path $out 'LiteTV-signed.apk'

    Write-Host 'Compiling framework resources...'
    Invoke-Native $aapt2 @('compile', '--dir', $resources, '-o', $compiledResources)
    Invoke-Native $aapt2 @('link', '-o', $resourceApk, '-I', $androidJar,
        '--manifest', $manifest, '--java', $generated, '--min-sdk-version', '23',
        '--target-sdk-version', '34', $compiledResources)

    $sources = @(Get-ChildItem -LiteralPath $sourceDir -Filter '*.java' -Recurse -File)
    $sources += @(Get-ChildItem -LiteralPath $generated -Filter '*.java' -Recurse -File)
    # javac argfiles preserve paths containing spaces and avoid command-line limits.
    $sourceArguments = @($sources | Sort-Object FullName | ForEach-Object {
        '"' + $_.FullName.Replace('\', '/') + '"'
    })
    $sourceList = Join-Path $work 'sources.txt'
    [IO.File]::WriteAllLines($sourceList, [string[]]$sourceArguments, (New-Object Text.UTF8Encoding($false)))
    Write-Host 'Compiling Java 8 bytecode with JDK 17...'
    Invoke-Native $javac @('-encoding', 'UTF-8', '-source', '8', '-target', '8',
        '-classpath', $androidJar, '-d', $classes, ('@' + $sourceList))
    Invoke-Native $jar @('--create', '--file', $classJar, '--no-manifest', '-C', $classes, '.')

    Write-Host 'Converting bytecode and packaging APK...'
    Invoke-Native $d8 @('--min-api', '23', '--lib', $androidJar, '--output', $dex, $classJar)
    $dexFiles = @(Get-ChildItem -LiteralPath $dex -Filter 'classes*.dex' -File | Sort-Object Name)
    if ($dexFiles.Count -eq 0) { throw 'D8 produced no classes.dex.' }
    Copy-Item -LiteralPath $resourceApk -Destination $unsigned
    $jarArguments = @('--update', '--file', $unsigned)
    foreach ($dexFile in $dexFiles) {
        $jarArguments += @('-C', $dex, $dexFile.Name)
    }
    Invoke-Native $jar $jarArguments
    Invoke-Native $zipalign @('-f', '4', $unsigned, $aligned)

    $keystore = Join-Path $keys 'debug.keystore'
    if (-not (Test-Path -LiteralPath $keystore -PathType Leaf)) {
        # Conventional LOCAL DEBUG credentials, not a production signing identity.
        Invoke-Native $keytool @('-genkeypair', '-noprompt', '-keystore', $keystore,
            '-storetype', 'JKS', '-storepass', 'android', '-keypass', 'android',
            '-alias', 'androiddebugkey', '-keyalg', 'RSA', '-keysize', '2048',
            '-validity', '10000', '-dname', 'CN=Android Debug,O=Android,C=US')
    }
    Write-Host 'Signing with the persistent local debug key...'
    Invoke-Native $apksigner @('sign', '--ks', $keystore, '--ks-key-alias', 'androiddebugkey',
        '--ks-pass', 'pass:android', '--key-pass', 'pass:android', '--out', $signed, $aligned)
    Invoke-Native $zipalign @('-c', '4', $signed)
    Invoke-Native $apksigner @('verify', '--verbose', $signed)
    Write-Host "Built and verified: $signed"
}
finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
    $env:TEMP = $oldTemp
    $env:TMP = $oldTmp
}
