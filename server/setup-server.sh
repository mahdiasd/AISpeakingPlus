#!/bin/bash
# setup-server.sh
set -e

echo "=> Fixing Ubuntu APT mirrors for Iran network..."
# تغییر مخازن اوبونتو ۲۴.۰۴ به سرورهای ایران برای دور زدن محدودیت‌های دانلود پکیج
if [ -f "/etc/apt/sources.list.d/ubuntu.sources" ]; then
    sed -i 's/archive.ubuntu.com/ir.archive.ubuntu.com/g' /etc/apt/sources.list.d/ubuntu.sources
    sed -i 's/security.ubuntu.com/ir.archive.ubuntu.com/g' /etc/apt/sources.list.d/ubuntu.sources
fi

# بک‌فال برای نسخه‌های قدیمی‌تر اوبونتو (محض اطمینان)
if [ -f "/etc/apt/sources.list" ]; then
    sed -i 's/archive.ubuntu.com/ir.archive.ubuntu.com/g' /etc/apt/sources.list
    sed -i 's/security.ubuntu.com/ir.archive.ubuntu.com/g' /etc/apt/sources.list
fi

echo "=> Checking dependencies..."

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

INSTALL_NEEDED=false

# بررسی نصب بودن Docker و پلاگین Docker Compose
if command_exists docker && docker compose version >/dev/null 2>&1; then
    echo "=> Docker and Docker Compose are already installed. Skipping..."
else
    echo "=> Docker is missing. Installing from native Ubuntu repositories..."
    INSTALL_NEEDED=true
    apt-get update
    apt-get install -y docker.io docker-compose-v2
fi

# بررسی نصب بودن Nginx
if command_exists nginx; then
    echo "=> Nginx is already installed. Skipping..."
else
    echo "=> Nginx is missing. Installing..."
    if [ "$INSTALL_NEEDED" = false ]; then apt-get update; fi
    apt-get install -y nginx
    systemctl enable nginx
    systemctl start nginx
fi

# تنظیم Mirror داکر (رانفلر به عنوان اصلی)
DAEMON_JSON="/etc/docker/daemon.json"
echo "=> Applying Docker Mirror configurations..."

mkdir -p /etc/docker
cat <<EOF > "$DAEMON_JSON"
{
  "registry-mirrors": [
    "https://mirror-docker.runflare.com",
    "https://docker.arvancloud.ir",
    "https://registry.docker.ir"
  ]
}
EOF

systemctl daemon-reload
systemctl restart docker

echo "=> Server setup completed."