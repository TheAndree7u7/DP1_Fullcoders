@echo off
echo Empaquetando el backend (Maven)...
cd Back-end\plg
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo Error al empaquetar el backend
    exit /b 1
)
cd ..\..\..

echo Construyendo y levantando los contenedores Docker...
docker-compose build
docker-compose up -d
if errorlevel 1 (
    echo Error al levantar los contenedores
    exit /b 1
)
echo ¡Backend y frontend desplegados correctamente en Docker!