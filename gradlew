#!/bin/sh
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
cd "$APP_HOME" || exit 1
JAVA_CMD=${JAVA_HOME:+$JAVA_HOME/bin/java}
JAVA_CMD=${JAVA_CMD:-java}
exec "$JAVA_CMD" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
