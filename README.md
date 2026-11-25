# FieldSaathi – ARM Edge AI

### Offline Edge-AI Agricultural Assistant

FieldSaathi is an AI-powered agricultural assistant designed to support plant identification and disease detection using on-device machine learning.

The project uses deep learning models converted into TensorFlow Lite format, with an Android application and Raspberry Pi 4B edge-computing platform planned for offline inference.

## Problem Statement

Farmers may face difficulties identifying crop diseases quickly, especially in areas with limited internet connectivity. Cloud-based AI solutions may not always be accessible.

FieldSaathi aims to provide an offline AI-based solution for plant classification and disease detection.

## Proposed Solution

FieldSaathi combines:
- Deep learning-based plant classification.
- Tomato multi-organ disease classification.
- TensorFlow Lite models for edge deployment.
- Android application for user interaction.
- Raspberry Pi 4B for edge-AI execution.

## Machine Learning Models

**1. PlantCity – Multi-Class Plant Classification**
- Architecture: MobileNetV2
- Number of classes: 52
- Input dimensions: 224 × 224 × 3
- Framework: TensorFlow / TensorFlow Lite
- Training environment: Google Colab

**2. Tomato Multi-Organ Model**
- Architecture: MobileNetV2
- Supports tomato leaf, stem and fruit image classification.
- TensorFlow Lite model format.
- Includes model metadata.

## Technology Stack

- Python
- TensorFlow
- TensorFlow Lite
- MobileNetV2
- Google Colab
- Android
- Raspberry Pi 4B

## Repository Structure

```text
FieldSaathi-ARM-Edge-AI/
│
├── Android_App/
│
├── Model/
│
├── PlantCity_ML/
│   ├── RaspberryPi/
│   └── PlantCity_Training.ipynb
│
└── README.md
```

## Current Development Status

- [x] PlantCity ML model training
- [x] Tomato Multi-Organ model training
- [x] TensorFlow Lite model conversion
- [x] Model files and metadata uploaded
- [x] Training notebook uploaded
- [x] Android application integration
- [x] Raspberry Pi deployment
- [x] Complete end-to-end system testing

## Project Objective

To develop an accessible, offline agricultural AI assistant that helps identify plants and detect crop diseases through edge-based machine learning.

---

**Project:** FieldSaathi – ARM Edge AI
