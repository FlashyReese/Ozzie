@ECHO off
echo "Gradlew Wrapper..."
call wrapper.bat
echo "Gradlew fatJar..."
call fatjar.bat
cd build\libs
dir Ozzie.jar /s /p

PAUSE