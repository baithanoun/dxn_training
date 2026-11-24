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
    val nameColor = if (onDark) Surface else BrandPlum
    val dxnColor = if (onDark) BrandRose300 else BrandRose600

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (large) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.brand_logo),
                    contentDescription = "شعار حكاية تميّز",
                    modifier = Modifier.size(128.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "حكاية تميّز",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = nameColor
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = "DXN",
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 2.sp,
                        color = dxnColor
                    )
                }
            }
        } else {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.brand_logo),
                    contentDescription = "شعار حكاية تميّز",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "حكاية تميّز",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = nameColor
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "DXN",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp,
                            color = dxnColor
                        )
                    }
                }
            }
        }
    }
}
