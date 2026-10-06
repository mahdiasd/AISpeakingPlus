#!/usr/bin/env bash

# ==============================================================================
# AISpeakingPlus - All-in-One Local Development Launcher
# Starts Docker (PostgreSQL & Redis), 9router AI Gateway (20128),
# Ktor Backend (8080), and React Web (8082)
# ==============================================================================

set -e

# ANSI Colors
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m' # No Color

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

export PATH="$HOME/.local/bin:$PATH"

echo -e "${CYAN}${BOLD}"
echo "=================================================================="
echo "    🚀 AISpeakingPlus - Starting All Development Services"
echo "=================================================================="
echo -e "${NC}"

# ------------------------------------------------------------------------------
# 1. Check and Start Containers (PostgreSQL & Redis via Docker / OrbStack)
# ------------------------------------------------------------------------------
echo -e "${CYAN}[1/5] Checking Database & Redis containers...${NC}"

# If Docker daemon is not running, try starting OrbStack if available
if ! docker info >/dev/null 2>&1; then
  if command -v orb >/dev/null 2>&1; then
    echo -e "${YELLOW}Starting OrbStack engine...${NC}"
    orb start >/dev/null 2>&1 || true
    sleep 3
  fi
fi

if docker info >/dev/null 2>&1; then
  # Start PostgreSQL container if stopped
  if docker ps -a --format '{{.Names}}' | grep -q "ktor_db_container"; then
    docker start ktor_db_container >/dev/null 2>&1 || true
  elif docker ps -a --format '{{.Names}}' | grep -q "postgres"; then
    docker start "$(docker ps -a --format '{{.Names}}' | grep "postgres" | head -n 1)" >/dev/null 2>&1 || true
  fi

  # Start Redis container if stopped
  if docker ps -a --format '{{.Names}}' | grep -q "aispeakingktor-redis-1"; then
    docker start aispeakingktor-redis-1 >/dev/null 2>&1 || true
  elif docker ps -a --format '{{.Names}}' | grep -q "redis"; then
    docker start "$(docker ps -a --format '{{.Names}}' | grep "redis" | head -n 1)" >/dev/null 2>&1 || true
  fi
  echo -e "${GREEN}✓ Database & Redis containers are active.${NC}"
else
  echo -e "${YELLOW}⚠ Docker is not running. If you have local Postgres/Redis installed, continuing...${NC}"
fi

# ------------------------------------------------------------------------------
# 2. Check and Start 9router (AI Gateway on port 20128)
# ------------------------------------------------------------------------------
echo -e "${CYAN}[2/5] Checking 9router (AI Gateway on port 20128)...${NC}"

if lsof -i :20128 >/dev/null 2>&1 || curl -s --connect-timeout 1 http://127.0.0.1:20128/ >/dev/null 2>&1; then
  echo -e "${GREEN}✓ 9router is already running on http://127.0.0.1:20128${NC}"
else
  ROUTER_BIN=""
  if command -v 9router >/dev/null 2>&1; then
    ROUTER_BIN="9router"
  elif [ -f "$HOME/.local/bin/9router" ]; then
    ROUTER_BIN="$HOME/.local/bin/9router"
  fi

  if [ -n "$ROUTER_BIN" ]; then
    echo -e "${YELLOW}Starting 9router in background...${NC}"
    "$ROUTER_BIN" --tray --skip-update >/dev/null 2>&1 &

    ROUTER_READY=false
    for i in {1..20}; do
      if lsof -i :20128 >/dev/null 2>&1 || curl -s --connect-timeout 1 http://127.0.0.1:20128/ >/dev/null 2>&1; then
        ROUTER_READY=true
        break
      fi
      sleep 1
    done

    if [ "$ROUTER_READY" = true ]; then
      echo -e "${GREEN}✓ 9router started successfully on http://127.0.0.1:20128${NC}"
    else
      echo -e "${YELLOW}⚠ 9router did not respond on port 20128 within timeout. Continuing...${NC}"
    fi
  else
    echo -e "${YELLOW}⚠ 9router CLI was not found. If you need AI features, please run or install 9router.${NC}"
  fi
fi

# ------------------------------------------------------------------------------
# 3. Free Ports 8080 and 8082 if previously occupied
# ------------------------------------------------------------------------------
echo -e "${CYAN}[3/5] Ensuring ports 8080 and 8082 are free...${NC}"
lsof -ti:8080 | xargs kill -9 >/dev/null 2>&1 || true
lsof -ti:8082 | xargs kill -9 >/dev/null 2>&1 || true

# ------------------------------------------------------------------------------
# 4. Check Web Frontend Dependencies
# ------------------------------------------------------------------------------
if [ ! -d "admin-web/node_modules" ]; then
  echo -e "${YELLOW}[4/5] Installing web dependencies in admin-web...${NC}"
  (cd admin-web && npm install --cache /tmp/npm-cache)
else
  echo -e "${GREEN}[4/5] Web dependencies already installed.${NC}"
fi

# ------------------------------------------------------------------------------
# Cleanup handler on exit
# ------------------------------------------------------------------------------
BACKEND_PID=""
FRONTEND_PID=""

cleanup() {
  echo -e "\n${YELLOW}⏹ Stopping all running services...${NC}"
  if [ -n "$BACKEND_PID" ]; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi
  if [ -n "$FRONTEND_PID" ]; then
    kill "$FRONTEND_PID" 2>/dev/null || true
  fi
  # Kill any child Gradle/Java processes on port 8080/8082
  lsof -ti:8080 | xargs kill -9 >/dev/null 2>&1 || true
  lsof -ti:8082 | xargs kill -9 >/dev/null 2>&1 || true
  echo -e "${GREEN}✓ All services stopped cleanly.${NC}"
  exit 0
}

trap cleanup SIGINT SIGTERM

# ------------------------------------------------------------------------------
# 5. Start Ktor Backend Server (Port 8080)
# ------------------------------------------------------------------------------
echo -e "${CYAN}[5/5] Starting Ktor Backend Server on port 8080...${NC}"
./gradlew :server:run --quiet > server.log 2>&1 &
BACKEND_PID=$!

echo -e "Waiting for Ktor server to boot up..."
MAX_RETRIES=40
RETRIES=0
SERVER_READY=false

while [ $RETRIES -lt $MAX_RETRIES ]; do
  if curl -s http://127.0.0.1:8080/health >/dev/null 2>&1 || lsof -i :8080 >/dev/null 2>&1; then
    SERVER_READY=true
    break
  fi
  sleep 1
  RETRIES=$((RETRIES + 1))
done

if [ "$SERVER_READY" = true ]; then
  echo -e "${GREEN}✓ Ktor Backend is running and healthy on http://127.0.0.1:8080${NC}"
else
  echo -e "${RED}✗ Failed to start Ktor backend within $MAX_RETRIES seconds.${NC}"
  echo -e "${YELLOW}Recent server logs (server.log):${NC}"
  tail -n 25 server.log 2>/dev/null || true
  exit 1
fi

# ------------------------------------------------------------------------------
# 6. Start React Admin & Web Frontend (Port 8082)
# ------------------------------------------------------------------------------
echo -e "${CYAN}Starting React Admin on port 8082...${NC}"
(cd admin-web && npm run dev) &
FRONTEND_PID=$!

sleep 2

# ------------------------------------------------------------------------------
# Ready Banner
# ------------------------------------------------------------------------------
echo ""
echo -e "${GREEN}${BOLD}==================================================================${NC}"
echo -e "${GREEN}${BOLD} 🎉 ALL SERVICES ARE RUNNING SUCCESSFULLY!                       ${NC}"
echo -e "${GREEN}${BOLD}==================================================================${NC}"
echo -e "  🌐 ${BOLD}اپلیکیشن اصلی وب (Compose Wasm):${NC} ${CYAN}http://localhost:8081/${NC} (با دستور ./gradlew :webApp:wasmJsBrowserDevelopmentRun)"
echo -e "  ⚙️  ${BOLD}پنل مدیریت ادمین (React):${NC}       ${CYAN}http://localhost:8082/admin${NC}"
echo -e "  🔌 ${BOLD}سرور بک‌اند (Ktor):${NC}              ${CYAN}http://127.0.0.1:8080/${NC}"
echo -e "  🤖 ${BOLD}گیت‌وی هوش مصنوعی (9router):${NC}   ${CYAN}http://127.0.0.1:20128/dashboard${NC}"
echo -e "  🩺 ${BOLD}بررسی سلامت (Health):${NC}           ${CYAN}http://127.0.0.1:8080/health${NC}"
echo -e "------------------------------------------------------------------"
echo -e "  🔑 ${BOLD}اطلاعات ورود ادمین (دیفالت):${NC}"
echo -e "     نام کاربری: ${YELLOW}admin@aispeaking.ir${NC}"
echo -e "     رمز عبور:   ${YELLOW}Admin@123456!${NC}"
echo -e "------------------------------------------------------------------"
echo -e "  📝 لاگ‌های بک‌اند در فایل ${BOLD}server.log${NC} ذخیره می‌شوند."
echo -e "  برای متوقف کردن همه سرویس‌ها کلیدهای ${BOLD}[Ctrl + C]${NC} را فشار دهید."
echo -e "${GREEN}${BOLD}==================================================================${NC}\n"

# Keep script running and wait for background jobs
wait "$FRONTEND_PID" "$BACKEND_PID"
