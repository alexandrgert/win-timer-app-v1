#!/usr/bin/env python3
"""Fail if Android versionName diverges from pyproject.toml (CI / pre-release)."""
from __future__ import annotations

import re
import sys
import tomllib
from pathlib import Path

PROJECT_DIR = Path(__file__).resolve().parents[1]
PYPROJECT = PROJECT_DIR / "pyproject.toml"
GRADLE = PROJECT_DIR / "android" / "app" / "build.gradle.kts"


def main() -> int:
    py_version = tomllib.loads(PYPROJECT.read_text(encoding="utf-8"))["project"]["version"]
    gradle_text = GRADLE.read_text(encoding="utf-8")
    name_match = re.search(r'versionName\s*=\s*"([^"]+)"', gradle_text)
    code_match = re.search(r"versionCode\s*=\s*(\d+)", gradle_text)
    if not name_match:
        print(f"versionName not found in {GRADLE}", file=sys.stderr)
        return 1
    android_name = name_match.group(1)
    if android_name != py_version:
        print(
            f"Version mismatch: pyproject.toml={py_version!r}, "
            f"android versionName={android_name!r}. "
            "Sync android/app/build.gradle.kts before push/CI.",
            file=sys.stderr,
        )
        return 1
    if code_match:
        expected_code = int(py_version.replace(".", ""))
        android_code = int(code_match.group(1))
        if android_code < expected_code:
            print(
                f"Warning: versionCode={android_code} looks low for {py_version} "
                f"(expected at least {expected_code}).",
                file=sys.stderr,
            )
    print(f"OK: all platforms at {py_version}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
