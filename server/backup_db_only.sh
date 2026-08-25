#!/bin/bash

set -o pipefail

CONTAINER_DB_NAME="ai-speaking-db-1"
DB_USER="postgres"
DB_NAME="ai_speaking"
DB_PASSWORD="asdfasWERASDCASDF89845@af"

# مسیر ذخیره سازی
BACKUP_DIR="./db_backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
FILENAME="$BACKUP_DIR/production_backup_$TIMESTAMP.sql.gz"

# ساخت پوشه
mkdir -p "$BACKUP_DIR"

echo "🚀 Starting Production Backup for $DB_NAME at $TIMESTAMP..."

# دستور اصلی بک‌آپ
# تغییرات مهم:
# 1. حذف -t : حیاتی برای سالم ماندن فایل خروجی
# 2. -e PGPASSWORD : برای جلوگیری از پرسیدن پسورد و گیر کردن اسکریپت
docker exec -e PGPASSWORD="$DB_PASSWORD" $CONTAINER_DB_NAME pg_dump -U $DB_USER $DB_NAME | gzip > "$FILENAME"

# بررسی نتیجه
if [ $? -eq 0 ]; then
  echo "✅ Backup Successful: $FILENAME"

  # نمایش حجم فایل برای اطمینان چشمی (اگر فایل چند بایت باشد یعنی خالی است و مشکل دارد)
  FILE_SIZE=$(du -h "$FILENAME" | cut -f1)
  echo "📦 Backup Size: $FILE_SIZE"

  # نگهداری بک‌آپ‌های ۱۰ روز اخیر و حذف قدیمی‌ها
  find "$BACKUP_DIR" -type f -name "*.sql.gz" -mtime +10 -delete
else
  echo "❌ CRITICAL: Backup FAILED! Please check logs."
  # اگر فایل خراب ساخته شده، پاکش کن که اشتباهی استفاده نشود
  rm -f "$FILENAME"
  exit 1
fi