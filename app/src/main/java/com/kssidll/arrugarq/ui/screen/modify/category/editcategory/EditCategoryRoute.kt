package com.kssidll.arrugarq.ui.screen.modify.category.editcategory


import androidx.compose.runtime.*
import androidx.compose.ui.res.*
import com.kssidll.arrugarq.R
import com.kssidll.arrugarq.ui.screen.modify.category.*
import dev.olshevski.navigation.reimagined.hilt.*
import kotlinx.coroutines.*

@Composable
fun EditCategoryRoute(
    categoryId: Long,
    navigateBack: () -> Unit,
    navigateBackDelete: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val viewModel: EditCategoryViewModel = hiltViewModel()

    LaunchedEffect(categoryId) {
        if (!viewModel.updateState(categoryId)) {
            navigateBack()
        }
    }

    ModifyCategoryScreenImpl(
        onBack = navigateBack,
        state = viewModel.screenState,
        onSubmit = {
            scope.launch {
                if (viewModel.updateCategory(categoryId)
                        .isNotError()
                ) {
                    navigateBack()
                }
            }
        },
        onDelete = {
            scope.launch {
                if (viewModel.deleteCategory(categoryId)
                        .isNotError()
                ) {
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
        mergeCandidates = viewModel.allMergeCandidates(categoryId),
        mergeConfirmMessageTemplate = stringResource(id = R.string.merge_action_message_template)
            .replace(
                "{value_1}",
                viewModel.mergeMessageCategoryName
            ),

        chosenMergeCandidate = viewModel.chosenMergeCandidate.value,
        onChosenMergeCandidateChange = {
            viewModel.chosenMergeCandidate.apply { value = it }
        },
        showMergeConfirmDialog = viewModel.showMergeConfirmDialog.value,
        onShowMergeConfirmDialogChange = {
            viewModel.showMergeConfirmDialog.apply { value = it }
        },
        submitButtonText = stringResource(id = R.string.item_product_category_edit),
    )
}
