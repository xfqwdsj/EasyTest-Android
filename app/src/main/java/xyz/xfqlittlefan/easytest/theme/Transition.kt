package xyz.xfqlittlefan.easytest.theme

import androidx.compose.animation.*

@ExperimentalAnimationApi
val fade = fadeIn() with fadeOut()

@ExperimentalAnimationApi
val slideUp = slideInVertically { it } + fadeIn() with slideOutVertically { -it } + fadeOut()

@ExperimentalAnimationApi
val slideDown = slideInVertically { -it } + fadeIn() with slideOutVertically { it } + fadeOut()

val slideInUp = slideInVertically { it }

val slideInDown = slideInVertically { -it }

val slideOutUp = slideOutVertically { -it }

val slideOutDown = slideOutVertically { it }

@ExperimentalAnimationApi
fun expand(expand: Boolean) = if (expand) slideDown else slideUp