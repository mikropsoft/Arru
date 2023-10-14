package com.kssidll.arrugarq.ui.screen.home.dashboard

import android.content.res.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.kssidll.arrugarq.data.data.*
import com.kssidll.arrugarq.domain.*
import com.kssidll.arrugarq.helper.*
import com.kssidll.arrugarq.ui.component.*
import com.kssidll.arrugarq.ui.component.list.*
import com.kssidll.arrugarq.ui.theme.*
import kotlinx.coroutines.flow.*

@Composable
fun DashboardScreen(
    onCategoryCardClick: () -> Unit,
    onShopCardClick: () -> Unit,
    totalSpentData: Flow<Float>,
    spentByShopData: Flow<List<ItemSpentByShop>>,
    spentByCategoryData: Flow<List<ItemSpentByCategory>>,
    spentByTimeData: Flow<List<ItemSpentByTime>>,
    spentByTimePeriod: TimePeriodFlowHandler.Periods,
    onSpentByTimePeriodSwitch: (TimePeriodFlowHandler.Periods) -> Unit,
) {
    DashboardScreenContent(
        onCategoryCardClick = onCategoryCardClick,
        onShopCardClick = onShopCardClick,
        totalSpentData = totalSpentData.collectAsState(0F).value,
        spentByShopData = spentByShopData.collectAsState(emptyList()).value,
        spentByCategoryData = spentByCategoryData.collectAsState(emptyList()).value,
        spentByTimeData = spentByTimeData.collectAsState(emptyList()).value,
        spentByTimePeriod = spentByTimePeriod,
        onSpentByTimePeriodSwitch = onSpentByTimePeriodSwitch,
    )
}

private val tileOuterPadding: Dp = 8.dp
private val tileInnerPadding: Dp = 12.dp

@Composable
private fun DashboardScreenContent(
    onCategoryCardClick: () -> Unit,
    onShopCardClick: () -> Unit,
    totalSpentData: Float,
    spentByShopData: List<ItemSpentByShop>,
    spentByCategoryData: List<ItemSpentByCategory>,
    spentByTimeData: List<ItemSpentByTime>,
    spentByTimePeriod: TimePeriodFlowHandler.Periods,
    onSpentByTimePeriodSwitch: (TimePeriodFlowHandler.Periods) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.verticalScroll(state = scrollState)
    ) {
        Spacer(Modifier.height(40.dp))

        TotalAverageAndMedianSpendingComponent(
            spentByTimeData = spentByTimeData,
            totalSpentData = totalSpentData,
        )

        Spacer(Modifier.height(28.dp))

        SpendingSummaryComponent(
            modifier = Modifier.animateContentSize(),
            spentByTimeData = spentByTimeData,
            spentByTimePeriod = spentByTimePeriod,
            onSpentByTimePeriodSwitch = onSpentByTimePeriodSwitch,
        )

        Spacer(Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .padding(tileOuterPadding)
                .clickable {
                    onCategoryCardClick()
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            RankingList(
                innerItemPadding = PaddingValues(tileInnerPadding),
                items = spentByCategoryData,
            )
        }

        Card(
            modifier = Modifier
                .padding(tileOuterPadding)
                .clickable {
                    onShopCardClick()
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            RankingList(
                innerItemPadding = PaddingValues(tileInnerPadding),
                items = spentByShopData,
            )
        }

    }
}

@Preview(
    group = "Dashboard Screen",
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    group = "Dashboard Screen",
    name = "Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DashboardScreenPreview() {
    ArrugarqTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DashboardScreenContent(
                onCategoryCardClick = {},
                onShopCardClick = {},
                totalSpentData = 16832.18F,
                spentByShopData = generateRandomItemSpentByShopList(),
                spentByCategoryData = generateRandomItemSpentByCategoryList(),
                spentByTimeData = generateRandomItemSpentByTimeList(),
                spentByTimePeriod = TimePeriodFlowHandler.Periods.Month,
                onSpentByTimePeriodSwitch = {},
            )
        }
    }
}
