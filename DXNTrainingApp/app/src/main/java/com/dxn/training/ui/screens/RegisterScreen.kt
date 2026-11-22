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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.viewmodel.AuthUiState
import com.dxn.training.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dxnId by remember { mutableStateOf("") }
    var leaderDxnId by remember { mutableStateOf("") }
    val state by authViewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthUiState.LoggedIn) onRegister()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardSurface)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "إنشاء حساب DXN",
            style = MaterialTheme.typography.headlineMedium,
            color = DXNBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthField(value = name, onValueChange = { name = it }, label = "الاسم الكامل")
        Spacer(modifier = Modifier.height(12.dp))
        AuthField(
            value = contact,
            onValueChange = { contact = it },
            label = "البريد/الهاتف",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthField(
            value = password,
            onValueChange = { password = it },
            label = "كلمة المرور",
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthField(value = dxnId, onValueChange = { dxnId = it }, label = "رقم عضوية DXN")
        Spacer(modifier = Modifier.height(12.dp))
        AuthField(
            value = leaderDxnId,
            onValueChange = { leaderDxnId = it },
            label = "رقم عضوية القائد (اختياري)"
        )

        (state as? AuthUiState.Error)?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error.message,
                color = DXNRedDeep,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        DxButton(
            text = if (state is AuthUiState.Loading) "جاري إنشاء الحساب..." else "إنشاء الحساب",
            onClick = {
                authViewModel.register(name, contact, password, dxnId, leaderDxnId)
            },
            enabled = state !is AuthUiState.Loading
        )
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
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
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
            visualTransformation = visualTransformation,
            singleLine = true
        )
    }
}
