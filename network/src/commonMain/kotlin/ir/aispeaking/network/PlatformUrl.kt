package ir.aispeaking.network

// Platform-specific base URL for API requests.
// Native targets use the absolute server URL directly.
// Browser targets (WASM/JS) use a relative URL so requests
// go through the local dev-server proxy, avoiding CORS issues.
expect fun platformBaseUrl(): String
