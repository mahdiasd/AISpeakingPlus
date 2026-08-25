#!/bin/bash
# deploy_from_mac.sh
set -e

# --- Configuration ---
SERVER_USER="ubuntu"
SERVER_IP="37.32.20.173"
SERVER_DIR="/home/ubuntu/ai-speaking"
APP_NAME="ai-speaking"
APP_VERSION="latest"
SSH_KEY_PATH="$HOME/.ssh/id_ed25519"
# ---------------------

echo "================================================="
echo " Starting Fast Deployment Process for $APP_NAME "
echo "================================================="

echo "=> Building Ktor FatJar..."
./gradlew buildFatJar

echo "=> Building Docker Image for target architecture (linux/amd64)..."
docker buildx build --platform linux/amd64 -t ${APP_NAME}:${APP_VERSION} --load .

echo "=> Preparing deploy directories and exporting App Image..."
mkdir -p deploy_files/images
docker save ${APP_NAME}:${APP_VERSION} > deploy_files/images/${APP_NAME}.tar

echo "=> Preparing files for transfer..."
cp docker-compose.yml deploy_files/
cp setup-server.sh deploy_files/
cp dns-config.sh deploy_files/
cp -r nginx deploy_files/

# کپی کردن پوشه استاتیک (شامل index.html و فایل‌های فرانت‌اند)
if [ -d "src/main/resources/static" ]; then
    cp -r src/main/resources/static deploy_files/
else
    echo "Warning: static folder not found in src/main/resources/!"
fi

echo "=> Syncing files to server ($SERVER_IP)..."
rsync -av --progress --partial --inplace --timeout=60 -e "ssh -i $SSH_KEY_PATH -o ServerAliveInterval=30 -o ServerAliveCountMax=6" ./deploy_files/ ${SERVER_USER}@${SERVER_IP}:${SERVER_DIR}/

echo "=> Executing remote setup and deployment on the VPS..."
ssh -i $SSH_KEY_PATH -o ServerAliveInterval=30 -o ServerAliveCountMax=6 ${SERVER_USER}@${SERVER_IP} << EOF
  cd ${SERVER_DIR}

  chmod +x setup-server.sh dns-config.sh
  sudo ./dns-config.sh
  sudo ./setup-server.sh

  echo "-> Loading App Docker Image (requires sudo)..."
  sudo docker load < images/${APP_NAME}.tar

  echo "-> Starting Containers..."
  sudo docker compose down
  sudo docker compose up -d

  echo "-> Setting up Static Files and Nginx configuration..."
  sudo mkdir -p /var/www/aispeaking.ir/html

  # انتقال فایل‌های استاتیک به مسیر روت وب‌سرور
  if [ -d "static" ]; then
      sudo cp -r static/* /var/www/aispeaking.ir/html/
  fi

  sudo cp nginx/application.conf /etc/nginx/conf.d/aispeaking.conf
  sudo nginx -t
  sudo systemctl restart nginx

  echo "-> Cleaning up old dangling images..."
  sudo docker image prune -f
EOF

echo "================================================="
echo " Deployment completed successfully! 🎉 "
echo "================================================="