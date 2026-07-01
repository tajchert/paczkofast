package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastErrorState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastLoadingIndicator
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastTopAppBar
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.ui.QrCodeImage
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelDetailRoute
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelMetadataLines

@Composable
fun ParcelDetailScreen(
    route: ParcelDetailRoute,
    onBack: () -> Unit,
    onCollect: () -> Unit,
    viewModel: ParcelDetailViewModel = hiltViewModel<ParcelDetailViewModel, ParcelDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(route.shipmentNumber) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ParcelDetailContent(
        uiState = uiState,
        onBack = onBack,
        onCollect = onCollect,
    )
}

@Composable
private fun ParcelDetailContent(
    uiState: ParcelDetailUiState,
    onBack: () -> Unit,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PaczkofastTopAppBar(
                title = "Parcel details",
                onNavigationClick = onBack,
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> PaczkofastLoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            uiState.errorMessage != null -> PaczkofastErrorState(
                message = uiState.errorMessage,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            uiState.parcel == null -> PaczkofastErrorState(
                message = "Parcel not found",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            else -> ParcelDetailBody(
                parcel = uiState.parcel,
                onCollect = onCollect,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun ParcelDetailBody(
    parcel: Parcel,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = parcel.shipmentNumber,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = parcel.status,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        HorizontalDivider()

        DetailSection(title = "Pickup point") {
            DetailRow(label = "Machine", value = parcel.pickupPoint?.name)
            DetailRow(label = "Address", value = parcel.pickupPoint?.addressLine)
            DetailRow(label = "Location", value = parcel.pickupPoint?.locationDescription)
        }

        DetailSection(title = "Dates") {
            DetailRow(label = "Stored", value = parcel.storedDate)
            DetailRow(label = "Expires", value = parcel.expiryDate)
        }

        val metadataLines = parcelMetadataLines(parcel)
        if (metadataLines.isNotEmpty()) {
            DetailSection(title = "Package info") {
                metadataLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        DetailSection(title = "Collection") {
            DetailRow(label = "Open code", value = parcel.openCode)
            parcel.qrCode?.takeIf { it.isNotBlank() }?.let { qrCode ->
                QrCodeImage(payload = qrCode, modifier = Modifier.size(240.dp))
            }
        }

        if (parcel.canCollectRemotely) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onCollect,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Open compartment")
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "Not available",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
