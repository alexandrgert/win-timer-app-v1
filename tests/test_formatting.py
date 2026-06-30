from __future__ import annotations

from timerapp_ag.domain.formatting import format_task_datetime


def test_format_task_datetime_from_iso() -> None:
    assert format_task_datetime("2026-06-28T20:31:00+03:00") == "28.06.2026 20:31"


def test_format_task_datetime_empty() -> None:
    assert format_task_datetime(None) == "—"
    assert format_task_datetime("") == "—"
