#!/bin/bash

JAR_NAME="lanterna-3.1.2.jar"
DOWNLOAD_URL="https://repo1.maven.org/maven2/com/googlecode/lanterna/lanterna/3.1.2/lanterna-3.1.2.jar"

# 1. Descargar Lanterna si no existe
if [ ! -f "$JAR_NAME" ]; then
    echo "Descargando Lanterna library..."
    curl -L -o "$JAR_NAME" "$DOWNLOAD_URL"
fi

# 2. Compilar
# Limpiar antiguos binarios para evitar conflictos
rm -f *.class
javac -nowarn -cp "$JAR_NAME" Config.java Tetris.java Game.java Main.java

# 3. Ejecutar
if [ $? -eq 0 ]; then
    java -cp ".:$JAR_NAME" Main
else
    echo "Error de compilación."
fi
