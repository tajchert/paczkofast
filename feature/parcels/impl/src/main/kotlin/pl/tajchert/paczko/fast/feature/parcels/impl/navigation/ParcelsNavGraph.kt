package pl.tajchert.paczko.fast.feature.parcels.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelCollectRoute
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelDetailRoute
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelListRoute
import pl.tajchert.paczko.fast.feature.parcels.impl.collect.CollectScreen
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
            onCollectClick = { shipmentNumber ->
                onNavigate(ParcelCollectRoute(shipmentNumber = shipmentNumber))
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
        CollectScreen(
            shipmentNumber = route.shipmentNumber,
            onBack = onBack,
        )
    }
}
