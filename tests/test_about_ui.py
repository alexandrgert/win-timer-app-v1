from __future__ import annotations

import pytest
from PySide6.QtWidgets import QApplication

from win_timer_app.controller import AppController
from win_timer_app.main_window import AboutDialog, MainWindow
from win_timer_app.storage import Storage


@pytest.fixture
def qapp() -> QApplication:
    app = QApplication.instance()
    if app is None:
        app = QApplication([])
    return app


def test_about_dialog_shows_app_title(qapp: QApplication, tmp_path) -> None:
    controller = AppController(Storage(path=tmp_path / "data.json"))
    dialog = AboutDialog(controller)
    assert dialog.windowTitle() == "О программе"
    title_widget = dialog.layout().itemAt(0).widget()
    assert title_widget is not None
    assert title_widget.text() == "TaskTimer"
    dialog.close()


def test_tray_menu_has_about_action(qapp: QApplication, tmp_path) -> None:
    controller = AppController(Storage(path=tmp_path / "data.json"))
    window = MainWindow(controller, qapp)
    if not window.tray_available:
        pytest.skip("system tray not available in this environment")
    menu = window.tray.contextMenu()
    assert menu is not None
    actions = [action.text() for action in menu.actions()]
    assert "О программе" in actions
    window.close()
