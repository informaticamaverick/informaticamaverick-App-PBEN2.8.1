package com.example.myapplication.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper para FirebaseAuth
 * Esto evita que KSP procese directamente la clase Java de Firebase
 */
@Singleton
class FirebaseAuthWrapper @Inject constructor() {
    val instance: FirebaseAuth = FirebaseAuth.getInstance()
}

/**
 * Wrapper para FirebaseFirestore  
 * Esto evita que KSP procese directamente la clase Java de Firebase
 */
@Singleton
class FirebaseFirestoreWrapper @Inject constructor() {
    val instance: FirebaseFirestore = FirebaseFirestore.getInstance()
}

/**
 * FirebaseWrappers: Wrappers de FirebaseAuth y Firestore para evitar problemas con KSP
 * en el procesamiento de clases Java de Firebase durante la compilación.
 */
