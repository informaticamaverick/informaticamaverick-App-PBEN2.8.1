@echo off
echo Compilando e instalando App del Prestador...
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set ANDROID_HOME=C:\Users\maver\AppData\Local\Android\Sdk
set PATH=%ANDROID_HOME%\platform-tools;%PATH%
gradlew.bat :prestador:installDebug
echo.
echo Compilacion terminada!
pause
