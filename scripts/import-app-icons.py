#!/usr/bin/env python3
"""Import full-color store icons plus the separate Android notification masks.

No icon geometry is drawn here. Run explicitly to refresh the checked-in assets;
building/running PixelBridge never needs network access for icons.
"""

import concurrent.futures
import hashlib
import io
import json
from pathlib import Path
import urllib.request
import urllib.error
import xml.etree.ElementTree as ET
from xml.sax.saxutils import quoteattr

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "docs/app-icons"
DEST = CATALOG / "notification-masks"
COLOR_DEST = CATALOG / "color"
ROUND_DEST = CATALOG / "round"
DRAWABLES = ROOT / "watch/src/main/res/drawable"
COLOR_DRAWABLES = ROOT / "watch/src/main/res/drawable-nodpi"
# Immutable upstream commit, corresponding to Simple Icons 14.15.0.
REVISION = "5664507081ffb65b5a2a0ed2bf2ee2b3b327cb4e"
BASE = f"https://raw.githubusercontent.com/simple-icons/simple-icons/{REVISION}"
ICONS = {
    "youtube": "YouTube",
    "telegram": "Telegram",
    "openai": "OpenAI",
    "reddit": "Reddit",
    "spotify": "Spotify",
    "whatsapp": "WhatsApp",
    "signal": "Signal",
    "discord": "Discord",
    "instagram": "Instagram",
    "facebook": "Facebook",
    "messenger": "Messenger",
    "tiktok": "TikTok",
    "x": "X",
    "viber": "Viber",
    "vk": "VK",
    "gmail": "Gmail",
    "googlemessages": "Google Messages",
    "googlecalendar": "Google Calendar",
    "googlemaps": "Google Maps",
    "googlechrome": "Google Chrome",
    "googledrive": "Google Drive",
    "slack": "Slack",
    "zoom": "Zoom",
    "protonmail": "Proton Mail",
    "thunderbird": "Thunderbird",
    "twitch": "Twitch",
    "netflix": "Netflix",
    "github": "GitHub",
    "element": "Element",
    "mastodon": "Mastodon",
    "steam": "Steam",
    "mailru": "Mail.Ru",
    "kinopoisk": "Kinopoisk",
    "vlc": "VLC media player",
    "youtubemusic": "YouTube Music",
    "applemusic": "Apple Music",
    "amazonmusic": "Amazon Music",
    "tidal": "TIDAL",
    "soundcloud": "SoundCloud",
    "pocketcasts": "Pocket Casts",
    "antennapod": "AntennaPod",
    "foobar2000": "foobar2000",
}
SOURCE_SLUGS = {"vlc": "vlcmediaplayer", "mailru": "maildotru"}


def download(url):
    for attempt in range(3):
        try:
            with urllib.request.urlopen(url, timeout=15) as response:
                return response.read()
        except urllib.error.URLError as error:
            if attempt == 2 or getattr(error, "code", None) == 404:
                raise


def import_icon(item, metadata):
    slug, title = item
    url = f"{BASE}/icons/{SOURCE_SLUGS.get(slug, slug)}.svg"
    svg = download(url)
    root = ET.fromstring(svg)
    assert root.attrib["viewBox"] == "0 0 24 24", slug
    paths = root.findall("{http://www.w3.org/2000/svg}path")
    assert paths and all(set(p.attrib) == {"d"} for p in paths), slug
    (DEST / f"{slug}.svg").write_bytes(svg)
    # Android small icons use alpha only. Keep the original path data exactly;
    # transparent surroundings and an opaque white path make a proper mask.
    vector = '<?xml version="1.0" encoding="utf-8"?>\n'
    vector += f'<!-- Simple Icons {REVISION}; see docs/app-icons/notification-masks/SOURCES.json. -->\n'
    vector += '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
    vector += '    android:width="24dp" android:height="24dp"\n'
    vector += '    android:viewportWidth="24" android:viewportHeight="24">\n'
    for path in paths:
        vector += '    <path android:fillColor="#FFFFFFFF"\n'
        vector += f'        android:pathData={quoteattr(path.attrib["d"])}/>\n'
    vector += '</vector>\n'
    (DRAWABLES / f"ic_app_{slug}.xml").write_text(vector)
    return {
        "slug": slug,
        "app": "ChatGPT / OpenAI" if slug == "openai" else title,
        "download": url,
        "sha256": hashlib.sha256(svg).hexdigest(),
        "upstream": metadata[title],
    }


def import_color_icon(source):
    cached = COLOR_DEST / source.get("original", "__missing__")
    data = cached.read_bytes() if cached.is_file() else download(source["download"])
    if source.get("sha256"):
        assert hashlib.sha256(data).hexdigest() == source["sha256"], source["slug"]
    image = Image.open(io.BytesIO(data))
    image.load()
    assert image.width == image.height and image.width >= 128, source["slug"]
    assert image.format in ("PNG", "WEBP", "JPEG"), image.format
    suffix = {"PNG": ".png", "WEBP": ".webp", "JPEG": ".jpg"}[image.format]
    original = source["slug"] + suffix
    (COLOR_DEST / original).write_bytes(data)
    # Preserve the original artwork/colors, and apply only the requested circular alpha mask.
    icon = image.convert("RGBA").resize((128, 128), Image.Resampling.LANCZOS)
    mask = Image.new("L", (512, 512), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, 511, 511), fill=255)
    mask = mask.resize((128, 128), Image.Resampling.LANCZOS)
    # Transparent source logos need a visible circular backing too (e.g. KDE Connect/foobar2000).
    backing = Image.new("RGBA", (128, 128), "white")
    backing.alpha_composite(icon)
    icon = backing
    icon.putalpha(mask)
    runtime = COLOR_DRAWABLES / f'ic_color_{source["slug"]}.png'
    icon.save(runtime, optimize=True)
    preview = source["slug"] + ".png"
    (ROUND_DEST / preview).write_bytes(runtime.read_bytes())
    return {
        **source,
        "original": original,
        "sha256": hashlib.sha256(data).hexdigest(),
        "size": list(image.size),
        "round_preview": preview,
    }


def main():
    DEST.mkdir(parents=True, exist_ok=True)
    COLOR_DRAWABLES.mkdir(parents=True, exist_ok=True)
    ROUND_DEST.mkdir(parents=True, exist_ok=True)
    # Migrate the first import's mask originals out of the color-icon review folder.
    for filename in [*(slug + ".svg" for slug in ICONS), "SOURCES.json", "LICENSE.md", "DISCLAIMER.md"]:
        old = CATALOG / filename
        if old.exists():
            old.replace(DEST / filename)
    metadata = {entry["title"]: entry for entry in json.loads(download(f"{BASE}/_data/simple-icons.json"))}
    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
        sources = list(pool.map(lambda item: import_icon(item, metadata), ICONS.items()))
    (DEST / "SOURCES.json").write_text(json.dumps(sources, indent=2, ensure_ascii=False) + "\n")
    for filename in ("LICENSE.md", "DISCLAIMER.md"):
        (DEST / filename).write_bytes(download(f"{BASE}/{filename}"))
    color_sources = json.loads((COLOR_DEST / "SOURCES.json").read_text())
    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
        colors = list(pool.map(import_color_icon, color_sources))
    (COLOR_DEST / "SOURCES.json").write_text(json.dumps(colors, indent=2, ensure_ascii=False) + "\n")
    lines = [
        "# Встроенные значки приложений", "",
        f"{len(colors)} круглых цветных иконок Android-приложений: [папка для проверки](round/).",
        "Изображения скачаны со страниц приложений в Google Play и RuStore.",
        "Оригиналы сохранены в [color/](color/), круглые версии — в [round/](round/).",
        "Иконки скачаны, не нарисованы и не перекрашены; у копий только обрезаны углы по кругу.", "",
        "Прозрачные логотипы размещены на белой круглой подложке.", "",
        "Цветные иконки используются в списке PixelBridge и как крупные значки уведомлений.",
        "В APK встроены копии размером 128×128 без изменения пропорций.",
        "Значки встроены в APK и работают без интернета.", "",
        "Маленький системный значок Android/Wear OS отображается как одноцветная маска.",
        "Для него отдельно оставлены [SVG-маски Simple Icons](notification-masks/).",
        "У плееров без отдельной монохромной маски используется общая музыкальная нота.",
        "У остальных приложений без отдельной маски маленький значок — логотип PixelBridge.",
        "Это ограничение системного маленького значка, а не цветной иконки в карточке.", "",
        "Прямые URL, страницы приложений, размеры и SHA-256: [color/SOURCES.json](color/SOURCES.json).",
        "Права на изображения и товарные знаки сохраняются за соответствующими владельцами.",
        "Лицензия CC0 в notification-masks относится к коллекции Simple Icons, не к цветным иконкам.", "",
        "| Приложение | Круглая иконка | Оригинал | Страница приложения |", "|---|---|---|---|",
    ]
    for source in colors:
        filename = source["original"]
        lines.append(f'| {source["app"]} | [PNG](round/{source["round_preview"]}) | [{filename}](color/{filename}) | [Источник]({source["page"]}) |')
    lines += ["", "Повторный импорт: `python3 scripts/import-app-icons.py` (нужен Pillow).",
              "Цветные Android-ресурсы: `watch/src/main/res/drawable-nodpi/ic_color_*.png`.",
              "Системные маски: `watch/src/main/res/drawable/ic_app_*.xml`.", ""]
    (CATALOG / "README.md").write_text("\n".join(lines))
    print(f"Imported {len(colors)} original color app icons into {COLOR_DEST}")
    print(f"Kept {len(sources)} separate system notification masks in {DEST}")


if __name__ == "__main__":
    main()
