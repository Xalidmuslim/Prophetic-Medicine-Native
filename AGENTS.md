# Codex instructions — Prophetic Medicine Native

## Current user-verified golden baseline (10 October 2026)
- **For any new work on the currently installed, redesigned “Медицина Пророка” app, start ONLY from** `golden/medicine-smooth-2026-10-10` at exact SHA `9a3b7cac1f24926966578e11248f0ac0e3f02a76`, **not** from `main` or old `modulecopy-preview`. This is the optimised Distinct Paper Android Release build whose first launch, home scrolling, reader scrolling and horizontal chapter swipes the user verified as smooth on their phone.
- Full performance invariant and QA rules: **`PERFORMANCE_GOLDEN_BASELINE.md`** in `main`; read it before changes.
- The `golden` branch is a recovery reference: **never commit to it, delete it, reset it or move it**. Create a new feature/fix branch at the exact SHA for each update. Do not merge blindly to `main` (it deliberately retains the historical original app).
- Preserve `MedicineHomeArtwork` background bitmap preloading, independent painters, no per-card offscreen FBO, R8-enabled release build, original antique artwork, and the already-smooth lazy Reader/swipes. Regression test real-device first launch and both scroll directions; a successful Debug build is not enough. Never declare a newer golden version without user confirmation.
- Only use the original `main` native v1.0 baseline if the user *explicitly asks* to return to that historical version.

## Baseline / scope
- This repository is the **standalone native Android** project "Медицина Пророка".
- Preserve the restored original Android app baseline in `main`: `versionCode 1`, `versionName '1.0.0-modulecopy-preview'`. **Do not redesign or change app code, content, navigation, version, or resources unless the user explicitly requests that change.**
- Never edit the separate medicine module in `Xalidmuslim/AlFatiha-Native` or other repositories as part of this task.

## When the user requests graphics
- Support the full asset workflow, not just a text prompt: **create or obtain the requested pictures (when an image-generation tool, supplied image, or authorized source is available), save the actual image files, import them into Android where requested, and make all resulting images separately obtainable by the user**.
- Check first whether image generation is actually available in the current Codex session. Use its image-generation capability when available. If unavailable, say so clearly and request source image files or a usable image-generation connection. **Never claim images were generated, delivered, or imported unless the files exist and have been verified.**
- Use one independent final image file per UI item: home hero, card background, treatment illustration, reading decoration, icons, etc. Do not use a composite icon sheet directly in Android; crop/extract each icon, preserve alpha transparency and a safe margin, and check for neighboring fragments.
- Respect the design the user specifies for this iteration. Default only when unspecified: understated warm cream/sand surfaces, graphite text, very restrained muted green, uncluttered minimalism. Avoid excessive foliage, ornamental borders, gradients, shadows, stock imagery and embedded text in backgrounds.
- Distinguish raster artwork (PNG/WebP, optimised for phone size) from simple UI symbols (prefer Compose vector icons where suitable). Use genuine transparent PNG/WebP for pictorial icons. Ensure no stretched pictures, text overlap, or clipped subjects on narrow Android screens.
- Store imported Android artwork under `app/src/main/res/drawable-nodpi/` or another appropriate Android resource directory, with stable lowercase_snake_case filenames. Keep any editable/source assets in a clearly labelled folder if supplied or generated. Verify image dimensions, opacity/alpha, file size, paths, and that every referenced resource exists.
- Only when explicitly asked to implement the design, wire assets into native Kotlin/Compose and build/test the resulting APK. Do not alter `book.json`, bookmarks/notes logic, or the baseline version unnecessarily.
- When asked to "send all images", provide **each actual image file**, its filename, and a usable download or repository URL. Also supply one ZIP of the image assets if the session supports file-artifact delivery, plus a contact sheet preview when useful. Do not present prompts or preview thumbnails as substitutes for downloadable files.
- In the completion report distinguish verified file creation/import/build/device QA from any incomplete step. If artifact upload or downloadable image links are unavailable in the current environment, say so instead of inventing links.
