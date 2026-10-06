package ir.aispeaking.network

// Android emulator uses 10.0.2.2 to access host machine's localhost:8080
actual fun platformBaseUrl(): String = "http://10.0.2.2:8080/"
