# Floot source archive — reference only

Original source for the user-owned Floot project "Медицина Пророка ﷺ":
- Floot project ID: `fc33ed3b-05ad-41b1-a847-2522dc79982d`
- Floot source version: `1790544415015` (2026-09-27)
- Recorded snapshot: 245 text files, 422888 characters, validated file-by-file against Floot's reported lengths.
- Original Floot project remains unchanged.

**This is React/TypeScript PWA source, NOT the source of the separately installed native Android APK.**

The Floot project's full Russian book text is loaded from an externally hosted asset
`/_cdn/static/82da772c-ee48-4088-b869-a69fb41348aa-tibb_full_ru_work.md`;
it is NOT included here. Floot-specific runtime dependencies and generated asset storage
may be required to execute the web source as-is.

A saved standalone Android v1.0.0 APK is a native Jetpack Compose APK containing an
`assets/book.json`, not a WebView wrapper. Its exact original Kotlin project has not
been recovered. Do not claim that this folder can reproduce the installed APK.
The Kotlin native app must either be recovered from its original source or rebuilt
separately after auditing parity with the installed version.

NEVER edit the embedded `medicine` module in `Xalidmuslim/AlFatiha-Native`.
The standalone Android program is an independent application with its own lifecycle.
Do not copy copyrighted translations or APK signing secrets into this public repository
without verifying authorization and security.
