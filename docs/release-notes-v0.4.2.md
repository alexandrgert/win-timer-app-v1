# TaskTimer link B24 — версия 0.4.2

## Что нового

### Надёжность WebDAV и слияния баз

- **Конфликт при выходе** распознаётся точнее — в том числе если ещё не было сохранённого хеша удалённой копии.
- **Уведомление о конфликте** при запуске показывается надёжнее: не только в трее, но и диалогом, если трей недоступен; с диска снимается **после** показа.
- **Объединение старых баз** учитывает не только число сессий, но и их содержимое (`started_at` / `ended_at`).
- **`WEBDAV_ENABLED` в окружении** больше не включает синхронизацию поверх явно выключенной настройки в `webdav.json`.
- **Восстановление из `.bak`:** битая схема в резервном файле не роняет запуск.

### Android (MVP)

- **Атомарное сохранение** `data.json` и резервная копия `.bak`.
- Совместимость с desktop: **`duration_minutes: null`** в focus timer.
- Таймер **не дублирует** открытые сессии при возобновлении.
- Запись на диск **вне главного потока**; ошибки сохранения показываются в UI.
- Резервное копирование Android-системой для данных приложения **отключено** (`allowBackup=false`).

### Сборки для всех платформ

В релизе — **Linux .deb**, **Windows .exe**, **macOS .zip** (arm64), **Android .apk**.

---

## Кому подойдёт обновление

- **0.4.1 или 0.4.0** — рекомендуется всем, особенно при WebDAV upload-only при выходе и Android MVP.
- Подробности 0.4.1: [release-notes-v0.4.1.md](release-notes-v0.4.1.md).

---

## Как обновиться

Скачайте сборку на [странице релиза v0.4.2](https://github.com/alexandrgert/timer-app/releases/tag/v0.4.2).
Минимальные требования: [system-requirements.md](system-requirements.md).

| Платформа | Файл | Установка |
|-----------|------|-----------|
| **Linux** (amd64) | `tasktimer-link-b24-0.4.2-amd64.deb` | `sudo dpkg -i …deb && sudo apt-get install -f` |
| **Windows** (x64) | `tasktimer-link-b24-0.4.2-win64.exe` | запустить exe |
| **macOS** 11+ | `tasktimer-link-b24-0.4.2-macos-arm64.zip` | распаковать, перетащить `.app` в Программы |
| **Android** 10+ | `tasktimer-link-b24-0.4.2-android.apk` | установить APK |

### Linux

```bash
wget https://github.com/alexandrgert/timer-app/releases/download/v0.4.2/tasktimer-link-b24-0.4.2-amd64.deb
sudo dpkg -i tasktimer-link-b24-0.4.2-amd64.deb
sudo apt-get install -f
tasktimer-link-b24
```

---

## Системные требования (кратко)

| Платформа | ОС | Процессор |
|-----------|-----|-----------|
| Linux | Debian 11+ / Ubuntu 20.04+, glibc ≥ 2.31 | x86_64 |
| Windows | 10 или 11, 64-bit | x64 |
| macOS | 11 Big Sur+ | Apple Silicon (arm64) |
| Android | 10 (API 29)+ | arm64 / armeabi-v7a |

Подробно: [system-requirements.md](system-requirements.md).

**Android APK** подписан debug-ключом (для тестов; не для Google Play без release keystore).

**macOS** — без Apple Developer ID; при первом запуске может понадобиться «Открыть в любом случае».

---

## Подробная инструкция

[ИНСТРУКЦИЯ.md](../ИНСТРУКЦИЯ.md)
