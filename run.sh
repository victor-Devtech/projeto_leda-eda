#!/usr/bin/env bash
set -euo pipefail

# Adiciona o OpenJDK do Homebrew ao PATH caso esteja instalado no macOS Apple Silicon/Intel
if [ -d "/opt/homebrew/opt/openjdk/bin" ]; then
    export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"
elif [ -d "/usr/local/opt/openjdk/bin" ]; then
    export PATH="/usr/local/opt/openjdk/bin:$PATH"
fi

MODO="${1:---tudo}"

mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Xmx4g -cp out br.edu.uepb.eda.Main "$MODO"
