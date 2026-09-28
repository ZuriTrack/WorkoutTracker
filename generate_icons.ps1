Add-Type -AssemblyName System.Drawing

$sourcePath = "C:\Users\THN_LAB\.gemini\antigravity\brain\712ddd1e-e2c5-49a0-9ebf-388e6a847bcb\.user_uploaded\media_1790616550369.jpg"
$resDir = "C:\Users\THN_LAB\.gemini\antigravity\scratch\workout_tracker\app\src\main\res"

$srcImage = [System.Drawing.Image]::FromFile($sourcePath)

function Resize-And-Save($src, $w, $h, $outPath) {
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $g.Clear([System.Drawing.Color]::Black)
    $g.DrawImage($src, 0, 0, $w, $h)
    $g.Dispose()
    
    if (Test-Path $outPath) { Remove-Item $outPath -Force }
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Created: $outPath ($w x $h)"
}

# 1. Standard launcher icons
$densities = @{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

# 2. Adaptive foreground icons (108dp)
$foregroundDensities = @{
    "mipmap-mdpi" = 108
    "mipmap-hdpi" = 162
    "mipmap-xhdpi" = 216
    "mipmap-xxhdpi" = 324
    "mipmap-xxxhdpi" = 432
}

# Remove old webp icons
Get-ChildItem -Path $resDir -Recurse -Include "ic_launcher*.webp" | Remove-Item -Force

foreach ($d in $densities.Keys) {
    $size = $densities[$d]
    $dir = Join-Path $resDir $d
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }
    
    Resize-And-Save $srcImage $size $size (Join-Path $dir "ic_launcher.png")
    Resize-And-Save $srcImage $size $size (Join-Path $dir "ic_launcher_round.png")
}

foreach ($d in $foregroundDensities.Keys) {
    $size = $foregroundDensities[$d]
    $dir = Join-Path $resDir $d
    Resize-And-Save $srcImage $size $size (Join-Path $dir "ic_launcher_foreground.png")
}

$srcImage.Dispose()
Write-Host "All icons generated successfully!"
