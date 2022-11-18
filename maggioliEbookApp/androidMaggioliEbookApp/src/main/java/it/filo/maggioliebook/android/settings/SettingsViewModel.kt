package it.filo.maggioliebook.android.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {

    private val _settings = MutableLiveData<Boolean>().apply {
        value = true // TODO
    }

    private val _text = MutableLiveData<String>().apply {
        value = "SETTINGS"
    }

    val settings: LiveData<Boolean> = _settings // TODO
    val text: LiveData<String> = _text
}