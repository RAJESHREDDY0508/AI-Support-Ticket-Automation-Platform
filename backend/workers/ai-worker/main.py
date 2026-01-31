#!/usr/bin/env python3
"""AI Worker: consume ticket.created.v1, process with retry, send failures to DLQ."""
import asyncio
import signal
import sys

from ai_worker.config import METRICS_PORT
from ai_worker.consumer import consume_loop
from ai_worker.log_config import configure_logging, get_logger
from ai_worker.metrics import start_metrics_server

logger = get_logger("main")


def main() -> int:
    configure_logging()
    start_metrics_server(METRICS_PORT)
    logger.info("metrics_server_started", port=METRICS_PORT)

    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)

    try:
        loop.run_until_complete(consume_loop())
    except KeyboardInterrupt:
        logger.info("shutdown_signal_received")
    except Exception as e:
        logger.exception("consumer_exited", error=str(e))
        return 1
    finally:
        loop.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())
