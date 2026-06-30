WebDAV между устройствами, даты задач, Android resume

**WebDAV — синхронизация между устройствами**
- Отдельные кнопки «Скачать и объединить» / «Загрузить сейчас» (десктоп и Android)
- Периодическая проверка сервера с запросом «Скачать и объединить?» / «Позже»
- Pull-before-push, hash-проверка `data.json`, merge статуса и сессий
- Android: экран WebDAV, WorkManager в фоне, in-app монитор; минимум 15 мин в фоне

**Список задач — десктоп**
- Даты «Создана» / «Завершена» в развёрнутой карточке (read-only)
- Адаптивная шапка, исправлено наложение UI при смене вкладок

**Android — задачи**
- Фильтры Сегодня / В работе / Все; зачёркнутые завершённые
- «Возобновить» для завершённых задач

**Настройки**
- Расширенная вкладка WebDAV (интервал, «Позже», sync on startup/shutdown)

---

| Платформа | Файл |
|-----------|------|
| Linux amd64 | `tasktimer-link-b24-0.5.31-amd64.deb` |
| Windows x64 | `tasktimer-link-b24-0.5.31-win64.exe` |
| macOS arm64 | `tasktimer-link-b24-0.5.31-macos-arm64.zip` |
| Android 10+ | `tasktimer-link-b24-0.5.31-android.apk` |

Инструкция: [ИНСТРУКЦИЯ.md](https://github.com/alexandrgert/timer-app/blob/main/ИНСТРУКЦИЯ.md) · подробные release notes: [docs/release-notes-v0.5.31.md](https://github.com/alexandrgert/timer-app/blob/main/docs/release-notes-v0.5.31.md)
