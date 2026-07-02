package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import androidx.compose.runtime.saveable.rememberSaveable
import pl.tajchert.paczko.fast.core.designsystem.component.DetailTopBar
import pl.tajchert.paczko.fast.core.designsystem.component.HoldToOpenPanel
import pl.tajchert.paczko.fast.core.designsystem.component.PrimaryActionButton
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
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
            viewModel.arm(currentShipmentNumber)
        } else {
            viewModel.onLocationPermissionDenied(currentShipmentNumber)
        }
    }

    LaunchedEffect(shipmentNumber) {
        if (hasLocationPermission(context)) {
            viewModel.arm(shipmentNumber)
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
        onConfirmed = { viewModel.start(currentShipmentNumber) },
        onBack = onBack,
    )
}

private val CollectState.isBoxOpen: Boolean
    get() = this is CollectState.Opened ||
        this is CollectState.WaitingForClosed ||
        this is CollectState.ConfirmingClosed ||
        this is CollectState.Claiming

private val CollectState.isFinishing: Boolean
    get() = this is CollectState.ConfirmingClosed || this is CollectState.Claiming

@Composable
private fun CollectContent(
    shipmentNumber: String,
    uiState: CollectUiState,
    onConfirmed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = uiState.state
    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        topBar = {
            if (state !is CollectState.Completed) {
                DetailTopBar(title = if (state.isBoxOpen) "Box open" else "Open box", onBack = onBack)
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            when {
                state is CollectState.Idle -> HoldToOpenPanel(
                    distanceText = uiState.distanceMeters?.let { "$it m" },
                    lockerCaption = uiState.lockerName?.let { "to locker $it" } ?: "to the locker",
                    subline = collectSubline(uiState.members.size),
                    onConfirmed = onConfirmed,
                    modifier = Modifier.fillMaxSize(),
                )

                state.isBoxOpen -> BoxOpenScreen(
                    members = uiState.members,
                    lockerName = uiState.lockerName,
                    finishing = state.isFinishing,
                )

                state is CollectState.Completed -> SuccessScreen(
                    members = uiState.members,
                    onBack = onBack,
                )

                else -> CollectStatus(state = state, onClose = onBack)
            }
        }
    }
}

private fun collectSubline(count: Int): String = when {
    count > 1 -> "$count parcels share this box — you'll take them all at once"
    else -> "Stand at the locker before you start"
}

@Composable
private fun CollectStatus(
    state: CollectState,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = collectMessage(state),
            style = MaterialTheme.typography.displaySmall,
            color = PaczkofastTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (state is CollectState.Failed || state is CollectState.Completed) {
            PrimaryActionButton(
                text = "Close",
                onClick = onClose,
                modifier = Modifier.padding(top = 28.dp),
            )
        }
    }
}

/**
 * Design 3b — the box is open: a bold count and a per-parcel checklist so
 * nothing is left behind. The door closing (detected by polling) advances the
 * flow to [SuccessScreen]; the checklist is a safety affordance.
 */
@Composable
private fun BoxOpenScreen(
    members: List<CollectMember>,
    lockerName: String?,
    finishing: Boolean,
) {
    val info = PaczkofastTheme.colors.infoAccent
    val checked = remember(members) { mutableStateListOf<String>() }
    val allChecked = members.isNotEmpty() && checked.size == members.size
    val count = members.size

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            RingIcon(ringColor = info.copy(alpha = 0.25f), fillColor = PaczkofastTheme.colors.infoSurface) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = info,
                    modifier = Modifier.size(44.dp),
                )
            }
            Text(
                text = (lockerName?.let { "Locker $it" } ?: "Locker").uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = PaczkofastTheme.colors.textMuted,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                text = "The box is open",
                style = MaterialTheme.typography.displaySmall,
                color = PaczkofastTheme.colors.textPrimary,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = if (count > 1) "$count parcels are in this box. Take them all." else "Take your parcel.",
                style = MaterialTheme.typography.bodyLarge,
                color = PaczkofastTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                members.forEach { member ->
                    val isChecked = member.shipmentNumber in checked
                    ChecklistRow(
                        title = member.title,
                        sizeLabel = member.sizeLabel,
                        checked = isChecked,
                        accent = info,
                        onToggle = {
                            if (isChecked) checked.remove(member.shipmentNumber)
                            else checked.add(member.shipmentNumber)
                        },
                    )
                }
            }
        }

        Text(
            text = when {
                finishing -> "Finishing up…"
                allChecked -> "Close the door firmly — it locks automatically"
                else -> "Tick off each parcel as you take it"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (allChecked) PaczkofastTheme.colors.textSecondary else PaczkofastTheme.colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp),
        )
    }
}

@Composable
private fun ChecklistRow(
    title: String,
    sizeLabel: String?,
    checked: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PaczkofastTheme.colors.infoSurface)
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (checked) Modifier.background(accent)
                    else Modifier.border(1.5.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = PaczkofastTheme.colors.onInfoAccent,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.5.sp),
            color = PaczkofastTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        sizeLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = PaczkofastTheme.colors.textMuted,
            )
        }
    }
}

/**
 * Design 3c — pickup succeeded: an amber check, a summary of what was collected
 * and a way back to the parcel list.
 */
@Composable
private fun SuccessScreen(
    members: List<CollectMember>,
    onBack: () -> Unit,
) {
    val accent = PaczkofastTheme.colors.accent
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            RingIcon(ringColor = accent.copy(alpha = 0.22f), fillColor = accent) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = PaczkofastTheme.colors.onAccent,
                    modifier = Modifier.size(52.dp),
                )
            }
            Text(
                text = "All picked up",
                style = MaterialTheme.typography.displayMedium,
                color = PaczkofastTheme.colors.textPrimary,
                modifier = Modifier.padding(top = 26.dp),
            )
            if (members.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(PaczkofastTheme.colors.cardSurface)
                        .border(1.dp, PaczkofastTheme.colors.cardBorder, MaterialTheme.shapes.extraLarge)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = (if (members.size > 1) "${members.size} parcels collected" else "1 parcel collected").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = PaczkofastTheme.colors.textMuted,
                    )
                    members.forEach { member ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = member.title,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.5.sp),
                                color = PaczkofastTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            member.sizeLabel?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PaczkofastTheme.colors.textMuted,
                                )
                            }
                        }
                    }
                }
            }
        }
        PrimaryActionButton(
            text = "Back to my parcels",
            onClick = onBack,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

/** Concentric ring + filled inner circle used by the box-open and success states. */
@Composable
private fun RingIcon(
    ringColor: androidx.compose.ui.graphics.Color,
    fillColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(176.dp)
            .clip(CircleShape)
            .border(1.dp, ringColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(fillColor),
            contentAlignment = Alignment.Center,
            content = { content() },
        )
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
