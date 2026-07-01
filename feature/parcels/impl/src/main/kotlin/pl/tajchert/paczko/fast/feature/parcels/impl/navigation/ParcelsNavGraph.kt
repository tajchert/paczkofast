package pl.tajchert.paczko.fast.feature.parcels.impl.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelCollectRoute
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelDetailRoute
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelListRoute
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.ParcelDetailScreen
import pl.tajchert.paczko.fast.feature.parcels.impl.list.ParcelListScreen

fun EntryProviderScope<NavKey>.parcelEntries(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
) {
    entry<ParcelListRoute> {
        ParcelListScreen(
            onParcelClick = { shipmentNumber ->
                onNavigate(ParcelDetailRoute(shipmentNumber = shipmentNumber))
            },
        )
    }

    entry<ParcelDetailRoute> { route ->
        ParcelDetailScreen(
            route = route,
            onBack = onBack,
            onCollect = {
                onNavigate(ParcelCollectRoute(shipmentNumber = route.shipmentNumber))
            },
        )
    }

    entry<ParcelCollectRoute> { route ->
        ParcelCollectPlaceholderScreen(
            shipmentNumber = route.shipmentNumber,
            onBack = onBack,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParcelCollectPlaceholderScreen(
    shipmentNumber: String,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Collect parcel") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Collect flow is coming next.",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = shipmentNumber,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            Button(onClick = onBack) {
                Text(text = "Back")
            }
        }
    }
}
