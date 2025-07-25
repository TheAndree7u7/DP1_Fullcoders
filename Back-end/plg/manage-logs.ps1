# Script para gestionar logs de la aplicación PLG
# Uso: .\manage-logs.ps1 [comando]

param(
    [Parameter(Position=0)]
    [string]$Command = "help"
)

$LogDir = "logs"
$LogFile = "$LogDir\application.log"

# Configurar codificación UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

function Show-Help {
    Write-Host "=== Gestor de Logs PLG ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Comandos disponibles:" -ForegroundColor Yellow
    Write-Host "  help     - Mostrar esta ayuda" -ForegroundColor White
    Write-Host "  show     - Mostrar contenido del log" -ForegroundColor White
    Write-Host "  tail     - Mostrar últimas 20 líneas" -ForegroundColor White
    Write-Host "  watch    - Monitorear logs en tiempo real" -ForegroundColor White
    Write-Host "  clear    - Limpiar archivo de log" -ForegroundColor White
    Write-Host "  size     - Mostrar tamaño del archivo" -ForegroundColor White
    Write-Host "  errors   - Mostrar solo errores" -ForegroundColor White
    Write-Host "  debug    - Mostrar logs de debug" -ForegroundColor White
    Write-Host "  info     - Mostrar logs de info" -ForegroundColor White
    Write-Host "  search   - Buscar texto específico" -ForegroundColor White
    Write-Host ""
    Write-Host "Ejemplos:" -ForegroundColor Yellow
    Write-Host "  .\manage-logs.ps1 show" -ForegroundColor Gray
    Write-Host "  .\manage-logs.ps1 tail" -ForegroundColor Gray
    Write-Host "  .\manage-logs.ps1 search 'ERROR'" -ForegroundColor Gray
}

function Test-LogFile {
    if (!(Test-Path $LogFile)) {
        Write-Host "❌ Archivo de log no encontrado: $LogFile" -ForegroundColor Red
        Write-Host "Asegúrate de que la aplicación esté ejecutándose." -ForegroundColor Yellow
        return $false
    }
    return $true
}

function Show-LogContent {
    if (!(Test-LogFile)) { return }
    
    Write-Host "=== Contenido del archivo de log ===" -ForegroundColor Cyan
    Get-Content $LogFile -Encoding UTF8
}

function Show-LogTail {
    if (!(Test-LogFile)) { return }
    
    Write-Host "=== Últimas 20 líneas del log ===" -ForegroundColor Cyan
    Get-Content $LogFile -Tail 20 -Encoding UTF8
}

function Watch-LogFile {
    if (!(Test-LogFile)) { return }
    
    Write-Host "=== Monitoreando logs en tiempo real (Ctrl+C para salir) ===" -ForegroundColor Cyan
    Get-Content $LogFile -Wait -Encoding UTF8
}

function Clear-LogFile {
    if (!(Test-LogFile)) { return }
    
    Clear-Content $LogFile
    Write-Host "✅ Archivo de log limpiado" -ForegroundColor Green
}

function Show-LogSize {
    if (!(Test-LogFile)) { return }
    
    $size = (Get-Item $LogFile).Length
    $sizeKB = [math]::Round($size / 1KB, 2)
    $sizeMB = [math]::Round($size / 1MB, 2)
    
    Write-Host "=== Tamaño del archivo de log ===" -ForegroundColor Cyan
    Write-Host "Bytes: $size" -ForegroundColor White
    Write-Host "KB: $sizeKB" -ForegroundColor White
    Write-Host "MB: $sizeMB" -ForegroundColor White
}

function Show-Errors {
    if (!(Test-LogFile)) { return }
    
    Write-Host "=== Logs de ERROR ===" -ForegroundColor Cyan
    Get-Content $LogFile -Encoding UTF8 | Select-String "ERROR" | ForEach-Object {
        Write-Host $_ -ForegroundColor Red
    }
}

function Show-DebugLogs {
    if (!(Test-LogFile)) { return }
    
    Write-Host "=== Logs de DEBUG ===" -ForegroundColor Cyan
    Get-Content $LogFile -Encoding UTF8 | Select-String "DEBUG" | ForEach-Object {
        Write-Host $_ -ForegroundColor Blue
    }
}

function Show-InfoLogs {
    if (!(Test-LogFile)) { return }
    
    Write-Host "=== Logs de INFO ===" -ForegroundColor Cyan
    Get-Content $LogFile -Encoding UTF8 | Select-String "INFO" | ForEach-Object {
        Write-Host $_ -ForegroundColor Green
    }
}

function Search-LogContent {
    param([string]$SearchTerm)
    
    if (!(Test-LogFile)) { return }
    
    if ([string]::IsNullOrEmpty($SearchTerm)) {
        Write-Host "❌ Debes especificar un término de búsqueda" -ForegroundColor Red
        Write-Host "Uso: .\manage-logs.ps1 search 'texto'" -ForegroundColor Yellow
        return
    }
    
    Write-Host "=== Buscando '$SearchTerm' en logs ===" -ForegroundColor Cyan
    Get-Content $LogFile -Encoding UTF8 | Select-String $SearchTerm | ForEach-Object {
        Write-Host $_ -ForegroundColor Yellow
    }
}

# Ejecutar comando
switch ($Command.ToLower()) {
    "help" { Show-Help }
    "show" { Show-LogContent }
    "tail" { Show-LogTail }
    "watch" { Watch-LogFile }
    "clear" { Clear-LogFile }
    "size" { Show-LogSize }
    "errors" { Show-Errors }
    "debug" { Show-DebugLogs }
    "info" { Show-InfoLogs }
    "search" { 
        if ($args.Count -gt 0) {
            Search-LogContent $args[0]
        } else {
            Write-Host "❌ Debes especificar un término de búsqueda" -ForegroundColor Red
        }
    }
    default {
        Write-Host "❌ Comando no reconocido: $Command" -ForegroundColor Red
        Show-Help
    }
} 