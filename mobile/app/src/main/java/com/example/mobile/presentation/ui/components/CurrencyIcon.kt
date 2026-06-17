package com.example.mobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobile.R
import com.example.mobile.ui.theme.AppTheme

@Composable
fun CoinIcon(
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.current.amber
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_coin),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun CoinPill(
    amount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = AppTheme.current.amberSoft
    ) {
        Row(
            modifier = modifier
                .background(AppTheme.current.amberSoft, CircleShape)
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoinIcon(
                modifier = Modifier.size(15.dp),
                tint = AppTheme.current.amber
            )
            Text(
                text = amount.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.current.amberDark
            )
        }
    }
}
