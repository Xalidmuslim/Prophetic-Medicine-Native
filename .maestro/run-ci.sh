#!/usr/bin/env bash
# Isolate the golden baseline home test from reader/navigation and retain diagnostics on failure.
set -eo pipefail
on_exit() {
  adb devices -l > adb-final.txt 2>&1 || true
  timeout 20s adb logcat -d -v threadtime > android-logcat.txt 2>&1 || true
}
trap on_exit EXIT

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm path com.xalid.meditsinaproroka.nativeapp.modulecopy.firstframe
maestro test .maestro/01-home-smoke.yaml 2>&1 | tee maestro-home.log
maestro test .maestro/02-content-navigation.yaml 2>&1 | tee maestro-content.log
