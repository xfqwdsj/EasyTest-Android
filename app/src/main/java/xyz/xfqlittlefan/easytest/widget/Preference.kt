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
import androidx.compose.foundation.shape.ZeroCornerSize
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

@DslMarker
annotation class PreferenceMarker

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

@PreferenceMarker
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
                            .padding(horizontal = 5.dp)
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
                    shape = MaterialTheme.shapes.medium.copy(bottomEnd = ZeroCornerSize, bottomStart = ZeroCornerSize)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 72.dp, top = 20.dp, bottom = 20.dp, end = 20.dp)
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
                        shape = MaterialTheme.shapes.medium.copy(topStart = ZeroCornerSize, topEnd = ZeroCornerSize)
                    )
            )
        }
    }

}

@PreferenceMarker
class PreferenceCategoryScope(private val itemList: MutableList<@Composable () -> Unit>, context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @OptIn(ExperimentalAnimationApi::class)
    fun edit(
        icon: ImageVector? = null,
        key: String,
        title: String,
        summary: String? = null,
        enabled: Boolean = true,
        defaultValue: String = ""
    ) {
        itemList.add {
            var showed by remember { mutableStateOf(false) }
            Surface(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        showed = true
                    }
            ) {
                if (showed) {
                    var value by remember { mutableStateOf(sharedPreferences.getString(key, defaultValue) ?: defaultValue) }
                    val onDismiss = { showed = false }
                    Dialog(
                        title = title,
                        onDismissRequest = onDismiss,
                        onConfirm = {
                            sharedPreferences.edit().putString(key, value).apply()
                            onDismiss()
                        },
                        onDismiss = onDismiss
                    ) {
                        TextField(
                            value = value,
                            onValueChange = { value = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
                            AnimatedContent(
                                targetState = title,
                                transitionSpec = { fadeIn() with fadeOut() }
                            ) {
                                Text(text = it, style = MaterialTheme.typography.subtitle1)
                            }
                            if (summary != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                AnimatedContent(
                                    targetState = summary,
                                    transitionSpec = { fadeIn() with fadeOut() }
                                ) {
                                    Text(text = it, style = MaterialTheme.typography.subtitle2)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
    fun colorPicker(
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
            ),
        onColorChange: (String) -> Unit = { }
    ) {
        itemList.add {
            var showed by remember { mutableStateOf(false) }
            Surface(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        showed = true
                    }
            ) {
                var value by remember { mutableStateOf(sharedPreferences.getString(key, defaultValue) ?: defaultValue) }
                if (showed) {
                    Dialog(
                        title = title,
                        onDismissRequest = {
                            showed = false
                        }
                    ) {
                        LazyVerticalGrid(
                            cells = GridCells.Adaptive(minSize = 68.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                        ) {
                            items(colors) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                value = it
                                                sharedPreferences
                                                    .edit()
                                                    .putString(key, value)
                                                    .apply()
                                                showed = false
                                                onColorChange(value)
                                            }
                                    ) {
                                        Spacer(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(color = colors(key = it, dark = !MaterialTheme.colors.isLight, system = isSystemInDarkTheme()).primary)
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
                        }
                    }
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
                            AnimatedContent(
                                targetState = title,
                                transitionSpec = { fadeIn() with fadeOut() }
                            ) {
                                Text(text = it, style = MaterialTheme.typography.subtitle1)
                            }
                            if (summary != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                AnimatedContent(
                                    targetState = summary,
                                    transitionSpec = { fadeIn() with fadeOut() }
                                ) {
                                    Text(text = it, style = MaterialTheme.typography.subtitle2)
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .padding(20.dp)
                                .size(32.dp)
                                .background(
                                    color = animateColorAsState(targetValue = colors(key = value, dark = !MaterialTheme.colors.isLight, system = isSystemInDarkTheme()).primary).value,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    fun switch(
        icon: ImageVector? = null,
        key: String,
        title: String,
        summary: String? = null,
        enabled: Boolean = true,
        defaultValue: Boolean = false,
        onCheckedChange: (Boolean) -> Unit = { }
    ) {
        itemList.add {
            var value by remember { mutableStateOf(sharedPreferences.getBoolean(key, defaultValue)) }
            Surface(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        value = !value
                        sharedPreferences
                            .edit()
                            .putBoolean(key, value)
                            .apply()
                        onCheckedChange(value)
                    }
            ) {
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
                            AnimatedContent(
                                targetState = title,
                                transitionSpec = { fadeIn() with fadeOut() }
                            ) {
                                Text(text = it, style = MaterialTheme.typography.subtitle1)
                            }
                            if (summary != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                AnimatedContent(
                                    targetState = summary,
                                    transitionSpec = { fadeIn() with fadeOut() }
                                ) {
                                    Text(text = it, style = MaterialTheme.typography.subtitle2)
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(20.dp)
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Switch(checked = value, onCheckedChange = null)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    fun menu(
        icon: ImageVector? = null,
        key: String,
        title: String,
        enabled: Boolean = true,
        defaultValue: Int = 0,
        items: List<String>,
        onSelectedChange: (Int) -> Unit = { }
    ) {
        itemList.add {
            var value by remember { mutableStateOf(sharedPreferences.getInt(key, defaultValue)) }
            var expanded by remember { mutableStateOf(false) }
            var summary by remember { mutableStateOf(items[value]) }
            Surface(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        expanded = true
                    }
            ) {
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
                            AnimatedContent(
                                targetState = title,
                                transitionSpec = { fadeIn() with fadeOut() }
                            ) {
                                Text(text = it, style = MaterialTheme.typography.subtitle1)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            AnimatedContent(
                                targetState = summary,
                                transitionSpec = { fadeIn() with fadeOut() }
                            ) {
                                Text(text = it, style = MaterialTheme.typography.subtitle2)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                items.forEachIndexed { index, text ->
                                    DropdownMenuItem(onClick = {
                                        expanded = false
                                        value = index
                                        summary = items[index]
                                        sharedPreferences.edit().putInt(key, value).apply()
                                        onSelectedChange(value)
                                    }) {
                                        Text(text = text)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                    }
                }
            }
        }
    }
}