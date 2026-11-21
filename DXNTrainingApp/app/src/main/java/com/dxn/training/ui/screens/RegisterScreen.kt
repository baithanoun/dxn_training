package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.theme.*

@Composable
fun RegisterScreen(onRegister: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dxnId by remember { mutableStateOf("") }
    var sponsorCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardSurface)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(
            text = "إنشاء حساب DXN",
            style = MaterialTheme.typography.headlineMedium,
            color = DXNBlue
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthField(value = name, onValueChange = { name = it }, label = "الاسم الكامل")
        Spacer(modifier = Modifier.height(12.dp))
        AuthField(
            value = email,
            onValueChange = { email = it },
            label = "البريد/الهاتف",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthField(value = dxnId, onValueChange = { dxnId = it }, label = "رقم عضوية DXN")
        Spacer(modifier = Modifier.height(12.dp))
        AuthField(
            value = sponsorCode,
            onValueChange = { sponsorCode = it },
            label = "كود القائد (اختياري)"
        )

        Spacer(modifier = Modifier.height(28.dp))
        DxButton(text = "إنشاء الحساب", onClick = onRegister)
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBack) {
            Text(text = "رجوع", color = TextMuted)
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Background,
                unfocusedContainerColor = Background,
                focusedBorderColor = DXNRed,
                unfocusedBorderColor = Disabled
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
    }
}
