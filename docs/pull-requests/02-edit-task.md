# PR 2: Редактирование задачи (название и описание)

**Target:** [useraitester-creator/win-timer-app-v1](https://github.com/useraitester-creator/win-timer-app-v1)  
**Branch:** `feat/upstream-edit-task` (локально, **не запушена**)  
**Worktree:** `../win-timer-upstream-pr2` относительно fork `timer-app`

## Summary

- `AppController.update_task()` — изменение названия и описания с валидацией непустого title.
- Диалог **«Редактировать задачу»** и кнопка **«Изменить»** в строке задачи (для незавершённых).
- После сохранения список и панель таймера обновляются через `refresh_ui()`.

## Исключено намеренно

WebDAV, Android, legacy merge, expandable task row / footer UX из fork.

## Источник (fork)

- `alexandrgert/timer-app` — `af09830`
- `src/timerapp_ag/domain/task_ops.py` → `update_task` inline в `controller.py`
- `TaskEditDialog` в `main_window.py`, кнопка в `ui/task_row.py`

## Test plan

- [x] `pytest` — все тесты зелёные
- [ ] Ручная проверка: «Изменить» → сохранить → название в списке и в панели таймера
- [ ] Пустое название → сообщение об ошибке, задача не меняется

## Зависимости от других PR

Независим от PR #1 (комментарии к сессиям). Можно мержить в любом порядке.

## Команды для отправки (когда будет готово)

```bash
cd /home/alex/cursorai/project/github/win-timer-upstream-pr2
git push -u origin feat/upstream-edit-task
gh pr create --repo useraitester-creator/win-timer-app-v1 \
  --base main \
  --head alexandrgert:feat/upstream-edit-task \
  --title "feat: редактирование названия и описания задачи" \
  --body-file docs/pull-requests/02-edit-task.md
```
