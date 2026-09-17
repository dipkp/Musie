<#
    Musie repair preflight automation.

    This script collects evidence and compares relevant Musie/Meld areas. It never
    copies upstream code, commits, pushes, changes versions, or creates releases.
    A provider fix must still be reviewed and phone-tested before it is applied.
#>

[CmdletBinding()]
param(
    [ValidateSet('Auto', 'YouTube', 'Spotify', 'ListenTogether', 'Build', 'Updater')]
    [string]$Scope = 'Auto',
    [string]$LogFile,
    [switch]$FetchUpstream,
    [switch]$Build,
    [switch]$InstallLatestApk,
    [switch]$CheckLatestRelease
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$reportDirectory = Join-Path $repoRoot "repair-reports\$timestamp"
$reportPath = Join-Path $reportDirectory 'Musie-Repair-Check.txt'
New-Item -ItemType Directory -Path $reportDirectory -Force | Out-Null

function Write-Report {
    param([Parameter(Mandatory)][AllowEmptyString()][string]$Message)
    Write-Host $Message
    Add-Content -LiteralPath $reportPath -Value $Message
}

function Test-CommandAvailable {
    param([Parameter(Mandatory)][string]$Name)
    return [bool](Get-Command $Name -ErrorAction SilentlyContinue)
}

function Get-CommandOutput {
    param(
        [Parameter(Mandatory)][scriptblock]$Command,
        [string]$FailureText = 'Command failed.'
    )
    try {
        $result = & $Command 2>&1
        if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) {
            Write-Report "$FailureText Exit code: $LASTEXITCODE"
        }
        return $result
    } catch {
        Write-Report "$FailureText $($_.Exception.Message)"
        return @()
    }
}

function Get-ScopePaths {
    param([Parameter(Mandatory)][string]$SelectedScope)
    switch ($SelectedScope) {
        'YouTube' {
            return @(
                'innertube/src/main/kotlin/com/metrolist/innertube/models/YouTubeClient.kt',
                'innertube/src/main/kotlin/com/metrolist/innertube/YouTube.kt',
                'app/src/main/kotlin/com/metrolist/music/utils/YTPlayerUtils.kt',
                'app/src/main/kotlin/com/metrolist/music/utils/cipher',
                'innertube/src/test/kotlin/com/metrolist/innertube/PlaybackForbiddenDiagnosticsTest.kt'
            )
        }
        'Spotify' {
            return @(
                'spotify/src/main/kotlin/com/metrolist/spotify/SpotifyAuth.kt',
                'spotify/src/main/kotlin/com/metrolist/spotify/Spotify.kt',
                'spotify/src/main/kotlin/com/metrolist/spotify/SpotifyHashProvider.kt',
                'app/src/main/kotlin/com/metrolist/music/utils/SpotifyTokenManager.kt',
                'app/src/main/kotlin/com/metrolist/music/utils/SpotifyHashSync.kt',
                'app/src/main/kotlin/com/metrolist/music/ui/screens/SpotifyLoginScreen.kt',
                'docs/spotify-gql-hashes.json',
                '.github/workflows/spotify-hash-check.yml'
            )
        }
        'ListenTogether' {
            return @(
                'app/src/main/kotlin/com/metrolist/music/listentogether',
                'app/src/main/kotlin/com/metrolist/music/service/MusicService.kt',
                'app/src/main/kotlin/com/metrolist/music/playback/PlayerConnection.kt',
                'app/src/main/kotlin/com/metrolist/music/MainActivity.kt'
            )
        }
        'Updater' {
            return @(
                'app/src/main/kotlin/com/metrolist/music/utils/Updater.kt',
                '.github/workflows/release.yml',
                'app/build.gradle.kts'
            )
        }
        default { return @() }
    }
}

function Detect-ScopeFromLog {
    param([string]$Path)
    if (-not $Path -or -not (Test-Path -LiteralPath $Path)) { return 'Auto' }
    $text = Get-Content -LiteralPath $Path -Raw
    if ($text -match 'IO_UNSPECIFIED|cipher|n-transform|YouTube|playerResponse|403') { return 'YouTube' }
    if ($text -match 'Spotify|api-partner|GraphQL|GQL|\b412\b|\b401\b') { return 'Spotify' }
    if ($text -match 'ListenTogether|WebSocket|room code|STATE_READY') { return 'ListenTogether' }
    if ($text -match 'tag_name|releases/latest|Updater') { return 'Updater' }
    if ($text -match 'FATAL EXCEPTION|BUILD FAILED|Execution failed') { return 'Build' }
    return 'Auto'
}

function Write-Section {
    param([Parameter(Mandatory)][string]$Title)
    Write-Report ''
    Write-Report "========== $Title =========="
}

Write-Report 'Musie Repair Check - evidence collection only'
Write-Report "Started: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss K')"
Write-Report "Repository: $repoRoot"

if (-not (Test-Path (Join-Path $repoRoot '.git'))) {
    throw "This script must be inside a cloned Musie repository. Expected .git at $repoRoot"
}

Write-Section 'Repository state'
Get-CommandOutput { git -C $repoRoot status --short } 'Could not read git status.' | ForEach-Object { Write-Report $_ }
Get-CommandOutput { git -C $repoRoot branch --show-current } 'Could not read current branch.' | ForEach-Object { Write-Report "Branch: $_" }
Get-CommandOutput { git -C $repoRoot remote -v } 'Could not read remotes.' | ForEach-Object { Write-Report $_ }
Get-CommandOutput { git -C $repoRoot log -5 --oneline } 'Could not read recent commits.' | ForEach-Object { Write-Report $_ }

Write-Section 'Build identity'
$gradleFile = Join-Path $repoRoot 'app\build.gradle.kts'
if (Test-Path -LiteralPath $gradleFile) {
    Select-String -Path $gradleFile -Pattern 'versionCode|versionName|applicationId' |
        ForEach-Object { Write-Report $_.Line.Trim() }
} else {
    Write-Report 'Missing app/build.gradle.kts'
}

Write-Section 'Required tools'
foreach ($command in @('git', 'java', 'adb')) {
    Write-Report "$command available: $(Test-CommandAvailable $command)"
}
if (Test-CommandAvailable 'java') {
    Get-CommandOutput { cmd.exe /d /c 'java -version 2>&1' } 'Could not read Java version.' |
        ForEach-Object { Write-Report $_ }
}
if (Test-CommandAvailable 'adb') {
    Get-CommandOutput { adb devices } 'Could not list Android devices.' | ForEach-Object { Write-Report $_ }
}

$detectedScope = if ($Scope -eq 'Auto') { Detect-ScopeFromLog $LogFile } else { $Scope }
Write-Report "Requested scope: $Scope"
Write-Report "Detected scope: $detectedScope"

if ($LogFile) {
    Write-Section 'Provided log analysis'
    if (Test-Path -LiteralPath $LogFile) {
        $copiedLog = Join-Path $reportDirectory 'input-log.txt'
        Copy-Item -LiteralPath $LogFile -Destination $copiedLog -Force
        $matches = Select-String -Path $LogFile -Pattern 'FATAL EXCEPTION|ANR|IO_UNSPECIFIED|403|401|412|Spotify|YouTube|ListenTogether|tag_name|BUILD FAILED' -CaseSensitive:$false
        if ($matches) {
            $matches | Select-Object -First 120 | ForEach-Object { Write-Report $_.Line }
        } else {
            Write-Report 'No known error signature found. Read input-log.txt manually.'
        }
    } else {
        Write-Report "Log file not found: $LogFile"
    }
}

if ($FetchUpstream) {
    Write-Section 'Fetch upstream Meld'
    $upstreamUrl = Get-CommandOutput { git -C $repoRoot remote get-url upstream } 'Upstream remote is not configured.'
    if (-not $upstreamUrl) {
        Write-Report 'Add it once with: git remote add upstream https://github.com/FrancescoGrazioso/Meld.git'
    } else {
        Get-CommandOutput { git -C $repoRoot fetch upstream --prune } 'Upstream fetch failed.' | ForEach-Object { Write-Report $_ }
    }
}

if ($detectedScope -in @('YouTube', 'Spotify', 'ListenTogether', 'Updater')) {
    Write-Section "Meld comparison - $detectedScope"
    $paths = Get-ScopePaths $detectedScope
    $upstreamHead = @(Get-CommandOutput { git -C $repoRoot rev-parse --verify upstream/main } 'upstream/main is unavailable.')
    if ($upstreamHead.Count -gt 0 -and $upstreamHead[0] -notmatch 'unavailable|fatal:') {
        Write-Report 'Recent upstream commits affecting this scope:'
        Get-CommandOutput { git -C $repoRoot log upstream/main --oneline -25 -- $paths } 'Could not inspect upstream commits.' |
            ForEach-Object { Write-Report $_ }
        Write-Report 'Files different between current main and upstream/main in this scope:'
        Get-CommandOutput { git -C $repoRoot diff --name-only main...upstream/main -- $paths } 'Could not compare relevant files.' |
            ForEach-Object { Write-Report $_ }
    }
}

if ($CheckLatestRelease) {
    Write-Section 'Public GitHub release and updater endpoint'
    try {
        if (Test-CommandAvailable 'curl.exe') {
            $releaseJson = & curl.exe --silent --show-error --fail --location --user-agent 'Musie-Repair-Check' 'https://api.github.com/repos/dipkp/Musie/releases/latest'
            if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($releaseJson)) {
                throw "GitHub latest-release request failed (curl exit code $LASTEXITCODE). Check your Internet/proxy connection."
            }
            $release = $releaseJson | ConvertFrom-Json
        } else {
            $release = Invoke-RestMethod -Uri 'https://api.github.com/repos/dipkp/Musie/releases/latest' -Headers @{ 'User-Agent' = 'Musie-Repair-Check' }
        }
        Write-Report "tag_name: $($release.tag_name)"
        Write-Report "name: $($release.name)"
        Write-Report "draft: $($release.draft)"
        Write-Report "prerelease: $($release.prerelease)"
        $release.assets | ForEach-Object { Write-Report "asset: $($_.name) - $($_.browser_download_url)" }
    } catch {
        Write-Report "Latest release check failed: $($_.Exception.Message)"
    }
}

if ($Build) {
    Write-Section 'FOSS debug build'
    $gradlew = Join-Path $repoRoot 'gradlew.bat'
    if (-not (Test-Path -LiteralPath $gradlew)) {
        Write-Report 'gradlew.bat not found. Build skipped.'
    } else {
        Push-Location $repoRoot
        try {
            & $gradlew ':app:assembleFossDebug' '--stacktrace' 2>&1 | Tee-Object -FilePath (Join-Path $reportDirectory 'build.log')
            Write-Report "Build exit code: $LASTEXITCODE"
        } finally {
            Pop-Location
        }
    }
}

Write-Section 'Latest APK'
$apk = Get-ChildItem (Join-Path $repoRoot 'app\build\outputs\apk') -Recurse -Filter '*.apk' -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($apk) {
    Write-Report "APK: $($apk.FullName)"
    Write-Report "APK timestamp: $($apk.LastWriteTime)"
    if ($InstallLatestApk) {
        if (-not (Test-CommandAvailable 'adb')) {
            Write-Report 'adb is unavailable. APK install skipped.'
        } else {
            & adb install -r $apk.FullName 2>&1 | Tee-Object -FilePath (Join-Path $reportDirectory 'install.log')
            Write-Report "Install exit code: $LASTEXITCODE"
        }
    }
} else {
    Write-Report 'No APK found. Use -Build to create one.'
}

Write-Section 'Result'
Write-Report "Report saved: $reportPath"
Write-Report 'Next: read the report, choose one exact upstream commit, then manually port only the necessary code.'
Write-Report 'This script intentionally does not auto-copy, commit, push, bump a version, or release.'
