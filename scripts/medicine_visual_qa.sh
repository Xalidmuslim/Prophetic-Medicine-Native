#!/usr/bin/env bash
set -euo pipefail
mkdir -p screenshots
PACKAGE=com.xalid.meditsinaproroka.nativeapp.premiumreview
ACTIVITY=com.xalid.meditsinaproroka.nativeapp.MainActivity
adb shell am start -n "$PACKAGE/$ACTIVITY"
sleep 6
adb shell pidof "$PACKAGE"
adb shell screencap -p /sdcard/medicine_home.png
adb pull /sdcard/medicine_home.png screenshots/home.png
SIZE=$(adb shell wm size | tr -d '\r' | grep 'Physical size' | sed -E 's/.*: ([0-9]+)x([0-9]+)/\1 \2/')
read -r W H <<< "$SIZE"
NAV_Y=$((H-135))
adb shell input tap $((W*9/10)) "$NAV_Y"
sleep 2
adb shell screencap -p /sdcard/medicine_more.png
adb pull /sdcard/medicine_more.png screenshots/more.png
adb shell input tap $((W*5/10)) "$NAV_Y"
sleep 2
adb shell screencap -p /sdcard/medicine_search.png
adb pull /sdcard/medicine_search.png screenshots/search.png
adb shell input tap $((W*3/10)) "$NAV_Y"
sleep 2
adb shell screencap -p /sdcard/medicine_topics.png
adb pull /sdcard/medicine_topics.png screenshots/topics.png
adb shell input tap $((W/10)) "$NAV_Y"
sleep 2
adb shell input swipe $((W/2)) $((H*3/4)) $((W/2)) $((H/3)) 380
sleep 2
adb shell screencap -p /sdcard/medicine_sections.png
adb pull /sdcard/medicine_sections.png screenshots/home_sections.png
adb shell logcat -d -t 800 '*:E' > screenshots/logcat_errors.txt || true
file screenshots/*.png
