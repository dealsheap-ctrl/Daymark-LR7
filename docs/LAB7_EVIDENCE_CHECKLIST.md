# Чек-лист доказательств для защиты лабораторной работы №7

Эта папка предназначена для реальных материалов из Android Studio. Снимки экрана и числовые результаты не создаются автоматически и не должны подменяться макетами.

## Структура для сохранения материалов

Создайте каталог `lab7-evidence/` в корне проекта и сохраняйте материалы с такими именами:

```text
lab7-evidence/
  01-logcat/
  02-debugger/
  03-exceptions/
  04-inspectors/
  05-junit/
  06-mocks/
  07-robolectric/
  08-room-network/
  09-espresso/
  10-page-object/
  11-device-matrix-monkey/
  12-jacoco/
  13-profiler-leakcanary/
  14-macrobenchmark-firebase/
  15-ci/
```

## Что сохранить по каждому заданию

| № | Имя материала | Что должно быть видно |
|---:|---|---|
| 1 | `01-logcat/logcat-levels.png` | Logcat с тегом Daymark и D/I/W/E; отдельно `logcat-release-no-debug.png` для Release без диагностических логов |
| 2 | `02-debugger/breakpoint.png` | Обычный, условный breakpoint и Logpoint; `watches-evaluate.png` — значения completed/total и выражение; `step-debugger.png` — Step Over/Into/Out; `logic-fixed-test.png` — тест после исправления |
| 3 | `03-exceptions/npe-stacktrace.png` | NPE с Break on caught; аналогично `index-stacktrace.png`, `network-stacktrace.png`; `exceptions-after-fix.png` — исправленные варианты/регрессионные тесты |
| 4 | `04-inspectors/layout-before-after.png` | Layout Inspector: старая проблемная иерархия и новая; `database-inspector.png` — строки daymark.db; `network-inspector.png` — GET, HTTP status, response |
| 5 | `05-junit/junit-report.png` | Gradle HTML report с фактическим числом passed/failed/skipped; код тестов — отдельный фрагмент из `app/src/test` |
| 6 | `06-mocks/mockito-verify.png` | Mockito `verifyBlocking`/проверка аргумента; `fake-repository-test.png`; запуск coroutine test |
| 7 | `07-robolectric/robolectric-report.png` | Успешные Robolectric-тесты; `timing-comparison.png` — фактическое время Robolectric и instrumented теста |
| 8 | `08-room-network/room-in-memory.png` | Успешные DAO-тесты и in-memory БД; `migration-pass.png`; результаты MockWebServer для 200/404/500/timeout/invalid JSON |
| 9 | `09-espresso/espresso-pass.png` | Espresso отчёт: создание задачи, пустая валидация, переход между экранами, Intent и ожидание Sync |
| 10 | `10-page-object/page-object-test.png` | Page Object класс и 3–5 повторных успешных прогонов; Orchestrator/animations settings |
| 11 | `11-device-matrix-monkey/device-matrix.csv` | Устройство, API, размер, ориентация, тема, язык, результат; `monkey-log.txt` и только реально найденные дефекты |
| 12 | `12-jacoco/jacoco-unit.html` | HTML JaCoCo отчёт; `jacoco-instrumented.html` — отдельный Android coverage отчёт; проценты и domain classes |
| 13 | `13-profiler-leakcanary/cpu-before-after.png` | CPU до/после; `memory-heap-before-after.png`; LeakCanary leak trace и повторная проверка после release; Energy и Network |
| 14 | `14-macrobenchmark-firebase/macrobenchmark-startup.png` | StartupTimingMetric и FrameTimingMetric отчёты; Firebase matrix, видео и логи для каждого устройства |
| 15 | `15-ci/github-actions-green.png` | Успешный workflow на PR; artifacts со Lint, ktlint, tests, JaCoCo, APK и instrumented reports; ссылка на репозиторий |

## Команды Windows PowerShell

Открыть PowerShell в корне `Daymark-LR7`.

```powershell
# 1. Сборка и Android Lint
.\gradlew.bat :app:assembleDebug :app:lintDebug

# 2. Unit / Mockito / Robolectric / MockWebServer
.\gradlew.bat :app:testDebugUnitTest

# 3. JaCoCo для бизнес-логики
.\gradlew.bat :app:jacocoTestReport :app:jacocoTestCoverageVerification

# 4. Сборка тестового APK
.\gradlew.bat :app:assembleDebugAndroidTest

# 5. Room / миграции / Espresso — нужен запущенный API 35/36 эмулятор
.\gradlew.bat :app:connectedDebugAndroidTest

# 6. Android instrumented coverage — после подключения/запуска эмулятора
.\gradlew.bat :app:createDebugCoverageReport

# 7. Startup + scroll Macrobenchmark — нужен поддерживаемый физический/виртуальный девайс
.\gradlew.bat :benchmark:connectedBenchmarkAndroidTest
```

Monkey после установки debug APK:

```powershell
adb shell monkey -p com.daymark.taskflow.debug --throttle 300 -v 500
```

Путь debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Правила фиксации результатов

1. Не записывайте «BUILD SUCCESSFUL», проценты JaCoCo, миллисекунды, FPS или результаты Firebase до фактического прогона.
2. Если инструмент недоступен, укажите «не выполнено в данной среде» и причину.
3. В таблицу дефектов включайте только воспроизведённые проблемы с подтверждающим Logcat/Stack Trace/Profiler evidence.
4. Для задания 2 сохраняйте снимки именно двух состояний: искусственно испорченная формула и исправленная формула с регрессионным тестом.
5. Для задания 3 сохраняйте Stack Trace и место исключения; наличие `try/catch` само по себе не считается устранением причины.
6. Для задания 13 сохраняйте leak trace до очистки удерживающей ссылки и повторную проверку после исправления.
7. Для задания 14 сохраняйте median/percentiles, а не придумывайте значения из эмулятора другого типа.
