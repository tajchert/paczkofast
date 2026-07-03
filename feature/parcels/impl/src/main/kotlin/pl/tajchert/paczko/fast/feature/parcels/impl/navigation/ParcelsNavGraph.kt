package pl.tajchert.paczko.fast.feature.parcels.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.tajchert.paczko.fast.feature.parcels.api.MultiPackageDetailRoute
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelCollectRoute
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelDetailRoute
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelListRoute
import pl.tajchert.paczko.fast.feature.parcels.impl.collect.CollectScreen
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.MultiPackageDetailScreen
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.ParcelDetailScreen
import pl.tajchert.paczko.fast.feature.parcels.impl.list.ParcelListScreen

fun EntryProviderScope<NavKey>.parcelEntries(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    entry<ParcelListRoute> {
        ParcelListScreen(
            onParcelClick = { shipmentNumber ->
                onNavigate(ParcelDetailRoute(shipmentNumber = shipmentNumber))
            },
            onOpenBox = { shipmentNumber ->
                onNavigate(MultiPackageDetailRoute(shipmentNumber = shipmentNumber))
            },
            onCollectClick = { shipmentNumber ->
                onNavigate(ParcelCollectRoute(shipmentNumber = shipmentNumber))
            },
            onOpenSettings = onOpenSettings,
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

    entry<MultiPackageDetailRoute> { route ->
        MultiPackageDetailScreen(
            route = route,
            onBack = onBack,
            onOpenParcel = { shipmentNumber ->
                onNavigate(ParcelDetailRoute(shipmentNumber = shipmentNumber))
            },
            onCollect = { shipmentNumber ->
                onNavigate(ParcelCollectRoute(shipmentNumber = shipmentNumber))
            },
        )
    }
}
