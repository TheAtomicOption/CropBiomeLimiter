#!/usr/bin/env sh
APP_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd) || exit 1
MINECRAFT_DIR=$(CDPATH= cd -- "$APP_DIR/../../.." && pwd) || exit 1
MODS_DIR="$MINECRAFT_DIR/mods"

MOD_JAR=$(find "$MODS_DIR" -maxdepth 1 -type f -name 'cropbiomelimiter-*.jar' ! -name '*-sources.jar' -print 2>/dev/null | sort | tail -n 1)

if [ -z "$MOD_JAR" ]; then
	printf '\nCould not find cropbiomelimiter-*.jar in:\n%s\n\n' "$MODS_DIR"
	printf 'Make sure the mod jar is installed in the Minecraft mods folder.\n\n'
	exit 1
fi

exec java -cp "$MOD_JAR" rocks.theatomicoption.cropbiomelimiter.configapp.ConfigAppServer "$APP_DIR"
