# Machine Learning Models – FieldSaathi

This directory contains the trained machine learning models used in the FieldSaathi smart agriculture system.

## 1. PlantCity – Multi-Crop Disease Classification

- Architecture: MobileNetV2
- Supported crops: 12
- Classification categories: 52
- Input shape: 224 × 224 × 3
- Input format: RGB
- Model format: TensorFlow Lite (FP32)
- Preprocessing: MobileNetV2 preprocessing included inside the model.

**Purpose:** Identifies supported crop and plant disease categories from input images.

## 2. Tomato Multi-Organ Classification

A separate MobileNetV2-based model developed for tomato plant analysis.

**Organ categories:**
- Leaf
- Stem
- Fruit

**Disease categories:**
- Viral
- Wilt
- Gray Mold

**Purpose:** Supports tomato plant organ identification and disease classification.

## 3. Model Deployment

The trained models are integrated into the FieldSaathi Android application using TensorFlow Lite.

The application processes plant images and displays the corresponding classification results.

## 4. Model Development

The models were developed and trained using Python and Google Colab.

The PlantCity training notebook is available in the `PlantCity_ML` directory.

## 5. Model Files

This directory contains the trained model files and their associated JSON metadata.

Refer to the individual metadata files for model input specifications and output class mappings.
