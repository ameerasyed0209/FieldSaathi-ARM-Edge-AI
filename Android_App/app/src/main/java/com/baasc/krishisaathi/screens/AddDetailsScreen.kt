package com.baasc.krishisaathi.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baasc.krishisaathi.PlantCityClassifier
import com.baasc.krishisaathi.R
import com.baasc.krishisaathi.components.CropImagePreview
import com.baasc.krishisaathi.components.KrishiPrimaryButton
import com.baasc.krishisaathi.components.KrishiTextField
import com.baasc.krishisaathi.components.StepProgressIndicator
import com.baasc.krishisaathi.data.CropSubmissionData
import com.baasc.krishisaathi.data.RegionLanguageData
import com.baasc.krishisaathi.data.VisionPrediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.baasc.krishisaathi.ui.theme.ButtonGreen
import com.baasc.krishisaathi.ui.theme.CardBackground
import com.baasc.krishisaathi.ui.theme.DividerColor
import com.baasc.krishisaathi.ui.theme.ForestGreen
import com.baasc.krishisaathi.ui.theme.InputBackground
import com.baasc.krishisaathi.ui.theme.InputBorder
import com.baasc.krishisaathi.ui.theme.KrishiSaathiTheme
import com.baasc.krishisaathi.ui.theme.TextPrimary
import com.baasc.krishisaathi.ui.theme.TextSecondary

data class CropOption(
    val key: String,
    val nameResId: Int,
    val iconResId: Int
)

data class SymptomOption(
    val key: String,
    val labelResId: Int
)

data class DurationPreset(
    val key: String,
    val labelResId: Int
)

@Composable
fun AddDetailsScreen(
    imageUriString: String?,
    initialPrediction: VisionPrediction? = null,
    selectedRegion: String = RegionLanguageData.DEFAULT_REGION,
    selectedLanguageCode: String = RegionLanguageData.DEFAULT_LANGUAGE,
    onNavigateBack: () -> Unit,
    onNavigateToGetAdvice: (CropSubmissionData) -> Unit
) {
    // Intercept system Back button / gesture to pop back to previous screen
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current

    // State for main crop image
    var currentMainImageUri by rememberSaveable { mutableStateOf(imageUriString) }

    // State for Vision AI prediction
    var currentPrediction by remember { mutableStateOf<VisionPrediction?>(initialPrediction) }

    // Reusable classifier instance for this screen
    val plantCityClassifier = remember {
        try {
            PlantCityClassifier(context)
        } catch (e: Exception) {
            null
        }
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            plantCityClassifier?.close()
        }
    }

    // Automatically run inference on image if prediction is not yet available (e.g. Gallery flow)
    androidx.compose.runtime.LaunchedEffect(currentMainImageUri) {
        val uriStr = currentMainImageUri
        if (uriStr != null && currentPrediction == null && plantCityClassifier != null) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(uriStr)
                    val pred = plantCityClassifier.predictFromUri(context, uri)
                    withContext(Dispatchers.Main) {
                        currentPrediction = pred
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AddDetailsScreen", "Failed to run Vision AI on gallery image: ${e.message}", e)
                }
            }
        }
    }

    // State for crop selection
    var selectedCropKey by rememberSaveable { mutableStateOf("") }
    var customCropName by rememberSaveable { mutableStateOf("") }
    var cropError by rememberSaveable { mutableStateOf<String?>(null) }

    // State for symptoms (multi-selection)
    val selectedSymptomKeys = remember { mutableStateListOf<String>() }

    // State for problem description
    var problemDescription by rememberSaveable { mutableStateOf("") }

    // State for growth duration
    var selectedDurationPreset by rememberSaveable { mutableStateOf("") }
    var customDurationNumber by rememberSaveable { mutableStateOf("") }
    var customDurationUnit by rememberSaveable { mutableStateOf("Days") }
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }
    var durationError by rememberSaveable { mutableStateOf<String?>(null) }

    // State for additional photos
    val additionalImageUris = remember { mutableStateListOf<String>() }

    // Validation string resources
    val errorNoCrop = stringResource(id = R.string.validation_error_no_crop)
    val errorNoDuration = stringResource(id = R.string.validation_error_no_duration)
    val errorNoPhoto = stringResource(id = R.string.validation_error_no_photo)

    // Main photo picker launcher (allows replacing or adding main image if absent)
    val mainPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentMainImageUri = uri.toString()
        }
    }

    // Additional photos picker launcher (supports multiple images)
    val additionalPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val uriStr = uri.toString()
            if (!additionalImageUris.contains(uriStr)) {
                additionalImageUris.add(uriStr)
            }
        }
    }

    val cropOptions = remember {
        listOf(
            CropOption("rice", R.string.crop_rice, R.drawable.ic_crop_rice),
            CropOption("maize", R.string.crop_maize, R.drawable.ic_crop_maize),
            CropOption("tomato", R.string.crop_tomato, R.drawable.ic_crop_tomato),
            CropOption("potato", R.string.crop_potato, R.drawable.ic_crop_potato),
            CropOption("wheat", R.string.crop_wheat, R.drawable.ic_crop_wheat),
            CropOption("other", R.string.crop_other, R.drawable.ic_crop_other)
        )
    }

    val symptomOptions = remember {
        listOf(
            SymptomOption("spots", R.string.symptom_spots),
            SymptomOption("yellow_leaves", R.string.symptom_yellow_leaves),
            SymptomOption("wilting", R.string.symptom_wilting),
            SymptomOption("insects", R.string.symptom_insects),
            SymptomOption("leaf_damage", R.string.symptom_leaf_damage),
            SymptomOption("poor_growth", R.string.symptom_poor_growth),
            SymptomOption("other", R.string.symptom_other)
        )
    }

    val durationPresets = remember {
        listOf(
            DurationPreset("less_1_week", R.string.duration_less_1_week),
            DurationPreset("1_2_weeks", R.string.duration_1_2_weeks),
            DurationPreset("2_4_weeks", R.string.duration_2_4_weeks),
            DurationPreset("1_2_months", R.string.duration_1_2_months),
            DurationPreset("2_3_months", R.string.duration_2_3_months),
            DurationPreset("more_3_months", R.string.duration_more_3_months)
        )
    }

    fun validateAndProceed() {
        cropError = null
        durationError = null

        val hasCustomDuration = customDurationNumber.trim().isNotEmpty()
        val finalDuration = if (hasCustomDuration) {
            "${customDurationNumber.trim()} $customDurationUnit"
        } else {
            selectedDurationPreset
        }

        val submission = CropSubmissionData(
            mainCropImageUri = currentMainImageUri,
            additionalImageUris = additionalImageUris.toList(),
            selectedCropKey = selectedCropKey,
            customCropName = customCropName.trim(),
            selectedSymptomKeys = selectedSymptomKeys.toList(),
            problemDescription = problemDescription.trim(),
            growthDuration = finalDuration,
            growthDurationUnit = customDurationUnit,
            selectedRegion = selectedRegion,
            selectedLanguage = selectedLanguageCode,
            visionPrediction = currentPrediction
        )
        onNavigateToGetAdvice(submission)
    }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNavigateBack,
                    shape = CircleShape,
                    color = InputBackground,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.add_details_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(
                        text = stringResource(id = R.string.add_details_subtitle),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 2 PROGRESS INDICATOR
            StepProgressIndicator(currentStep = 2)

            Spacer(modifier = Modifier.height(18.dp))

            // 1. MAIN CROP IMAGE SECTION
            val mainUri = currentMainImageUri?.let { Uri.parse(it) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.main_crop_photo_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        // Regional Context Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ForestGreen.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedRegion,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (mainUri != null) {
                        CropImagePreview(
                            imageUri = mainUri,
                            onRemoveImage = {
                                currentMainImageUri = null
                                currentPrediction = null
                            }
                        )
                        if (currentPrediction != null) {
                            val pred = currentPrediction!!
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (pred.isUncertain) Color(0xFFFFF3E0) else Color(0xFFF1F8F2),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (pred.isUncertain) Color(0xFFFFB74D) else Color(0xFFD6EAD8)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Vision AI: ${pred.crop}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = ForestGreen
                                        )
                                        Text(
                                            text = pred.formattedConfidence,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (pred.isUncertain) Color(0xFFE65100) else ButtonGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (pred.isUncertain) {
                                            "⚠️ Uncertain result - capture a clearer photo if possible."
                                        } else if (pred.isHealthy) {
                                            "Condition: Healthy / Normal leaf"
                                        } else {
                                            "Detected: ${pred.disease}"
                                        },
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    } else {
                        // Safe placeholder card if no photo was received
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(InputBackground.copy(alpha = 0.5f))
                                .border(
                                    width = 1.5.dp,
                                    color = InputBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    mainPhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(id = R.string.no_main_photo),
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = R.string.choose_main_photo),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. CROP SELECTION SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(id = R.string.select_crop_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.select_crop_subtitle),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-Column Grid of Crop Cards (2 rows of 3)
                    for (row in 0 until 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                if (index < cropOptions.size) {
                                    val crop = cropOptions[index]
                                    val isSelected = selectedCropKey == crop.key

                                    CropCard(
                                        crop = crop,
                                        isSelected = isSelected,
                                        onClick = {
                                            selectedCropKey = crop.key
                                            if (cropError != null) cropError = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        if (row == 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Display custom crop name input if "other" is selected
                    AnimatedVisibility(
                        visible = selectedCropKey == "other",
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            Text(
                                text = stringResource(id = R.string.enter_crop_name),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            KrishiTextField(
                                value = customCropName,
                                onValueChange = {
                                    customCropName = it
                                    if (cropError != null) cropError = null
                                },
                                placeholder = stringResource(id = R.string.enter_crop_name_placeholder),
                                leadingIcon = Icons.Default.Eco,
                                isError = cropError != null,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )
                        }
                    }

                    if (cropError != null) {
                        Text(
                            text = cropError.orEmpty(),
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. PROBLEM / SYMPTOMS SELECTION SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(id = R.string.symptoms_section_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.symptoms_section_subtitle),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Flow of Selectable Symptom Chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in symptomOptions.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val s1 = symptomOptions[i]
                                val isSelected1 = selectedSymptomKeys.contains(s1.key)
                                SymptomChip(
                                    option = s1,
                                    isSelected = isSelected1,
                                    onClick = {
                                        if (isSelected1) {
                                            selectedSymptomKeys.remove(s1.key)
                                        } else {
                                            selectedSymptomKeys.add(s1.key)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                if (i + 1 < symptomOptions.size) {
                                    val s2 = symptomOptions[i + 1]
                                    val isSelected2 = selectedSymptomKeys.contains(s2.key)
                                    SymptomChip(
                                        option = s2,
                                        isSelected = isSelected2,
                                        onClick = {
                                            if (isSelected2) {
                                                selectedSymptomKeys.remove(s2.key)
                                            } else {
                                                selectedSymptomKeys.add(s2.key)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. DESCRIBE THE PROBLEM (OPTIONAL)
                    Text(
                        text = stringResource(id = R.string.describe_problem_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.describe_problem_subtitle),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = problemDescription,
                        onValueChange = { problemDescription = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.describe_problem_placeholder),
                                fontSize = 12.sp,
                                color = TextSecondary.copy(alpha = 0.8f)
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = ButtonGreen,
                            unfocusedBorderColor = InputBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. CROP GROWTH DURATION SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(id = R.string.growth_duration_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.growth_duration_subtitle),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2-Column Grid of Duration Presets
                    for (i in durationPresets.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val d1 = durationPresets[i]
                            val isSelected1 = selectedDurationPreset == d1.key && customDurationNumber.isEmpty()
                            DurationChip(
                                preset = d1,
                                isSelected = isSelected1,
                                onClick = {
                                    selectedDurationPreset = d1.key
                                    customDurationNumber = "" // clear custom if preset selected
                                    if (durationError != null) durationError = null
                                },
                                modifier = Modifier.weight(1f)
                            )

                            if (i + 1 < durationPresets.size) {
                                val d2 = durationPresets[i + 1]
                                val isSelected2 = selectedDurationPreset == d2.key && customDurationNumber.isEmpty()
                                DurationChip(
                                    preset = d2,
                                    isSelected = isSelected2,
                                    onClick = {
                                        selectedDurationPreset = d2.key
                                        customDurationNumber = "" // clear custom if preset selected
                                        if (durationError != null) durationError = null
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        if (i + 2 < durationPresets.size) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Growth Duration Option
                    Text(
                        text = stringResource(id = R.string.or_custom_duration),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Duration Number Input
                        OutlinedTextField(
                            value = customDurationNumber,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    customDurationNumber = input
                                    if (input.isNotEmpty()) {
                                        selectedDurationPreset = "" // clear preset if custom is typed
                                        if (durationError != null) durationError = null
                                    }
                                }
                            },
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.custom_duration_placeholder),
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InputBackground,
                                unfocusedContainerColor = InputBackground,
                                focusedBorderColor = ButtonGreen,
                                unfocusedBorderColor = InputBorder
                            ),
                            modifier = Modifier.weight(1.2f)
                        )

                        // Unit Selector Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                onClick = { isUnitDropdownExpanded = true },
                                shape = RoundedCornerShape(14.dp),
                                color = InputBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, InputBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val unitLabel = when (customDurationUnit) {
                                        "Days" -> stringResource(id = R.string.unit_days)
                                        "Weeks" -> stringResource(id = R.string.unit_weeks)
                                        else -> stringResource(id = R.string.unit_months)
                                    }
                                    Text(
                                        text = unitLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = ForestGreen
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isUnitDropdownExpanded,
                                onDismissRequest = { isUnitDropdownExpanded = false }
                            ) {
                                listOf(
                                    Pair("Days", R.string.unit_days),
                                    Pair("Weeks", R.string.unit_weeks),
                                    Pair("Months", R.string.unit_months)
                                ).forEach { (unitKey, stringRes) ->
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(id = stringRes)) },
                                        onClick = {
                                            customDurationUnit = unitKey
                                            isUnitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (durationError != null) {
                        Text(
                            text = durationError.orEmpty(),
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. ADDITIONAL PHOTOS SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(id = R.string.add_more_photos_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.add_more_photos_subtitle),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        // Thumbnails of already added photos
                        additionalImageUris.forEach { uriString ->
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, InputBorder, RoundedCornerShape(14.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.bg_tropical_leaves),
                                    contentDescription = "Additional crop photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Remove X button
                                Surface(
                                    onClick = { additionalImageUris.remove(uriString) },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Add photo button [+]
                        Surface(
                            onClick = {
                                additionalPhotosLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = InputBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, InputBorder),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(id = R.string.add_photo_btn),
                                    tint = ForestGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(id = R.string.add_photo_btn),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForestGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 7. NEXT: GET ADVICE BUTTON
            KrishiPrimaryButton(
                text = stringResource(id = R.string.btn_get_advice),
                onClick = { validateAndProceed() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CONTINUE WITHOUT CROP DETAILS (OPTIONAL DETAILS)
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    val submission = CropSubmissionData(
                        mainCropImageUri = currentMainImageUri,
                        additionalImageUris = additionalImageUris.toList(),
                        selectedCropKey = selectedCropKey,
                        customCropName = customCropName.trim(),
                        selectedSymptomKeys = selectedSymptomKeys.toList(),
                        problemDescription = problemDescription.trim(),
                        growthDuration = "",
                        growthDurationUnit = "",
                        selectedRegion = selectedRegion,
                        selectedLanguage = selectedLanguageCode,
                        visionPrediction = currentPrediction
                    )
                    onNavigateToGetAdvice(submission)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreen)
            ) {
                Text(
                    text = "Continue without crop details",
                    color = ForestGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun CropCard(
    crop: CropOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ButtonGreen.copy(alpha = 0.12f) else InputBackground.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) ForestGreen else InputBorder
        ),
        modifier = modifier.height(102.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            // Selected Checkmark Badge
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = ForestGreen,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // Small but clearly visible crop icon (44-48 dp)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = crop.iconResId),
                        contentDescription = stringResource(id = crop.nameResId),
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = crop.nameResId),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) ForestGreen else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SymptomChip(
    option: SymptomOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) ButtonGreen.copy(alpha = 0.12f) else InputBackground.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ForestGreen else InputBorder
        ),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = option.labelResId),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) ForestGreen else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = ForestGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DurationChip(
    preset: DurationPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) ButtonGreen.copy(alpha = 0.12f) else InputBackground.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ForestGreen else InputBorder
        ),
        modifier = modifier.height(46.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = preset.labelResId),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) ForestGreen else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = ForestGreen,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddDetailsScreenPreview() {
    KrishiSaathiTheme {
        AddDetailsScreen(
            imageUriString = null,
            selectedRegion = "Karnataka",
            selectedLanguageCode = "kn",
            onNavigateBack = {},
            onNavigateToGetAdvice = {}
        )
    }
}
