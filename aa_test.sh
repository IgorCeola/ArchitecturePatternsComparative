set -uo pipefail

SERIAL="$1"
LABEL="$2"
MODE="${3:-novo}"
export ANDROID_SERIAL="$SERIAL"

OUT="aa/$LABEL"
RAW="macrobenchmark/build/outputs/connected_android_test_additional_output"

if [[ "$MODE" != "retomar" ]]; then
  rm -rf "$OUT"
fi
mkdir -p "$OUT"

battery_temp() {
  adb shell dumpsys battery | awk -F': ' '/temperature/ {print $2; exit}' | tr -d '\r'
}

wait_cool() {
  while (( $(battery_temp) > 340 )); do  # 34,0 °C
    echo "Bateria a $(battery_temp) décimos de °C; aguardando 60 s..."
    sleep 60
  done
}

for b in 1 2 3; do
  if (( b % 2 == 1 )); then ordem=(MVVM MVI); else ordem=(MVI MVVM); fi
  for rotulo in "${ordem[@]}"; do
    alvo="$OUT/S1_CONTROL_${rotulo}_b${b}.json"
    if [[ -f "$alvo" ]]; then
      echo "já existe: $alvo"
      continue
    fi
    wait_cool
    echo "$(date -Iseconds) bloco=$b rotulo=$rotulo temp=$(battery_temp)" | tee -a "$OUT/log.txt"
    rm -rf "$RAW"
    if ! ./gradlew :macrobenchmark:connectedBenchmarkAndroidTest \
         -Pandroid.testInstrumentationRunnerArguments.class=com.example.benchmark.FeedBenchmark \
         -Pandroid.testInstrumentationRunnerArguments.architecture=MVVM \
         -Pandroid.testInstrumentationRunnerArguments.scenario=S1_CONTROL \
         -Pandroid.testInstrumentationRunnerArguments.iterations=10; then
      echo "$(date -Iseconds) FALHOU bloco=$b rotulo=$rotulo" | tee -a "$OUT/log.txt"
      echo "Corrija a causa e rode de novo com: ./aa_test.sh $SERIAL $LABEL retomar"
      exit 1
    fi
    cp "$(find "$RAW" -name '*benchmarkData.json' | head -n 1)" "$alvo"
  done
done

echo "Teste A/A concluído em $OUT:"
ls "$OUT"