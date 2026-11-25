# 🌱 FieldSaathi – AI-Powered Smart Agriculture Assistant

**An Edge-AI-Based Smart Agriculture System for Plant Disease Detection and Intelligent Agricultural Advisory**

## 1. Project Overview

FieldSaathi is an AI-powered smart agriculture assistant designed to help farmers identify plant diseases and access relevant agricultural guidance.

The system combines deep learning-based plant image classification with a Large Language Model (LLM) for agricultural advisory. An Android application performs image-based disease detection, while a Raspberry Pi 4B hosts a locally deployed LLM to generate agricultural recommendations.

FieldSaathi integrates computer vision, machine learning, edge computing and natural language processing into a unified smart agriculture solution.

## 2. Problem Statement

Farmers often face difficulties identifying crop diseases at an early stage and accessing timely agricultural guidance. Traditional disease identification methods may require expert consultation, which can be time-consuming and difficult to access.

Cloud-dependent AI systems may also introduce internet connectivity requirements, response delays and data-transfer limitations.

FieldSaathi addresses these challenges through:

- AI-based plant disease identification using images.
- Lightweight deep learning models for image classification.
- On-device image inference using TensorFlow Lite.
- Local LLM-based agricultural advisory.
- Communication between the Android application and Raspberry Pi through a local HTTP API.

## 3. Project Objectives

- Develop an AI-based plant disease detection system.
- Train and deploy deep learning models for supported crops and diseases.
- Implement MobileNetV2-based image classification using TensorFlow Lite.
- Develop a multi-organ classification system for tomato plants.
- Integrate image classification into an Android application.
- Deploy Llama 3.2 1B Instruct on Raspberry Pi 4B.
- Implement a local API for agricultural advisory.
- Integrate the Android application with the Raspberry Pi advisory system.
- Explore efficient edge-AI deployment for smart agriculture.

## 4. System Architecture

FieldSaathi follows a two-device edge-AI architecture consisting of an Android smartphone and a Raspberry Pi 4B.

### A. Android Application – Vision AI

The Android application serves as the user interface and image-classification module.

**Main functionalities:**
- Capture plant images using the smartphone camera.
- Select existing images from the gallery.
- Perform image classification using trained TensorFlow Lite models.
- Identify supported crops, plant organs and disease categories.
- Display prediction results and confidence scores.
- Communicate with the Raspberry Pi advisory service.

### B. Raspberry Pi – Agricultural Advisory AI

The Raspberry Pi acts as the local AI inference server.

**Main functionalities:**
- Run the Llama 3.2 1B Instruct language model.
- Execute model inference using ExecuTorch.
- Handle agricultural advisory requests through a local HTTP API.
- Generate agricultural guidance based on the supplied crop, disease and user query.
- Return generated responses to the Android application.

### C. System Workflow

1. The user captures or selects a plant image through the Android application.
2. The image is preprocessed and passed to the appropriate classification model.
3. The model predicts the supported crop, plant organ or disease category.
4. The application displays the classification result.
5. The user can request additional agricultural guidance.
6. The Android application sends the advisory request to the Raspberry Pi through the local API.
7. The Llama model processes the request and generates a response.
8. The generated advisory is returned to the Android application.

## 5. Hardware Utilization

| Hardware | Utilization |
|---|---|
| Android Smartphone | User interface, camera input and on-device image classification |
| Raspberry Pi 4B (4 GB RAM) | Local LLM inference and agricultural advisory |
| Smartphone Camera | Plant image acquisition |

**Communication:** Local network communication between the Android application and Raspberry Pi through an HTTP API.

## 6. Software and Machine Learning Implementation

### A. PlantCity – Multi-Crop Disease Classification

PlantCity is the primary plant disease classification model developed using MobileNetV2.

**Model specifications:**

- Architecture: MobileNetV2
- Framework: TensorFlow and TensorFlow Lite
- Supported crop types: 12
- Classification categories: 52
- Input dimensions: 224 × 224 × 3
- Input colour format: RGB
- Model format: TensorFlow Lite (FP32)
- Preprocessing: MobileNetV2 `preprocess_input` included inside the model.

The model was trained using a labelled plant image dataset and converted into TensorFlow Lite format for deployment in the Android application.

### B. Tomato Multi-Organ Classification

A separate MobileNetV2-based model was developed for tomato plant organ and disease classification.

**Organ categories:**
- Leaf
- Stem
- Fruit

**Disease categories:**
- Viral
- Wilt
- Gray Mold

The model predicts the plant organ and corresponding disease category using the supported combinations defined in its metadata. It is integrated into the Android application workflow.

### C. Android Application Development

The Android application integrates the trained machine learning models and provides the interface through which users interact with FieldSaathi.

**Technologies:**
- Programming language: Kotlin
- UI framework: Jetpack Compose
- Machine learning deployment: TensorFlow Lite

**Implemented functionalities:**
- Camera and gallery image input.
- Plant image classification.
- PlantCity model integration.
- Tomato Multi-Organ model integration.
- Display of prediction results.
- Communication with the Raspberry Pi advisory API.

### D. Agricultural Advisory – Llama 3.2 1B Instruct

The agricultural advisory component uses a locally deployed language model.

**Implementation specifications:**
- Language model: Llama 3.2 1B Instruct
- Deployment hardware: Raspberry Pi 4B (4 GB RAM)
- Operating system: Debian 13 ARM64
- Model export: GPTQ-labelled model export
- Inference runtime: ExecuTorch
- API: Local HTTP advisory API
- Endpoint: `/ask`
- Port: `8000`

The Raspberry Pi processes advisory requests locally and returns generated agricultural guidance to the Android application.

## 7. Hardware and Software Optimization

The project uses techniques intended to support deployment on resource-constrained edge devices.

### Hardware Optimization
- Raspberry Pi 4B is used as a compact edge-computing platform.
- Image classification is performed on the Android device, reducing the need to transfer every image to a remote server.
- The architecture distributes workloads between the smartphone and Raspberry Pi.

### Software and Model Optimization
- MobileNetV2 provides a lightweight deep learning architecture suitable for mobile deployment.
- TensorFlow Lite enables deployment of image-classification models on Android.
- The models use a fixed 224 × 224 input resolution.
- The Llama model uses a GPTQ-labelled export in the Raspberry Pi deployment workflow.
- ExecuTorch is used as the language-model inference runtime.
- A local HTTP API enables communication between the two devices.

## 8. Results and Performance

The project has integrated the plant image classification and agricultural advisory components.

**Implemented results:**
- PlantCity model trained for 52 classification categories across 12 crop types.
- PlantCity model converted into TensorFlow Lite format.
- Tomato Multi-Organ model developed and integrated.
- Android application implemented with image input and classification functionality.
- Llama 3.2 1B Instruct exported and deployed on Raspberry Pi.
- ExecuTorch inference environment implemented.
- Local agricultural advisory API implemented.
- Android-to-Raspberry Pi communication and integration verified.

Quantitative performance measurements, including classification accuracy, inference latency, memory consumption and CPU utilization, should be reported using corresponding experimental logs and test results.

## 9. Implementation Status

| Component | Status |
|---|---|
| PlantCity dataset preparation and model training | Completed |
| PlantCity TensorFlow Lite conversion | Completed |
| PlantCity model validation | Completed |
| Tomato Multi-Organ model development | Completed |
| Tomato Multi-Organ model integration | Completed |
| Android application development | Completed |
| Android Vision AI integration | Completed |
| Llama 3.2 1B model export | Completed |
| ExecuTorch setup | Completed |
| Raspberry Pi LLM deployment and inference | Completed |
| Local agricultural advisory API | Completed |
| Android–Raspberry Pi integration | Verified |

## 10. Future Scope

- Integrate speech recognition using Vosk.
- Add text-to-speech functionality using Piper.
- Develop a complete voice-based agricultural assistant.
- Improve multilingual agricultural advisory capabilities.
- Expand the dataset to support additional crops and diseases.
- Improve model generalization using more real-world plant images.
- Conduct systematic performance benchmarking for inference time, CPU utilization and memory consumption.
- Explore additional model optimization techniques for resource-constrained devices.

## 11. Technologies Used

| Category | Technologies |
|---|---|
| Programming Languages | Python, Kotlin |
| Deep Learning | TensorFlow, MobileNetV2 |
| Mobile Deployment | TensorFlow Lite |
| Android Development | Android Studio, Jetpack Compose |
| Language Model | Llama 3.2 1B Instruct |
| Edge AI Runtime | ExecuTorch |
| Hardware | Raspberry Pi 4B, Android Smartphone |
| Operating System | Debian 13 ARM64 |
| API Communication | HTTP |
| Development Environment | Google Colab |
| Version Control | Git, GitHub |

## 12. Repository Structure

```text
FieldSaathi-ARM-Edge-AI/
│
├── Android_App/
│   ├── app/
│   │   └── src/main/
│   │       ├── assets/
│   │       ├── java/com/baasc/krishisaathi/
│   │       └── AndroidManifest.xml
│   ├── cameratest/
│   ├── gradle/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── README.md
│
├── Model/
│   ├── PlantCity_MobileNetV2_FP32.tflite
│   ├── class_names.json
│   ├── Tomato_MultiOrgan_MobileNetV2_FP32.tflite
│   ├── Tomato_MultiOrgan_Metadata.json
│   └── README.md
│
├── PlantCity_ML/
│   ├── Copy_of_Cropdisease.ipynb
│   ├── RaspberryPi/
│   │   ├── server/
│   │   │   └── server.py
│   │   ├── setup.sh
│   │   └── README.md
│   └── README.md
│
└── README.md
```

## 13. Conclusion

FieldSaathi demonstrates the integration of computer vision, deep learning and language-model-based agricultural assistance using edge-AI devices.

By combining MobileNetV2-based plant disease classification on an Android smartphone with a locally deployed Llama language model on Raspberry Pi, the system provides an integrated approach to plant disease identification and agricultural advisory.

The integration of image-classification models, the Android application and the Raspberry Pi advisory service establishes a foundation for developing accessible and efficient AI-powered agricultural solutions.

## 14. Repository

**Project Name:** FieldSaathi – AI-Powered Smart Agriculture Assistant

**Project Type:** Edge AI / Machine Learning / Smart Agriculture

**GitHub Repository:** https://github.com/ameerasyed0209/FieldSaathi-ARM-Edge-AI

---

*FieldSaathi – Bringing AI-powered plant intelligence and agricultural assistance closer to farmers.*
