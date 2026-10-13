package com.gluedin.ott.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.domain.model.Resource
import com.gluedin.ott.domain.usecase.GetContentDetailsUseCase
import com.gluedin.ott.presentation.base.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContentDetailViewModel(
    private val getContentDetailsUseCase: GetContentDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ContentItem>>(UiState.Loading)
    val uiState: StateFlow<UiState<ContentItem>> = _uiState.asStateFlow()

    fun loadContent(contentId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = getContentDetailsUseCase(contentId)) {
                is Resource.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                is Resource.Loading -> {
                    _uiState.value = UiState.Loading
                }
            }
        }
    }

    fun setInitialContent(item: ContentItem) {
        _uiState.value = UiState.Success(item)
    }
}

class ContentDetailViewModelFactory(
    private val getContentDetailsUseCase: GetContentDetailsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContentDetailViewModel::class.java)) {
            return ContentDetailViewModel(getContentDetailsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
