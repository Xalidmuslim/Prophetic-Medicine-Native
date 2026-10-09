"""Deterministic Android exports from separate generated PNGs (Pillow required)."""
import argparse
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

parser = argparse.ArgumentParser()
parser.add_argument('sources', type=Path)
parser.add_argument('--output', type=Path, default=Path(__file__).resolve().parents[1]/'app/src/main/res/drawable-nodpi')
a = parser.parse_args()
a.output.mkdir(parents=True, exist_ok=True)
names = ['antique_open_book','antique_search','antique_mortar','antique_books','antique_scroll','antique_remedies','antique_bookmark','nav_home','nav_topics','nav_search','nav_bookmark','nav_more','antique_foliage']
for name in names:
    im=Image.open(a.sources/(name+'.png')).convert('RGBA')
    arr=np.array(im)
    # Generation sometimes leaves low-alpha specks. Remove only negligible pixels;
    # retain every visible object/leaf and antialiased contours inside the bounds.
    alpha=arr[:,:,3]
    alpha[alpha<12]=0
    mask=Image.fromarray(alpha).point(lambda x: 255 if x>=32 else 0)
    bbox=mask.getbbox()
    assert bbox, name
    crop=Image.fromarray(arr).crop(bbox)
    crop.thumbnail((388,388), Image.Resampling.LANCZOS)
    out=Image.new('RGBA',(512,512))
    out.alpha_composite(crop,((512-crop.width)//2,(512-crop.height)//2))
    out.save(a.output/(name+'.webp'),format='WEBP',quality=92,method=6)

for name, size in [('antique_hero',(1179,420)),('antique_parchment',(768,768)),('antique_button',(1100,390))]:
    im=Image.open(a.sources/(name+'.png')).convert('RGB')
    im.thumbnail(size,Image.Resampling.LANCZOS)
    if name == 'antique_parchment':
        pixels=np.array(im,dtype=float)
        pixels=np.clip(np.array([233,218,200])+(pixels-pixels.mean(axis=(0,1))),0,255).astype('uint8')
        im=Image.fromarray(pixels)
    im.save(a.output/(name+'.webp'),quality=89,method=6)
# Independent calm paper surface, using the generated paper fibres, rather than
# a raster card including borders, counters, or text. Its frame is native Compose.
paper=np.array(Image.open(a.sources/'antique_parchment.png').convert('RGB').resize((512,512)),dtype=float)
base=np.array([241,231,216])
paper=np.clip(base+(paper-paper.mean(axis=(0,1)))*0.38,0,255).astype('uint8')
Image.fromarray(paper).save(a.output/'antique_card_paper.webp',quality=90,method=6)

hero=Image.open(a.output/'antique_hero.webp').convert('RGB')
hero.crop((round(hero.width*0.60),0,hero.width,hero.height)).save(a.output/'antique_hero_scene.webp',quality=91,method=6)
