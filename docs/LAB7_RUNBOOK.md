# Лабораторная работа №7 — карта выполнения и получения доказательств

Документ связывает все 15 пунктов методички с конкретными классами, командами и экранными действиями проекта Daymark. Не подставляйте фиктивные значения Profiler/JaCoCo: после запуска сохраняйте реальные отчёты и снимки экрана.

## 1. Logcat и логирование

`debug/AppLog.kt` содержит методы `d/i/w/e`; все уровни проходят через проверку `BuildConfig.DEBUG`, поэтому приложение не пишет диагностические логи в release. `TasksViewModel` логирует создание задачи и ход синхронизации. Кнопка `Write Logcat sample entries` в debug-панели записывает четыре безопасных примера, включая error с тестовым исключением без падения процесса.

Действия: запустить debug-приложение; добавить задачу и нажать Sync; открыть Logcat; выбрать процесс `com.daymark.taskflow.debug`; установить фильтр `tag:Daymark`; в Settings нажать Write Logcat sample entries — приложение создаст безопасные d/i/w/e сообщения без падения; отдельно выбрать уровни Debug, Info, Warning и Error. Скриншоты: 1) весь набор уровней; 2) фильтр по тегу и уровню; 3) release-приложение без диагностических записей.

## 2. Android Studio Debugger

Проверяемая логика находится в `domain/TaskMetrics.kt`, `TaskValidator.kt` и `TaskInteractor.kt`. В качестве контролируемой ошибки временно замените формулу `completed / total * 100` на `completed / (total + 1) * 100` в `completionPercent`.

Правильный исходный код в `domain/TaskMetrics.kt`:

```kotlin
val rawPercent = ((completed.toDouble() / total.toDouble()) * 100.0).toInt()
```

Для учебной инъекции временно замените его ошибочным знаменателем:

```kotlin
val rawPercent = ((completed.toDouble() / (total + 1).toDouble()) * 100.0).toInt()
```

На данных `completed=1`, `total=3` ошибочная версия показывает 25%, правильная — 33%. Поставьте обычный breakpoint в `completionPercent`; условный breakpoint `total == 3`; logpoint с выражением `"done=$completed total=$total"`; запустите Insights. Используйте Step Over на вычислении, Step Into в тесте/методе, Step Out для выхода; в Watches добавьте `completed`, `total`, `rawPercent`; в Evaluate Expression вычислите `completed.toDouble() / total * 100`. Найдите ошибку на случае 1 из 3, восстановите исходную правильную формулу и повторно запустите `TaskDomainTest`.

Последовательность: воспроизведение → breakpoint → проверка входных данных → вычисление промежуточного результата → причина (неверный знаменатель) → исправление → регрессионный тест.

## 3. Разбор крэшей

Три debug-only кнопки размещены в Settings → Developer diagnostics: `Reproduce NullPointerException`, `Reproduce IndexOutOfBoundsException`, `Reproduce NetworkOnMainThreadException`. Код — `DebugCrashScenarios.kt`; блок панели отсутствует в release. Исключения намеренно возникают в debug-сценариях, затем перехватываются на границе `SettingsFragment`, полный stack trace записывается через `AppLog.e`, а приложение показывает Toast и остаётся открытым. Для NetworkOnMainThreadException выполняется loopback-only сетевой вызов под StrictMode с `penaltyLog`; `penaltyDeath` не используется, поскольку он завершает весь процесс.

В Android Studio откройте `Run → View Breakpoints`, добавьте Exception Breakpoints для `NullPointerException`, `IndexOutOfBoundsException` и `NetworkOnMainThreadException`; установите остановку на `Caught` (и `Uncaught`, если нужно отдельно показать фатальный сценарий). Нажимайте по одной кнопке под Debugger, после остановки изучите стек и продолжите выполнение. После этого приложение остаётся открытым, а полный stack trace доступен в Logcat по тегу `Daymark`.

Чтобы продемонстрировать именно исправление причин, а не только перехват:
- NPE: вместо `value!!.length` используйте `value?.length ?: 0` либо `requireNotNull(value)` там, где null запрещён контрактом.
- IndexOutOfBounds: вместо прямого `items[index]` применяйте проверку диапазона или `items.getOrNull(index)`.
- NetworkOnMainThread: сетевой вызов выполняйте внутри `withContext(Dispatchers.IO)`, как в `HttpTaskApi.fetchTodos()`.

После показа Stack Trace сравните ошибочный фрагмент с безопасным вариантом, сохраните оба скриншота и выполните регрессионные тесты. Это намеренные диагностические сценарии, а не дефекты обычного пользовательского пути.

## 4. Layout / Database / Network Inspector

- **Layout Inspector:** сравните старую и исправленную иерархию экрана Tasks. Найденная проблема старой версии — `RecyclerView` с `wrap_content` находился внутри `NestedScrollView` с отключённой вложенной прокруткой. При длинном списке это заставляло список измеряться по содержимому и мешало штатной виртуализации строк. В исправленной версии `ScrollView` содержит только заголовок/сводку/фильтры, а `RecyclerView` находится в отдельном `FrameLayout` с `layout_height=match_parent`, `nestedScrollingEnabled=true` и viewport высотой `0dp + layout_weight=1`. В Layout Inspector подтвердите, что RecyclerView больше не является потомком ScrollView.
- **Database Inspector:** `View → Tool Windows → App Inspection → Database Inspector`, выберите `daymark.db`, запрос `SELECT * FROM tasks ORDER BY dueAt` и проверьте добавление/выполнение задачи.
- **Network Inspector:** нажмите Sync на экране Tasks; запрос `GET https://jsonplaceholder.typicode.com/todos?_limit=5`, посмотрите status, headers, latency и response body. При отсутствии интернета приложение отображает сообщение, сохраняя локальные задачи.

Снимки: иерархия Layout Inspector; таблица Room после добавления записи; HTTP request/response в Network Inspector.

## 5. Unit-тесты бизнес-логики

13 тестов JUnit 4 в `TaskDomainTest.kt`, 5 параметризованных случаев в `TaskValidatorParameterizedTest.kt`, а также тесты Interactor, Repository, HTTP API, fake-репозитория и ViewModel: пустая строка, trim/collapse whitespace, длина 80/81, ограничение заметки, проценты 0/33/100, выход за границу, подсчёт и overdue. `TaskInteractorTest.kt` дополняет сценарии записи. Запуск: `./gradlew :app:testDebugUnitTest`. Откройте HTML/XML отчёт Gradle и сохраните скриншот успешного прогона и списка тестов.

## 6. Моки и изоляция

`TaskInteractorTest` подменяет `TaskRepository` Mockito-моком и проверяет вызов через `verifyBlocking` и аргументы через `check`. `DefaultTaskRepositoryTest` изолирует DAO и REST API. `InMemoryTaskRepositoryFake` — отдельная работающая fake-реализация `TaskRepository` на `MutableStateFlow`; `Stub` — заранее заданный ответ `MockWebServer`; `Mock` — объект Mockito с проверяемыми вызовами.

Для корутин тесты используют `runTest` из `kotlinx-coroutines-test`; реальный HTTP вынесен в IO dispatcher. Сохраните скриншот теста, где `verifyBlocking` подтверждает передачу нормализованной задачи.

## 7. Robolectric

`AndroidContextRobolectricTest.kt` запускается на JVM (`./gradlew :app:testDebugUnitTest`) и проверяет запись/чтение SharedPreferences, `R.string.app_name`, action/type/extra объекта Intent без запуска эмулятора. Сравнение скорости: запустите `AndroidContextRobolectricTest` и `TaskDaoInstrumentedTest`, возьмите фактическое время из Gradle HTML report/CI и запишите рядом метод выполнения (JVM/Robolectric против устройства/эмулятора). Значения не нужно заранее вписывать.

## 8. Room и сеть

- Room in-memory DAO: `TaskDaoInstrumentedTest.kt` — insert, Flow observation, complete, delete, delete completed.
- Миграции 1 → 2 → 3: `TaskMigrationInstrumentedTest.kt` создаёт схему v1 с legacy-записью, запускает цепочку миграций и проверяет сохранность данных, `isPinned=false` и `remoteId=null`. Версия 3 добавляет `remoteId` для идемпотентного REST-импорта.
- Повторная синхронизация: `RepositorySyncInstrumentedTest.kt` дважды импортирует один удалённый элемент и подтверждает, что локальная запись остаётся одна и сохраняет свой ID. Существующий локальный note/priority/pin не теряются при обновлении удалённого статуса.
- MockWebServer: `HttpTaskApiTest.kt` проверяет 200, 404, 500, задержку ответа (read timeout), невалидный JSON. Всё тестируется локально без доступа к настоящему сервису.

Запуск JVM части: `:app:testDebugUnitTest`; Room/миграции/repository-sync/Espresso: `:app:connectedDebugAndroidTest`. В текущей версии — 32 метода JVM-тестов (36 выполнений с учётом пяти вариантов параметризованного теста), 10 Android instrumented-тестов и 2 Macrobenchmark-сценария. Реальные числа passed/failed фиксируйте только после запуска. Для миграций применяется последовательность `MIGRATION_1_2` и `MIGRATION_2_3`; destructive fallback отключён, чтобы не удалять существующие пользовательские данные.

## 9. Espresso UI

`TaskFlowUiTest.kt` проверяет добавление задачи через форму, отказ пустого названия, фильтр Done, переход в Insights и системный share chooser через Espresso Intents. `AppOperationTracker.resource` ожидает завершения фоновых операций Room и Sync, включая начальное заполнение списка. `DaymarkTestRunner` запускает `DaymarkTestApplication` с фиксированным fake API, чтобы UI-тесты не зависели от живого интернета. Для теста нужен включённый эмулятор/устройство и установленная debug-сборка.

## 10. Page Object и flaky-тесты

`TasksPageObject.kt` хранит селекторы/действия отдельно от сценариев. Для стабильности:
- `testOptions.animationsDisabled = true`;
- Android Test Orchestrator включён в `testOptions.execution`;
- `clearPackageData=true` перед каждым инструментированным тестом;
- ожидание загрузки Room/операций и Sync делается через IdlingResource и тестовый fake API, а не `Thread.sleep` или внешний интернет;
- сценарий создаёт собственную запись и не зависит от фиксированного порядка списка.

Проверка: повторить Espresso класс 3–5 раз на эмуляторе; сравнить результат. Если тест падает, изучить фактический stack trace, не добавлять произвольные паузы.

## 11. Матрица устройств и ручное тестирование

| № | Проверка | Шаги | Ожидаемый результат |
|---:|---|---|---|
| 1 | Первый запуск | Очистить данные, открыть приложение | Три стартовые задачи, прогресс и статистика отображаются |
| 2 | Создание | Add a task, заполнить название и заметку, Save | Задача появляется в All, счётчики пересчитываются |
| 3 | Валидация | Отправить пустое название | Форма остаётся открытой, показана ошибка, запись не создаётся |
| 4 | Выполнение | Поставить отметку у открытой задачи | Задача перечёркнута/перемещена, Done и Insights обновлены |
| 5 | Фильтры | Выбрать Open, затем Done, затем All | Каждый список показывает только соответствующие задачи |
| 6 | Удаление | Нажать delete у задачи; в Settings удалить завершённые | Удалённые записи исчезают из списка и БД |
| 7 | Сеть | Выключить сеть, нажать Sync; включить сеть и повторить | В offline — понятное сообщение и приложение работает; online — импортированы элементы |
| 8 | Поворот | Повернуть устройство на экране Tasks/Insights | Приложение не падает; данные и выбранная вкладка сохраняются |
| 9 | Жизненный цикл | Свернуть приложение, вернуться; затем завершить процесс и открыть заново | Данные читаются из Room, приложение восстанавливается |
| 10 | Адаптация | API 26/35/36; компактный/большой экран; portrait/landscape; dark theme; другой язык | Нет обрезанного текста/перекрытий; основные действия доступны |

Дополнительные прогоны: `adb shell monkey -p com.daymark.taskflow.debug --throttle 300 -v 500` (пакет debug именно `com.daymark.taskflow.debug`). Зафиксируйте найденные реальные дефекты; если дефектов нет, так и укажите, не придумывайте.

Матрица устройств: API 26 (минимальная поддержка), API 35, API 36; маленький экран 5.4", типовой 6.1", большой 6.7"; portrait/landscape; светлая/тёмная тема; системный язык English/Russian. В Firebase Test Lab используйте минимум два разных device model/API.

## 12. JaCoCo

Плагин JaCoCo 0.8.13 и задачи `jacocoTestReport`, `jacocoTestCoverageVerification` формируют JVM-отчёт. Quality gate установлен на **70% line coverage пакета `domain`** (TaskValidator, TaskMetrics, TaskInteractor); Activity/XML намеренно не входят в знаменатель. Запуск JVM: `./gradlew :app:testDebugUnitTest :app:jacocoTestReport :app:jacocoTestCoverageVerification`. Для instrumented-тестов включён `enableAndroidTestCoverage=true`; запустите `./gradlew :app:createDebugCoverageReport` (при наличии подключённого устройства/эмулятора). Это отдельный Android coverage HTML-отчёт; не складывайте его с JVM-процентом в одну цифру без явного объединения execution data. Снимите оба отчёта и запишите фактические проценты. Не считать порог 70% уже полученным результатом.

## 13. Profiler, LeakCanary, StrictMode

Debug-приложение включает StrictMode (`detectAll().penaltyLog()`), LeakCanary 2.14 и логирование. Сетевой слой работает на IO; Room использует suspend/Flow; UI собирает потоки через `repeatOnLifecycle`, ссылки на ViewBinding очищаются в `onDestroyView`, репозиторий получает Application-scoped контекст.

Профилирование: Android Studio → Profiler → CPU (добавление 20–50 задач/скролл), Memory (record heap dump после навигации между вкладками/возврата), Energy, Network (Sync). Сохранить реальные скриншоты и сравнить до/после.

Для демонстрации Activity leak откройте Settings → Developer diagnostics и нажмите `Simulate Activity memory leak (debug)`, затем поверните экран или пересоздайте Activity. LeakCanary должен показать путь удержания через `DebugLeakScenarios.retainedActivity`. После сохранения trace нажмите `Release retained Activity` на новом экране Settings; ссылка очищается. Повторите lifecycle-сценарий и подтвердите отсутствие повторной утечки. **Сценарий опциональный и запускается только вручную в debug**; release использует no-op реализацию, ViewModel не хранит Activity, а ViewBinding обнуляется.

Для метрик до/после снимите одинаковый сценарий до оптимизации (например, временно выполните сетевой запрос в UI-потоке/удержите Activity) и после исправления (IO + lifecycle-aware collection + очистка binding). Внесите реальный P50/median CPU/Memory/Energy/Network из Profiler; здесь намеренно не указаны вымышленные числа.

## 14. Macrobenchmark и Firebase Test Lab

`benchmark/src/.../StartupBenchmark.kt` содержит два измерения: холодный старт `StartupTimingMetric` и прокрутка длинного списка `FrameTimingMetric`, по 5 итераций. Вариант приложения `benchmark` создаёт 30 синтетических задач только для измерения прокрутки; в обычном debug/release-поведении остаются 3 начальные задачи. Benchmark-вариант наследует release-настройки, не является debuggable, подписывается debug-ключом и объявлен `profileable` через `app/src/benchmark/AndroidManifest.xml`; тестовый Benchmark APK сам остаётся debuggable. Запуск: `./gradlew :benchmark:connectedBenchmarkAndroidTest`. Сохраните медиану/percentiles и отчёт Macrobenchmark, не указывайте придуманные миллисекунды.

GitHub workflow `firebase-test-lab.yml` запускается вручную после добавления secrets `GCP_SA_KEY`, `FIREBASE_PROJECT_ID`, `FIREBASE_RESULTS_BUCKET`. Он отправляет debug APK и instrumentation APK на два профиля устройств (Pixel 8 (`shiba`) / API 34 и Pixel 8a (`akita`) / API 35), включает Android Test Orchestrator и запись видео. Перед запуском проверьте доступность модели и версии в своём проекте командой `gcloud firebase test android models list`; каталог устройств меняется. Сохраните Firebase Test Lab matrix, видео, logs и outcomes. Без этих secrets облачный job не может запуститься.

## 15. CI и итоговый отчёт качества

`.github/workflows/android-ci.yml`: PR/push → Android Lint → ktlintCheck → unit tests → JaCoCo 70% → assemble → emulator instrumentation tests и Android coverage; отчёты загружаются как CI artifacts. `ktlintCheck` включён, но временно настроен `ignoreFailures=true` в первом проходе, чтобы собрать список замечаний, не блокируя сборку; Android Lint и тестовый/coverage quality gate остаются блокирующими. После форматирования Kotlin-файлов переведите `ignoreFailures=false`. Firebase workflow отделён из-за credentials/стоимости.

В итоговую таблицу внесите только найденные дефекты и фактические статусы. Приоритет: P0 — потеря данных/невозможность запуска; P1 — основной сценарий не работает; P2 — некритичная функциональность; P3 — косметика. Укажите ссылку на GitHub repository после публикации каталога проекта.

## Команды для Windows PowerShell

Запускайте из корневого каталога проекта, где находится `gradlew.bat`:

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:jacocoTestReport :app:jacocoTestCoverageVerification
.\gradlew.bat :app:assembleDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:createDebugCoverageReport
.\gradlew.bat :benchmark:connectedBenchmarkAndroidTest
```

Подробный перечень снимков для всех пунктов — `docs/LAB7_EVIDENCE_CHECKLIST.md`. Таблица для регистрации фактически обнаруженных ошибок — `docs/DEFECT_LOG_TEMPLATE.md`.

## Контрольные вопросы

1. **Unit и instrumented тесты; integration и UI.** Unit-тест изолирует небольшую функцию/класс и работает на JVM без Android-устройства. Instrumented-тест исполняется на Android runtime эмулятора/устройства и проверяет платформенные компоненты. Интеграционный тест проверяет взаимодействие нескольких модулей (например, Repository + Room/HTTP); UI-тест проверяет пользовательский сценарий через интерфейс и действия.
2. **Пирамида тестирования.** В основании много быстрых и дешёвых unit-тестов, выше меньше интеграционных, на вершине немного UI/E2E. Большинство тестов — unit, потому что они быстрые, локализуют причину отказа и дешевле поддерживаются.
3. **Mock, stub, fake.** Stub возвращает заранее заданные ответы. Mock дополнительно проверяет взаимодействия/вызовы. Fake — упрощённая рабочая реализация (например, in-memory репозиторий). Изоляция зависимостей делает тест детерминированным и позволяет понять, какой компонент нарушил контракт.
4. **IdlingResource и flaky.** IdlingResource сообщает Espresso, что асинхронная операция завершилась и UI готов к проверке. Flaky-тест нестабилен из-за гонок, анимаций, сетевых задержек, общего состояния и зависимости от порядка запуска. Решение — управляемое ожидание, очистка данных, выключение анимаций и изоляция.
5. **Утечки памяти.** Частые причины: статическая ссылка на Activity/View/Context, незавершённый callback/listener, coroutine/job без отмены, observer без снятия, binding фрагмента после `onDestroyView`. Ищут через heap dump/Memory Profiler и LeakCanary.
6. **ANR.** Application Not Responding — приложение не отвечает из-за блокировки главного потока (долгая синхронная работа, I/O, deadlock). Диагностика: ANR traces, Logcat, CPU Profiler, StrictMode и анализ main thread stack.
7. **Отладка и тестирование.** Отладка — процесс локализации и исправления причины уже замеченной ошибки с debugger/logs/profiler. Тестирование — систематическая проверка поведения по тест-кейсам/ожиданиям для поиска регрессий и подтверждения качества.
