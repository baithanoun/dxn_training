package com.dxn.training.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class Gender { MALE, FEMALE, UNKNOWN }

fun String?.toGender(): Gender = when {
    this?.equals("female", ignoreCase = true) == true -> Gender.FEMALE
    this?.equals("male", ignoreCase = true) == true -> Gender.MALE
    else -> Gender.UNKNOWN
}

val LocalGender = staticCompositionLocalOf { Gender.UNKNOWN }

@Composable
fun g(male: String, female: String): String = LocalGender.current.pick(male, female)

fun Gender.pick(male: String, female: String): String =
    if (this == Gender.FEMALE) female else male
