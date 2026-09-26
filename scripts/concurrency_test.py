#!/usr/bin/env python3
"""Send concurrent create requests and fail on errors or duplicate codes."""

import argparse
import concurrent.futures
import json
import sys
import time
import urllib.error
import urllib.request


def create_short_url(base_url: str, index: int) -> str:
    body = json.dumps({"url": f"https://example.com/load-test/{index}"}).encode()
    request = urllib.request.Request(
        f"{base_url.rstrip('/')}/api/v1/urls",
        data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=15) as response:
        if response.status != 201:
            raise RuntimeError(f"request {index}: expected 201, got {response.status}")
        return json.load(response)["code"]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:8080")
    parser.add_argument("--requests", type=int, default=2_000)
    parser.add_argument("--concurrency", type=int, default=100)
    args = parser.parse_args()

    started = time.monotonic()
    errors = []
    codes = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=args.concurrency) as executor:
        futures = [executor.submit(create_short_url, args.base_url, i) for i in range(args.requests)]
        for future in concurrent.futures.as_completed(futures):
            try:
                codes.append(future.result())
            except (OSError, RuntimeError, KeyError, json.JSONDecodeError) as error:
                errors.append(str(error))

    elapsed = time.monotonic() - started
    duplicates = len(codes) - len(set(codes))
    print(
        f"completed={len(codes)} errors={len(errors)} duplicates={duplicates} "
        f"elapsed={elapsed:.2f}s rate={len(codes) / elapsed:.1f} req/s"
    )
    if errors:
        print("first errors:", *errors[:5], sep="\n  ", file=sys.stderr)
    return 1 if errors or duplicates or len(codes) != args.requests else 0


if __name__ == "__main__":
    raise SystemExit(main())
