@echo off
echo ========================================
echo    REINICIANDO APLICACION CON MEJORAS
echo ========================================

echo.
echo 1. Deteniendo contenedores Docker...
docker-compose down

echo.
echo 2. Compilando aplicacion backend...
cd Back-end/plg
call mvnw clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en la compilacion
    pause
    exit /b 1
)

echo.
echo 3. Volviendo al directorio raiz...
cd ../..

echo.
echo 4. Reconstruyendo imagenes Docker...
docker-compose build --no-cache

echo.
echo 5. Iniciando aplicacion...
docker-compose up -d

echo.
echo 6. Esperando a que la aplicacion inicie...
timeout /t 10 /nobreak > nul

echo.
echo 7. Mostrando logs del backend...
docker-compose logs -f backend

echo.
echo ========================================
echo      APLICACION REINICIADA
echo ========================================
echo.
echo Frontend: http://localhost:5173
echo Backend: http://localhost:8080
echo.

pause 