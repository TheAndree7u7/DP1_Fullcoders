@echo off
setlocal enabledelayedexpansion

REM =============================
echo [1/3] Empaquetando el backend (Maven)...
cd Back-end\plg
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Fallo al empaquetar el backend. Revisa la salida anterior para detalles.
    pause
    exit /b 1
)
cd ..\..\

REM Usar ruta absoluta del docker-compose.yml
set DOCKER_COMPOSE_FILE=%~dp0docker-compose.yml

echo [2/3] Construyendo las imagenes Docker...
docker-compose -f "%DOCKER_COMPOSE_FILE%" build
if errorlevel 1 (
    echo [ERROR] Fallo al construir las imagenes Docker. Revisa la salida anterior para detalles.
    pause
    exit /b 1
)

echo [3/3] Levantando los contenedores Docker...
docker-compose -f "%DOCKER_COMPOSE_FILE%" up -d
if errorlevel 1 (
    echo [ERROR] Fallo al levantar los contenedores Docker. Revisa la salida anterior para detalles.
    pause
    exit /b 1
)

echo [OK] ¡Backend y frontend desplegados correctamente en Docker!
endlocal
