#!/usr/bin/env sh
APP_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd) || exit 1
MINECRAFT_DIR=$(CDPATH= cd -- "$APP_DIR/../../.." && pwd) || exit 1
REPO_DIR=$(CDPATH= cd -- "$MINECRAFT_DIR/.." && pwd 2>/dev/null || printf '')
MODS_DIR="$MINECRAFT_DIR/mods"

find_mod_jar() {
	if [ ! -d "$1" ]; then
		return 0
	fi
	find "$1" -maxdepth 1 -type f -name 'cropbiomelimiter-*.jar' ! -name '*-sources.jar' -print 2>/dev/null | sort | tail -n 1
}

MOD_JAR=$(find_mod_jar "$MODS_DIR")
if [ -z "$MOD_JAR" ] && [ -n "$REPO_DIR" ] && [ -f "$REPO_DIR/build.gradle" ]; then
	MOD_JAR=$(find_mod_jar "$REPO_DIR/build/libs")
fi

if [ -n "$MOD_JAR" ]; then
	CONFIG_APP_CLASSPATH=$MOD_JAR
elif [ -n "$REPO_DIR" ] && [ -f "$REPO_DIR/build/classes/java/main/rocks/theatomicoption/cropbiomelimiter/configapp/ConfigAppServer.class" ]; then
	CONFIG_APP_CLASSPATH="$REPO_DIR/build/classes/java/main:$REPO_DIR/build/resources/main"
fi

if [ -z "$CONFIG_APP_CLASSPATH" ]; then
	printf '\nCould not find the Crop Biome Limiter config app helper.\n'
	printf 'Looked for an installed mod jar in:\n%s\n' "$MODS_DIR"
	if [ -n "$REPO_DIR" ] && [ -f "$REPO_DIR/build.gradle" ]; then
		printf '\nAlso looked for dev build output in:\n%s\n%s\n' "$REPO_DIR/build/libs" "$REPO_DIR/build/classes/java/main"
	fi
	printf '\nBuild or install the mod jar, then try again.\n\n'
	exit 1
fi

exec java -cp "$CONFIG_APP_CLASSPATH" rocks.theatomicoption.cropbiomelimiter.configapp.ConfigAppServer "$APP_DIR"
