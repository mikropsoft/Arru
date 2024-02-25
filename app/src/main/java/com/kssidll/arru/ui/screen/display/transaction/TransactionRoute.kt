package com.kssidll.arru.ui.screen.display.transaction

import androidx.compose.runtime.*
import com.kssidll.arru.domain.data.*
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
            .collectAsState(initial = Data.Loading()).value,
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