#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

JAR_NAME="lanterna-3.1.2.jar"
DOWNLOAD_URL="https://repo1.maven.org/maven2/com/googlecode/lanterna/lanterna/3.1.2/lanterna-3.1.2.jar"
BUILD_DIR="build/classes"
MAIN_CLASS="Main"
SOURCE_FILES=(Config.java Tetris.java Game.java Main.java)

download_lanterna() {
    if [[ -s "$JAR_NAME" ]]; then
        return
    fi

    if ! command -v curl >/dev/null 2>&1; then
        echo "curl is required to download $JAR_NAME." >&2
        exit 1
    fi

    echo "Downloading Lanterna..."
    curl --fail --location --output "$JAR_NAME.tmp" "$DOWNLOAD_URL"
    mv "$JAR_NAME.tmp" "$JAR_NAME"
}

needs_compile() {
    local main_class_file="$BUILD_DIR/${MAIN_CLASS}.class"

    [[ ! -f "$main_class_file" ]] && return 0

    for source_file in "${SOURCE_FILES[@]}"; do
        [[ "$source_file" -nt "$main_class_file" ]] && return 0
    done

    return 1
}

compile_sources() {
    mkdir -p "$BUILD_DIR"
    echo "Compiling Java sources..."
    javac -Xlint:all -cp "$JAR_NAME" -d "$BUILD_DIR" "${SOURCE_FILES[@]}"
}

download_lanterna

if needs_compile; then
    compile_sources
fi

java -cp "$BUILD_DIR:$JAR_NAME" "$MAIN_CLASS"
