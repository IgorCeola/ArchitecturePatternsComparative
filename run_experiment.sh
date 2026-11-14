#!/usr/bin/env bash

set -euo pipefail

SERIAL="$1"
LABEL="$2"
export ANDROID_SERIAL="$SERIAL"

OUT="results/$LABEL"
RAW="macrobenchmark/build/outputs/connected_android_test_additional_output"
SCENARIOS=(S1_CONTROL S2_ITEM_UPDATES S3_USER_INTERACTION S4_AUX_STATE)
mkdir -p "$OUT"

# Metadados do aparelho
{
  echo "model=$(adb shell getprop ro.product.model | tr -d '\r')"
  echo "android=$(adb shell getprop ro.build.version.release | tr -d '\r')"
  echo "build=$(adb shell getprop ro.build.display.id | tr -d '\r')"
  echo "commit=$(git rev-parse HEAD)"
} > "$OUT/device.txt"

battery_temp() {
  adb shell dumpsys battery | awk -F': ' '/temperature/ {print $2; exit}' | tr -d '\r'
}

wait_cool() {
  while (( $(battery_temp) > 340 )); do  # 34,0 °C
    echo "Bateria a $(battery_temp) décimos de °C; aguardando 60 s..."
    sleep 60
  done
}

for block in 1 2 3; do
  for idx in "${!SCENARIOS[@]}"; do
    scenario="${SCENARIOS[$idx]}"
    if (( (block + idx) % 2 == 1 )); then order=(MVVM MVI); else order=(MVI MVVM); fi

    for arch in "${order[@]}"; do
      wait_cool
      echo "$(date -Iseconds) bloco=$block cenario=$scenario arq=$arch temp=$(battery_temp)" | tee -a "$OUT/log.txt"
      rm -rf "$RAW"

      ok=0
      for tentativa in 1 2; do
        if ./gradlew :macrobenchmark:connectedBenchmarkAndroidTest \
             -Pandroid.testInstrumentationRunnerArguments.class=com.example.benchmark.FeedBenchmark \
             -Pandroid.testInstrumentationRunnerArguments.architecture="$arch" \
             -Pandroid.testInstrumentationRunnerArguments.scenario="$scenario" \
             -Pandroid.testInstrumentationRunnerArguments.iterations=10; then
          ok=1; break
        fi
        echo "$(date -Iseconds) FALHA tentativa=$tentativa bloco=$block cenario=$scenario arq=$arch" | tee -a "$OUT/log.txt"
        adb uninstall com.example.architecturepatternscomparative >/dev/null 2>&1 || true
        sleep 30
        rm -rf "$RAW"
      done
      if (( ok == 0 )); then
        echo "Falhou duas vezes; interrompendo." | tee -a "$OUT/log.txt"
        exit 1
      fi

      json=$(find "$RAW" -name '*benchmarkData.json' | head -n 1)
      cp "$json" "$OUT/${scenario}_${arch}_b${block}.json"
    done
  done
done
