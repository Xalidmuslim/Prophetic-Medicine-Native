"""Import two purpose-generated understated medicine scenes as offline native drawables."""
from pathlib import Path
from urllib.request import Request, urlopen
from PIL import Image
import io
images={
"medicine_minimal_hero":"https://pikaso.cdnpk.net/private/production/5680648109/render.png?token=exp=1791849600~hmac=ed5270e58bee24c93a72109db17dc8e11eff4f0e4866a055708ccf09e55437a9",
"medicine_minimal_card":"https://pikaso.cdnpk.net/private/production/5680648390/render.png?token=exp=1791849600~hmac=66ac4f3efabbad5e87c9b752fe33a32557f007cb360fb87fd63554a57d380fdb",
}
sizes={"medicine_minimal_hero":(1152,864),"medicine_minimal_card":(1152,648)}
dest=Path("app/src/main/res/drawable-nodpi")
dest.mkdir(exist_ok=True,parents=True)
for k,u in images.items():
    with urlopen(Request(u,headers={"User-Agent":"Mozilla/5.0"}),timeout=65) as resp:
        raw=resp.read()
    im=Image.open(io.BytesIO(raw)).convert("RGB")
    assert im.width>=650 and im.height>=450,(k,im.size)
    im=im.resize(sizes[k],Image.Resampling.LANCZOS)
    file=dest/(k+".webp")
    im.save(file,"WEBP",quality=90,method=6)
    with Image.open(file) as check:check.verify()
    assert file.stat().st_size>20_000
    print("offline asset",file,file.stat().st_size,flush=True)
