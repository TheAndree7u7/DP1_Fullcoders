@echo off
echo.
echo 📄 Mostrando archivo de logs...
echo 📁 Ubicación: %CD%\Back-end\logs\application.log
echo.

if exist "Back-end\logs\application.log" (
    echo ✅ Archivo de logs encontrado
    echo.
    echo ================================================
    echo CONTENIDO DEL ARCHIVO DE LOGS:
    echo ================================================
    type "Back-end\logs\application.log"
    echo.
    echo ================================================
    echo FIN DEL ARCHIVO DE LOGS
    echo ================================================
) else (
    echo ❌ No se encontró el archivo de logs
    echo 💡 Ejecuta primero la aplicación para generar logs
)

echo.
echo 🔄 Para actualizar, ejecuta este script de nuevo
pause 