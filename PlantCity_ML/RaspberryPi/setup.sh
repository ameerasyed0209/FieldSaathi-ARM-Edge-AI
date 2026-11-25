#!/bin/bash

set -e

echo "======================================"
echo " KrishiSaathi Advisory AI Setup"
echo "======================================"

echo ""
echo "Checking Python..."
python3 --version

echo ""
echo "Checking model..."

MODEL="$HOME/llama-3.2-1b/pretrained/Llama-3.2-1B-Instruct-8da4w-gptq-2k.pte"

if [ -f "$MODEL" ]; then
    echo "Model found:"
    ls -lh "$MODEL"
else
    echo "ERROR: Llama model not found."
    echo "$MODEL"
    exit 1
fi

echo ""
echo "Checking tokenizer..."

TOKENIZER="$HOME/llama-3.2-1b/original/tokenizer.model"

if [ -f "$TOKENIZER" ]; then
    echo "Tokenizer found:"
    ls -lh "$TOKENIZER"
else
    echo "ERROR: tokenizer.model not found."
    echo "$TOKENIZER"
    exit 1
fi

echo ""
echo "Checking ExecuTorch runner..."

RUNNER="$HOME/executorch/build-native/examples/models/llama/llama_main"

if [ -x "$RUNNER" ]; then
    echo "ExecuTorch runner found:"
    ls -lh "$RUNNER"
else
    echo "ERROR: llama_main not found or not executable."
    echo "$RUNNER"
    exit 1
fi

echo ""
echo "Testing runner..."

"$RUNNER" --help >/dev/null 2>&1 || true

echo ""
echo "======================================"
echo " Setup check completed"
echo "======================================"

echo ""
echo "Start the API with:"
echo ""
echo "python3 server/server.py"
