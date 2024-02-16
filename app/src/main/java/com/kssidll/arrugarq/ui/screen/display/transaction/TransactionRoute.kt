package com.kssidll.arrugarq.ui.screen.display.transaction

import androidx.compose.runtime.*
import dev.olshevski.navigation.reimagined.hilt.*

@Composable
fun TransactionRoute(
    transactionId: Long,
    navigateBack: () -> Unit,
    navigateTransactionEdit: (transactionId: Long) -> Unit,
    navigateItemAdd: (transactionId: Long) -> Unit,
    navigateProduct: (productId: Long) -> Unit,
    navigateItemEdit: (itemId: Long) -> Unit,
    navigateCategory: (categoryId: Long) -> Unit,
    navigateProducer: (producerId: Long) -> Unit,
    navigateShop: (shopId: Long) -> Unit,
) {
    val viewModel: TransactionViewModel = hiltViewModel()

    TransactionScreen(
        onBack = navigateBack,
        transaction = viewModel.transaction(transactionId)
            .collectAsState(initial = null).value,
        onEditAction = {
            navigateTransactionEdit(transactionId)
        },
        onItemAddClick = navigateItemAdd,
        onItemClick = navigateProduct,
        onItemLongClick = navigateItemEdit,
        onItemCategoryClick = navigateCategory,
        onItemProducerClick = navigateProducer,
        onItemShopClick = navigateShop,
    )
}