from PIL import Image, ImageDraw, ImageFilter
import io, urllib.request
from pathlib import Path
SRC = "https://cdn.openart.ai/openart-uploads/production/attachment-transfers/b31d093e0e111291ff276e80874dc83ab218957e7f86d9932a97767b563f25cf.jpg"
req = urllib.request.Request(SRC, headers={"User-Agent":"Medicine-Reference-Asset-Importer/1.0"})
with urllib.request.urlopen(req, timeout=45) as response:
    raw = response.read()
image = Image.open(io.BytesIO(raw)).convert("RGB")
assert image.size == (1024,1536), ("Wrong source dimensions", image.size)
dst = Path("app/src/main/res/drawable-nodpi")
dst.mkdir(parents=True, exist_ok=True)
def crop_asset(name, bbox, out_size, mode="fade", quality=86):
    rgba=image.crop(bbox).resize(out_size,Image.Resampling.LANCZOS).convert("RGBA")
    w,h=rgba.size
    mask=Image.new("L",(w,h),0)
    d=ImageDraw.Draw(mask)
    if mode=="hero":
        d.rectangle((max(12,int(w*.14)),8,w-4,h-12),fill=255)
        mask=mask.filter(ImageFilter.GaussianBlur(radius=19))
    elif mode=="reading":
        d.rounded_rectangle((8,8,w-8,h-8),radius=15,fill=255)
        mask=mask.filter(ImageFilter.GaussianBlur(radius=8))
    elif mode=="therapy":
        d.rounded_rectangle((6,5,w-6,h-5),radius=12,fill=255)
        mask=mask.filter(ImageFilter.GaussianBlur(radius=7))
    else:
        d.rounded_rectangle((4,4,w-4,h-4),radius=10,fill=255)
        mask=mask.filter(ImageFilter.GaussianBlur(radius=3))
    rgba.putalpha(mask)
    output=dst/(name+".webp")
    rgba.save(output,format="WEBP",quality=quality,method=6)
    assert output.stat().st_size>700
    print(name,output.stat().st_size, out_size)
crop_asset("medicine_photo_hero",(580,117,1018,425),(438,308),"hero",88)
crop_asset("medicine_photo_reading",(39,476,338,766),(299,290),"reading",90)
crop_asset("medicine_photo_treatments",(43,875,355,1008),(312,133),"therapy",88)
icons={
  "book":(55,1028,198,1116),
  "topics":(545,1028,687,1116),
  "remedy":(55,1151,195,1240),
  "bookmark":(543,1149,688,1241),
  "notes":(55,1268,197,1365),
  "source":(542,1264,690,1365),
}
for name,box in icons.items():
    crop_asset("medicine_photo_ic_"+name,box,(114,88),"icon",88)
with open("design/REFERENCE_ASSET_PROVENANCE.md","w",encoding="utf-8") as f:
    f.write("# Original user-provided visual reference\n\n")
    f.write("Source: the visual reference supplied by the app owner on 2026-10-08 (1024 x 1536).\n")
    f.write("Nine offline alpha-masked WebP photos cropped from that source: hero, book, remedy, six icons.\n")
    f.write("No user book text, settings, reader screens or other assets are modified.\n")
