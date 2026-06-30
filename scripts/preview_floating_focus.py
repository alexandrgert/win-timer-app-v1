#!/usr/bin/env python3
"""Render floating widget during focus mode to PNG for UI preview."""
from __future__ import annotations

import os
import sys
import tempfile
from pathlib import Path

os.environ.setdefault("QT_QPA_PLATFORM", "offscreen")

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from PySide6.QtCore import QTimer  # noqa: E402
from PySide6.QtWidgets import QApplication  # noqa: E402

from timerapp_ag.controller import AppController  # noqa: E402
from timerapp_ag.main_window import MainWindow  # noqa: E402
from timerapp_ag.storage import Storage  # noqa: E402


def main() -> int:
    app = QApplication(sys.argv)
    data_dir = Path(tempfile.mkdtemp(prefix="timerapp-preview-"))
    controller = AppController(Storage(path=data_dir / "data.json"))
    task = controller.create_task("ЮНИМ оценка", description="Пример", start_now=True)
    controller.stop_task(task.id)

    original_single_shot = QTimer.singleShot
    QTimer.singleShot = lambda *args, **kwargs: None  # type: ignore[method-assign, assignment]

    window = MainWindow(controller, app)
    window._start_focus_timer(20)
    QTimer.singleShot = original_single_shot  # type: ignore[method-assign, assignment]

    window.floating.adjustSize()
    app.processEvents()

    output = ROOT / "docs/assets/floating-focus-preview.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    if not window.floating.grab().save(str(output)):
        print(f"Failed to save {output}", file=sys.stderr)
        return 1
    print(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
