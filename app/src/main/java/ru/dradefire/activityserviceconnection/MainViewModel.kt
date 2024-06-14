package ru.dradefire.activityserviceconnection

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _mainText = MutableStateFlow("")
    val mainText = _mainText.asStateFlow()

    fun setMainText(s: String) {
        _mainText.update { s }
    }
}