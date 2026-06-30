from __future__ import annotations

from datetime import datetime, timedelta, timezone

from timerapp_ag.domain.datetime_util import (
    duration_seconds,
    parse_iso_datetime,
    session_local_date,
)
from timerapp_ag.models import Session, Task, TaskStatus


def test_parse_iso_datetime_handles_android_offset() -> None:
    parsed = parse_iso_datetime("2026-06-28T20:31:00+03:00")
    assert parsed.utcoffset() is not None
    assert parsed.hour in {17, 18, 19, 20, 21}  # local-dependent


def test_duration_seconds_running_session_with_offset_start() -> None:
    tz = timezone(timedelta(hours=3))
    start = datetime(2026, 6, 28, 20, 31, 0, tzinfo=tz)
    now = start + timedelta(seconds=4)
    seconds = duration_seconds(
        start.isoformat(),
        None,
        now=now,
    )
    assert seconds == 4


def test_duration_seconds_paused_session_with_offset() -> None:
    seconds = duration_seconds(
        "2026-06-28T20:31:00+03:00",
        "2026-06-28T20:31:04+03:00",
    )
    assert seconds == 4


def test_session_local_date_uses_local_calendar_day() -> None:
    assert session_local_date("2026-06-28T20:31:00+03:00") == "2026-06-28"


def test_task_total_seconds_with_android_timestamps() -> None:
    task = Task(
        id="t1",
        day="2026-06-28",
        title="test webdav1",
        status=TaskStatus.PAUSED,
        sessions=[
            Session(
                id="s1",
                started_at="2026-06-28T20:31:00+03:00",
                ended_at="2026-06-28T20:31:04+03:00",
            ),
        ],
    )
    assert task.total_seconds() == 4
