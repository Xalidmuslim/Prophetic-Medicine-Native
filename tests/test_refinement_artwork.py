"""Protect nine-patch control strips and full transparent icon contours."""
import unittest
from pathlib import Path
from PIL import Image
ART=Path(__file__).resolve().parents[1]/'app/src/main/res/drawable-nodpi'
class RefinementArtworkTest(unittest.TestCase):
    def test_frame_has_scalable_strips_and_fixed_corners(self):
        im=Image.open(ART/'antique_card_frame.9.png').convert('RGBA')
        self.assertEqual(im.size,(386,386))
        self.assertEqual(im.getpixel((100,0)),(0,0,0,255))
        self.assertEqual(im.getpixel((0,100)),(0,0,0,255))
        self.assertEqual(im.getpixel((10,0))[3],0)
        self.assertEqual(im.getpixel((0,10))[3],0)
        self.assertEqual(im.getpixel((0,0))[3],0)
    def test_every_tight_icon_preserves_safe_transparent_margins(self):
        for name in ['open_book','search','mortar','books','scroll','remedies','bookmark']:
            im=Image.open(ART/f'antique_{name}_detail.webp').convert('RGBA')
            box=im.getchannel('A').getbbox()
            self.assertGreaterEqual(min(box[0],box[1],im.width-box[2],im.height-box[3]),23,name)
    def test_new_hero_matches_the_reference_header_proportions(self):
        with Image.open(ART/'antique_hero_refined.webp') as im:
            self.assertAlmostEqual(im.width/im.height,3.45,places=2)
