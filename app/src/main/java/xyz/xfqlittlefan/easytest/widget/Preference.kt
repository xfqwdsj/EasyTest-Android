package xyz.xfqlittlefan.easytest.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import xyz.xfqlittlefan.easytest.theme.Black

@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class PreferenceItem

@Composable
fun PreferenceContainer(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    content: PreferenceScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        content = MaterialPreferenceScope().getLazyListScope(content)
    )
}

interface PreferenceScope {

    fun getLazyListScope(scope: PreferenceScope.() -> Unit): LazyListScope.() -> Unit

    fun category(title: String, items: @PreferenceItem PreferenceScope.() -> Unit)

    @PreferenceItem
    fun editPreference(icon: ImageVector, key: String, title: String, summary: String, enabled: Boolean, defaultValue: String)

}

private class MaterialPreferenceScope : PreferenceScope {
    private val itemList = mutableListOf<@Composable () -> Unit>()

    override fun getLazyListScope(scope: PreferenceScope.() -> Unit): LazyListScope.() -> Unit {
        TODO("Not yet implemented")
    }

    override fun category(title: String, items: PreferenceScope.() -> Unit) {
        itemList.apply {
            add {
                if (itemList.isNotEmpty()) {
                    Spacer(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(color = Black.copy(alpha = 0.1f))
                    )
                }
            }

        }
    }

    override fun editPreference(icon: ImageVector, key: String, title: String, summary: String, enabled: Boolean, defaultValue: String) {
        TODO("Not yet implemented")
    }

}