from __future__ import annotations

import sys

from PySide6.QtNetwork import QLocalServer, QLocalSocket
from PySide6.QtWidgets import QApplication

from .controller import AppController
from .main_window import MainWindow
from .storage import Storage

_INSTANCE_KEY = "TaskTimer-SingleInstance"


def _signal_existing_instance() -> bool:
    """Return True if another instance is already running (and was notified)."""
    socket = QLocalSocket()
    socket.connectToServer(_INSTANCE_KEY)
    if socket.waitForConnected(300):
        socket.write(b"show")
        socket.flush()
        socket.waitForBytesWritten(300)
        socket.disconnectFromServer()
        return True
    return False


def main() -> int:
    app = QApplication(sys.argv)
    app.setQuitOnLastWindowClosed(False)

    # Single-instance guard: if already running, ask that instance to surface and exit.
    if _signal_existing_instance():
        return 0

    server = QLocalServer()
    QLocalServer.removeServer(_INSTANCE_KEY)  # clear any stale socket from a crash
    server.listen(_INSTANCE_KEY)

    controller = AppController(Storage())
    window = MainWindow(controller, app)
    window.bind_single_instance_server(server)
    window.show()
    return app.exec()
