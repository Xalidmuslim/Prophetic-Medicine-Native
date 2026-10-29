# Maestro — golden-derived Android E2E

**Source base:** `golden/medicine-smooth-2026-10-10` at `9a3b7cac1f24926966578e11248f0ac0e3f02a76`.

The protected golden branch is never changed. This QA experiment lives only on `qa/maestro-e2e-2026-10-30`; do not merge anything into the golden branch. `main` is the historical old app, not the approved baseline.

## Covered (initial smoke suite)
- Launch verified home and first-page content.
- Open book contents; return and open search.

**Not covered yet:** bookmarks, reading persistence, all topic paths and cards, full text/search correctness, animation jank or real-device 120 FPS. These need separate test fixtures and actual profiling.

## Local command
Build debug APK from the QA branch with Gradle 8.11.1 and JDK 17, install on an Android emulator, then run:

```sh
maestro test .maestro
```

GitHub Actions workflow runs on QA branch pushes and manual dispatch. `clearState` wipes only the target emulator's app data; never use a phone with personal bookmarks or notes.
