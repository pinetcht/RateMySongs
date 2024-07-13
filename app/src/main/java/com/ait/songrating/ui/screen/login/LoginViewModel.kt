package com.ait.songrating.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    var loginUiState: LoginUiState by mutableStateOf(
        LoginUiState.Init
    )
    private lateinit var auth: FirebaseAuth

    init{
        auth = Firebase.auth
    }

    fun registerUser(email: String, password: String){
        loginUiState = LoginUiState.Loading

        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    loginUiState = LoginUiState.RegisterSuccess
                }

                // ex if username already used
                .addOnFailureListener{
                    loginUiState = LoginUiState.Error(it.message)
                }

        }
        // network issues
        catch(e: Exception) {
            loginUiState = LoginUiState.Error(e.message)
        }
    }

    // can only be called from a thread or coroutine
    suspend fun loginUser(email: String, password: String): AuthResult? {
        loginUiState = LoginUiState.Loading
        try {
            // await will freeze if we don't use a suspend function (from another background thread)
            val result = auth.signInWithEmailAndPassword(email,password).await()
            if (result.user != null) {
                loginUiState = LoginUiState.LoginSuccess
            } else {
                loginUiState = LoginUiState.Error("Login failed")
            }
            return result

        } catch (e: Exception) {
            return null
        }
    }
}



sealed interface LoginUiState {
    object Init : LoginUiState
    object Loading : LoginUiState
    object LoginSuccess : LoginUiState
    object RegisterSuccess : LoginUiState
    data class Error(val error: String?) : LoginUiState
}