package com.shivams.mockmate.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun <T> SortControls(
    currentSortCriteria: T,
    sortCriteriaOptions: List<T>,
    onSortCriteriaChange: (T) -> Unit,
    sortAscending: Boolean,
    onSortAscendingChange: (Boolean) -> Unit,
    getDisplayName: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showClearOption: Boolean = false,
    onClearSort: (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = { Text("Sort by") },
    rowArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    trailingSortIcon: @Composable (() -> Unit)? = null,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val isEnabled = enabled && sortCriteriaOptions.isNotEmpty()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = rowArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (isEnabled) expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = getDisplayName(currentSortCriteria),
                onValueChange = {},
                readOnly = true,
                label = label,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                trailingIcon = {
                    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "dropdownRotation")
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(rotation),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                enabled = isEnabled,
                singleLine = true,
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (showClearOption && onClearSort != null) {
                    DropdownMenuItem(
                        text = { Text("Clear Sort") },
                        onClick = { onClearSort(); expanded = false }
                    )
                }
                sortCriteriaOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(getDisplayName(option)) },
                        onClick = {
                            onSortCriteriaChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { onSortAscendingChange(!sortAscending) },
            enabled = isEnabled
        ) {
            trailingSortIcon?.invoke() ?: run {
                Icon(
                    imageVector = if (sortAscending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = if (sortAscending) "Ascending" else "Descending",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
