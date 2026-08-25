package ir.speaking.admin.admin.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*

fun configureFirebase() {
    val serviceAccountStream = Application::class.java.classLoader.getResourceAsStream("service-account-key.json")
        ?: throw IllegalStateException("Firebase service account key file not found in resources.")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
        .build()

    if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options)
        println("Firebase has been initialized.")
    }
}