@echo off
call gradlew --stop
call gradlew --stop
call gradlew testDebugUnitTest --no-daemon --rerun-tasks
