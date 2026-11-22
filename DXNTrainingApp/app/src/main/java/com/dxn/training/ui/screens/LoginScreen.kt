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
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by authViewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthUiState.LoggedIn) onLogin()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardSurface)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "تسجيل الدخول",
            style = MaterialTheme.typography.headlineMedium,
            color = DXNBlue
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthTextField(
            value = login,
            onValueChange = { login = it },
            label = "البريد / رقم الهاتف",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "كلمة المرور",
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password
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

        Spacer(modifier = Modifier.height(32.dp))
        DxButton(
            text = if (state is AuthUiState.Loading) "جاري الدخول..." else "دخول",
            onClick = { authViewModel.login(login, password) },
            enabled = state !is AuthUiState.Loading
        )

        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onRegister) {
            Text(
                text = "ليس لديك حساب؟ سجّل",
                color = DXNRed,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = "أو سجل عبر Google / Apple",
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
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
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
    }
}
