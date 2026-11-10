package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidColors
import com.example.ui.theme.liquidGlass

@Composable
fun MarkdownViewer(
    content: String,
    onToggleCheckbox: (lineIndex: Int, isChecked: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val lines = content.lines()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var inCodeBlock = false
        val codeBuffer = StringBuilder()

        lines.forEachIndexed { index, rawLine ->
            val trimmed = rawLine.trim()

            // Handle Code block fence
            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    // Close code block
                    val codeContent = codeBuffer.toString().trimEnd()
                    CodeBlockView(code = codeContent, onCopy = {
                        clipboardManager.setText(AnnotatedString(codeContent))
                    })
                    codeBuffer.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                return@forEachIndexed
            }

            if (inCodeBlock) {
                codeBuffer.append(rawLine).append("\n")
                return@forEachIndexed
            }

            // Headers
            when {
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# ").trim(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                // Checkbox / Todo item
                trimmed.startsWith("[ ]") || trimmed.startsWith("[x]") -> {
                    val isChecked = trimmed.startsWith("[x]")
                    val taskText = trimmed.substring(3).trim()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onToggleCheckbox(index, !isChecked)
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isChecked) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineMarkdown(taskText),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                                color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
                // Bullet point
                trimmed.startsWith("- ") || trimmed.startsWith("• ") -> {
                    val bulletText = trimmed.substring(2).trim()
                    Row(
                        modifier = Modifier.padding(start = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineMarkdown(bulletText),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                // Blockquote
                trimmed.startsWith("> ") -> {
                    val quoteText = trimmed.removePrefix("> ").trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(
                                shape = RoundedCornerShape(12.dp),
                                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                elevation = 1.dp
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = quoteText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                // Horizontal divider
                trimmed == "---" -> {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                // Empty line
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                // Standard text
                else -> {
                    Text(
                        text = parseInlineMarkdown(rawLine),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(
    code: String,
    onCopy: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.92f),
                elevation = 4.dp
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CODE SNIPPET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 1.sp
                )
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = Color(0xFFF1F5F9),
                lineHeight = 18.sp
            )
        }
    }
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            // Bold **text**
            val boldStart = text.indexOf("**", cursor)
            if (boldStart != -1) {
                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd != -1) {
                    append(text.substring(cursor, boldStart))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(boldStart + 2, boldEnd))
                    }
                    cursor = boldEnd + 2
                    continue
                }
            }

            // Italic *text*
            val italicStart = text.indexOf("*", cursor)
            if (italicStart != -1) {
                val italicEnd = text.indexOf("*", italicStart + 1)
                if (italicEnd != -1) {
                    append(text.substring(cursor, italicStart))
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(italicStart + 1, italicEnd))
                    }
                    cursor = italicEnd + 1
                    continue
                }
            }

            // Code `inline`
            val codeStart = text.indexOf("`", cursor)
            if (codeStart != -1) {
                val codeEnd = text.indexOf("`", codeStart + 1)
                if (codeEnd != -1) {
                    append(text.substring(cursor, codeStart))
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x2264748B)
                        )
                    ) {
                        append(text.substring(codeStart + 1, codeEnd))
                    }
                    cursor = codeEnd + 1
                    continue
                }
            }

            // Highlight <mark>text</mark>
            val markStart = text.indexOf("<mark", cursor)
            if (markStart != -1) {
                val tagClose = text.indexOf(">", markStart)
                val markEnd = text.indexOf("</mark>", tagClose + 1)
                if (tagClose != -1 && markEnd != -1) {
                    append(text.substring(cursor, markStart))
                    withStyle(SpanStyle(background = LiquidColors.HighlightYellow)) {
                        append(text.substring(tagClose + 1, markEnd))
                    }
                    cursor = markEnd + 7
                    continue
                }
            }

            // Remainder
            append(text.substring(cursor))
            break
        }
    }
}
