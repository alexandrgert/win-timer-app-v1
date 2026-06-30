#!/usr/bin/env python3
"""Render task list states to PNG for UI preview."""
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


def _save(window: MainWindow, name: str) -> Path:
    output = ROOT / "docs/assets" / name
    output.parent.mkdir(parents=True, exist_ok=True)
    window.resize(1180, 760)
    for _ in range(5):
        QApplication.processEvents()
    if not window.tasks_page.grab().save(str(output)):
        raise RuntimeError(f"Failed to save {output}")
    return output


def main() -> int:
    app = QApplication(sys.argv)
    data_dir = Path(tempfile.mkdtemp(prefix="timerapp-preview-"))
    controller = AppController(Storage(path=data_dir / "data.json"))
    task = controller.create_task("test webdav1", description="", start_now=True)
    controller.stop_task(task.id)
    controller.complete_task(task.id)

    original_single_shot = QTimer.singleShot
    QTimer.singleShot = lambda *args, **kwargs: None  # type: ignore[method-assign, assignment]

    window = MainWindow(controller, app)
    QTimer.singleShot = original_single_shot  # type: ignore[method-assign, assignment]

    window.resize(1180, 760)
    window.show()
    app.processEvents()

    window._set_view("all")
    window._on_task_row_selected(task.id)
    for _ in range(5):
        app.processEvents()
    expanded = _save(window, "task-card-expanded-all.png")

    window._set_view("plan")
    for _ in range(5):
        app.processEvents()
    collapsed = _save(window, "task-card-after-tab-switch.png")

    print(expanded)
    print(collapsed)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
