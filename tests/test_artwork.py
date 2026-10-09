"""Checks imported artwork integrity; run with python3 -m unittest discover -s tests."""
import unittest
from pathlib import Path
from PIL import Image
ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / 'app/src/main/res/drawable-nodpi'
ICONS = ['antique_open_book', 'antique_search', 'antique_mortar', 'antique_books', 'antique_scroll', 'antique_remedies', 'antique_bookmark', 'nav_home', 'nav_topics', 'nav_search', 'nav_bookmark', 'nav_more']
class ArtworkTest(unittest.TestCase):
    def test_reading_book_survives_the_shorter_icon_viewport(self):
        with Image.open(ART/'antique_open_book.webp') as im:
            bbox=im.getchannel('A').point(lambda a: 255 if a >= 32 else 0).getbbox()
        # A 52×42 dp Crop viewport trims 49 px of the 512 px square at each end.
        self.assertGreaterEqual(bbox[1],49)
        self.assertLessEqual(bbox[3],463)

    def test_pictorial_icons_are_transparent_and_have_safe_margins(self):
        for name in ICONS:
            with self.subTest(asset=name):
                path = ART / (name + '.webp')
                self.assertTrue(path.is_file(), f'Missing independent raster: {name}')
                im = Image.open(path).convert('RGBA')
                self.assertEqual(im.size, (512, 512))
                alpha = im.getchannel('A')
                self.assertEqual(alpha.getextrema(), (0, 255))
                bbox = alpha.point(lambda a: 255 if a >= 32 else 0).getbbox()
                self.assertIsNotNone(bbox)
                self.assertGreaterEqual(min(bbox[0],bbox[1],512-bbox[2],512-bbox[3]), 55)
                self.assertGreater(max(bbox[2]-bbox[0],bbox[3]-bbox[1]), 340)
                self.assertLess(path.stat().st_size, 250000)
    def test_backgrounds_are_small_real_independent_bitmaps(self):
        for name in ['antique_hero', 'antique_hero_scene', 'antique_parchment', 'antique_card_paper', 'antique_button', 'antique_foliage']:
            with self.subTest(asset=name):
                path=ART/(name+'.webp')
                self.assertTrue(path.is_file(), f'Missing layer: {name}')
                with Image.open(path) as im:
                    self.assertGreaterEqual(min(im.size),200)
                self.assertLess(path.stat().st_size,350000)
if __name__=='__main__': unittest.main()
