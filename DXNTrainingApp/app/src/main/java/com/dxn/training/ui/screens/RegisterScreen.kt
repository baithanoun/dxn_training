package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.dxn.training.BuildConfig
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
    var gender by remember { mutableStateOf<String?>(null) }
    var acceptTerms by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val state by authViewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthUiState.LoggedIn) onRegister()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "إنشاء حساب DXN",
            style = MaterialTheme.typography.headlineMedium,
            color = BrandPlum700
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
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "الجنس *",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderChoice(
                label = "ذكر",
                selected = gender == "male",
                onClick = { gender = "male" },
                modifier = Modifier.weight(1f)
            )
            GenderChoice(
                label = "أنثى",
                selected = gender == "female",
                onClick = { gender = "female" },
                modifier = Modifier.weight(1f)
            )
        }

        (state as? AuthUiState.Error)?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error.message,
                color = Error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it })
            Column {
                Text(
                    text = "أوافق على سياسة الخصوصية والشروط",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.BASE_URL + "privacy")))
                    }) { Text("سياسة الخصوصية", style = MaterialTheme.typography.bodySmall) }
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.BASE_URL + "terms")))
                    }) { Text("الشروط", style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        DxButton(
            text = if (state is AuthUiState.Loading) "جاري إنشاء الحساب..." else "إنشاء الحساب",
            onClick = {
                val selectedGender = gender ?: return@DxButton
                authViewModel.register(name, contact, password, dxnId, leaderDxnId, selectedGender, acceptTerms)
            },
            enabled = gender != null && acceptTerms && state !is AuthUiState.Loading
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
                focusedContainerColor = BrandIvory,
                unfocusedContainerColor = BrandIvory,
                focusedBorderColor = BrandPlum,
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

@Composable
private fun GenderChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .heightIn(min = 52.dp)
            .selectable(selected = selected, onClick = onClick)
            .semantics { contentDescription = label },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) BrandPlum100 else BrandIvory,
            contentColor = if (selected) BrandPlum700 else TextMuted
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
