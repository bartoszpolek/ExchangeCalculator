package com.example.exchange.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exchange.core.designsystem.R
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.core.designsystem.tokens.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    sheetState: SheetState,
    title: String,
    closeContentDescription: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        shape = BottomSheetShape,
        containerColor = MaterialTheme.colorScheme.background,
        scrimColor = MaterialTheme.colorScheme.scrim,
        dragHandle = { AppBottomSheetDragHandle() },
    ) {
        AppBottomSheetContent(
            title = title,
            closeContentDescription = closeContentDescription,
            onClose = onDismissRequest,
            content = content,
        )
    }
}

@Composable
private fun AppBottomSheetContent(
    title: String,
    closeContentDescription: String,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        AppBottomSheetHeader(
            title = title,
            closeContentDescription = closeContentDescription,
            onClose = onClose,
        )
        AppBottomSheetContentCard(content = content)
    }
}

@Composable
private fun AppBottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm, bottom = Spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(DragHandleWidth)
                .height(DragHandleHeight)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = DragHandleAlpha,
                    ),
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun AppBottomSheetHeader(
    title: String,
    closeContentDescription: String,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = closeContentDescription,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun AppBottomSheetContentCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(content = content)
    }
}

@Preview(name = "AppBottomSheet", showBackground = true, backgroundColor = 0xFF888888)
@Composable
private fun AppBottomSheetPreview() {
    ExchangeTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = BottomSheetShape,
        ) {
            AppBottomSheetContent(
                title = "Bottom sheet",
                closeContentDescription = "Close",
                onClose = {},
            ) {
                Text(
                    text = "First item",
                    modifier = Modifier.padding(Spacing.lg),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Second item",
                    modifier = Modifier.padding(Spacing.lg),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

private val BottomSheetShape: Shape
    @Composable
    get() = MaterialTheme.shapes.extraLarge.copy(
        bottomStart = ZeroCornerSize,
        bottomEnd = ZeroCornerSize,
    )

private val DragHandleWidth = 36.dp
private val DragHandleHeight = 5.dp
private const val DragHandleAlpha = 0.30f
