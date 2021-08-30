package xyz.xfqlittlefan.easytest.widget

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import xyz.xfqlittlefan.easytest.theme.Black

@Composable
fun PreferenceContainer(
    modifier: Modifier = Modifier,
    context: Context,
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
        content = PreferenceScope(context).apply(content).getLazyListScope()
    )
}

class PreferenceScope(private val context: Context) {
    private val itemList = mutableListOf<@Composable () -> Unit>()

    fun getLazyListScope(): LazyListScope.() -> Unit = {
        items(itemList) { it() }
    }

    fun category(title: String, items: PreferenceCategoryScope.() -> Unit) {
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
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = 72.dp, top = 20.dp, bottom = 20.dp)
                            .wrapContentSize()
                    ) {
                        Text(
                            text = title,
                            color = MaterialTheme.colors.primary,
                            style = MaterialTheme.typography.subtitle2
                        )
                    }
                }
            }
        }
        items(PreferenceCategoryScope(itemList, context))
        itemList.add {
            Spacer(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(
                        color = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                    )
            )
        }
    }

}

class PreferenceCategoryScope(private val itemList: MutableList<@Composable () -> Unit>, private val context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun editPreference(icon: ImageVector? = null, key: String, title: String, summary: String? = null, enabled: Boolean = true, defaultValue: String) {
        itemList.add {
            var showingDialog by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {

                    }
            ) {
                if (showingDialog) {
                    AlertDialog(
                        onDismissRequest = { showingDialog = false },
                        title = { Text(text = title) },
                        text = {

                        },
                        confirmButton = {

                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon == null) {
                        Spacer(
                            modifier = Modifier
                                .padding(20.dp)
                                .size(32.dp)
                        )
                    } else {
                        Icon(imageVector = icon, contentDescription = title)
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(text = title, style = MaterialTheme.typography.subtitle1)
                        if (summary != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = summary, style = MaterialTheme.typography.subtitle2)
                        }
                    }
                }
            }
        }
    }

}