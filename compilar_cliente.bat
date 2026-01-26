@echo off
echo Compilando e instalando App del Cliente...
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set ANDROID_HOME=C:\Users\maver\AppData\Local\Android\Sdk
set PATH=%ANDROID_HOME%\platform-tools;%PATH%
gradlew.bat :app:installDebug
echo.
echo Lanzando la app...
adb shell am start -n com.example.myapplication/.MainActivity
echo.
echo App iniciada!
pause
