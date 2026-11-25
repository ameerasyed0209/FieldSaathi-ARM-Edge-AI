# PlantCity – Machine Learning Development

This directory contains the machine learning development resources for the PlantCity plant disease classification model used in FieldSaathi.

## 1. Project Overview

PlantCity is a deep learning-based multi-crop disease classification model developed using MobileNetV2.

The model classifies plant images into 52 categories across 12 crop types and is designed for deployment in the FieldSaathi Android application.

## 2. Model Architecture

- Model: MobileNetV2
- Framework: TensorFlow / Keras
- Input shape: 224 × 224 × 3
- Input colour format: RGB
- Output: 52 classification categories
- Deployment format: TensorFlow Lite (FP32)

MobileNetV2 is used as the feature extraction and image classification architecture.

## 3. Dataset

The model was trained using a labelled plant image dataset containing images from 12 crop types.

The dataset includes different plant conditions and disease categories represented by 52 classification classes.

The dataset is organized into class-based folders for model training and validation.

## 4. Model Training

Model development and training were performed using Google Colab.

The training workflow includes:

1. Importing the required Python libraries.
2. Loading and preparing the plant image dataset.
3. Resizing images to 224 × 224 pixels.
4. Preparing training and validation datasets.
5. Building the MobileNetV2-based classification model.
6. Training the model using the prepared dataset.
7. Evaluating the trained model.
8. Saving the trained model for deployment.

## 5. Model Preprocessing

- Input images are represented in RGB format.
- Images are resized to 224 × 224 pixels.
- MobileNetV2 `preprocess_input` is included inside the deployed model.

This ensures that the model applies its expected preprocessing during inference.

## 6. TensorFlow Lite Conversion

The trained PlantCity model was converted into TensorFlow Lite format for Android deployment.

**Deployment model:**
`PlantCity_MobileNetV2_FP32.tflite`

The converted model is stored in the repository's `Model/` directory.

## 7. Model Validation

The converted TensorFlow Lite model was validated to check:

- Model input dimensions.
- Input data type.
- Output dimensions.
- Output data type.
- Compatibility with the expected 52 classification categories.

## 8. Training Notebook

The Google Colab training notebook is available here:

`Copy_of_Cropdisease.ipynb`

This notebook contains the PlantCity model development and training workflow.

## 9. Deployment

The trained TensorFlow Lite model is integrated into the FieldSaathi Android application for plant image classification.

The Android application processes input images and displays the predicted classification results.

## 10. Technologies Used

- Python
- Google Colab
- TensorFlow
- Keras
- MobileNetV2
- TensorFlow Lite
- NumPy

---

**Project:** FieldSaathi – AI-Powered Smart Agriculture Assistant  
**Module:** PlantCity Multi-Crop Disease Classification
