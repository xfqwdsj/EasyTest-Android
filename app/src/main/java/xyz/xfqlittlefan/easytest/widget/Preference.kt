package xyz.xfqlittlefan.easytest.widget

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import xyz.xfqlittlefan.easytest.theme.Black
import xyz.xfqlittlefan.easytest.theme.colors

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
                    Text(
                        modifier = Modifier
                            .padding(start = 72.dp, top = 20.dp, bottom = 20.dp)
                            .wrapContentHeight(),
                        text = title,
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.subtitle2
                    )
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

class PreferenceCategoryScope(private val itemList: MutableList<@Composable () -> Unit>, context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun editPreference(
        icon: ImageVector? = null,
        key: String,
        title: String,
        summary: String? = null,
        enabled: Boolean = true,
        defaultValue: String = ""
    ) {
        itemList.add {
            var showingDialog by remember { mutableStateOf(false) }
            Surface(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        showingDialog = true
                    }
            ) {
                if (showingDialog) {
                    var value by remember { mutableStateOf(sharedPreferences.getString(key, defaultValue) ?: defaultValue) }
                    AlertDialog(
                        onDismissRequest = { showingDialog = false },
                        title = { Text(text = title) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                TextField(
                                    value = value,
                                    onValueChange = { value = it }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                sharedPreferences.edit().putString(key, value).apply()
                                showingDialog = false
                            }) {
                                Text(text = stringResource(id = android.R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showingDialog = false
                            }) {
                                Text(text = stringResource(id = android.R.string.cancel))
                            }
                        }
                    )
                }
                CompositionLocalProvider(
                    values = arrayOf(LocalContentAlpha provides if (enabled) ContentAlpha.high else ContentAlpha.disabled)
                ) {
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
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .size(32.dp)
                            )
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

    @OptIn(ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
    fun colorPickerPreference(
        icon: ImageVector? = null,
        key: String,
        title: String,
        summary: String? = null,
        enabled: Boolean = true,
        defaultValue: String = "Blue",
        colors: List<String> =
            listOf(
                "Red",
                "Pink",
                "Purple",
                "DeepPurple",
                "Indigo",
                "Blue",
                "LightBlue",
                "Cyan",
                "Teal",
                "Green",
                "LightGreen",
                "Lime",
                "Yellow",
                "Amber",
                "Orange",
                "DeepOrange",
                "Brown",
                "Gray",
                "BlueGray"
            )
    ) {
        itemList.add {
            var showingDialog by remember { mutableStateOf(false) }
            Surface(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        showingDialog = true
                    }
            ) {
                var value by remember { mutableStateOf(sharedPreferences.getString(key, defaultValue) ?: defaultValue) }
                if (showingDialog) {
                    AlertDialog(
                        onDismissRequest = { showingDialog = false },
                        title = { Text(text = title) },
                        text = {
                            LazyVerticalGrid(
                                cells = GridCells.Adaptive(minSize = 68.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(colors) {
                                    Box(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                value = it
                                            }
                                    ) {
                                        Spacer(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(color = colors(key = it, dark = isSystemInDarkTheme()).primary)
                                        )
                                        AnimatedVisibility(
                                            visible = value == it,
                                            modifier = Modifier.matchParentSize(),
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = stringResource(android.R.string.ok),
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .padding(10.dp),
                                                    tint = MaterialTheme.colors.surface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                sharedPreferences.edit().putString(key, value).apply()
                                showingDialog = false
                            }) {
                                Text(text = stringResource(id = android.R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                value = sharedPreferences.getString(key, defaultValue) ?: defaultValue
                                showingDialog = false
                            }) {
                                Text(text = stringResource(id = android.R.string.cancel))
                            }
                        }
                    )
                }
                CompositionLocalProvider(
                    values = arrayOf(LocalContentAlpha provides if (enabled) ContentAlpha.high else ContentAlpha.disabled)
                ) {
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
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .size(32.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, style = MaterialTheme.typography.subtitle1)
                            if (summary != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = summary, style = MaterialTheme.typography.subtitle2)
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .padding(20.dp)
                                .size(32.dp)
                                .background(
                                    color = animateColorAsState(targetValue = colors(key = value, dark = isSystemInDarkTheme()).primary).value,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}