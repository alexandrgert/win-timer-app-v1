from __future__ import annotations

from win_timer_app.controller import AppController
from win_timer_app.main_window import (
    format_tray_tooltip,
    main_window_is_open,
    resolve_floating_task,
    tray_tooltip_task_titles,
)
from win_timer_app.models import Task, TaskStatus


def test_running_tasks_returns_active_timer(controller: AppController) -> None:
    task = controller.create_task("Alpha", start_now=True)
    running = controller.running_tasks()
    assert len(running) == 1
    assert running[0].id == task.id


def test_running_tasks_empty_when_stopped(controller: AppController) -> None:
    task = controller.create_task("Beta", start_now=True)
    controller.stop_task(task.id)
    assert controller.running_tasks() == []


def test_timer_panel_task_returns_paused_when_not_running(controller: AppController) -> None:
    task = controller.create_task("Paused", start_now=True)
    controller.stop_task(task.id)
    panel = controller.timer_panel_task()
    assert panel is not None
    assert panel.id == task.id


def test_format_tray_tooltip_window_visible_shows_app_title() -> None:
    text = format_tray_tooltip(
        window_visible=True,
        app_title="TaskTimer 1.0",
        task_titles=["Задача A"],
    )
    assert text == "TaskTimer 1.0"


def test_format_tray_tooltip_hidden_shows_one_task_per_line() -> None:
    text = format_tray_tooltip(
        window_visible=False,
        app_title="TaskTimer 1.0",
        task_titles=["Задача A", "Задача B"],
    )
    assert text == "Задача A\nЗадача B"


def test_format_tray_tooltip_hidden_without_tasks_shows_idle_hint() -> None:
    text = format_tray_tooltip(
        window_visible=False,
        app_title="TaskTimer 1.0",
        task_titles=[],
    )
    assert text == "Нет активных таймеров"


def test_format_tray_tooltip_minimized_window_shows_tasks() -> None:
    """Minimized window is not 'open' — tray should list tasks, not app title."""
    window_open = main_window_is_open(is_visible=True, is_minimized=True)
    text = format_tray_tooltip(
        window_visible=window_open,
        app_title="TaskTimer 1.0",
        task_titles=["Задача A"],
    )
    assert text == "Задача A"


def test_tray_tooltip_task_titles_includes_paused_floating_task() -> None:
    paused = Task(id="p1", day="2026-06-15", title="Paused task", status=TaskStatus.PAUSED)
    titles = tray_tooltip_task_titles(
        running_task_titles=[],
        floating_task=paused,
    )
    assert titles == ["Paused task"]


def test_tray_tooltip_task_titles_deduplicates_running_and_floating() -> None:
    running = Task(id="r1", day="2026-06-15", title="Same", status=TaskStatus.RUNNING)
    titles = tray_tooltip_task_titles(
        running_task_titles=["Same"],
        floating_task=running,
    )
    assert titles == ["Same"]


def test_resolve_floating_task_returns_paused_tracked_task(controller: AppController) -> None:
    task = controller.create_task("Paused", start_now=True)
    controller.stop_task(task.id)

    resolved, tracked = resolve_floating_task(
        active=None,
        tracked_task_id=task.id,
        find_task=controller.find_task,
    )
    assert resolved is not None
    assert resolved.id == task.id
    assert tracked == task.id


def test_resolve_floating_task_panel_task_when_no_tracking(controller: AppController) -> None:
    task = controller.create_task("Paused on panel", start_now=True)
    controller.stop_task(task.id)

    resolved, tracked = resolve_floating_task(
        active=None,
        tracked_task_id=None,
        find_task=controller.find_task,
        panel_task=controller.timer_panel_task(),
    )
    assert resolved is not None
    assert resolved.id == task.id
    assert tracked == task.id


def test_resolve_floating_task_completed_tracked_falls_back_to_panel(
    controller: AppController,
) -> None:
    done = controller.create_task("Done", start_now=True)
    controller.complete_task(done.id)
    paused = controller.create_task("Still paused", start_now=True)
    controller.stop_task(paused.id)

    resolved, tracked = resolve_floating_task(
        active=None,
        tracked_task_id=done.id,
        find_task=controller.find_task,
        panel_task=controller.timer_panel_task(),
    )
    assert resolved is not None
    assert resolved.id == paused.id
    assert tracked == paused.id


def test_main_window_is_open() -> None:
    assert main_window_is_open(is_visible=True, is_minimized=False) is True
    assert main_window_is_open(is_visible=True, is_minimized=True) is False
    assert main_window_is_open(is_visible=False, is_minimized=False) is False
