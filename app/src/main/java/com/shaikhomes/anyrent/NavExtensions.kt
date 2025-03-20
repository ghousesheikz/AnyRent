package com.shaikhomes.anyrent

import androidx.annotation.NonNull
import androidx.navigation.NavController
import androidx.navigation.NavDirections

fun NavController.safeNavigate(@NonNull direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
}