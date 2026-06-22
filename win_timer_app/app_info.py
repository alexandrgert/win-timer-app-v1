"""Название приложения и версия для окна и диалога «О программе»."""
from __future__ import annotations

import sys
from pathlib import Path

APP_TITLE = "TaskTimer"


def app_install_dir() -> Path:
    """Каталог исполняемого файла (рядом может лежать VERSION)."""
    return Path(sys.executable).resolve().parent


def resolve_app_version_label() -> str:
    """Версия из VERSION рядом с бинарником или «dev» в режиме исходников."""
    version_file = app_install_dir() / "VERSION"
    if version_file.is_file():
        value = version_file.read_text(encoding="utf-8").strip()
        if value:
            return value
    return "dev"


def resolve_app_title() -> str:
    """Window/tray title: app name, with version when known."""
    version = resolve_app_version_label()
    if version == "dev":
        return APP_TITLE
    return f"{APP_TITLE} {version}"
