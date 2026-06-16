from __future__ import annotations

import json
from datetime import datetime
from pathlib import Path

from ..models import Task
from .state import AppState


def _session_total_seconds(task: Task) -> int:
    total = 0
    for session in task.sessions:
        if not session.started_at:
            continue
        try:
            start = datetime.fromisoformat(session.started_at)
            if session.ended_at:
                end = datetime.fromisoformat(session.ended_at)
                total += max(0, int((end - start).total_seconds()))
            else:
                total += max(0, int((datetime.now() - start).total_seconds()))
        except ValueError:
            continue
    return total


def task_richer(candidate: Task, current: Task) -> bool:
    if len(candidate.sessions) != len(current.sessions):
        return len(candidate.sessions) > len(current.sessions)
    cand_seconds = _session_total_seconds(candidate)
    curr_seconds = _session_total_seconds(current)
    if cand_seconds != curr_seconds:
        return cand_seconds > curr_seconds
    return candidate.created_at >= current.created_at


def score_data_file(path: Path) -> tuple[int, int, float]:
    try:
        payload = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return (0, 0, 0.0)
    tasks = payload.get("tasks")
    task_count = len(tasks) if isinstance(tasks, list) else 0
    stat = path.stat()
    return (task_count, stat.st_size, stat.st_mtime)


def pick_best_data_file(candidates: list[Path]) -> Path | None:
    if not candidates:
        return None
    return max(candidates, key=score_data_file)


def merge_states(states: list[AppState]) -> AppState:
    """Объединить несколько состояний: задачи по id, ui из «самого полного» файла."""
    if not states:
        return AppState()
    ranked = sorted(
        states,
        key=lambda state: (len(state.tasks), sum(len(task.sessions) for task in state.tasks)),
        reverse=True,
    )
    merged_ui = dict(ranked[0].ui)
    tasks_by_id: dict[str, Task] = {}
    for state in states:
        for task in state.tasks:
            existing = tasks_by_id.get(task.id)
            if existing is None or task_richer(task, existing):
                tasks_by_id[task.id] = task
    return AppState(tasks=list(tasks_by_id.values()), ui=merged_ui)


def _sessions_equivalent(left_sessions: list, right_sessions: list) -> bool:
    if len(left_sessions) != len(right_sessions):
        return False
    left_sorted = sorted(left_sessions, key=lambda session: session.id)
    right_sorted = sorted(right_sessions, key=lambda session: session.id)
    for left_session, right_session in zip(left_sorted, right_sorted, strict=True):
        if left_session.id != right_session.id:
            return False
        if left_session.started_at != right_session.started_at:
            return False
        if left_session.ended_at != right_session.ended_at:
            return False
    return True


def states_equivalent(left: AppState, right: AppState) -> bool:
    if len(left.tasks) != len(right.tasks):
        return False
    left_tasks = sorted(left.tasks, key=lambda task: task.id)
    right_tasks = sorted(right.tasks, key=lambda task: task.id)
    for left_task, right_task in zip(left_tasks, right_tasks, strict=True):
        if left_task.id != right_task.id:
            return False
        if left_task.title != right_task.title:
            return False
        if not _sessions_equivalent(left_task.sessions, right_task.sessions):
            return False
    return left.ui == right.ui
