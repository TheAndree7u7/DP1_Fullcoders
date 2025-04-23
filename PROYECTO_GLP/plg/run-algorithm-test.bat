@echo off
echo Running algorithm tests with profile: test-algorithm
cd e:\PROYECTOS\DP1\DP1_2025\DP1_Fullcoders\PROYECTO_GLP\plg
REM Using Maven wrapper from the parent directory
..\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test-algorithm
pause

