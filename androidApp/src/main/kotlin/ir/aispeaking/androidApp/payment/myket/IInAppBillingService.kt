package ir.aispeaking.androidApp.payment.myket

import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel

interface IInAppBillingService : IInterface {
    fun isBillingSupported(apiVersion: Int, packageName: String, type: String): Int
    fun getBuyIntent(apiVersion: Int, packageName: String, sku: String, type: String, developerPayload: String?): Bundle?
    fun getPurchases(apiVersion: Int, packageName: String, type: String, continuationToken: String?): Bundle?
    fun consumePurchase(apiVersion: Int, packageName: String, purchaseToken: String): Int

    class Stub(private val binder: IBinder) : IInAppBillingService {
        override fun asBinder(): IBinder = binder

        override fun isBillingSupported(apiVersion: Int, packageName: String, type: String): Int {
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            return try {
                data.writeInterfaceToken(DESCRIPTOR)
                data.writeInt(apiVersion)
                data.writeString(packageName)
                data.writeString(type)
                binder.transact(TRANSACTION_isBillingSupported, data, reply, 0)
                reply.readException()
                reply.readInt()
            } finally {
                reply.recycle()
                data.recycle()
            }
        }

        override fun getBuyIntent(
            apiVersion: Int,
            packageName: String,
            sku: String,
            type: String,
            developerPayload: String?
        ): Bundle? {
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            return try {
                data.writeInterfaceToken(DESCRIPTOR)
                data.writeInt(apiVersion)
                data.writeString(packageName)
                data.writeString(sku)
                data.writeString(type)
                data.writeString(developerPayload)
                binder.transact(TRANSACTION_getBuyIntent, data, reply, 0)
                reply.readException()
                if (reply.readInt() != 0) Bundle.CREATOR.createFromParcel(reply) else null
            } finally {
                reply.recycle()
                data.recycle()
            }
        }

        override fun getPurchases(
            apiVersion: Int,
            packageName: String,
            type: String,
            continuationToken: String?
        ): Bundle? {
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            return try {
                data.writeInterfaceToken(DESCRIPTOR)
                data.writeInt(apiVersion)
                data.writeString(packageName)
                data.writeString(type)
                data.writeString(continuationToken)
                binder.transact(TRANSACTION_getPurchases, data, reply, 0)
                reply.readException()
                if (reply.readInt() != 0) Bundle.CREATOR.createFromParcel(reply) else null
            } finally {
                reply.recycle()
                data.recycle()
            }
        }

        override fun consumePurchase(apiVersion: Int, packageName: String, purchaseToken: String): Int {
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            return try {
                data.writeInterfaceToken(DESCRIPTOR)
                data.writeInt(apiVersion)
                data.writeString(packageName)
                data.writeString(purchaseToken)
                binder.transact(TRANSACTION_consumePurchase, data, reply, 0)
                reply.readException()
                reply.readInt()
            } finally {
                reply.recycle()
                data.recycle()
            }
        }

        companion object {
            private const val DESCRIPTOR = "com.android.vending.billing.IInAppBillingService"
            private const val TRANSACTION_isBillingSupported = IBinder.FIRST_CALL_TRANSACTION + 0
            private const val TRANSACTION_getBuyIntent = IBinder.FIRST_CALL_TRANSACTION + 2
            private const val TRANSACTION_getPurchases = IBinder.FIRST_CALL_TRANSACTION + 3
            private const val TRANSACTION_consumePurchase = IBinder.FIRST_CALL_TRANSACTION + 4

            fun asInterface(binder: IBinder?): IInAppBillingService? {
                if (binder == null) return null
                val iin = binder.queryLocalInterface(DESCRIPTOR)
                if (iin is IInAppBillingService) return iin
                return Stub(binder)
            }
        }
    }
}
