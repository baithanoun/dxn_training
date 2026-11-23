package com.dxn.training.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dxn.training.R
import com.dxn.training.ui.theme.*

enum class BrandMarkSize { Small, Large }

@Composable
fun BrandMark(
    size: BrandMarkSize = BrandMarkSize.Small,
    onDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val large = size == BrandMarkSize.Large
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.brand_logo),
                contentDescription = "شعار حكاية تميّز",
                modifier = Modifier.size(if (large) 96.dp else 40.dp)
            )
            Spacer(modifier = Modifier.width(if (large) 16.dp else 8.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "حكاية تميّز",
                    style = if (large) {
                        MaterialTheme.typography.headlineMedium
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (onDark) Surface else BrandPlum
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = "DXN",
                        style = if (large) {
                            MaterialTheme.typography.labelLarge
                        } else {
                            MaterialTheme.typography.labelSmall
                        },
                        letterSpacing = 2.sp,
                        color = if (onDark) BrandRose300 else BrandRose600
                    )
                }
            }
        }
    }
}
