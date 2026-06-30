# PR 9: Tooltip иконки в трее при сворачивании окна

**Target:** [useraitester-creator/win-timer-app-v1](https://github.com/useraitester-creator/win-timer-app-v1)  
**Branch:** `feat/upstream-tray-tooltip` (локально, **не запушена**)  
**Worktree:** `../win-timer-upstream-pr9` относительно fork `timer-app`

## Summary

Динамическая подсказка иконки в системном трее:

| Состояние окна | Tooltip |
|----------------|---------|
| Окно **развёрнуто** | Название приложения (`TaskTimer` / `TaskTimer x.y.z`) |
| Окно **скрыто** (трей, minimize) | Названия активных и приостановленных задач (по строке) |
| Нет таймеров | `Нет активных таймеров` |

### Исправленные сценарии (из fork `bb1bca9`, v0.4.5)

1. **Minimize** — раньше `isVisible()` оставался `True`, подсказка показывала название приложения вместо задач. Теперь «окно открыто» = `isVisible() and not isMinimized()`.
2. **Пауза без виджета** — fallback на `timer_panel_task()` (running или последняя paused), если `_mini_task_id` ещё не задан.
3. **Закрытие плавающего виджета (✕)** — tooltip обновляется сразу после скрытия виджета.

## Исключено намеренно

WebDAV, Android, legacy merge, focus-line в tooltip (можно отдельным PR).

## Источник (fork)

- `alexandrgert/timer-app` — `bb1bca9`
- `format_tray_tooltip`, `resolve_floating_task`, `timer_panel_task`, `_update_tray_tooltip`

## Test plan

- [x] `pytest` — все тесты зелёные
- [ ] Свернуть окно кнопкой minimize → tooltip показывает задачу
- [ ] Стоп задачи → tooltip «Нет активных таймеров» или paused
- [ ] ✕ на плавающем виджете → tooltip обновился

## Зависимости

Независим от PR #4 (комментарии) и PR #5 (редактирование задачи).

## Команды для отправки (когда будет готово)

```bash
cd /home/alex/cursorai/project/github/win-timer-upstream-pr9
git push -u origin feat/upstream-tray-tooltip
gh pr create --repo useraitester-creator/win-timer-app-v1 \
  --base main \
  --head alexandrgert:feat/upstream-tray-tooltip \
  --title "fix: tooltip трея при сворачивании и паузе задачи" \
  --body-file docs/pull-requests/09-tray-tooltip.md
```
