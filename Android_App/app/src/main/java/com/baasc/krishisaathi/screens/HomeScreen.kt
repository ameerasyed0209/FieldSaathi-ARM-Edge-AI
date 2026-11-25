package com.baasc.krishisaathi.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baasc.krishisaathi.R
import com.baasc.krishisaathi.components.CropImagePreview
import com.baasc.krishisaathi.components.KrishiPrimaryButton
import com.baasc.krishisaathi.components.StepProgressIndicator
import com.baasc.krishisaathi.data.RegionLanguageData
import com.baasc.krishisaathi.ui.theme.ButtonGreen
import com.baasc.krishisaathi.ui.theme.CardBackground
import com.baasc.krishisaathi.ui.theme.DividerColor
import com.baasc.krishisaathi.ui.theme.ForestGreen
import com.baasc.krishisaathi.ui.theme.InputBackground
import com.baasc.krishisaathi.ui.theme.InputBorder
import com.baasc.krishisaathi.ui.theme.KrishiSaathiTheme
import com.baasc.krishisaathi.ui.theme.TextPrimary
import com.baasc.krishisaathi.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    selectedRegion: String = RegionLanguageData.DEFAULT_REGION,
    selectedLanguageCode: String = RegionLanguageData.DEFAULT_LANGUAGE,
    onNavigateToScan: () -> Unit,
    onNavigateToAddDetails: (String?) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedBottomNavIndex by rememberSaveable { mutableIntStateOf(0) }

    // When an image preview is active on the Home screen, pressing Back returns to the clean Home state
    BackHandler(enabled = selectedImageUriString != null) {
        selectedImageUriString = null
    }

    // Native Android photo picker launcher (direct to photo picker, no "Open with" chooser)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUriString = uri.toString()
        }
    }

    val pleaseSelectPhotoNotice = stringResource(id = R.string.please_select_or_scan_image)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .shadow(12.dp)
                    .navigationBarsPadding()
            ) {
                val navItems = listOf(
                    Triple(stringResource(id = R.string.nav_home), Icons.Default.Home, 0),
                    Triple(stringResource(id = R.string.nav_scan), Icons.Default.QrCodeScanner, 1),
                    Triple(stringResource(id = R.string.nav_advisory), Icons.AutoMirrored.Filled.MenuBook, 2),
                    Triple(stringResource(id = R.string.nav_profile), Icons.Default.AccountCircle, 3)
                )

                navItems.forEach { (label, icon, index) ->
                    val selected = selectedBottomNavIndex == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            selectedBottomNavIndex = index
                            if (index == 1) {
                                onNavigateToScan()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (selected) ForestGreen else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) ForestGreen else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = ForestGreen.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CardBackground)
                .statusBarsPadding()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 1. TOP HEADER: KrishiSaathi Official Logo Branding
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.krishisaathi_logo),
                    contentDescription = "KrishiSaathi",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                // Logout button
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(InputBackground)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = stringResource(id = R.string.logout),
                        tint = ForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. STEP PROGRESS INDICATOR: Step 1 (Upload Photo) -> Step 2 (Add Details) -> Step 3 (Get Advice)
            StepProgressIndicator(currentStep = 1)

            Spacer(modifier = Modifier.height(20.dp))

            // 3. MAIN CARD: Upload / Scan Crop Photo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Card Title & Subtitle
                    Text(
                        text = stringResource(id = R.string.upload_the_photo_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(id = R.string.upload_the_photo_desc),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // IMAGE PREVIEW OR UPLOAD BOX
                    val currentUri = selectedImageUriString?.let { Uri.parse(it) }

                    if (currentUri != null) {
                        CropImagePreview(
                            imageUri = currentUri,
                            onRemoveImage = { selectedImageUriString = null }
                        )
                    } else {
                        // Dashed-style Upload / Scan Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(InputBackground.copy(alpha = 0.5f))
                                .border(
                                    width = 1.5.dp,
                                    color = InputBorder,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = R.string.tap_to_upload_or_take),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(id = R.string.tap_to_upload_sub),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // PRIMARY ACTION: "Scan Me" Button
                    Button(
                        onClick = onNavigateToScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonGreen,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.scan_me),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // SECONDARY ACTION: "Upload Image" Button
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.upload_image),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // TIP BANNER
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF1F8F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6EAD8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.tip_photo_advice),
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // NEXT: ADD DETAILS BUTTON
                    KrishiPrimaryButton(
                        text = stringResource(id = R.string.next_add_details),
                        onClick = {
                            if (selectedImageUriString == null) {
                                Toast.makeText(context, pleaseSelectPhotoNotice, Toast.LENGTH_SHORT).show()
                            }
                            // Always navigate forward to Add Details screen and pass selected crop image
                            onNavigateToAddDetails(selectedImageUriString)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. QUICK STARTS SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.quick_starts_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.quick_starts_desc),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val commonProblems = listOf(
                        stringResource(id = R.string.problem_maize_blight),
                        stringResource(id = R.string.problem_purple_maize),
                        stringResource(id = R.string.problem_soybean_nitrogen),
                        stringResource(id = R.string.problem_soybean_root_rot),
                        stringResource(id = R.string.problem_fall_armyworm),
                        stringResource(id = R.string.problem_pod_borer)
                    )

                    // 2-column grid of problem chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in commonProblems.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickStartChip(
                                    text = commonProblems[i],
                                    onClick = { onNavigateToAddDetails(selectedImageUriString) },
                                    modifier = Modifier.weight(1f)
                                )
                                if (i + 1 < commonProblems.size) {
                                    QuickStartChip(
                                        text = commonProblems[i + 1],
                                        onClick = { onNavigateToAddDetails(selectedImageUriString) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickStartChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = InputBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, InputBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    KrishiSaathiTheme {
        HomeScreen(
            selectedRegion = "Karnataka",
            selectedLanguageCode = "kn",
            onNavigateToScan = {},
            onNavigateToAddDetails = {},
            onLogout = {}
        )
    }
}
