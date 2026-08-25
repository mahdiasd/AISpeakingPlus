package ir.aispeaking.sharedui.utils.lifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
private fun LifecycleAwareComponent(
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
    onDestroy: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnResume by rememberUpdatedState(newValue = onResume)
    val currentOnPause by rememberUpdatedState(newValue = onPause)
    val currentOnDestroy by rememberUpdatedState(newValue = onDestroy)


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    currentOnResume()
                }

                Lifecycle.Event.ON_PAUSE -> currentOnPause()
                Lifecycle.Event.ON_DESTROY -> currentOnDestroy()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun OnResume(callBack: () -> Unit) {
    val rememberCallBack by rememberUpdatedState(newValue = callBack)
    LifecycleAwareComponent(onResume = rememberCallBack)
}

@Composable
fun OnPause(callBack: () -> Unit) {
    val rememberCallBack by rememberUpdatedState(newValue = callBack)
    LifecycleAwareComponent(onPause = rememberCallBack)
}

@Composable
fun OnDestroy(callBack: () -> Unit) {
    val rememberCallBack by rememberUpdatedState(newValue = callBack)
    LifecycleAwareComponent(onDestroy = rememberCallBack)
}