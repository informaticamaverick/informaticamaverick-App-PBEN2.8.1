package com.example.myapplication.prestador.ui.dashboard

import androidx.lifecycle.ViewModel
import com.example.myapplication.prestador.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel class PrestadorDashboardViewModel @Inject constructor(
    private val  authRepository: AuthRepository) : ViewModel() {
    fun signOut() {
        authRepository.signOut()
    }
}
