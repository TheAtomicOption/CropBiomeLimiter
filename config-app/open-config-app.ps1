param(
	[int]$PreferredPort = 41731,
	[switch]$NoBrowser
)

$ErrorActionPreference = "Stop"
$appDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$indexPath = Join-Path $appDirectory "index.html"

if (-not (Test-Path -LiteralPath $indexPath -PathType Leaf)) {
	throw "index.html was not found beside this launcher."
}

function Get-FreePort {
	param([int]$StartPort)

	for ($port = $StartPort; $port -lt ($StartPort + 100); $port++) {
		$probe = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $port)
		try {
			$probe.Start()
			$probe.Stop()
			return $port
		} catch {
			try {
				$probe.Stop()
			} catch {
			}
		}
	}

	throw "No available local port was found."
}

function Get-ContentType {
	param([string]$Path)

	switch ([System.IO.Path]::GetExtension($Path).ToLowerInvariant()) {
		".html" { return "text/html; charset=utf-8" }
		".css" { return "text/css; charset=utf-8" }
		".js" { return "text/javascript; charset=utf-8" }
		".json" { return "application/json; charset=utf-8" }
		".cmd" { return "text/plain; charset=utf-8" }
		".ps1" { return "text/plain; charset=utf-8" }
		default { return "application/octet-stream" }
	}
}

function Get-SafeFilePath {
	param([string]$RawPath)

	$requestPath = $RawPath.Split("?")[0]
	if ([string]::IsNullOrWhiteSpace($requestPath) -or $requestPath -eq "/") {
		$requestPath = "/index.html"
	}

	$relativePath = [System.Uri]::UnescapeDataString($requestPath.TrimStart("/"))
	$relativePath = $relativePath.Replace("/", [System.IO.Path]::DirectorySeparatorChar)
	$combinedPath = [System.IO.Path]::GetFullPath((Join-Path $appDirectory $relativePath))
	$rootPath = [System.IO.Path]::GetFullPath($appDirectory + [System.IO.Path]::DirectorySeparatorChar)

	if (-not $combinedPath.StartsWith($rootPath, [System.StringComparison]::OrdinalIgnoreCase)) {
		return $null
	}

	return $combinedPath
}

function Write-HttpResponse {
	param(
		[System.IO.Stream]$Stream,
		[int]$StatusCode,
		[string]$StatusText,
		[byte[]]$Body,
		[string]$ContentType
	)

	$headers = "HTTP/1.1 $StatusCode $StatusText`r`nContent-Type: $ContentType`r`nContent-Length: $($Body.Length)`r`nCache-Control: no-store`r`nConnection: close`r`n`r`n"
	$headerBytes = [System.Text.Encoding]::ASCII.GetBytes($headers)
	$Stream.Write($headerBytes, 0, $headerBytes.Length)
	if ($Body.Length -gt 0) {
		$Stream.Write($Body, 0, $Body.Length)
	}
}

$port = Get-FreePort -StartPort $PreferredPort
$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $port)
$listener.Start()
$url = "http://127.0.0.1:$port/index.html"

Write-Host ""
Write-Host "Crop Biome Limiter Config App is ready."
Write-Host "Opening $url"
Write-Host "Keep this window open while using the app. Close it when you are done."
Write-Host ""

if (-not $NoBrowser) {
	Start-Process $url
}

try {
	while ($true) {
		$client = $listener.AcceptTcpClient()
		try {
			$stream = $client.GetStream()
			$reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::ASCII, $false, 1024, $true)
			$requestLine = $reader.ReadLine()
			while ($true) {
				$line = $reader.ReadLine()
				if ($null -eq $line -or $line.Length -eq 0) {
					break
				}
			}

			if ([string]::IsNullOrWhiteSpace($requestLine)) {
				continue
			}

			$parts = $requestLine.Split(" ")
			if ($parts.Length -lt 2 -or $parts[0] -ne "GET") {
				$body = [System.Text.Encoding]::UTF8.GetBytes("Only GET requests are supported.")
				Write-HttpResponse -Stream $stream -StatusCode 405 -StatusText "Method Not Allowed" -Body $body -ContentType "text/plain; charset=utf-8"
				continue
			}

			$filePath = Get-SafeFilePath -RawPath $parts[1]
			if ($null -eq $filePath -or -not (Test-Path -LiteralPath $filePath -PathType Leaf)) {
				$body = [System.Text.Encoding]::UTF8.GetBytes("Not found.")
				Write-HttpResponse -Stream $stream -StatusCode 404 -StatusText "Not Found" -Body $body -ContentType "text/plain; charset=utf-8"
				continue
			}

			$body = [System.IO.File]::ReadAllBytes($filePath)
			Write-HttpResponse -Stream $stream -StatusCode 200 -StatusText "OK" -Body $body -ContentType (Get-ContentType -Path $filePath)
		} catch {
			try {
				$body = [System.Text.Encoding]::UTF8.GetBytes("The config app server hit an internal error.")
				Write-HttpResponse -Stream $stream -StatusCode 500 -StatusText "Internal Server Error" -Body $body -ContentType "text/plain; charset=utf-8"
			} catch {
			}
		} finally {
			$client.Close()
		}
	}
} finally {
	$listener.Stop()
}
