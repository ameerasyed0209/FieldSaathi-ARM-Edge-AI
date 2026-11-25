# Raspberry Pi – Edge AI Runtime

This directory documents the Raspberry Pi-based edge AI component of FieldSaathi.

## 1. Overview

The Raspberry Pi acts as the local AI advisory server in the FieldSaathi system. It hosts a locally deployed Large Language Model (LLM) to generate agricultural guidance in response to requests from the Android application.

## 2. Hardware and Operating System

- Hardware: Raspberry Pi 4B
- RAM: 4 GB
- Operating System: Debian 13 ARM64

## 3. Language Model

- Model: Llama 3.2 1B Instruct
- Model export: GPTQ-labelled model export
- Inference runtime: ExecuTorch

The language model is deployed on the Raspberry Pi to provide locally executed agricultural advisory functionality.

## 4. Advisory API

A local HTTP API is implemented to receive advisory requests from the Android application.

- Endpoint: `/ask`
- Port: `8000`
- Purpose: Process agricultural queries and return generated advisory responses.

## 5. System Workflow

1. The Android application prepares an agricultural advisory request.
2. The request is sent to the Raspberry Pi through the local network.
3. The local API receives the request.
4. The Llama 3.2 1B Instruct model processes the input using ExecuTorch.
5. The generated response is returned to the Android application.

## 6. Android Integration

The Raspberry Pi advisory component is integrated with the FieldSaathi Android application.

The two devices communicate through the local HTTP API, allowing the application to access the locally deployed language model.

## 7. Implementation Status

- Raspberry Pi environment setup: Completed
- Llama model export: Completed
- ExecuTorch runtime setup: Completed
- Local LLM inference: Tested
- HTTP advisory API: Implemented
- Android–Raspberry Pi integration: Verified

## 8. Future Scope

- Improve inference speed and memory efficiency.
- Conduct detailed CPU and RAM benchmarking.
- Extend multilingual agricultural advisory capabilities.
- Integrate speech recognition using Vosk.
- Integrate text-to-speech using Piper.

---

**Project:** FieldSaathi – AI-Powered Smart Agriculture Assistant  
**Module:** Raspberry Pi Edge AI and Agricultural Advisory
