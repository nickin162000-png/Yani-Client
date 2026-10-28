#!/usr/bin/env python3
"""Converte os nomes SRG (func_XXXX / field_XXXX) do codigo para os nomes MCP (stable_22, 1.8.9).

O codigo-fonte do client usa nomes SRG. O ForgeGradle 2 compila com nomes MCP, entao este
script roda antes do build (no GitHub Actions) e troca os nomes. Nomes sem mapeamento
continuam como SRG, que o ForgeGradle tambem aceita.
"""
import csv, io, re, sys, urllib.request, zipfile, pathlib

URL = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/22-1.8.9/mcp_stable-22-1.8.9.zip"

def load_map(zf):
    m = {}
    for name in ("methods.csv", "fields.csv"):
        with zf.open(name) as f:
            for row in csv.DictReader(io.TextIOWrapper(f, "utf-8")):
                m[row["searge"]] = row["name"]
    return m

def convert(root, m):
    pat = re.compile(r"\b(func_\d+_[A-Za-z0-9_]+|field_\d+_[A-Za-z0-9_]+)\b")
    n = 0
    for p in pathlib.Path(root).rglob("*.java"):
        s = p.read_text(encoding="utf-8")
        t = pat.sub(lambda x: m.get(x.group(1), x.group(1)), s)
        if t != s:
            p.write_text(t, encoding="utf-8")
            n += 1
    return n

if __name__ == "__main__":
    src = sys.argv[1] if len(sys.argv) > 1 else "src/main/java"
    zpath = sys.argv[2] if len(sys.argv) > 2 else None
    data = open(zpath, "rb").read() if zpath else urllib.request.urlopen(URL).read()
    with zipfile.ZipFile(io.BytesIO(data)) as zf:
        mapping = load_map(zf)
    print("mapeamentos:", len(mapping), "| arquivos alterados:", convert(src, mapping))
