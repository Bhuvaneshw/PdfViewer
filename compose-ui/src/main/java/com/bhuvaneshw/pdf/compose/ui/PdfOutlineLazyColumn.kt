package com.bhuvaneshw.pdf.compose.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.bhuvaneshw.pdf.model.SideBarTreeItem

/**
 * A Composable that displays a tree-like structure in a lazy-loading list, ideal for navigation
 * outlines in a PDF viewer.
 *
 * @param items The list of [SideBarTreeItem]s to display at the root level.
 * @param onItemClick A lambda that's invoked when a leaf item is clicked.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param title An optional title to be displayed at the top of the list.
 * @param contentColor The color for the text and icons within the list.
 * @param arrowResId An optional drawable resource for the expand/collapse arrow icon.
 */
@Composable
fun PdfOutlineLazyColumn(
    items: List<SideBarTreeItem>,
    onItemClick: (SideBarTreeItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    contentColor: Color? = null,
    @DrawableRes arrowResId: Int? = null,
) {
    var expandedState by remember { mutableStateOf(mapOf<String, Boolean>()) }

    val flatList = remember(items, expandedState) {
        flattenTree(items, 0, expandedState)
    }

    Column(modifier = modifier) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp),
            )
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(flatList, key = { it.item.id }) { node ->
                OutlineRow(
                    node = node,
                    arrowResId = arrowResId,
                    onToggle = {
                        val id = node.item.id
                        val state = expandedState.toMutableMap()

                        state[id] = !(state[id] ?: false)
                        expandedState = state
                    },
                    onItemClick = onItemClick,
                    contentColor = contentColor,
                )
            }
        }
    }
}

@Composable
internal fun OutlineRow(
    node: OutlineNode,
    arrowResId: Int?,
    onToggle: () -> Unit,
    onItemClick: (SideBarTreeItem) -> Unit,
    contentColor: Color? = null,
) {
    val rotation by animateFloatAsState(
        if (node.isExpanded) 0f else -90f,
        label = "arrowAnim"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (node.item.children.isNotEmpty()) onToggle()
                else onItemClick(node.item)
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .padding(start = (node.depth * 16).dp)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(
                arrowResId ?: R.drawable.outline_arrow_drop_down_24
            ),
            contentDescription = null,
            modifier = Modifier.graphicsLayer {
                if (arrowResId == null) {
                    rotationZ = rotation
                    alpha = if (node.item.children.isEmpty()) 0f else 1f
                }
            },
            tint = contentColor ?: Color.Unspecified
        )

        Spacer(Modifier.width(4.dp))
        Text(
            text = node.item.title ?: "",
            color = contentColor ?: Color.Unspecified
        )
    }
}
