from __future__ import annotations

import json
from pathlib import Path
from unittest.mock import MagicMock, patch

import pytest

from timerapp_ag.storage import Storage
from timerapp_ag.webdav_client import WebDavClient, WebDavError
from datetime import datetime, timedelta

from timerapp_ag.webdav_config import (
    WebDavConfig,
    clear_pending_remote_remind,
    normalize_remind_later_minutes,
    save_pending_remote_remind,
    save_webdav_config,
    should_show_remote_prompt,
    webdav_config_path,
)
from timerapp_ag.webdav_sync import pull_and_merge, push_local


@pytest.fixture
def webdav_config() -> WebDavConfig:
    return WebDavConfig(
        enabled=True,
        url="https://cloud.example.com/dav/",
        username="alex",
        password="secret",
        remote_path="tasktimer/data.json",
    )


def test_webdav_config_round_trip(tmp_path: Path, monkeypatch) -> None:
    path = tmp_path / "webdav.json"
    monkeypatch.setattr("timerapp_ag.platform_paths.webdav_config_path", lambda: path)
    config = WebDavConfig(
        enabled=True,
        url="https://x/",
        username="u",
        password="p",
        shutdown_upload_only=True,
        pending_notice="pending",
        last_remote_content_hash="abc",
    )
    save_webdav_config(config)
    loaded = json.loads(path.read_text(encoding="utf-8"))
    assert loaded["url"] == "https://x/"
    assert loaded["password"] == "p"
    assert loaded["device_id"]
    assert loaded["shutdown_upload_only"] is True
    assert loaded["sync_interval_minutes"] == 0
    assert loaded["pending_notice"] == "pending"
    assert loaded["last_remote_content_hash"] == "abc"


def test_saved_enabled_false_is_not_overridden_by_env(tmp_path: Path, monkeypatch) -> None:
    path = tmp_path / "webdav.json"
    monkeypatch.setattr("timerapp_ag.platform_paths.webdav_config_path", lambda: path)
    monkeypatch.setenv("WEBDAV_ENABLED", "true")
    save_webdav_config(WebDavConfig(enabled=False, url="https://x/", username="u", password="p"))

    from timerapp_ag.webdav_config import load_webdav_config

    assert load_webdav_config().enabled is False


def test_peek_and_clear_pending_notice(tmp_path: Path, monkeypatch) -> None:
    path = tmp_path / "webdav.json"
    monkeypatch.setattr("timerapp_ag.platform_paths.webdav_config_path", lambda: path)
    from timerapp_ag.webdav_config import (
        clear_webdav_pending_notice,
        peek_webdav_pending_notice,
        save_webdav_pending_notice,
    )

    save_webdav_pending_notice("hello")
    assert peek_webdav_pending_notice() == "hello"
    clear_webdav_pending_notice()
    assert peek_webdav_pending_notice() == ""


def test_remote_url_joins_base_and_path(webdav_config: WebDavConfig) -> None:
    assert webdav_config.remote_url() == "https://cloud.example.com/dav/tasktimer/data.json"


def test_meta_remote_url_joins_base_and_path(webdav_config: WebDavConfig) -> None:
    assert (
        webdav_config.meta_remote_url()
        == "https://cloud.example.com/dav/tasktimer/data.sync-meta.json"
    )


def test_pull_and_merge_downloads_remote(tmp_path: Path, webdav_config: WebDavConfig) -> None:
    storage = Storage(path=tmp_path / "data.json", migrate_legacy=False)
    storage.save(storage.load())
    remote_payload = json.dumps(
        {"tasks": [{"id": "remote", "day": "2026-06-15", "title": "Из облака"}], "ui": {}}
    ).encode("utf-8")

    def fake_urlopen(request, timeout=0):
        method = request.get_method()
        if method == "GET":
            response = MagicMock()
            response.status = 200
            response.read.return_value = remote_payload
            response.headers.items.return_value = []
            response.__enter__ = lambda self: response
            response.__exit__ = lambda *args: None
            return response
        raise AssertionError(f"Unexpected method {method}")

    with patch("timerapp_ag.webdav_client.urllib.request.urlopen", side_effect=fake_urlopen):
        with patch.object(WebDavClient, "exists", return_value=True):
            with patch("timerapp_ag.webdav_sync.mark_webdav_sync_ok"):
                outcome = pull_and_merge(storage, webdav_config)

    assert outcome.state is not None
    assert {task.id for task in outcome.state.tasks} == {"remote"}
    reloaded = json.loads(storage.path.read_text(encoding="utf-8"))
    assert reloaded["tasks"][0]["title"] == "Из облака"


def test_push_local_uploads_file(tmp_path: Path, webdav_config: WebDavConfig) -> None:
    storage = Storage(path=tmp_path / "data.json", migrate_legacy=False)
    storage.save(storage.load())
    calls: list[str] = []

    def fake_urlopen(request, timeout=0):
        method = request.get_method()
        calls.append(method)
        response = MagicMock()
        response.status = 201 if method == "MKCOL" else 204
        response.read.return_value = b""
        response.headers.items.return_value = []
        response.__enter__ = lambda self: response
        response.__exit__ = lambda *args: None
        return response

    with patch("timerapp_ag.webdav_client.urllib.request.urlopen", side_effect=fake_urlopen):
        with patch.object(WebDavClient, "exists", return_value=False):
            with patch("timerapp_ag.webdav_sync.mark_webdav_sync_ok"):
                push_local(storage, webdav_config)

    assert "PUT" in calls


def test_webdav_client_requires_configuration() -> None:
    with pytest.raises(WebDavError):
        WebDavClient(WebDavConfig())


def test_normalize_remind_later_minutes_defaults_to_fifteen() -> None:
    assert normalize_remind_later_minutes(99) == 15
    assert normalize_remind_later_minutes(30) == 30


def test_should_show_remote_prompt_respects_remind_at() -> None:
    future = (datetime.now() + timedelta(minutes=30)).isoformat(timespec="seconds")
    past = (datetime.now() - timedelta(minutes=1)).isoformat(timespec="seconds")
    config = WebDavConfig(pending_remote_hash="hash-a", pending_remote_remind_at=future)
    assert should_show_remote_prompt(config, "hash-a") is False
    assert should_show_remote_prompt(config, "hash-b") is True
    config = WebDavConfig(pending_remote_hash="hash-a", pending_remote_remind_at=past)
    assert should_show_remote_prompt(config, "hash-a") is True


def test_save_pending_remote_remind_persists_hash(tmp_path: Path, monkeypatch) -> None:
    path = tmp_path / "webdav.json"
    monkeypatch.setattr("timerapp_ag.platform_paths.webdav_config_path", lambda: path)
    config = WebDavConfig(
        enabled=True,
        url="https://x/",
        username="u",
        password="p",
        sync_remind_later_minutes=5,
    )
    save_webdav_config(config)
    save_pending_remote_remind(config, "remote-hash")
    loaded = json.loads(path.read_text(encoding="utf-8"))
    assert loaded["pending_remote_hash"] == "remote-hash"
    assert loaded["pending_remote_remind_at"]
    clear_pending_remote_remind(WebDavConfig.from_dict(loaded))
    cleared = json.loads(path.read_text(encoding="utf-8"))
    assert cleared["pending_remote_hash"] == ""
    assert cleared["pending_remote_remind_at"] is None
