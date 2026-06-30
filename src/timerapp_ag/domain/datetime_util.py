"""Разбор ISO-дат из data.json (в т.ч. с offset от Android) и расчёт длительности."""
from __future__ import annotations

from datetime import datetime


def local_now() -> datetime:
    return datetime.now().astimezone()


def parse_iso_datetime(raw: str) -> datetime:
    """Привести ISO-строку к aware datetime в локальной зоне."""
    dt = datetime.fromisoformat(raw)
    if dt.tzinfo is None:
        return dt.replace(tzinfo=local_now().tzinfo)
    return dt.astimezone()


def coerce_now(reference: datetime, now: datetime | None = None) -> datetime:
    """Согласовать «сейчас» с timezone опорной метки."""
    if now is None:
        return local_now()
    if reference.tzinfo is None:
        return now.replace(tzinfo=None) if now.tzinfo else now
    if now.tzinfo is None:
        return now.replace(tzinfo=reference.tzinfo)
    return now.astimezone(reference.tzinfo)


def duration_seconds(
    started_at: str,
    ended_at: str | None,
    *,
    now: datetime | None = None,
) -> int:
    if not started_at:
        return 0
    try:
        start = parse_iso_datetime(started_at)
    except ValueError:
        return 0
    try:
        if ended_at:
            end = parse_iso_datetime(ended_at)
        else:
            end = coerce_now(start, now)
        return max(0, int((end - start).total_seconds()))
    except ValueError:
        return 0


def session_local_date(started_at: str) -> str | None:
    try:
        return parse_iso_datetime(started_at).date().isoformat()
    except ValueError:
        return None
