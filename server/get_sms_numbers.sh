
#!/bin/bash

SERVER_IP="37.32.20.173"
SERVER_USER="ubuntu"

echo "⏳ در حال اتصال به سرور و دریافت شماره‌های گروه اول (۴۶ کاربر دارای اشتراک)..."
# گرفتن شماره‌های گروه اول با فرمت کاما با استفاده از string_agg در دیتابیس
ssh $SERVER_USER@$SERVER_IP "cd ~/ai-speaking && docker compose exec -T db psql -U postgres -d ai_speaking -t -A -c \"SELECT string_agg(u.phone_number, ',') FROM (SELECT DISTINCT usr.phone_number FROM purchase p JOIN \\\"user\\\" usr ON p.user_id = usr.id WHERE p.expiry_date > '2026-03-01 00:00:00' AND p.status = 'SUCCESS') u;\"" > group_1_subscribed.txt

echo "⏳ در حال دریافت شماره‌های گروه دوم (۲۵۴ کاربر اخیر بدون اشتراک)..."
# گرفتن ۲۵۴ کاربر فعال اخیر که در گروه اول نیستند
ssh $SERVER_USER@$SERVER_IP "cd ~/ai-speaking && docker compose exec -T db psql -U postgres -d ai_speaking -t -A -c \"SELECT string_agg(u.phone_number, ',') FROM (SELECT phone_number FROM \\\"user\\\" WHERE id NOT IN (SELECT user_id FROM purchase WHERE expiry_date > '2026-03-01 00:00:00' AND status = 'SUCCESS') ORDER BY created_at DESC LIMIT 254) u;\"" > group_2_recent.txt

echo "✅ عملیات با موفقیت انجام شد!"
echo "📂 فایل‌های group_1_subscribed.txt و group_2_recent.txt در همین پوشه ساخته شدند."