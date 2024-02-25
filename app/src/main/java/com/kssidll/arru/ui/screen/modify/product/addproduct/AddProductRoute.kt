package com.kssidll.arru.ui.screen.modify.product.addproduct

import androidx.compose.runtime.*
import com.kssidll.arru.domain.data.*
import com.kssidll.arru.ui.screen.modify.product.*
import dev.olshevski.navigation.reimagined.hilt.*
import kotlinx.coroutines.*

@Composable
fun AddProductRoute(
    defaultName: String?,
    navigateBack: (productId: Long?) -> Unit,
    navigateCategoryAdd: (query: String?) -> Unit,
    navigateProducerAdd: (query: String?) -> Unit,
    navigateCategoryEdit: (categoryId: Long) -> Unit,
    navigateProducerEdit: (producerId: Long) -> Unit,
    providedProducerId: Long?,
    providedCategoryId: Long?,
) {
    val scope = rememberCoroutineScope()
    val viewModel: AddProductViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.screenState.name.value = Field.Loaded(defaultName)
    }

    LaunchedEffect(providedProducerId) {
        viewModel.setSelectedProducer(providedProducerId)
    }

    LaunchedEffect(providedCategoryId) {
        viewModel.setSelectedCategory(providedCategoryId)
    }

    ModifyProductScreenImpl(
        onBack = {
            navigateBack(null)
        },
        state = viewModel.screenState,
        categories = viewModel.allCategories()
            .collectAsState(initial = Data.Loading()).value,
        producers = viewModel.allProducers()
            .collectAsState(initial = Data.Loading()).value,
        onNewProducerSelected = {
            viewModel.onNewProducerSelected(it)
        },
        onNewCategorySelected = {
            viewModel.onNewCategorySelected(it)
        },
        onSubmit = {
            scope.launch {
                val result = viewModel.addProduct()
                if (result.isNotError()) {
                    navigateBack(result.id)
                }
            }
        },
        onCategoryAddButtonClick = navigateCategoryAdd,
        onProducerAddButtonClick = navigateProducerAdd,
        onItemCategoryLongClick = navigateCategoryEdit,
        onItemProducerLongClick = navigateProducerEdit,
    )
}
