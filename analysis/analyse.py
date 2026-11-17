"""Análise do experimento MVVM vs MVI."""

import json
import re
import sys
from pathlib import Path

import numpy as np
import pandas as pd
from scipy import stats

NAME = re.compile(r"(?P<scenario>S\d_[A-Z_]+)_(?P<arch>MVVM|MVI)_b(?P<block>\d+)\.json$")
METRICS = ["p90_cpu_ms", "p50_cpu_ms", "jank_ratio", "state_update_ms"]
PRIMARY = "p90_cpu_ms"
FRAME_BUDGET_MS = 1000 / 60
EXCLUDED: list[dict] = []
MIN_FRAMES_RATIO = 0.5


def load_iterations(root: Path) -> pd.DataFrame:
    rows = []
    for path in sorted(root.glob("*/*.json")):
        match = NAME.search(path.name)
        if not match:
            continue
        data = json.loads(path.read_text())
        bench = data["benchmarks"][0]
        sampled = bench["sampledMetrics"]
        single = bench.get("metrics", {})
        cpu_runs = sampled["frameDurationCpuMs"]["runs"]
        overrun_runs = sampled.get("frameOverrunMs", {}).get("runs", [None] * len(cpu_runs))
        state_key = next((k for k in single if k.startswith("FeedStateUpdate") and k.endswith("Ms")), None)
        state_runs = single[state_key]["runs"] if state_key else [np.nan] * len(cpu_runs)

        for i, cpu in enumerate(cpu_runs):
            cpu = np.asarray(cpu)
            if cpu.size == 0:
                EXCLUDED.append({"device": path.parent.name, "file": path.name, "iteration": i + 1,
                                 "frames": 0, "reason": "sem quadros"})
                continue
            overrun = overrun_runs[i]
            rows.append({
                "device": path.parent.name,
                "scenario": match["scenario"],
                "arch": match["arch"],
                "block": int(match["block"]),
                "iteration": i,
                "file": path.name,
                "frames": len(cpu),
                "p50_cpu_ms": np.percentile(cpu, 50),
                "p90_cpu_ms": np.percentile(cpu, 90),
                "jank_ratio": np.mean(cpu > FRAME_BUDGET_MS),
                "overrun_ratio": np.mean(np.asarray(overrun) > 0) if overrun is not None else np.nan,
                "state_update_ms": state_runs[i],
            })
    return pd.DataFrame(rows)


def hedges_g(a: np.ndarray, b: np.ndarray) -> float:
    na, nb = len(a), len(b)
    pooled = np.sqrt(((na - 1) * a.var(ddof=1) + (nb - 1) * b.var(ddof=1)) / (na + nb - 2))
    if pooled == 0:
        return 0.0
    d = (a.mean() - b.mean()) / pooled
    return d * (1 - 3 / (4 * (na + nb) - 9))


def welch_ci(a: np.ndarray, b: np.ndarray, level: float = 0.95) -> tuple[float, float]:
    va, vb = a.var(ddof=1) / len(a), b.var(ddof=1) / len(b)
    se = np.sqrt(va + vb)
    df = (va + vb) ** 2 / (va ** 2 / (len(a) - 1) + vb ** 2 / (len(b) - 1))
    t = stats.t.ppf((1 + level) / 2, df)
    diff = a.mean() - b.mean()
    return diff - t * se, diff + t * se


def holm(pvalues: pd.Series) -> pd.Series:
    order = pvalues.sort_values().index
    m = len(pvalues)
    adjusted, running = {}, 0.0
    for rank, idx in enumerate(order):
        running = max(running, min(1.0, (m - rank) * pvalues[idx]))
        adjusted[idx] = running
    return pd.Series(adjusted).reindex(pvalues.index)


def compare(df: pd.DataFrame) -> pd.DataFrame:
    results = []
    for (device, scenario), group in df.groupby(["device", "scenario"]):
        for metric in METRICS:
            mvvm = group.loc[group.arch == "MVVM", metric].dropna().to_numpy()
            mvi = group.loc[group.arch == "MVI", metric].dropna().to_numpy()
            if len(mvvm) < 3 or len(mvi) < 3:
                continue
            if np.ptp(np.concatenate([mvvm, mvi])) == 0:
                continue
            low, high = welch_ci(mvi, mvvm)
            results.append({
                "device": device, "scenario": scenario, "metric": metric,
                "n_mvvm": len(mvvm), "n_mvi": len(mvi),
                "mean_mvvm": mvvm.mean(), "mean_mvi": mvi.mean(),
                "diff_mvi_minus_mvvm": mvi.mean() - mvvm.mean(),
                "ci95_low": low, "ci95_high": high,
                "shapiro_p_mvvm": stats.shapiro(mvvm).pvalue,
                "shapiro_p_mvi": stats.shapiro(mvi).pvalue,
                "welch_p": stats.ttest_ind(mvi, mvvm, equal_var=False).pvalue,
                "mannwhitney_p": stats.mannwhitneyu(mvi, mvvm, alternative="two-sided").pvalue,
                "hedges_g": hedges_g(mvi, mvvm),
            })
    out = pd.DataFrame(results)
    out["welch_p_holm"] = out.groupby("metric")["welch_p"].transform(holm)
    out["primary"] = out["metric"] == PRIMARY
    return out


if __name__ == "__main__":
    root = Path(sys.argv[1] if len(sys.argv) > 1 else "results")
    iterations = load_iterations(root)
    if iterations.empty:
        sys.exit(f"Nenhum JSON válido encontrado em {root}/*/ "
                 "(esperado: <aparelho>/<CENARIO>_<MVVM|MVI>_b<bloco>.json)")
    median_frames = iterations.groupby(["device", "scenario", "arch"])["frames"].transform("median")
    short = iterations["frames"] < MIN_FRAMES_RATIO * median_frames
    for _, row in iterations[short].iterrows():
        EXCLUDED.append({"device": row.device, "file": row.file, "iteration": row.iteration + 1,
                         "frames": row.frames,
                         "reason": f"menos da metade da mediana da condição ({median_frames[_]:.0f})"})
    iterations_all = iterations.copy()
    iterations_all["excluded"] = short
    iterations = iterations[~short]
    if EXCLUDED:
        print("Iterações excluídas:")
        for item in EXCLUDED:
            print(f"  - {item['device']}/{item['file']}, iteração {item['iteration']}: "
                  f"{item['frames']} quadros ({item['reason']})")
        print()
    pd.DataFrame(EXCLUDED).to_csv("excluded.csv", index=False)
    iterations_all.to_csv("iterations_all.csv", index=False)
    iterations.to_csv("iterations.csv", index=False)
    tests = compare(iterations)
    tests.to_csv("tests.csv", index=False)
    print(tests.loc[tests.primary, ["device", "scenario", "mean_mvvm", "mean_mvi",
                                    "ci95_low", "ci95_high", "welch_p_holm", "hedges_g"]]
          .round(4).to_string(index=False))
