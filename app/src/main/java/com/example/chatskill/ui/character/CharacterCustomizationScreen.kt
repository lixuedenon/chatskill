package com.example.chatskill.ui.character

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatskill.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCustomizationScreen(
    isMale: Boolean,
    themeColor: Color,
    viewModel: CharacterCustomizationViewModel,
    onBackClick: () -> Unit,
    onConfirm: (CustomCharacter) -> Unit
) {
    val selectedAgeRange by viewModel.selectedAgeRange.collectAsState()
    val selectedPersonality by viewModel.selectedPersonality.collectAsState()
    val selectedEducation by viewModel.selectedEducation.collectAsState()
    val isValid by viewModel.isValid.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自定义角色") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "为你的聊天对象设定基本信息\n（系统将自动分配姓名）",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            SectionTitle("年龄段")
            AgeRangeSelector(
                selected = selectedAgeRange,
                onSelected = viewModel::onAgeRangeSelected,
                themeColor = themeColor
            )
            
            SectionTitle("性格")
            PersonalitySelector(
                selected = selectedPersonality,
                onSelected = viewModel::onPersonalitySelected,
                themeColor = themeColor
            )
            
            SectionTitle("教育程度")
            EducationSelector(
                selected = selectedEducation,
                onSelected = viewModel::onEducationSelected,
                themeColor = themeColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val character = viewModel.createCustomCharacter(
                        gender = if (isMale) Gender.FEMALE else Gender.MALE
                    )
                    character?.let { onConfirm(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "开始对话",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun AgeRangeSelector(
    selected: AgeRange?,
    onSelected: (AgeRange) -> Unit,
    themeColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AgeRange.values().forEach { ageRange ->
            SelectableChip(
                text = ageRange.displayName,
                isSelected = selected == ageRange,
                onClick = { onSelected(ageRange) },
                themeColor = themeColor
            )
        }
    }
}

@Composable
private fun PersonalitySelector(
    selected: PersonalityType?,
    onSelected: (PersonalityType) -> Unit,
    themeColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PersonalityType.values().forEach { personality ->
            SelectableCard(
                title = personality.displayName,
                description = personality.description,
                isSelected = selected == personality,
                onClick = { onSelected(personality) },
                themeColor = themeColor
            )
        }
    }
}

@Composable
private fun EducationSelector(
    selected: EducationLevel?,
    onSelected: (EducationLevel) -> Unit,
    themeColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EducationLevel.values().forEach { education ->
            SelectableCard(
                title = education.displayName,
                description = education.speakingStyle,
                isSelected = selected == education,
                onClick = { onSelected(education) },
                themeColor = themeColor
            )
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = if (isSelected) themeColor.copy(alpha = 0.1f) else Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) themeColor else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) themeColor else Color.Black
        )
    }
}

@Composable
private fun SelectableCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) themeColor.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = themeColor
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}