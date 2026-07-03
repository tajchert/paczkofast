package pl.tajchert.paczko.fast.feature.parcels.impl.detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.DeadlineCard
import pl.tajchert.paczko.fast.core.designsystem.component.DetailTopBar
import pl.tajchert.paczko.fast.core.designsystem.component.OutlinedStatusChip
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastLoadingIndicator
import pl.tajchert.paczko.fast.core.designsystem.component.PrimaryActionButton
import pl.tajchert.paczko.fast.core.designsystem.component.StatusChip
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.ui.QrPanel
import pl.tajchert.paczko.fast.feature.parcels.api.MultiPackageDetailRoute

/**
 * Multi-package box detail (7a): shared identity, a tappable member sublist,
 * one deadline, one QR/code and a single open action. Per-parcel tracking lives
 * on each member's own detail screen.
 */
@Composable
fun MultiPackageDetailScreen(
    route: MultiPackageDetailRoute,
    onBack: () -> Unit,
    onOpenParcel: (shipmentNumber: String) -> Unit,
    onCollect: (shipmentNumber: String) -> Unit,
    viewModel: MultiPackageDetailViewModel = hiltViewModel<MultiPackageDetailViewModel, MultiPackageDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(route.shipmentNumber) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MultiPackageDetailContent(
        uiState = uiState,
        onBack = onBack,
        onOpenParcel = onOpenParcel,
        onCollect = onCollect,
    )
}

@Composable
private fun MultiPackageDetailContent(
    uiState: MultiPackageDetailUiState,
    onBack: () -> Unit,
    onOpenParcel: (String) -> Unit,
    onCollect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        topBar = { DetailTopBar(title = "Box details", onBack = onBack) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            PaczkofastLoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            return@Scaffold
        }
        val count = uiState.members.size
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    uiState.statusLabel?.let { StatusChip(text = it) }
                    OneBoxChip()
                }
                Text(
                    text = if (count == 1) "1 parcel" else "$count parcels",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PaczkofastTheme.colors.textPrimary,
                )
                uiState.lockerLine?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = PaczkofastTheme.colors.textMuted,
                    )
                }
            }

            MemberSublist(members = uiState.members, onOpenParcel = onOpenParcel)

            if (uiState.deadlineText != null && uiState.progress != null) {
                DeadlineCard(
                    deadlineText = uiState.deadlineText,
                    countdownText = uiState.countdownText.orEmpty(),
                    progress = uiState.progress,
                    urgent = uiState.urgent,
                )
            }

            uiState.qrCode?.takeIf(String::isNotBlank)?.let { qr ->
                QrPanel(payload = qr, code = uiState.openCode, qrSize = 150)
                Text(
                    text = "One code opens the box — take all $count parcels",
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.textMuted,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            if (uiState.canCollect && uiState.representativeShipmentNumber != null) {
                PrimaryActionButton(
                    text = "Open box · $count parcels",
                    onClick = { onCollect(uiState.representativeShipmentNumber) },
                )
            }

            Text(
                text = "Tracking is per parcel — open a parcel above to see its timeline",
                style = MaterialTheme.typography.bodySmall,
                color = PaczkofastTheme.colors.textFaint,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun OneBoxChip() {
    val info = PaczkofastTheme.colors.infoAccent
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(info.copy(alpha = 0.16f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = info,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = "ONE BOX",
            style = MaterialTheme.typography.labelSmall,
            color = info,
        )
    }
}

@Composable
private fun MemberSublist(
    members: List<BoxMember>,
    onOpenParcel: (String) -> Unit,
) {
    val info = PaczkofastTheme.colors.infoAccent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(PaczkofastTheme.colors.infoSurface)
            .border(1.dp, PaczkofastTheme.colors.infoBorder, MaterialTheme.shapes.extraLarge),
    ) {
        members.forEachIndexed { index, member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenParcel(member.shipmentNumber) }
                    .padding(horizontal = 18.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = info,
                    modifier = Modifier.size(16.dp),
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = member.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                        color = PaczkofastTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = member.shipmentNumberLine,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                        ),
                        color = PaczkofastTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                member.sizeLabel?.let { OutlinedStatusChip(text = it) }
                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleLarge,
                    color = PaczkofastTheme.colors.textFaint,
                )
            }
            if (index != members.lastIndex) {
                HorizontalDivider(
                    color = info.copy(alpha = 0.12f),
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
        }
    }
}
