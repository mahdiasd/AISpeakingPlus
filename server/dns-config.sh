#!/bin/bash
# dns-config.sh
set -e

echo "=> Configuring DNS with ArvanCloud and Google..."

RESOLVED_CONF="/etc/systemd/resolved.conf"

# بک‌آپ گرفتن از فایل اورجینال در صورت عدم وجود
if [ ! -f "${RESOLVED_CONF}.bak" ]; then
    cp "$RESOLVED_CONF" "${RESOLVED_CONF}.bak"
fi

# اعمال DNS های آروان‌کلود و گوگل
sed -i 's/^#*DNS=.*/DNS=217.218.127.127 217.218.155.155 8.8.8.8/' "$RESOLVED_CONF"
sed -i 's/^#*FallbackDNS=.*/FallbackDNS=1.1.1.1 8.8.4.4/' "$RESOLVED_CONF"

# راه‌اندازی مجدد سرویس برای اعمال تغییرات
systemctl restart systemd-resolved

echo "=> DNS configuration applied successfully."
resolvectl status | grep 'DNS Servers' -A 2