package com.kssidll.arrugarq.ui.screen.modify.product.editproduct


import androidx.compose.runtime.*
import androidx.compose.ui.res.*
import com.kssidll.arrugarq.R
import com.kssidll.arrugarq.ui.screen.modify.product.*
import dev.olshevski.navigation.reimagined.hilt.*
import kotlinx.coroutines.*

@Composable
fun EditProductRoute(
    productId: Long,
    navigateBack: () -> Unit,
    navigateBackDelete: () -> Unit,
    navigateCategoryAdd: (query: String?) -> Unit,
    navigateProducerAdd: (query: String?) -> Unit,
    navigateCategoryEdit: (categoryId: Long) -> Unit,
    navigateProducerEdit: (producerId: Long) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val viewModel: EditProductViewModel = hiltViewModel()

    LaunchedEffect(productId) {
        if (!viewModel.updateState(productId)) {
            navigateBack()
        }
    }

    ModifyProductScreenImpl(
        onBack = navigateBack,
        state = viewModel.screenState,
        categories = viewModel.allCategories()
            .collectAsState(initial = emptyList()).value,
        producers = viewModel.allProducers()
            .collectAsState(initial = emptyList()).value,
        onSubmit = {
            scope.launch {
                if (viewModel.updateProduct(productId)) {
                    navigateBack()
                }
            }
        },
        onDelete = {
            scope.launch {
                if (viewModel.deleteProduct(productId)) {
                    navigateBackDelete()
                }
            }
        },
        onMerge = {
            scope.launch {
                if (viewModel.mergeWith(it)) {
                    navigateBackDelete()
                }
            }
        },
        mergeCandidates = viewModel.allMergeCandidates(productId),
        mergeConfirmMessageTemplate = stringResource(id = R.string.merge_action_message_template)
            .replace(
                "{value_1}",
                viewModel.mergeMessageProductName
            ),

        chosenMergeCandidate = viewModel.chosenMergeCandidate.value,
        onChosenMergeCandidateChange = {
            viewModel.chosenMergeCandidate.apply { value = it }
        },
        showMergeConfirmDialog = viewModel.showMergeConfirmDialog.value,
        onShowMergeConfirmDialogChange = {
            viewModel.showMergeConfirmDialog.apply { value = it }
        },
        submitButtonText = stringResource(id = R.string.item_product_edit),
        onCategoryAddButtonClick = navigateCategoryAdd,
        onProducerAddButtonClick = navigateProducerAdd,
        onItemCategoryLongClick = navigateCategoryEdit,
        onItemProducerLongClick = navigateProducerEdit,
    )
}
