#!/usr/bin/env python3
"""Download existing Google Material icons and preserve their SVG paths in Android vectors."""
import concurrent.futures
import hashlib
import json
from pathlib import Path
import urllib.request
import xml.etree.ElementTree as ET
from xml.sax.saxutils import quoteattr

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "docs/music-icons"
REV = "10ecc14790612ca8fd164e3f4ffe9c16fc50d5b7"
BASE = f"https://raw.githubusercontent.com/google/material-design-icons/{REV}"
ICONS = {"music_note": "image", **dict.fromkeys(
    ["play_arrow", "pause", "skip_next", "skip_previous", "volume_up", "volume_down"], "av",
)}


def fetch(url):
    return urllib.request.urlopen(url, timeout=20).read()


def import_icon(item):
    name, category = item
    url = f"{BASE}/src/{category}/{name}/materialicons/24px.svg"
    data = fetch(url)
    (DEST / f"{name}.svg").write_bytes(data)
    paths = ET.fromstring(data).findall("{http://www.w3.org/2000/svg}path")
    vector = '<?xml version="1.0" encoding="utf-8"?>\n'
    vector += '<!-- Google Material Icons, Apache-2.0; see docs/music-icons/SOURCES.json. -->\n'
    vector += '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
    vector += '    android:width="24dp" android:height="24dp"\n'
    vector += '    android:viewportWidth="24" android:viewportHeight="24">\n'
    for path in paths:
        if path.get("fill") != "none":
            vector += f'    <path android:fillColor="#FFFFFFFF" android:pathData={quoteattr(path.attrib["d"])}/>\n'
    vector += '</vector>\n'
    (ROOT / f"watch/src/main/res/drawable/ic_music_{name}.xml").write_text(vector)
    return {"name": name, "download": url, "sha256": hashlib.sha256(data).hexdigest()}


DEST.mkdir(parents=True, exist_ok=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
    sources = list(pool.map(import_icon, ICONS.items()))
(DEST / "SOURCES.json").write_text(json.dumps(sources, indent=2) + "\n")
(DEST / "LICENSE").write_bytes(fetch(f"{BASE}/LICENSE"))
print(f"Imported {len(sources)} unchanged Material icons")
