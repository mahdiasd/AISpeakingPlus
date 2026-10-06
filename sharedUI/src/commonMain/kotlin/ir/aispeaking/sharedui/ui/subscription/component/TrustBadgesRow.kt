package ir.aispeaking.sharedui.ui.subscription.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_done
import org.jetbrains.compose.resources.painterResource

@Composable
fun TrustBadgesRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrustBadgeItem(text = "فعال‌سازی آنی")
        TrustBadgeItem(text = "تراکنش امن")
        TrustBadgeItem(text = "پشتیبانی ۲۴ ساعته")
    }
}

@Composable
private fun TrustBadgeItem(
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_done),
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = text,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
        )
    }
}
