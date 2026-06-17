package com.example.mobile.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobile.ui.theme.AppTheme

@Composable
fun EmptyStateCard(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    hint: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.current.primaryContainer.copy(alpha = 0.28f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AppTheme.current.violet,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.current.violet,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.current.muted,
                textAlign = TextAlign.Center
            )
            if (hint != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.current.amber,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LoadingStateCard(
    modifier: Modifier = Modifier,
    message: String = "Loading your dragon's journey..."
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.current.primaryContainer.copy(alpha = 0.28f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = AppTheme.current.violet
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.current.muted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorStateCard(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.current.dangerSoft.copy(alpha = 0.28f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Something needs attention",
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.current.danger,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.current.danger,
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("Try again")
                }
            }
        }
    }
}
