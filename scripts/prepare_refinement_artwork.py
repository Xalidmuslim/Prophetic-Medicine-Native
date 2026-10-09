"""Export generated independent artwork; retain fixed frame corners via Android nine-patch."""
from pathlib import Path
import argparse
from PIL import Image, ImageDraw
p=argparse.ArgumentParser();p.add_argument('sources',type=Path);a=p.parse_args()
out=Path(__file__).resolve().parents[1]/'app/src/main/res/drawable-nodpi'
hero=Image.open(a.sources/'hero.png').convert('RGB')
# Trim only empty upper architecture; keep the full book/mortar silhouettes.
hero=hero.crop((0,round(hero.height-hero.width/3.45),hero.width,hero.height))
hero.thumbnail((1200,348),Image.Resampling.LANCZOS)
hero.save(out/'antique_hero_refined.webp',quality=92,method=6)
card=Image.open(a.sources/'card.png').convert('RGBA')
# Drop only low-opacity generation specks outside the actual paper silhouette.
box=card.getchannel('A').point(lambda x:255 if x>=128 else 0).getbbox()
card=card.crop(box).resize((384,384),Image.Resampling.LANCZOS)
frame=Image.new('RGBA',(386,386));frame.alpha_composite(card,(1,1));d=ImageDraw.Draw(frame)
# Fixed 36px corners; stretch only quiet middle sections, never corners/rim.
d.line((37,0,348,0),fill='black');d.line((0,37,0,348),fill='black')
d.line((17,385,368,385),fill='black');d.line((385,17,385,368),fill='black')
frame.save(out/'antique_card_frame.9.png')
# Independent fibers under native text; frame remains a separate scalable layer.
card.crop((48,48,336,336)).convert('RGB').resize((512,512),Image.Resampling.LANCZOS).save(out/'antique_card_paper.webp',quality=94,method=6)

# Tight proportional viewports increase the visible object size without clipping.
for name in ['antique_open_book','antique_search','antique_mortar','antique_books','antique_scroll','antique_remedies','antique_bookmark']:
    im=Image.open(out/(name+'.webp')).convert('RGBA')
    box=im.getchannel('A').getbbox()
    crop=im.crop(box)
    margin=24
    detailed=Image.new('RGBA',(crop.width+margin*2,crop.height+margin*2))
    detailed.alpha_composite(crop,(margin,margin))
    detailed.save(out/(name+'_detail.webp'),quality=94,method=6)
