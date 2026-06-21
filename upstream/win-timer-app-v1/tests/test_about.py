from __future__ import annotations

from pathlib import Path

from win_timer_app.app_info import APP_TITLE, resolve_app_version_label
from win_timer_app.runtime_info import bitrix_webhook_configured, build_about_report


def test_resolve_app_version_label_defaults_to_dev() -> None:
    assert resolve_app_version_label() == "dev"


def test_bitrix_webhook_configured() -> None:
    assert not bitrix_webhook_configured(stored_webhook="")
    assert bitrix_webhook_configured(stored_webhook="https://x.bitrix24.ru/rest/1/a/")


def test_build_about_report_includes_title_and_data_path(tmp_path: Path) -> None:
    data_path = tmp_path / "data.json"
    report = build_about_report(stored_webhook="", data_path=data_path)
    assert APP_TITLE in report
    assert str(data_path) in report
    assert "Вебхук Битрикс24: не настроен" in report
