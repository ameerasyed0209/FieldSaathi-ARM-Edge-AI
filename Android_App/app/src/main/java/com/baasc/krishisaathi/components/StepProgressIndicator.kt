package com.baasc.krishisaathi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baasc.krishisaathi.R
import com.baasc.krishisaathi.ui.theme.DividerColor
import com.baasc.krishisaathi.ui.theme.ForestGreen
import com.baasc.krishisaathi.ui.theme.TextSecondary

@Composable
fun StepProgressIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val step1Title = stringResource(id = R.string.step_1_upload)
    val step2Title = stringResource(id = R.string.step_2_details)
    val step3Title = stringResource(id = R.string.step_3_advice)

    val steps = listOf(
        Pair(1, step1Title),
        Pair(2, step2Title),
        Pair(3, step3Title)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, (stepNumber, title) ->
            val isActive = stepNumber <= currentStep
            val isCurrent = stepNumber == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) ForestGreen else DividerColor
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        color = if (isActive) Color.White else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) ForestGreen else TextSecondary,
                    maxLines = 1
                )
            }

            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(bottom = 18.dp),
                    thickness = 1.5.dp,
                    color = if (stepNumber < currentStep) ForestGreen else DividerColor
                )
            }
        }
    }
}
