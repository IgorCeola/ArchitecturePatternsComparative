import json
import sys
from pathlib import Path

import numpy as np
from datetime import datetime

SCENARIOS = ["S1_CONTROL", "S2_ITEM_UPDATES", "S3_USER_INTERACTION", "S4_AUX_STATE"]
ARCHS = ["MVVM", "MVI"]
BLOCKS = [1, 2, 3]
EXPECTED_ITERATIONS = 10

folder = Path(sys.argv[1] if len(sys.argv) > 1 else "results/a51")
problems = []

print(f"== {folder}\n")

device = folder / "device.txt"
if device.exists():
    print(device.read_text(encoding="utf-8").strip(), "\n")
else:
    problems.append("device.txt ausente")

log = folder / "log.txt"
if log.exists():
    lines = log.read_text(encoding="utf-8").splitlines()
    falhas = [l for l in lines if "FALHA" in l]
    temps = [int(l.split("temp=")[1]) for l in lines if "temp=" in l and l.split("temp=")[1].strip().isdigit()]
    print(f"log.txt: {len(lines)} linhas, {len(falhas)} falha(s) registradas")
    for l in falhas:
        print("   ", l)
    if temps:
        print(f"temperatura no início das execuções: {min(temps)/10:.1f} a {max(temps)/10:.1f} °C")
    print()
else:
    problems.append("log.txt ausente")

header = f"{'cenário':<20}{'arq':<6}{'bloco':>5}{'iter':>6}{'quadros':>9}{'P90 CPU':>9}{'jank':>7}{'FSU count':>11}  {'arquivo gravado em':<16}"
print(header)
print("-" * len(header))

for scenario in SCENARIOS:
    for arch in ARCHS:
        for block in BLOCKS:
            path = folder / f"{scenario}_{arch}_b{block}.json"
            if not path.exists():
                problems.append(f"faltando: {path.name}")
                print(f"{scenario:<20}{arch:<6}{block:>5}   FALTANDO")
                continue
            data = json.loads(path.read_text(encoding="utf-8"))
            benches = data.get("benchmarks", [])
            names = {b.get("className") for b in benches}
            if names != {"com.example.benchmark.FeedBenchmark"}:
                problems.append(f"{path.name}: benchmarks inesperados {names}")
            b = benches[0]
            cpu_runs = b["sampledMetrics"]["frameDurationCpuMs"]["runs"]
            n_iter = len(cpu_runs)
            if n_iter != EXPECTED_ITERATIONS:
                problems.append(f"{path.name}: {n_iter} iterações (esperado {EXPECTED_ITERATIONS})")
            vazias = [i + 1 for i, r in enumerate(cpu_runs) if len(r) == 0]
            if vazias:
                problems.append(f"{path.name}: iteração(ões) sem nenhum quadro: {vazias}")
            validas = [r for r in cpu_runs if len(r) > 0]
            frames = np.mean([len(r) for r in cpu_runs])
            p90 = np.mean([np.percentile(r, 90) for r in validas]) if validas else float("nan")
            jank = np.mean([np.mean(np.asarray(r) > 1000 / 60) for r in validas]) if validas else float("nan")
            fsu = b["metrics"].get("FeedStateUpdateCount", {}).get("runs", [])
            fsu_txt = f"{min(fsu):.0f}-{max(fsu):.0f}" if fsu else "?"
            if scenario == "S1_CONTROL" and fsu and max(fsu) != 0:
                problems.append(f"{path.name}: FeedStateUpdateCount deveria ser 0 no S1")
            if scenario == "S3_USER_INTERACTION" and fsu and (min(fsu) != 12 or max(fsu) != 12):
                problems.append(f"{path.name}: FeedStateUpdateCount deveria ser 12 no S3 ({fsu_txt})")
            marca = "  <- quadros por iteração: " + str([len(r) for r in cpu_runs]) if vazias else ""
            print(f"{scenario:<20}{arch:<6}{block:>5}{n_iter:>6}{frames:>9.0f}{p90:>9.2f}{jank:>7.1%}{fsu_txt:>11}  {datetime.fromtimestamp(path.stat().st_mtime):%d/%m %H:%M}{marca}")

print()
if problems:
    print(f"ATENÇÃO: {len(problems)} problema(s):")
    for p in problems:
        print(" -", p)
else:
    print("OK: 24 arquivos, 10 iterações cada, só FeedBenchmark, contagens coerentes.")
