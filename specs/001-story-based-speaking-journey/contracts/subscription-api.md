# API Contract: Subscriptions & Plans

**Version**: `2.0.0`  
**Base URL**: `/api/v2/subscriptions`  

---

## 1. Get Subscription Plans

Fetches all active subscription plans, pricing, durations, and promo discounts.

- **Endpoint**: `GET /api/v2/subscriptions/plans`
- **Authentication**: Public

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Subscription plans retrieved",
  "data": [
    {
      "id": "plan_monthly",
      "type": "MONTHLY",
      "titleFa": "اشتراک ۱ ماهه",
      "durationDays": 30,
      "priceTomans": 199000,
      "discountPercent": 0,
      "badge": null
    },
    {
      "id": "plan_quarterly",
      "type": "QUARTERLY",
      "titleFa": "اشتراک ۳ ماهه",
      "durationDays": 90,
      "priceTomans": 490000,
      "discountPercent": 18,
      "badge": "محبوب‌ترین"
    },
    {
      "id": "plan_biannual",
      "type": "BIANNUAL",
      "titleFa": "اشتراک ۶ ماهه",
      "durationDays": 180,
      "priceTomans": 890000,
      "discountPercent": 25,
      "badge": "بیشترین تخفیف"
    }
  ]
}
```

---

## 2. Check User Subscription Status

Checks whether the authenticated user has an active entitlement to access Stage 3+.

- **Endpoint**: `GET /api/v2/subscriptions/status`
- **Authentication**: `Authorization: Bearer <token>` required.

### Response (200 OK - Active)
```json
{
  "status": 200,
  "message": "Subscription status",
  "data": {
    "isSubscriber": true,
    "planType": "QUARTERLY",
    "expiresAt": "2026-12-18T10:00:00Z",
    "remainingDays": 90
  }
}
```

### Response (200 OK - Inactive / Free)
```json
{
  "status": 200,
  "message": "Subscription status",
  "data": {
    "isSubscriber": false,
    "planType": null,
    "expiresAt": null,
    "remainingDays": 0
  }
}
```
