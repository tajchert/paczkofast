package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import androidx.compose.runtime.saveable.rememberSaveable
import pl.tajchert.paczko.fast.core.model.collect.CollectState

@Composable
fun CollectScreen(
    shipmentNumber: String,
    onBack: () -> Unit,
    viewModel: CollectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentShipmentNumber by rememberUpdatedState(shipmentNumber)
    var permissionRequested by rememberSaveable(shipmentNumber) {
        mutableStateOf(false)
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.start(currentShipmentNumber)
        } else {
            viewModel.onLocationPermissionDenied(currentShipmentNumber)
        }
    }

    LaunchedEffect(shipmentNumber) {
        if (hasLocationPermission(context)) {
            viewModel.start(shipmentNumber)
        } else if (!permissionRequested) {
            permissionRequested = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    CollectContent(
        shipmentNumber = shipmentNumber,
        uiState = uiState,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectContent(
    shipmentNumber: String,
    uiState: CollectUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
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
                text = shipmentNumber,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = collectMessage(uiState.state),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
            if (uiState.state is CollectState.Failed || uiState.state is CollectState.Completed) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    Text(text = "Close")
                }
            }
        }
    }
}

private fun collectMessage(state: CollectState): String = when (state) {
    CollectState.Idle -> "Ready"
    CollectState.Validating -> "Checking parcel and location"
    is CollectState.Opening -> "Opening compartment"
    is CollectState.WaitingForOpened -> "Waiting for the door to open"
    is CollectState.Opened -> "Take the parcel and close the door"
    is CollectState.WaitingForClosed -> "Waiting for the door to close"
    is CollectState.ConfirmingClosed -> "Confirming closed compartment"
    is CollectState.Claiming -> "Confirming pickup"
    CollectState.Completed -> "Parcel collected"
    is CollectState.Failed -> state.message
    CollectState.Canceled -> "Collection canceled"
}

private fun hasLocationPermission(context: Context): Boolean {
    val hasFineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    return hasFineLocation || hasCoarseLocation
}
