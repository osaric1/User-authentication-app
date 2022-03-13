package com.example.zavrsni

import androidx.biometric.BiometricManager;
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BiometricManager.from(this).canAuthenticate()
    }

}