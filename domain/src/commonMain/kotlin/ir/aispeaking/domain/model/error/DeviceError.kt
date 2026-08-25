package ir.aispeaking.domain.model.error

sealed class DeviceError(open val message: String) : AppError {

    data object Unexpected : DeviceError(message = "لطفا ابتدا وارد حساب کاربری خود شوید.")

    data object NotFound : DeviceError(message = "چیزی یافت نشد!")

    data object NotAuthenticated : DeviceError(message = "لطفا ابتدا وارد حساب کاربری خود شوید.")

    data object MetadataNotFound : DeviceError("تو اطلاعات دریافتی مشکل داریم. لطفا با پشتیبانی تماس بگیرید!")
}