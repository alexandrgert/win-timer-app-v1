from __future__ import annotations

from pathlib import Path

import pytest

from win_timer_app.controller import AppController
from win_timer_app.storage import Storage


@pytest.fixture
def storage(tmp_path: Path) -> Storage:
    """Storage backed by an isolated temp data.json (no real AppData writes)."""
    return Storage(path=tmp_path / "data.json")


@pytest.fixture
def controller(storage: Storage) -> AppController:
    return AppController(storage)
