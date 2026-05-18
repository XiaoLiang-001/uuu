# -*- coding: utf-8 -*-
import zlib
import urllib.request
from pathlib import Path

def plantuml_encode(text: str) -> str:
    compressed = zlib.compress(text.encode("utf-8"))[2:-4]

    def encode6bit(b: int) -> str:
        if b < 10:
            return chr(48 + b)
        b -= 10
        if b < 26:
            return chr(65 + b)
        b -= 26
        if b < 26:
            return chr(97 + b)
        b -= 26
        if b == 0:
            return "0"
        if b == 1:
            return "1"
        return "?"

    def encode3bytes(b1: int, b2: int, b3: int) -> str:
        x = (b1 << 16) + (b2 << 8) + b3
        return "".join(encode6bit((x >> (18 - 6 * i)) & 0x3F) for i in range(4))

    res = []
    for i in range(0, len(compressed), 3):
        if i + 2 < len(compressed):
            res.append(encode3bytes(compressed[i], compressed[i + 1], compressed[i + 2]))
        elif i + 1 < len(compressed):
            res.append(encode3bytes(compressed[i], compressed[i + 1], 0))
        else:
            res.append(encode3bytes(compressed[i], 0, 0))
    return "".join(res)


for name in ["login-flow-cn.puml", "login-flow-detail.puml"]:
    text = Path(__file__).with_name(name).read_text(encoding="utf-8")
    code = plantuml_encode(text)
    url = f"http://www.plantuml.com/plantuml/png/{code}"
    try:
        with urllib.request.urlopen(url, timeout=30) as r:
            data = r.read(8)
            ok = data[:4] == b"\x89PNG"
        print(name, "OK" if ok else "FAIL", "size", len(data))
    except Exception as e:
        print(name, "ERR", e)
