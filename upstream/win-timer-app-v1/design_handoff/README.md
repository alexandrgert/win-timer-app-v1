# Handoff: TaskTimer UI Redesign — Bitrix24 Style

## Overview

Редизайн десктопного приложения TaskTimer (Python + PySide6/Qt).  
Цель — привести интерфейс в соответствие с визуальным языком Bitrix24:
нейтральный светлый фон, синий акцент `#3B83F6`, белые карточки с тенями,
скруглённые углы 8–12px, лёгкий шрифт Inter.

## About the Design Files

Файлы в этом пакете — **дизайн-референсы на HTML/CSS/React**.  
Их НЕ нужно запускать в продакшне. Задача — **воссоздать этот же визуал
в существующем PySide6-коде** (`win_timer_app/main_window.py`) средствами
Qt Stylesheets (QSS), заменив текущие стили.

## Fidelity

**High-fidelity.** Цвета, отступы, скругления и интерактивные состояния
описаны точно — разработчик должен воспроизвести их в QSS максимально близко.

---

## Design Tokens

### Цвета

```
/* Фон и поверхности */
--bg:       #F2F3F7   /* фон окна, QMainWindow background */
--surface:  #FFFFFF   /* белые карточки, панели */
--surface2: #F5F6FA   /* вторичный фон: кнопки-призраки, поля ввода */

/* Границы */
--br:       rgba(0,0,0, .08)  ≈ #DCDEE3  /* основная граница */
--br2:      rgba(0,0,0, .13)  ≈ #D0D2D8  /* акцентная граница, фокус */

/* Текст */
--text:     #252835   /* основной */
--t2:       #828B9A   /* вторичный */
--t3:       #B8BDC9   /* плейсхолдер, disabled */

/* Акцент — Bitrix24 Blue */
--accent:   #3B83F6
--accent-h: #2563EB   /* hover */
--accent-d: rgba(59,131,246, .10)  ≈ #E8F0FD  /* лёгкая подложка */

/* Статусы */
--green:    #27AE60   /* «В работе» */
--green-d:  rgba(39,174,96, .10)  ≈ #E8F6EF
--amber:    #E07B35   /* «Пауза» */
--amber-d:  rgba(224,123,53, .10) ≈ #FDF0E6
--red:      #E05353   /* «Завершить», деструктивные действия */
--red-d:    rgba(224,83,83, .10)  ≈ #FDE8E8

/* Тёмный блок таймера (правая панель) */
--timer-bg:         #131720
--timer-bg-running: линейный градиент от #0E1D17 к #131720
--timer-border:     rgba(255,255,255, .06)
--timer-border-run: rgba(39,174,96, .25)
--timer-card-bg:    rgba(255,255,255, .05)
--timer-text:       rgba(255,255,255, .75)
--timer-muted:      rgba(255,255,255, .28)
--timer-digit:      #7EB3FA   /* цифры таймера в покое */
--timer-digit-run:  #27AE60   /* цифры таймера при запуске */
```

### Типографика

```
Font-family:  Inter (основной), system-ui как fallback
              Roboto Mono (все числовые значения времени)

Размеры:
  11px  — вторичные метки (сег./всего, подписи)
  12px  — кнопки, фильтры, действия
  12.5px — название задачи в строке
  14px  — заголовки разделов
  38px  — большой таймер (Roboto Mono, weight 300)

Веса:
  300 — body текст, значения времени
  400 — обычный текст
  500 — кнопки, активные состояния
```

### Отступы

```
Padding окна/панелей:   20px
Gap между карточками:   6px
Высота строки задачи:   48px
Высота кнопок subbar:   28–30px
Padding строки задачи:  0 14px
Ширина панели таймера:  268px
```

### Border-radius

```
Карточки задач:         10px
Кнопки (subbar):        8px
Кнопки (Старт/Стоп):    10px
Кнопки (модалки):       8px
Боковая панель кнопки:  10px
Логотип/иконки sidebar: 10px
Попап календаря:        12px
Ячейки календаря:       8px
Textarea:               10px
Модальное окно:         14px
```

### Тени

```
Карточка (idle):  box-shadow: 0 1px 3px rgba(0,0,0,.06), 0 2px 8px rgba(0,0,0,.04)
Карточка (hover): box-shadow: 0 2px 10px rgba(0,0,0,.08), 0 4px 20px rgba(0,0,0,.05)
Модальное окно:   box-shadow: 0 12px 40px rgba(0,0,0,.12)
Активный chip:    box-shadow: 0 1px 4px rgba(0,0,0,.07)
```

---

## Layout Structure

```
QMainWindow
├── QSplitter (горизонтальный)
│   ├── Sidebar (width: 52px, bg: #FFFFFF, border-right: 1px solid #DCDEE3)
│   │   ├── Logo icon (32×32px, radius 10px, bg: #3B83F6)
│   │   ├── NavButton "Задачи"   (38×38px, radius 10px)
│   │   ├── NavButton "Фокус"    (38×38px, radius 10px)
│   │   ├── [spacer]
│   │   └── NavButton "Настройки"
│   │
│   └── MainArea (flex: 1)
│       ├── Subbar (height: 48px, bg: #FFFFFF, border-bottom: 1px #DCDEE3)
│       │   ├── FilterChip "Сегодня"    (pill, radius 8px)
│       │   ├── FilterChip "В работе"   (pill, radius 8px)
│       │   ├── FilterChip "Все"        (pill, radius 8px)
│       │   ├── DatePickerButton        (radius 8px)
│       │   ├── [spacer]
│       │   ├── Button "С портала"      (ghost, radius 8px)
│       │   └── Button "+ Новая задача" (accent #3B83F6, radius 8px)
│       │
│       └── ContentRow (horizontal)
│           ├── TaskList (flex: 1, bg: #F2F3F7, padding: 12px 20px, gap: 6px)
│           │   └── TaskRow × N (height: 48px, bg: #FFF, radius 10px, shadow)
│           │
│           └── TimerPanel (width: 268px, bg: #131720, dark)
│               ├── Label "ТАЙМЕР"
│               ├── TimerCard (radius 12px, glass bg)
│               │   ├── TaskName (text: rgba(255,255,255,.75))
│               │   ├── TimeClock (38px, Roboto Mono)
│               │   └── Sub: Сегодня / Всего
│               ├── ProgressBar (3px, green)
│               └── Buttons: Стоп / Завершить задачу
```

---

## Screens / Views

### 1. Главное окно — вкладка «Задачи»

**Subbar (панель фильтров)**
- Высота: 48px, фон белый, нижняя граница `#DCDEE3`
- Три chip-кнопки: «Сегодня» / «В работе» / «Все»
  - Idle: `bg #F5F6FA`, `border transparent`, `color #828B9A`, `radius 8px`
  - Active: `bg #FFFFFF`, `border 1px #D0D2D8`, `color #3B83F6`, `font-weight 500`, `shadow 0 1px 4px rgba(0,0,0,.07)`
  - Hover: `bg #FFFFFF`, `color #828B9A`
- Кнопка-дата «📅 15 июня»: ghost, `border 1px #D0D2D8`, `radius 8px`, при клике → попап с мини-календарём
- Кнопка «С портала»: ghost, `border 1px #D0D2D8`, `radius 8px`, `color #828B9A`
- Кнопка «+ Новая задача»: `bg #3B83F6`, `color #FFF`, `radius 8px`, hover → `#2563EB`

**Строка задачи (TaskRow)**
- Высота: 48px, `bg #FFFFFF`, `radius 10px`, `border 1px #DCDEE3`
- Тень idle: `0 1px 3px rgba(0,0,0,.06), 0 2px 8px rgba(0,0,0,.04)`
- Gap между строками: 6px
- Padding: `0 14px`
- Содержимое (слева → справа):
  1. **Цветная точка** (6×6px, circle):
     - running: `#27AE60` + пульсация opacity
     - paused:  `#E07B35`
     - todo:    прозрачная, `border 1.5px #B8BDC9`
     - done:    `#B8BDC9`
  2. **Название задачи** — `font-size 12.5px`, `color #252835`, truncate
  3. **[spacer]**
  4. **Время** (появляется всегда):
     - Лейбл «сег.» `color #B8BDC9`, `font-size 10px`
     - Значение `Roboto Mono 11px #828B9A`; если активная → `#27AE60`
     - Разделитель `·`
     - Лейбл «всего», значение аналогично
  5. **Действия** (появляются при hover и всегда у running):
     - Иконка история `26×26px`, `radius 7px`
     - Текст «Открыть в Б24» `font-size 11px`, `radius 7px`
     - Текст «Завершить» `font-size 11px`, `radius 7px`
     - Иконка корзины `26×26px`, `radius 7px`, hover → `bg #FDE8E8, color #E05353`
     - Кнопка Старт/Стоп `height 26px`, `radius 7px`

- **Состояние running:**
  - `border 1px rgba(39,174,96,.28)`
  - `background linear-gradient(to right, rgba(39,174,96,.06) 0%, #FFF 52%)`
  - Левая полоска: `3px wide, bg #27AE60`
  - Кнопка «Стоп»: `bg #F5F6FA`, `border 1px #D0D2D8`

- **Состояние done:**
  - `opacity: 0.68`
  - Название зачёркнуто, `color #B8BDC9`

### 2. Правая панель «Таймер» (тёмная)

**Без активной задачи:**
- `bg #131720`, иконка часов + текст «Выберите задачу и нажмите Старт»
- Иконка в контейнере 48×48px, `radius 12px`, `bg rgba(255,255,255,.06)`

**С активной задачей:**
- `bg #131720`
- Лейбл «ТАЙМЕР»: `font-size 9.5px`, `letter-spacing .12em`, `color rgba(255,255,255,.28)`
- TimerCard:
  - `bg rgba(255,255,255,.05)`, `border 1px rgba(255,255,255,.08)`, `radius 12px`, `padding 14px`
  - Название: `font-size 12.5px`, `color rgba(255,255,255,.75)`, 2 строки максимум
  - Цифры: `Roboto Mono 38px weight 300`, цвет `#7EB3FA` (в покое)
  - Подписи: `font-size 9px`, `color rgba(255,255,255,.25)`
  - Значения: `Roboto Mono 11.5px`, `color rgba(255,255,255,.45)`
- ProgressBar: `height 3px`, `bg rgba(255,255,255,.08)`, заливка `#27AE60`
- Кнопка Стоп: `bg rgba(255,255,255,.07)`, `border 1px rgba(255,255,255,.10)`, `color rgba(255,255,255,.55)`, `radius 10px`
- Кнопка Завершить: `bg rgba(224,83,83,.14)`, `color #F47A7A`, `border 1px rgba(224,83,83,.20)`, `radius 10px`

**При запущенном таймере (running):**
- Фон панели: `linear-gradient(160deg, #0E1D17 0%, #111C1A 45%, #131720 100%)`
- Левый border: `rgba(39,174,96,.25)`
- TimerCard: `bg rgba(39,174,96,.10)`, `border rgba(39,174,96,.25)`
- Цифры таймера: `#27AE60`
- Подзначения: `rgba(39,174,96,.80)`

### 3. Sidebar (левая иконочная панель)

- Ширина: 52px, `bg #FFFFFF`, `border-right 1px #DCDEE3`
- Логотип: 32×32px, `radius 10px`, `bg #3B83F6`
- Кнопки навигации: 38×38px, `radius 10px`
  - Idle: `color #B8BDC9`
  - Hover: `bg #F5F6FA`, `color #828B9A`
  - Active: `bg rgba(59,131,246,.10)`, `color #3B83F6`

### 4. Мини-календарь (попап)

- `bg #FFFFFF`, `border 1px #DCDEE3`, `radius 12px`, `padding 14px`
- `shadow: 0 8px 24px rgba(0,0,0,.10)`
- Ширина: 228px
- Заголовок: месяц+год, кнопки `<` `>` (24×24px, `radius 5px`)
- Сетка 7 колонок, ячейки 28×28px, `radius 8px`
- Текущий день: `color #3B83F6`, `font-weight 500`
- Выбранный день: `bg #3B83F6`, `color #FFF`
- Hover: `bg rgba(59,131,246,.10)`, `color #3B83F6`

### 5. Модальные окна (Новая задача / История / Настройки)

- `bg #FFFFFF`, `radius 14px`, `padding 24px`
- `shadow: 0 12px 40px rgba(0,0,0,.12)`
- Backdrop: полупрозрачный + blur 6px
- Заголовок: `font-size 14px`, `font-weight 500`
- Кнопка закрыть: 28×28px, `radius 8px`, `bg #F5F6FA`
- Textarea: `radius 10px`, `border 1px #D0D2D8`, focus → `border #3B83F6`
- Кнопка «Отмена»: `bg #F5F6FA`, `color #828B9A`, `radius 8px`
- Кнопка «Добавить»: `bg #3B83F6`, `color #FFF`, `radius 8px`

### 6. Вкладка «Фокус»

- Центрированная карточка, `bg #FFFFFF`, `radius 16px`, `shadow var(--sh2)`
- Цифры обратного отсчёта: `Roboto Mono 64px weight 300`, `color #3B83F6`
- Done: цифры → `#27AE60`
- Чипы длительности: `border 1px #D0D2D8`, `radius 8px`, active → `bg #3B83F6 color #FFF`
- Кнопка Старт: `bg #3B83F6`, `color #FFF`, `radius 10px`, `height 40px`
- Кнопка Пауза: ghost, `bg #F5F6FA`, `border 1px #D0D2D8`

---

## Interactions & Behavior

| Элемент | Действие |
|---|---|
| Строка задачи | Hover → показать кнопки действий |
| «Старт» | Запустить таймер; если другая задача активна — поставить её на паузу |
| «Стоп» | Остановить таймер, статус → paused |
| «Завершить» (в строке) | Статус → done |
| «Завершить задачу» (таймер) | Остановить + статус → done |
| Корзина | Удалить задачу из списка |
| «Открыть в Б24» | Открыть ссылку в браузере (если интегрировано) |
| Иконка история | Открыть модальное окно истории сессий |
| «+ Новая задача» | Открыть модальное окно создания |
| Пробел | Старт/стоп активной задачи (keyboard shortcut) |
| Фильтры (Сегодня / В работе / Все) | Переключить видимость списка |
| Дата в subbar | Открыть мини-календарь, выбрать день |
| Сворачивание | Трей + плавающий виджет поверх окон |

### Анимации

- Появление actions в строке: `opacity 0→1`, `transition 120ms`
- Смена состояния панели таймера: `transition background 400ms, border-color 400ms`
- Смена цвета цифр таймера: `transition color 400ms`
- Пульсация зелёной точки: `opacity 1→0.35→1`, `2s infinite`
- Появление попапа/модалки: `opacity 0→1 + translateY(-4px→0)`, `140ms ease-out`

---

## State Management (PySide6)

```python
# Сигналы которые нужно добавить / обновить в AppController
task_started(task_id: str)      # → TimerPanel зелёный режим
task_stopped(task_id: str)      # → TimerPanel покой
task_completed(task_id: str)    # → строка done + opacity
timer_tick(seconds: int)        # → обновить цифры таймера
filter_changed(filter: str)     # → перерисовать список

# QSS dynamic properties (для переключения стилей в Qt)
setProperty("status", "running")  # на виджете строки
setProperty("status", "paused")
setProperty("status", "done")
# После setProperty вызывать: widget.style().unpolish(widget); widget.style().polish(widget)
```

---

## QSS Quick-Reference

Основной QSS-блок для подключения в `main_window.py`:

```css
/* ── Главное окно ────────────────────── */
QMainWindow { background: #F2F3F7; }

/* ── Sidebar ─────────────────────────── */
#sidebar { background: #FFFFFF; border-right: 1px solid #DCDEE3; }
#sidebar QPushButton {
  width: 38px; height: 38px; border: none; border-radius: 10px;
  background: transparent; color: #B8BDC9;
}
#sidebar QPushButton:hover { background: #F5F6FA; color: #828B9A; }
#sidebar QPushButton[active="true"] {
  background: rgba(59,131,246, 0.10); color: #3B83F6;
}

/* ── Subbar ──────────────────────────── */
#subbar { background: #FFFFFF; border-bottom: 1px solid #DCDEE3; }
#filterChip {
  height: 28px; border: 1px solid transparent; border-radius: 8px;
  background: #F5F6FA; color: #828B9A; font-size: 12px;
  padding: 0 13px;
}
#filterChip[active="true"] {
  background: #FFFFFF; border-color: #D0D2D8;
  color: #3B83F6; font-weight: 500;
}
#btnAccent {
  height: 30px; border: none; border-radius: 8px;
  background: #3B83F6; color: #FFFFFF; font-size: 12px; font-weight: 500;
}
#btnAccent:hover { background: #2563EB; }

/* ── Task Row ────────────────────────── */
#taskRow {
  background: #FFFFFF; border: 1px solid #DCDEE3; border-radius: 10px;
  min-height: 48px;
}
#taskRow:hover { border-color: #D0D2D8; }
#taskRow[status="running"] {
  border-color: rgba(39,174,96, 0.28);
  background: qlineargradient(x1:0,y1:0,x2:1,y2:0,
    stop:0 rgba(39,174,96,0.06), stop:0.52 #FFFFFF);
}
#taskRow[status="done"] { opacity: 0.68; }
#taskName { font-size: 12px; color: #252835; }
#taskRow[status="done"] #taskName {
  color: #B8BDC9; text-decoration: line-through;
}

/* ── Timer Panel ─────────────────────── */
#timerPanel { background: #131720; border-left: 1px solid rgba(255,255,255,0.06); }
#timerPanel[running="true"] { border-left-color: rgba(39,174,96,0.25); }
#timerDigits {
  font-family: "Roboto Mono"; font-size: 38px; font-weight: 300; color: #7EB3FA;
}
#timerPanel[running="true"] #timerDigits { color: #27AE60; }
#btnStop {
  height: 38px; border-radius: 10px; border: 1px solid rgba(255,255,255,0.10);
  background: rgba(255,255,255,0.07); color: rgba(255,255,255,0.55);
}
#btnComplete {
  height: 38px; border-radius: 10px; border: 1px solid rgba(224,83,83,0.20);
  background: rgba(224,83,83,0.14); color: #F47A7A;
}

/* ── Progress Bar ────────────────────── */
QProgressBar {
  height: 3px; border: none; border-radius: 2px;
  background: rgba(255,255,255,0.08);
}
QProgressBar::chunk { background: #27AE60; border-radius: 2px; }
```

---

## Assets & Icons

Все иконки — линейные SVG (`stroke`, не `fill`), `strokeWidth: 1.5px`.  
Рекомендуется использовать библиотеку **qtawesome** или конвертировать SVG в QIcon.

| Иконка | Назначение |
|---|---|
| clock circle + стрелки | Таймер / навигация |
| target circle | Фокус |
| gear / settings | Настройки |
| plus | Новая задача |
| trash | Удалить |
| history arrows | История |
| play triangle | Старт |
| pause bars | Стоп |
| calendar | Выбор даты |

---

## Files

| Файл | Описание |
|---|---|
| `Task Timer v2.html` | Полный интерактивный прототип (HTML + React) |
| `tt-app-v2.jsx` | React-компоненты прототипа |
| `README.md` | Этот документ |

> **Для разработчика:** откройте `Task Timer v2.html` в браузере для живого
> просмотра всех состояний (запущен, пауза, завершено, модалки, фокус-режим).
> Используйте его как эталон при написании QSS и верстке виджетов.
