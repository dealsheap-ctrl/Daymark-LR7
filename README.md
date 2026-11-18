# Daymark — лабораторная работа №7

> **Важно для Gradle Sync:** в `app/build.gradle.kts` настройка JVM уже переведена на актуальный Kotlin DSL `kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }`. Не возвращайте `android { kotlinOptions { jvmTarget = "17" } }`: с текущей версией Kotlin Gradle Plugin это приводит к ошибке компиляции build script.

**Платформа:** Android, Kotlin, Views/XML  
**Совместимость сборки:** AGP 8.13.2 · Gradle 8.13 · Kotlin Gradle Plugin 2.3.21 · JDK 17 · compileSdk 36 · ktlint Gradle Plugin 14.2.0 / ktlint 1.8.0  
**AndroidX Core / AppCompat:** core-ktx 1.17.0 · appcompat 1.7.1 (закреплены совместимые версии для AGP 8.13.x и API 36)  
**Пакет:** `com.daymark.taskflow`  
**Мин. API:** 26 · **compileSdk:** 36 · **targetSdk:** 35  
**Хранилище:** Room / SQLite · **сеть:** OkHttp + Gson · **архитектура:** Activity + Fragments + ViewModel + Repository

Daymark — небольшой планировщик с локальным списком задач, приоритетами, отметкой выполнения, удалением, фильтрацией, аналитикой и импортом демонстрационных задач из REST API. Все три раздела работают с одним `TasksViewModel` и одним Room-репозиторием, поэтому изменения списка немедленно отражаются в статистике и фильтрах.

## Открытие в Android Studio

1. Распаковать каталог проекта и открыть **корневую папку `Daymark-LR7`** через `File → Open`.
2. Установить JDK 17 или новее и Android SDK Platform 36, Build Tools 36.0.0, Platform Tools и Emulator.
3. Дождаться Gradle Sync. Первый Sync загружает Android Gradle Plugin, Kotlin и зависимости Maven, поэтому требуется доступ к интернету.
4. Создать эмулятор API 35/36 в Device Manager либо подключить Android-устройство с включённой USB debugging.
5. Выбрать конфигурацию `app` и запустить `debug`.

В проект добавлен Gradle bootstrap-wrapper: `gradlew` / `gradlew.bat` загружает Gradle 8.13 из официального distribution URL при первом запуске и сверяет SHA-256 с закреплённым значением из официального списка контрольных сумм. Android Studio можно открыть непосредственно на корневой папке и выполнять Sync обычным способом.

## Что умеет приложение

- **Tasks:** добавление задачи с названием, заметкой и приоритетом; валидация; отметка выполнения; удаление; фильтры All / Open / Done; синхронизация с JSONPlaceholder.
- **Insights:** статистика открытых/завершённых задач и процент выполнения, автоматически обновляемые из того же потока данных.
- **Settings:** системный share chooser, удаление завершённых задач и debug-панель воспроизведения трёх исключений (перехватываются с записью stack trace в Logcat без закрытия приложения), тестовых Logcat-сообщений и Activity leak.
- Светлая и тёмная темы имеют отдельные палитры; выбор темы следует системной настройке Android.
- Room сохраняет задачи локально. При первом запуске создаются три стартовые записи. Удаление/отметка/добавление сразу попадают в БД. Повторный импорт REST-задач идемпотентен по `remoteId`: он обновляет уже импортированную строку, а не создаёт дубликат. Схема БД обновлена до версии 3 миграциями 1 → 2 → 3 без destructive fallback. Миграция 1 → 2 добавляет `isPinned`, 2 → 3 добавляет `remoteId` для идемпотентного REST-импорта.
- Сеть выполняется через OkHttp на `Dispatchers.IO`; пустой ответ, HTTP-ошибка, некорректный JSON и отсутствие соединения обрабатываются без падения интерфейса.

## Команды

Из корня проекта:

```bash
# Основная сборка и Android Lint
./gradlew :app:assembleDebug :app:lintDebug

# JVM unit-тесты (JUnit, Mockito, Robolectric, MockWebServer)
./gradlew :app:testDebugUnitTest

# Отчёт JaCoCo бизнес-логики и проверка порога 70%
./gradlew :app:jacocoTestReport :app:jacocoTestCoverageVerification

# Сборка instrumented тестов
./gradlew :app:assembleDebugAndroidTest

# Запуск Espresso / Room на включённом эмуляторе или устройстве
./gradlew :app:connectedDebugAndroidTest

# Macrobenchmark холодного старта (вариант benchmark, нужен API 26+ эмулятор/устройство)
./gradlew :benchmark:connectedBenchmarkAndroidTest

# Полный локальный quality gate
./gradlew :app:check :app:lintDebug
```

На Windows использовать `gradlew.bat` вместо `./gradlew`.

## Тесты в проекте

| Набор | Расположение | Что проверяет |
|---|---|---|
| JUnit 4 | `app/src/test/.../domain` | валидация, нормализация, метрики и граничные значения (13 тестов + 5 параметризованных кейсов) |
| Mockito / Fake | `TaskInteractorTest.kt`, `DefaultTaskRepositoryTest.kt`, `InMemoryTaskRepositoryFake.kt` | verify вызовов и аргументов; рабочая in-memory fake-реализация |
| Robolectric | `AndroidContextRobolectricTest.kt` | SharedPreferences, строковые ресурсы, Intent без эмулятора |
| ViewModel / Flow | `TasksViewModelTest.kt`, `MainDispatcherRule.kt` | StateFlow и seed-логика на управляемом TestDispatcher |
| MockWebServer | `HttpTaskApiTest.kt` | HTTP 200, 404, 500, timeout, malformed JSON |
| Room | `app/src/androidTest/.../TaskDaoInstrumentedTest.kt` | in-memory DAO: insert, observe, complete, delete |
| Migration | `TaskMigrationInstrumentedTest.kt` | миграция схемы 1 → 2 → 3 с сохранением старой записи и проверкой новых nullable/default полей |
| Espresso | `TaskFlowUiTest.kt` + `TasksPageObject.kt` | добавление задачи, навигация, системный share chooser, IdlingResource |
| Macrobenchmark | `benchmark/.../StartupBenchmark.kt` | холодный старт (`StartupTimingMetric и FrameTimingMetric`) и прокрутка длинного списка (`FrameTimingMetric`), по 5 итераций |

В JVM-наборе 29 тестовых методов; параметризованный валидатор выполняется в пяти вариантах, поэтому всего получается 36 JVM-выполнения. Дополнительно подготовлены 10 instrumented-тестов Room/миграции/идемпотентной синхронизации/Espresso и 2 Macrobenchmark-сценария. Эти наборы ещё нужно запустить на Android SDK/эмуляторе.

## Отчёты

Подробный порядок действий находится в `docs/LAB7_RUNBOOK.md`, список обязательных скриншотов и отчётов — в `docs/LAB7_EVIDENCE_CHECKLIST.md`, журнал дефектов — в `docs/DEFECT_LOG_TEMPLATE.md`. Фактические результаты Android-сборки, эмулятора, Profiler и Firebase Test Lab заполняются после реального запуска; не используйте старый DOCX как доказательство ещё не выполненных прогонов.

- Unit tests: `app/build/reports/tests/testDebugUnitTest/index.html`
- JaCoCo unit: `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- Android instrumented coverage: `:app:createDebugCoverageReport` (HTML обычно в `app/build/reports/coverage/`, execution data в `app/build/outputs/code_coverage/`)
- ktlint: `app/build/reports/ktlint/`
- Android Lint: `app/build/reports/lint-results-debug.html`
- Instrumented coverage: задача `:app:createDebugCoverageReport` / отчёт в `app/build/reports/coverage/` (путь может отличаться между версиями AGP).

Порог JaCoCo 70% настроен для бизнес-пакета `domain`, а не для всего Android UI-кода: это соответствует требованию покрывать логику, а не искусственно увеличивать процент тестами для Activity и XML.

## CI

`.github/workflows/android-ci.yml` запускает Android Lint, ktlintCheck, unit-тесты, JaCoCo, сборку и instrumented-тесты в эмуляторе при `push` / `pull_request`. Android Lint, unit-тесты и порог JaCoCo блокируют CI; ktlint включён с публикацией отчёта и временно не блокирует сборку, пока проект не будет полностью отформатирован. `.github/workflows/firebase-test-lab.yml` — ручной запуск Firebase Test Lab на нескольких конфигурациях. Для Firebase необходимо добавить GitHub Secrets `GCP_SA_KEY`, `FIREBASE_PROJECT_ID`, `FIREBASE_RESULTS_BUCKET`.

## Важные границы проверки

Исходники, тесты и CI-конфигурации подготовлены в проекте. Этот контейнер не содержит Android SDK, Gradle distribution, Android Studio или эмулятор и не имеет сетевого доступа для скачивания Maven/Google Maven артефактов. Поэтому APK, фактические результаты тестовых прогонов, отчёт JaCoCo, замеры Profiler и Firebase Test Lab в этой среде не создавались. После открытия проекта в Android Studio эти команды должны быть выполнены, а сформированные результаты приложены к отчёту. Никакие значения производительности или скриншоты эмулятора не выдуманы.
