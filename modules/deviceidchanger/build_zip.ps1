Add-Type -AssemblyName System.IO.Compression.FileSystem
$src = 'C:\Users\anderson\Projetos\UnlockXiaomi\modules\deviceidchanger\module'
$dst = 'C:\Users\anderson\Projetos\UnlockXiaomi\modules\deviceidchanger\DeviceID-Plus.zip'
if (Test-Path $dst) { Remove-Item $dst }
$zip = [IO.Compression.ZipFile]::Open($dst, 'Create')
Get-ChildItem -Recurse -File $src | ForEach-Object {
  $rel = $_.FullName.Substring($src.Length + 1).Replace('\', '/')
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, $rel) | Out-Null
}
$zip.Dispose()
[IO.Compression.ZipFile]::OpenRead($dst).Entries | Select-Object -ExpandProperty FullName
