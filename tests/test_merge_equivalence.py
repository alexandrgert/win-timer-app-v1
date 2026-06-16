from __future__ import annotations

from timerapp_ag.domain.merge import states_equivalent, task_richer
from timerapp_ag.domain.state import AppState
from timerapp_ag.models import Session, Task, TaskStatus


def _task(task_id: str, *, sessions: list[Session] | None = None) -> Task:
    return Task(
        id=task_id,
        day="2026-06-15",
        title="Task",
        status=TaskStatus.OPEN,
        sessions=sessions or [],
        created_at="2026-06-15T10:00:00",
    )


def test_states_equivalent_detects_session_content_difference() -> None:
    left = AppState(
        tasks=[
            _task(
                "t1",
                sessions=[
                    Session(id="s1", started_at="2026-06-15T10:00:00", ended_at="2026-06-15T11:00:00"),
                ],
            ),
        ],
    )
    right = AppState(
        tasks=[
            _task(
                "t1",
                sessions=[
                    Session(id="s1", started_at="2026-06-15T10:00:00", ended_at="2026-06-15T12:00:00"),
                ],
            ),
        ],
    )

    assert states_equivalent(left, right) is False


def test_task_richer_prefers_longer_session_history() -> None:
    shorter = _task(
        "t1",
        sessions=[
            Session(id="s1", started_at="2026-06-15T10:00:00", ended_at="2026-06-15T10:30:00"),
        ],
    )
    longer = _task(
        "t1",
        sessions=[
            Session(id="s1", started_at="2026-06-15T10:00:00", ended_at="2026-06-15T12:00:00"),
        ],
    )

    assert task_richer(longer, shorter) is True
    assert task_richer(shorter, longer) is False
