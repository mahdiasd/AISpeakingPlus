package ir.aispeaking.network

// Root-relative base so URLs become "/v1/config" etc.
// These are served by the webpack dev-server proxy which forwards
// them to the real API server, avoiding CORS during local development.
actual fun platformBaseUrl(): String = "http://localhost:8080/"
