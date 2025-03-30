@echo off
call mvnw.cmd clean install
if %errorlevel% equ 0 (
    call mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m"
)