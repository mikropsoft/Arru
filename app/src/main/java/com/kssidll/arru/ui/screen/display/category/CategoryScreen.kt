package com.kssidll.arru.ui.screen.display.category


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.paging.*
import androidx.paging.compose.*
import com.kssidll.arru.*
import com.kssidll.arru.R
import com.kssidll.arru.data.data.*
import com.kssidll.arru.domain.*
import com.kssidll.arru.domain.data.*
import com.kssidll.arru.helper.*
import com.kssidll.arru.ui.component.*
import com.kssidll.arru.ui.component.list.*
import com.kssidll.arru.ui.component.other.*
import com.kssidll.arru.ui.theme.*
import com.patrykandpatrick.vico.core.entry.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * @param onBack Called to request a back navigation
 * @param category Category for which the data is displayed
 * @param transactionItems Transaction items of [category]
 * @param spentByTimeData Data list representing [category] spending for current [spentByTimePeriod]
 * @param totalSpentData Value representing total [category] spending
 * @param spentByTimePeriod Time period to get the [spentByTimeData] by
 * @param onSpentByTimePeriodSwitch Called to request [spentByTimePeriod] switch, Provides new period as argument
 * @param chartEntryModelProducer Model producer for [spentByTimeData] chart
 * @param onItemClick Callback called when the item is clicked. Provides product id as parameter
 * @param onItemProducerClick Callback called when the item producer label is clicked. Provides producer id as parameter
 * @param onItemShopClick Callback called when the item shop label is clicked. Provides shop id as parameter
 * @param onItemLongClick Callback called when the item is long clicked/pressed. Provides item id as parameter
 * @param onEditAction Callback called when the 'edit' action is triggered
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
internal fun CategoryScreen(
    onBack: () -> Unit,
    category: ProductCategory?,
    transactionItems: LazyPagingItems<FullItem>,
    spentByTimeData: Data<List<ItemSpentByTime>>,
    totalSpentData: Data<Float?>,
    spentByTimePeriod: TimePeriodFlowHandler.Periods?,
    onSpentByTimePeriodSwitch: (TimePeriodFlowHandler.Periods) -> Unit,
    chartEntryModelProducer: ChartEntryModelProducer,
    onItemClick: (productId: Long) -> Unit,
    onItemProducerClick: (producerId: Long) -> Unit,
    onItemShopClick: (shopId: Long) -> Unit,
    onItemLongClick: (itemId: Long) -> Unit,
    onEditAction: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    var previousFirstVisibleItemIndex by remember { mutableIntStateOf(0) }

    var returnActionButtonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(firstVisibleItemIndex) {
        if (
            previousFirstVisibleItemIndex > firstVisibleItemIndex + 1 &&
            firstVisibleItemIndex >= 10
        ) {
            // scrolling up
            returnActionButtonVisible = true
            previousFirstVisibleItemIndex = firstVisibleItemIndex
        } else if (
            previousFirstVisibleItemIndex < firstVisibleItemIndex - 1 ||
            firstVisibleItemIndex < 10
        ) {
            // scrolling down
            returnActionButtonVisible = false
            previousFirstVisibleItemIndex = firstVisibleItemIndex
        }
    }

    Scaffold(
        topBar = {
            SecondaryAppBar(
                onBack = onBack,
                title = {
                    Text(
                        text = category?.name.orEmpty(),
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    // 'edit' action
                    IconButton(
                        onClick = {
                            onEditAction()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(27.dp),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = returnActionButtonVisible,
                enter = slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseOut
                    ),
                    initialOffsetX = { it }
                ),
                exit = slideOutHorizontally(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseIn
                    ),
                    targetOffsetX = { it }
                )
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = null,
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            stickyHeader {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(40.dp))

                    val chartData = if (spentByTimeData is Data.Loaded) {
                        spentByTimeData.data
                    } else emptyList()

                    val totalSpent = if (totalSpentData is Data.Loaded) {
                        totalSpentData.data ?: 0f
                    } else 0f

                    TotalAverageAndMedianSpendingComponent(
                        spentByTimeData = chartData,
                        totalSpentData = totalSpent,
                    )

                    Spacer(Modifier.height(28.dp))

                    AnimatedVisibility(visible = spentByTimeData.loadedData()) {
                        if (spentByTimeData is Data.Loaded) {
                            SpendingSummaryComponent(
                                spentByTimeData = spentByTimeData.data,
                                spentByTimePeriod = spentByTimePeriod,
                                onSpentByTimePeriodUpdate = onSpentByTimePeriodSwitch,
                                columnChartEntryModelProducer = chartEntryModelProducer,
                            )

                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // TODO change this to center title large like on dashboard
                    AnimatedVisibility(visible = transactionItems.itemCount == 0) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.no_data_to_display_text),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            fullItemListContent(
                transactionItems = transactionItems,
                onItemClick = {
                    onItemClick(it.product.id)
                },
                onItemLongClick = {
                    onItemLongClick(it.id)
                },
                onProducerClick = {
                    onItemProducerClick(it.id)
                },
                onShopClick = {
                    onItemShopClick(it.id)
                }
            )
        }
    }
}

@PreviewLightDark
@PreviewExpanded
@Composable
fun CategoryScreenPreview() {
    ArrugarqTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CategoryScreen(
                onBack = {},
                category = null,
                transactionItems = flowOf(PagingData.from(FullItem.generateList())).collectAsLazyPagingItems(),
                spentByTimeData = Data.Loaded(ItemSpentByTime.generateList()),
                totalSpentData = Data.Loaded(generateRandomFloatValue()),
                spentByTimePeriod = TimePeriodFlowHandler.Periods.Month,
                onSpentByTimePeriodSwitch = {},
                chartEntryModelProducer = ChartEntryModelProducer(),
                onItemClick = {},
                onItemProducerClick = {},
                onItemShopClick = {},
                onItemLongClick = {},
                onEditAction = {},
            )
        }
    }
}
