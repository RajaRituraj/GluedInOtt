package com.gluedin.ott.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gluedin.ott.domain.model.HomeFeed
import com.gluedin.ott.domain.model.Resource
import com.gluedin.ott.domain.usecase.GetHomeFeedUseCase
import com.gluedin.ott.presentation.base.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeFeedUseCase: GetHomeFeedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeFeed>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeFeed>> = _uiState.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = getHomeFeedUseCase()) {
                is Resource.Success -> {
                    val feed = result.data
                    if (feed.rails.isEmpty() && feed.heroBanner.title.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(feed)
                    }
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

    fun retry() {
        loadHomeFeed()
    }
}

class HomeViewModelFactory(
    private val getHomeFeedUseCase: GetHomeFeedUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(getHomeFeedUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
