# Reference artwork

The home screen remains native Android/Compose. All text, counters, progress,
frames, button click targets and navigation are native views. The supplied
reference is a visual guide, never a flattened screen background.

## Independent Android resources

`app/src/main/res/drawable-nodpi/` contains generated, optimized WebP artwork:

- Main icons: `antique_open_book`, `antique_search`, `antique_mortar`,
  `antique_books`, `antique_scroll`, `antique_remedies`, `antique_bookmark`.
- Navigation: `nav_home`, `nav_topics`, `nav_search`, `nav_bookmark`, `nav_more`.
- Backgrounds/decorations: `antique_hero`, `antique_hero_scene`,
  `antique_parchment`, `antique_card_paper`, `antique_button`, `antique_foliage`.

Every pictorial icon is a separate 512×512 transparent bitmap with safe inner
margins. The original generated PNGs are supplied in the delivery asset ZIP;
the navigation atlas was extracted into independent images before import.
The existing application logo remains unchanged.

Calm paper regions sampled from the reference have median RGB (233, 218, 200)
for the page and (241, 231, 216) for the reading card. The paper exports and
native light surfaces use these measured tones (`#E9DAC8`, `#F1E7D8`).

## Reproduce exports

Host requirements: Python 3, Pillow, NumPy. Android builds do not require them.

```sh
python3 scripts/prepare_artwork.py /path/to/raw/pngs
python3 -m unittest discover -s tests
```

The export script trims negligible alpha specks, preserves aspect ratios,
normalizes visible scale, exports optimized WebP, and derives the independent
card paper and right-anchored hero crop. Card frames remain scalable native
shapes. These image-integrity tests are host tests, not Android UI tests.

The baseline book data, reader, store, routes, search bridge, version and
application ID are preserved. Actual device validation and build evidence are
reported separately in the delivery report.

## Reference refinement (2026-10-09)

The refinement keeps the original content/store/navigation unchanged. New generated
`antique_hero_refined.webp` is a full-width 3.45:1 composition; native text stays separate.
`antique_card_frame.9.png` preserves fixed corners and shallow paper edges through
Android nine-patch stretch strips. `antique_card_paper.webp` is a separate fiber
texture. Seven `_detail.webp` icons retain their complete alpha contours with 24px
transparent margins in proportionate rectangular viewports; no pictorial icon is
replaced by a flat symbol.

Reproduce exports with:
`python3 scripts/prepare_refinement_artwork.py /path/to/refinement/raw`
The two generated source PNGs are delivered separately from Android exports.
