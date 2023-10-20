package com.kssidll.arrugarq.ui.screen.shared


import android.content.res.Configuration.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import com.kssidll.arrugarq.R
import com.kssidll.arrugarq.data.data.*
import com.kssidll.arrugarq.ui.component.field.*
import com.kssidll.arrugarq.ui.theme.*

private val ItemHorizontalPadding: Dp = 20.dp

@Composable
fun EditProductVariantScreen(
    onBack: () -> Unit,
    state: EditProductVariantScreenState,
    onSubmit: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                focusRequester.requestFocus()
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    EditScreen(
        onBack = onBack,
        title = stringResource(id = R.string.item_product_variant_full),
        onDelete = onDelete,
        onSubmit = onSubmit,
        submitButtonText = stringResource(id = R.string.item_product_variant_add),
        submitButtonDescription = stringResource(id = R.string.item_product_variant_add_description),
    ) {
        StyledOutlinedTextField(
            singleLine = true,
            value = state.name.value,
            onValueChange = {
                state.name.value = it
                state.validateName()
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSubmit()
                }
            ),
            label = {
                Text(
                    text = stringResource(R.string.item_product_variant),
                )
            },
            isError = if (state.attemptedToSubmit.value) state.nameError.value else false,
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .padding(horizontal = ItemHorizontalPadding)
        )
    }
}

data class EditProductVariantScreenState(
    val attemptedToSubmit: MutableState<Boolean> = mutableStateOf(false),

    val name: MutableState<String> = mutableStateOf(String()),
    val nameError: MutableState<Boolean> = mutableStateOf(false),
)

/**
 * Validates name field and updates its error flag
 * @return true if field is of correct value, false otherwise
 */
fun EditProductVariantScreenState.validateName(): Boolean {
    return !(name.value.isBlank()).also { nameError.value = it }
}

/**
 * Validates state fields and updates state flags
 * @return true if all fields are of correct value, false otherwise
 */
fun EditProductVariantScreenState.validate(): Boolean {
    return validateName()
}

/**
 * performs data validation and tries to extract embedded data
 * @param productId: Id of the product that the variant is being created for
 * @return Null if validation sets error flags, extracted data otherwise
 */
fun EditProductVariantScreenState.extractProducerOrNull(productId: Long): ProductVariant? {
    if (!validate()) return null

    return ProductVariant(
        productId = productId,
        name = name.value.trim(),
    )
}

@Preview(
    group = "EditProductVariantScreen",
    name = "Dark",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    group = "EditProductVariantScreen",
    name = "Light",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
fun EditProductVariantScreenPreview() {
    ArrugarqTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            EditProductVariantScreen(
                onBack = {},
                state = EditProductVariantScreenState(),
                onSubmit = {},
            )
        }
    }
}
