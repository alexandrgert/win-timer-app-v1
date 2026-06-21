"""Сведения о версии, системе и среде выполнения для диалога «О программе»."""
from __future__ import annotations

import os
import platform
import sys
from importlib.metadata import PackageNotFoundError, version
from pathlib import Path

from .app_info import APP_TITLE, app_install_dir, resolve_app_version_label


def _qt_versions() -> tuple[str, str]:
    try:
        from PySide6.QtCore import qVersion

        return qVersion(), version("PySide6")
    except (ImportError, PackageNotFoundError):
        return "—", "—"


def bitrix_webhook_configured(*, stored_webhook: str = "") -> bool:
    return bool((stored_webhook or "").strip())


def build_about_report(*, stored_webhook: str = "", data_path: Path | str | None = None) -> str:
    data_file = Path(data_path) if data_path is not None else None
    qt_version, pyside_version = _qt_versions()
    bitrix_status = "настроен" if bitrix_webhook_configured(stored_webhook=stored_webhook) else "не настроен"

    lines = [
        "Версия",
        f"  {resolve_app_version_label()}",
        "",
        "Система",
        f"  ОС: {platform.system()} {platform.release()}",
        f"  Сборка ОС: {platform.version()}",
        f"  Архитектура: {platform.machine()}",
        "",
        "Среда выполнения",
        f"  Python: {sys.version.split()[0]}",
        f"  Исполняемый файл: {sys.executable}",
        f"  Qt: {qt_version}",
        f"  PySide6: {pyside_version}",
        f"  Платформа Qt: {os.environ.get('QT_QPA_PLATFORM', 'по умолчанию')}",
        "",
        "Данные",
        f"  Каталог установки: {app_install_dir()}",
    ]
    if data_file is not None:
        lines.append(f"  Файл данных: {data_file}")
    lines.append(f"  Вебхук Битрикс24: {bitrix_status}")
    lines.append(f"  Приложение: {APP_TITLE}")
    return "\n".join(lines)
