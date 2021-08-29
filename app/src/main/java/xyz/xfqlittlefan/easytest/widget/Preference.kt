package xyz.xfqlittlefan.easytest.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import xyz.xfqlittlefan.easytest.theme.Black

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
    content: @Composable PreferenceScope.() -> Unit
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

    fun getLazyListScope(scope: @Composable PreferenceScope.() -> Unit): LazyListScope.() -> Unit

    fun category(title: String, items: PreferenceCategoryScope.() -> Unit)

}

class MaterialPreferenceScope : PreferenceScope {
    private val itemList = mutableListOf<@Composable () -> Unit>()

    override fun getLazyListScope(scope: @Composable PreferenceScope.() -> Unit): LazyListScope.() -> Unit {
        TODO("Not yet implemented")
    }

    override fun category(title: String, items: PreferenceCategoryScope.() -> Unit) {
        itemList.apply {
            if (itemList.isNotEmpty()) {
                add {
                    Spacer(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(color = Black.copy(alpha = 0.1f))
                    )
                }
            }
            add {
                Surface(
                    modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp, end = 10.dp)
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ) {
                    Text(
                        text = title,
                        modifier = Modifier
                            .padding(start = 72.dp)
                            .wrapContentSize(),
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.subtitle2
                    )
                }
            }
        }
        items(PreferenceCategoryScope(itemList))
        itemList.add {
            Spacer(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    .background(
                        color = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                    )
            )
        }
    }

}

class PreferenceCategoryScope(private val itemList: MutableList<@Composable () -> Unit>) {

    fun editPreference(icon: ImageVector? = null, key: String, title: String, summary: String? = null, enabled: Boolean = true, defaultValue: String) {
        itemList.add {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .clickable {

                    }
            ) {

            }
        }
    }

}