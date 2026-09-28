package com.example.workouttracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.model.MuscleCategory

@Composable
fun CategoryBadge(
    category: MuscleCategory,
    modifier: Modifier = Modifier
) {
    val categoryColor = category.color
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(categoryColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(categoryColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (MaterialTheme.colorScheme.background == Color.White) {
                categoryColor
            } else {
                categoryColor
            }
        )
    }
}
