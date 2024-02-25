package com.kssidll.arru.ui.screen.display.product


import androidx.compose.runtime.*
import androidx.paging.compose.*
import com.kssidll.arru.domain.data.*
import dev.olshevski.navigation.reimagined.hilt.*

@Composable
fun ProductRoute(
    productId: Long,
    navigateBack: () -> Unit,
    navigateCategory: (categoryId: Long) -> Unit,
    navigateProducer: (producerId: Long) -> Unit,
    navigateShop: (shopId: Long) -> Unit,
    navigateItemEdit: (itemId: Long) -> Unit,
    navigateProductEdit: () -> Unit,
) {
    val viewModel: ProductViewModel = hiltViewModel()

    LaunchedEffect(productId) {
        if (!viewModel.performDataUpdate(productId)) {
            navigateBack()
        }
    }

    ProductScreen(
        onBack = navigateBack,
        product = viewModel.product,
        transactionItems = viewModel.transactions()
            .collectAsLazyPagingItems(),
        spentByTimeData = viewModel.spentByTimeData?.collectAsState(initial = Data.Loading())?.value
            ?: Data.Loaded(emptyList()),
        productPriceByShopByTimeData = viewModel.productPriceByShop()
            ?.collectAsState(initial = Data.Loading())?.value ?: Data.Loaded(emptyList()),
        totalSpentData = viewModel.productTotalSpent()
            ?.collectAsState(initial = Data.Loading())?.value ?: Data.Loaded(0F),
        spentByTimePeriod = viewModel.spentByTimePeriod,
        onSpentByTimePeriodSwitch = {
            viewModel.switchPeriod(it)
        },
        chartEntryModelProducer = viewModel.chartEntryModelProducer,
        onItemCategoryClick = navigateCategory,
        onItemProducerClick = navigateProducer,
        onItemShopClick = navigateShop,
        onItemLongClick = navigateItemEdit,
        onEditAction = navigateProductEdit,
    )
}