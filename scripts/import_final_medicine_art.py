"""One-time import of owner-selected original illustrations as offline Android WebP.
Images were created for this app; nothing is downloaded at app runtime.
Remove this one-time fetcher from repository after successful import.
"""
import io
from pathlib import Path
from urllib.request import Request, urlopen
from PIL import Image

IMAGES = {
    "more_sheet": "https://pikaso.cdnpk.net/private/production/5680507853/3703898081.png?token=exp=1791849600~hmac=42d845e479742208eb2d188cc20d29c76a05d5aeedfa296e77656c3af89258aa",
    "home_sheet": "https://pikaso.cdnpk.net/private/production/5680531229/3703909243.png?token=exp=1791849600~hmac=d7ff0518db4cb3d9e22ecdbad7032c67775472dad8d52aa365c590e444569eec",
    "portrait": "https://pikaso.cdnpk.net/private/production/5680531570/3703909403.png?token=exp=1791849600~hmac=994df876ed04b5cfccf3d841d983e9d095e0b7bb29b01b461380e21c3cb0daf8",
    "reading": "https://pikaso.cdnpk.net/private/production/5680508467/3703898345.png?token=exp=1791849600~hmac=41879222736c74210df58ef01866d554fddaf5a23d29ae1ab6f09dae56ff0b9c",
    "treatments": "https://pikaso.cdnpk.net/private/production/5680508783/3703898500.png?token=exp=1791849600~hmac=e82e9b323bf27e338c874006b17c10abd6ede4a42d60893598a7f174addd4ae6",
    "quote": "https://pikaso.cdnpk.net/private/production/5680509075/3703898632.png?token=exp=1791849600~hmac=02fa9a5e246593ccc19798a642859833e020ee20e468c0587a429a9a8872526b",
}
OUT=Path("app/src/main/res/drawable-nodpi")
OUT.mkdir(parents=True, exist_ok=True)
def downloaded(key):
    req=Request(IMAGES[key],headers={"User-Agent":"Mozilla/5.0"})
    with urlopen(req, timeout=90) as res:
        data=res.read()
    im=Image.open(io.BytesIO(data)); im.load()
    print(key, im.size, len(data),flush=True)
    return im
def save(name,image):
    target=OUT/(name+".webp")
    image.save(target,"WEBP",quality=87,method=5)
    assert target.stat().st_size>1400, target
    with Image.open(target) as a:
        a.verify()
    print("BUNDLED",target,target.stat().st_size,flush=True)
def icon_sheet(key,cols,rows,names):
    im=downloaded(key).convert("RGBA")
    W,H=im.size
    assert len(names)==cols*rows,(key,len(names))
    for k,name in enumerate(names):
        x=k%cols; y=k//cols
        cell=im.crop((round(W*x/cols),round(H*y/rows),round(W*(x+1)/cols),round(H*(y+1)/rows)))
        rect=cell.getchannel("A").point(lambda v:255 if v>22 else 0).getbbox()
        assert rect, (name,"empty icon")
        cell=cell.crop(rect)
        cell.thumbnail((238,238),Image.Resampling.LANCZOS)
        out=Image.new("RGBA",(280,280),(0,0,0,0))
        out.alpha_composite(cell,((280-cell.width)//2,(280-cell.height)//2))
        save(name,out)
icon_sheet("home_sheet",3,2,["medicine_home_icon_"+x for x in ("book","topics","remedy","bookmark","notes","source")])
icon_sheet("more_sheet",4,3,["medicine_more_%02d"%k for k in range(12)])
hero=downloaded("portrait").convert("RGB")
assert hero.size==(1086,1448),hero.size
hero=hero.crop((0,410,1086,1225)).resize((1120,840),Image.Resampling.LANCZOS)
save("medicine_home_hero_full",hero)
reading=downloaded("reading").convert("RGB")
assert reading.size==(2172,724),reading.size
reading=reading.crop((0,0,1700,724)).resize((1100,470),Image.Resampling.LANCZOS)
save("medicine_reading_background_full",reading)
treat=downloaded("treatments").convert("RGB")
assert treat.size==(2172,724),treat.size
treat=treat.crop((0,110,2172,650)).resize((1200,300),Image.Resampling.LANCZOS)
save("medicine_treatments_background_full",treat)
quote=downloaded("quote").convert("RGB")
assert quote.size==(1448,1086),quote.size
quote.thumbnail((780,600),Image.Resampling.LANCZOS)
save("medicine_hadith_frame",quote)
files=list(OUT.glob("medicine_more_*.webp"))+list(OUT.glob("medicine_home_icon_*.webp"))+[OUT/(k+".webp") for k in ("medicine_home_hero_full","medicine_reading_background_full","medicine_treatments_background_full","medicine_hadith_frame")]
assert len(files)==22 and all(f.exists() for f in files)
print("ASSET VALIDATION PASS",len(files),flush=True)
