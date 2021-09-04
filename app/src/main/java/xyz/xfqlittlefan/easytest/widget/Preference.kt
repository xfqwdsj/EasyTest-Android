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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.preference.PreferenceManager
import xyz.xfqlittlefan.easytest.theme.Black
import xyz.xfqlittlefan.easytest.theme.colors
import kotlin.math.max

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
                    PreferenceDialog(
                        title = title,
                        onConfirm = {
                            sharedPreferences.edit().putString(key, value).apply()
                            showed = false
                        },
                        onDismiss = onDismiss
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                value = value,
                                onValueChange = { value = it }
                            )
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
        dark: Boolean = false,
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
                    PreferenceDialog(
                        title = title,
                        showButtons = false,
                        onDismiss = {
                            showed = false
                        }
                    ) {
                        LazyVerticalGrid(
                            cells = GridCells.Adaptive(minSize = 68.dp),
                            modifier = Modifier.fillMaxWidth()
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
                                    color = animateColorAsState(targetValue = colors(value, dark, isSystemInDarkTheme()).primary).value,
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

    @Composable
    private fun PreferenceDialog(
        title: String,
        showButtons: Boolean = true,
        onConfirm: () -> Unit = { },
        onDismiss: () -> Unit = { },
        content: @Composable () -> Unit
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(shape = RoundedCornerShape(10.dp)) {
                Column {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .weight(weight = 1f, fill = false)
                    ) { content() }
                    Spacer(modifier = Modifier.height(24.dp))
                    if (showButtons) {
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            AlertDialogFlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 12.dp) {
                                TextButton(onClick = onDismiss) {
                                    Text(text = stringResource(id = android.R.string.cancel))
                                }
                                TextButton(onClick = onConfirm) {
                                    Text(text = stringResource(id = android.R.string.ok))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun AlertDialogFlowRow(
        mainAxisSpacing: Dp,
        crossAxisSpacing: Dp,
        content: @Composable () -> Unit
    ) {
        Layout(content) { measurables, constraints ->
            val sequences = mutableListOf<List<Placeable>>()
            val crossAxisSizes = mutableListOf<Int>()
            val crossAxisPositions = mutableListOf<Int>()

            var mainAxisSpace = 0
            var crossAxisSpace = 0

            val currentSequence = mutableListOf<Placeable>()
            var currentMainAxisSize = 0
            var currentCrossAxisSize = 0

            val childConstraints = Constraints(maxWidth = constraints.maxWidth)

            // Return whether the placeable can be added to the current sequence.
            fun canAddToCurrentSequence(placeable: Placeable) =
                currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() +
                        placeable.width <= constraints.maxWidth

            // Store current sequence information and start a new sequence.
            fun startNewSequence() {
                if (sequences.isNotEmpty()) {
                    crossAxisSpace += crossAxisSpacing.roundToPx()
                }
                sequences += currentSequence.toList()
                crossAxisSizes += currentCrossAxisSize
                crossAxisPositions += crossAxisSpace

                crossAxisSpace += currentCrossAxisSize
                mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

                currentSequence.clear()
                currentMainAxisSize = 0
                currentCrossAxisSize = 0
            }

            for (measurable in measurables) {
                // Ask the child for its preferred size.
                val placeable = measurable.measure(childConstraints)

                // Start a new sequence if there is not enough space.
                if (!canAddToCurrentSequence(placeable)) startNewSequence()

                // Add the child to the current sequence.
                if (currentSequence.isNotEmpty()) {
                    currentMainAxisSize += mainAxisSpacing.roundToPx()
                }
                currentSequence.add(placeable)
                currentMainAxisSize += placeable.width
                currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
            }

            if (currentSequence.isNotEmpty()) startNewSequence()

            val mainAxisLayoutSize = if (constraints.maxWidth != Constraints.Infinity) {
                constraints.maxWidth
            } else {
                max(mainAxisSpace, constraints.minWidth)
            }
            val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

            layout(mainAxisLayoutSize, crossAxisLayoutSize) {
                sequences.forEachIndexed { i, placeables ->
                    val childrenMainAxisSizes = IntArray(placeables.size) { j ->
                        placeables[j].width +
                                if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
                    }
                    val arrangement = Arrangement.Bottom
                    // Handle vertical direction
                    val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }
                    with(arrangement) {
                        arrange(mainAxisLayoutSize, childrenMainAxisSizes, mainAxisPositions)
                    }
                    placeables.forEachIndexed { j, placeable ->
                        placeable.place(
                            x = mainAxisPositions[j],
                            y = crossAxisPositions[i]
                        )
                    }
                }
            }
        }
    }
}