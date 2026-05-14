package edu.gr05513.locator

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import edu.gr05513.locator.locating.Locator

class MainViewModel : ViewModel() {
    var accessLocation by mutableStateOf(false)

    val locations = mutableStateListOf<Location>()
}