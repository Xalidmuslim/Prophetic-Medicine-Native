"""One-off import of user-approved AI-made medicinal art into offline Android assets.

Generated in this conversation, not sourced from a book or the earlier mockup.
All URLs are temporary transfer links: do not use these at application runtime.
"""
import io
from pathlib import Path
from urllib.request import Request, urlopen
from PIL import Image, ImageDraw, ImageFilter

SRC = {
    "medicine_topic_unified": "https://pikaso.cdnpk.net/private/production/5680212204/3703759817.png?token=exp=1791849600~hmac=6f1dfef5e4f3ba7ef515acce486b22acdcebf7fb31ba4609d7acd9c9dad2dd70",
    "medicine_photo_treatments_new": "https://pikaso.cdnpk.net/private/production/5680210878/3703759218.png?token=exp=1791849600~hmac=b201d429d607c8602fced6f6b42dbe002b568cc607240dc442b00650d7b3418f",
    "medicine_photo_reading_new": "https://pikaso.cdnpk.net/private/production/5680212634/3703759995.png?token=exp=1791849600~hmac=f7c95ffe4dd6675bc3ff5a70bbe36f671b0e42456e1d8d64a15c8bd6e39db956",
}
OUT = Path("app/src/main/res/drawable-nodpi")
OUT.mkdir(parents=True, exist_ok=True)


def feather(image, margin=12):
    """Create alpha fading to real card background instead of hard rectangular crops."""
    rgba = image.convert("RGBA")
    w, h = rgba.size
    alpha = Image.new("L", (w, h), 0)
    d = ImageDraw.Draw(alpha)
    d.rounded_rectangle((margin, margin, w - margin, h - margin), radius=16, fill=255)
    alpha = alpha.filter(ImageFilter.GaussianBlur(radius=margin))
    rgba.putalpha(alpha)
    return rgba


def get(name):
    req = Request(SRC[name], headers={"User-Agent": "Mozilla/5.0 PropheticMedicine/1.0"})
    with urlopen(req, timeout=75) as resp:
        raw = resp.read()
    image = Image.open(io.BytesIO(raw)).convert("RGB")
    print("Downloaded", name, image.size, len(raw), flush=True)
    return image


topic = get("medicine_topic_unified")
# The generated icon is a complete centered raised tile. Remove outer white canvas;
# soften corners so it can sit on the same ivory card in all topics.
w, h = topic.size
topic = topic.crop((int(w * .075), int(h * .07), int(w * .925), int(h * .92)))
topic = topic.resize((252, 252), Image.Resampling.LANCZOS)
topic = feather(topic, 9)
topic.save(OUT / "medicine_topic_unified.webp", "WEBP", quality=91, method=6)

banner = get("medicine_photo_treatments_new")
# Source is extra wide with empty space at right. Crop only that empty field;
# keep complete stone mortar, oil, herbs and honey in the displayed object group.
w, h = banner.size
banner = banner.crop((int(w * .025), 0, int(w * .74), h))
banner = banner.resize((440, 205), Image.Resampling.LANCZOS)
banner = feather(banner, 11)
banner.save(OUT / "medicine_photo_treatments_new.webp", "WEBP", quality=90, method=6)

reading = get("medicine_photo_reading_new")
# Full medicinal portrait, no Quran or book. Scale without cropping.
reading = reading.resize((298, 372), Image.Resampling.LANCZOS)
reading = feather(reading, 10)
reading.save(OUT / "medicine_photo_reading_new.webp", "WEBP", quality=90, method=6)

for name in SRC:
    fp = OUT / (name + ".webp")
    with Image.open(fp) as valid:
        valid.verify()
    assert fp.stat().st_size > 1500, fp
    print("Bundled", fp, fp.stat().st_size, flush=True)
