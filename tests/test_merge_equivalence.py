from __future__ import annotations

from timerapp_ag.domain.merge import merge_task_pair, states_equivalent, task_richer
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


def test_merge_task_pair_unions_sessions_from_both_copies() -> None:
    local = _task("t1", sessions=[])
    remote = _task(
        "t1",
        sessions=[
            Session(
                id="s1",
                started_at="2026-06-28T20:31:00+03:00",
                ended_at="2026-06-28T20:31:04+03:00",
            ),
        ],
    )
    merged = merge_task_pair(local, remote)
    assert len(merged.sessions) == 1
    assert merged.total_seconds() == 4


def test_merge_task_pair_keeps_both_session_ids() -> None:
    local = _task(
        "t1",
        sessions=[
            Session(id="s1", started_at="2026-06-15T10:00:00", ended_at="2026-06-15T10:30:00"),
        ],
    )
    remote = _task(
        "t1",
        sessions=[
            Session(
                id="s2",
                started_at="2026-06-28T20:31:00+03:00",
                ended_at="2026-06-28T20:31:04+03:00",
            ),
        ],
    )
    merged = merge_task_pair(local, remote)
    assert {session.id for session in merged.sessions} == {"s1", "s2"}
    assert merged.total_seconds() == 30 * 60 + 4


def test_merge_task_pair_keeps_completed_when_other_copy_is_richer() -> None:
    local = Task(
        id="t1",
        day="2026-06-15",
        title="Local",
        status=TaskStatus.COMPLETED,
        completed_at="2026-06-15T12:00:00",
        sessions=[
            Session(id="s1", started_at="2026-06-15T10:00:00", ended_at="2026-06-15T11:00:00"),
            Session(id="s2", started_at="2026-06-15T11:30:00", ended_at="2026-06-15T12:00:00"),
        ],
        created_at="2026-06-15T10:00:00",
    )
    remote = _task(
        "t1",
        sessions=[
            Session(id="s1", started_at="2026-06-15T10:00:00", ended_at="2026-06-15T10:30:00"),
        ],
    )

    merged = merge_task_pair(local, remote)

    assert merged.status == TaskStatus.COMPLETED
    assert merged.completed_at == "2026-06-15T12:00:00"
    assert len(merged.sessions) == 2


def test_merge_task_pair_running_session_overrides_completed() -> None:
    local = Task(
        id="t1",
        day="2026-06-15",
        title="Local",
        status=TaskStatus.COMPLETED,
        completed_at="2026-06-15T12:00:00",
        sessions=[
            Session(id="s1", started_at="2026-06-15T10:00:00", ended_at="2026-06-15T11:00:00"),
        ],
        created_at="2026-06-15T10:00:00",
    )
    remote = Task(
        id="t1",
        day="2026-06-15",
        title="Remote",
        status=TaskStatus.RUNNING,
        sessions=[
            Session(id="s2", started_at="2026-06-15T13:00:00", ended_at=None),
        ],
        created_at="2026-06-15T10:00:00",
    )

    merged = merge_task_pair(local, remote)

    assert merged.status == TaskStatus.RUNNING
    assert merged.completed_at is None


def test_merge_task_pair_paused_when_sessions_ended_and_not_completed() -> None:
    local = Task(
        id="t1",
        day="2026-06-15",
        title="Local",
        status=TaskStatus.OPEN,
        sessions=[
            Session(id="s1", started_at="2026-06-15T10:00:00", ended_at="2026-06-15T11:00:00"),
        ],
        created_at="2026-06-15T10:00:00",
    )
    remote = Task(
        id="t1",
        day="2026-06-15",
        title="Remote",
        status=TaskStatus.OPEN,
        sessions=[
            Session(id="s2", started_at="2026-06-15T12:00:00", ended_at="2026-06-15T12:30:00"),
        ],
        created_at="2026-06-15T10:00:00",
    )

    merged = merge_task_pair(local, remote)

    assert merged.status == TaskStatus.PAUSED
    assert merged.completed_at is None
    assert len(merged.sessions) == 2
