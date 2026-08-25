#!/bin/bash
set -e

echo "========================================="
echo " 🚀 Starting Server Update Process..."
echo "========================================="

# 1. گرفتن آخرین کدهای پوش شده از گیت‌هاب
echo "📥 Pulling latest changes from Git..."
git pull origin main

# 2. بیلد کردن پروژه روی خود سرور
echo "📦 Building Ktor FatJar..."
./gradlew buildFatJar

# 3. ساخت ایمیج داکر روی سرور (بدون نیاز به انتقال فایل حجیم از مک)
echo "🐳 Building new Docker image..."
docker build -t ai-speaking:latest .

# 4. اعمال تغییرات بدون داون‌تایم طولانی
echo "🔄 Restarting Docker containers..."
docker compose down
docker compose up -d

# 5. پاک کردن ایمیج‌های قدیمی
echo "🧹 Cleaning up old Docker images..."
docker image prune -f

echo "========================================="
echo " ✅ Update Completed Successfully!"
echo "========================================="