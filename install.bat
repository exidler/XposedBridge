cd /d %~dp0
dir out\production\XposedBridge\XposedBridge.apk
adb push out\production\XposedBridge\XposedBridge.apk /data/local/tmp
adb shell su -c 'mv /data/local/tmp/XposedBridge.apk /data/xposed/XposedBridge.jar.newversion'
pause
