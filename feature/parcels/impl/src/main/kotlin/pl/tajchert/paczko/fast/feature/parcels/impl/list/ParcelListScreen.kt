package pl.tajchert.paczko.fast.feature.parcels.impl.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastEmptyState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastErrorState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastTopAppBar
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelMetadataLines

@Composable
fun ParcelListScreen(
    onParcelClick: (shipmentNumber: String) -> Unit,
    viewModel: ParcelListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    ParcelListContent(
        uiState = uiState,
        onParcelClick = onParcelClick,
        onRefreshClick = viewModel::refresh,
    )
}

@Composable
private fun ParcelListContent(
    uiState: ParcelListUiState,
    onParcelClick: (shipmentNumber: String) -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PaczkofastTopAppBar(
                title = "Paczkofast",
                actions = {
                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !uiState.isRefreshing,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh parcels",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (uiState.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.parcels.isNotEmpty() && uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            when {
                uiState.parcels.isNotEmpty() -> ParcelList(
                    parcels = uiState.parcels,
                    onParcelClick = onParcelClick,
                )

                uiState.errorMessage != null -> PaczkofastErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRefreshClick,
                    modifier = Modifier.fillMaxSize(),
                )

                else -> PaczkofastEmptyState(
                    icon = Icons.Outlined.Inbox,
                    title = "No parcels",
                    description = "Your tracked parcels will appear here.",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ParcelList(
    parcels: List<Parcel>,
    onParcelClick: (shipmentNumber: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = parcels,
            key = { parcel -> parcel.shipmentNumber },
        ) { parcel ->
            ParcelCard(
                parcel = parcel,
                onClick = { onParcelClick(parcel.shipmentNumber) },
            )
        }
    }
}

@Composable
private fun ParcelCard(
    parcel: Parcel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = parcel.shipmentNumber,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = parcel.status,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = parcel.pickupPoint?.name ?: "Pickup point pending",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            parcel.expiryDate?.let { expiryDate ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Expires: $expiryDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            parcelMetadataLines(parcel).forEach { metadataLine ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = metadataLine,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
