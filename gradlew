#!/bin/sh
# Minimal Gradle wrapper launcher. Requires gradle/wrapper/gradle-wrapper.jar
# (the CI workflow generates it if missing; locally run: gradle wrapper --gradle-version 8.7).
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi
if [ ! -f "$CLASSPATH" ]; then
  echo "gradle-wrapper.jar is missing. Run: gradle wrapper --gradle-version 8.7" >&2
  exit 1
fi
exec "$JAVACMD" -Dorg.gradle.appname=gradlew -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
