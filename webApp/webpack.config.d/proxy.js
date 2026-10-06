// Webpack dev-server configuration for the WASM/JS web app.
// Proxies relative API requests (e.g. /v1/config, /api/v1/stt) to the
// real backend, avoiding CORS errors during local development since the
// browser sees the requests as same-origin (localhost:8080).
(function (config) {
    config.devServer = config.devServer || {};
    config.devServer.port = 8081;
    const backendTarget = process.env.BACKEND_TARGET || "http://127.0.0.1:8080";
    config.devServer.proxy = [
        {
            context: ["/v1/**"],
            target: backendTarget,
            secure: false,
            changeOrigin: true,
            logLevel: "debug"
        },
        {
            // Server-side STT endpoint (WebSocket + REST) — must be proxied
            // alongside the rest of the /api/ tree so the browser can hit it
            // during local development. WebSocket upgrades are forwarded by
            // webpack-dev-server by default for proxied targets.
            context: ["/api/**"],
            target: backendTarget,
            secure: false,
            changeOrigin: true,
            logLevel: "debug",
            ws: true
        },
        {
            context: ["/resources/**", "/uploads/**"],
            target: backendTarget,
            secure: false,
            changeOrigin: true,
            logLevel: "debug"
        }
    ];
})(config);
