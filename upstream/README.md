# Upstream: win-timer-app-v1

Снимок репозитория [useraitester-creator/win-timer-app-v1](https://github.com/useraitester-creator/win-timer-app-v1) для сравнения и переноса изменений.

Основной продукт — **TaskTimer link B24** в корне репозитория (`timerapp_ag/`). Эта папка — исходный **TaskTimer** (пакет `win_timer_app`), не собирается как часть релизов link B24.

Обновление снимка:

```bash
rm -rf upstream/win-timer-app-v1
git clone --depth 1 https://github.com/useraitester-creator/win-timer-app-v1.git upstream/win-timer-app-v1
rm -rf upstream/win-timer-app-v1/.git
```
