package com.kssidll.arrugarq.ui.screen.modify.item

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.kssidll.arrugarq.data.data.*
import com.kssidll.arrugarq.data.repository.*
import com.kssidll.arrugarq.domain.data.*
import com.kssidll.arrugarq.ui.screen.modify.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Base [ViewModel] class for Item modification view models
 * @property loadLastItem Initializes start state, should be called as child in init of inheriting view model
 * @property screenState A [ModifyItemScreenState] instance to use as screen state representation
 */
abstract class ModifyItemViewModel: ViewModel() {
    private var mProductListener: Job? = null
    private var mVariantListener: Job? = null

    protected abstract val itemRepository: ItemRepositorySource
    protected abstract val productRepository: ProductRepositorySource
    protected abstract val variantsRepository: VariantRepositorySource

    internal val screenState: ModifyItemScreenState = ModifyItemScreenState()

    suspend fun setSelectedProductToProvided(
        providedProductId: Long?,
        providedVariantId: Long?
    ) {
        if (providedProductId != null) {
            screenState.selectedProduct.apply { value = value.toLoading() }
            screenState.selectedVariant.apply { value = value.toLoading() }

            val product: Product? = providedProductId.let { productRepository.get(it) }
            val variant: ProductVariant? = providedVariantId?.let { variantsRepository.get(it) }

            // providedVariantId is null only when we create a new product
            // doing this allows us to skip data re-update on variant change
            // not doing this would wipe user input data to last item data on variant change
            // which is an unexpected behavior
            onNewProductSelected(
                product,
                providedVariantId == null
            )

            onNewVariantSelected(variant)
        }
    }

    suspend fun onNewProductSelected(
        product: Product?,
        loadLastItemProductData: Boolean = true
    ) {
        screenState.selectedProduct.value = Field.Loaded(product)
        updateProductVariants()

        setNewProductListener(product)
        setNewVariantListener(null)

        if (loadLastItemProductData) {
            loadLastItemDataForProduct(product)
        }
    }

    fun onNewVariantSelected(variant: ProductVariant?) {
        screenState.selectedVariant.value = Field.Loaded(variant)
        setNewVariantListener(variant)
    }

    private fun setNewProductListener(product: Product?) {
        mProductListener?.cancel()
        if (product != null) {
            mProductListener = viewModelScope.launch {
                productRepository.getFlow(product.id)
                    .collectLatest {
                        screenState.selectedProduct.value = Field.Loaded(it)
                    }
            }
        }
    }

    private fun setNewVariantListener(variant: ProductVariant?) {
        mVariantListener?.cancel()
        if (variant != null) {
            mVariantListener = viewModelScope.launch {
                variantsRepository.getFlow(variant.id)
                    .collectLatest {
                        screenState.selectedVariant.value = Field.Loaded(it)
                    }
            }
        }
    }

    private suspend fun loadLastItemDataForProduct(product: Product?) {
        screenState.selectedVariant.apply { value = value.toLoading() }
        screenState.price.apply { value = value.toLoading() }
        screenState.quantity.apply { value = value.toLoading() }

        val lastItem: Item? = product?.let {
            productRepository.newestItem(it)
        }

        val variant: ProductVariant? = lastItem?.variantId?.let { variantsRepository.get(it) }
        val price: String? = lastItem?.actualPrice()
            ?.toString()
        val quantity: String? = lastItem?.actualQuantity()
            ?.toString()

        onNewVariantSelected(variant)

        screenState.price.value = Field.Loaded(price)
        screenState.quantity.value = Field.Loaded(quantity)
    }

    /**
     * Fetches start data to state
     */
    protected fun loadLastItem() = viewModelScope.launch {
        screenState.allToLoading()

        val lastItem: Item? = itemRepository.newest()

        updateStateForItem(lastItem)
    }

    /**
     * @return List of all products
     */
    fun allProducts(): Flow<List<ProductWithAltNames>> {
        return productRepository.allWithAltNamesFlow()
    }

    private val mProductVariants: MutableState<Flow<List<ProductVariant>>> =
        mutableStateOf(emptyFlow())
    val productVariants: Flow<List<ProductVariant>> by mProductVariants
    private var mUpdateProductVariantsJob: Job? = null

    /**
     * Updates [productVariants] to represent available variants for currently set product
     */
    private fun updateProductVariants() {
        mUpdateProductVariantsJob?.cancel()
        mUpdateProductVariantsJob = viewModelScope.launch {
            mProductVariants.value =
                screenState.selectedProduct.value.data?.let { variantsRepository.byProductFlow(it) }
                    ?: emptyFlow()
        }
    }

    /**
     * Updates the state to represent [item], doesn't switch state to loading status as it should be done before fetching the item
     */
    protected suspend fun updateStateForItem(
        item: Item?,
    ) {
        val product: Product? = item?.productId?.let { productRepository.get(it) }
        val variant: ProductVariant? = item?.variantId?.let { variantsRepository.get(it) }
        val price: String? = item?.actualPrice()
            ?.toString()
        val quantity: String? = item?.actualQuantity()
            ?.toString()

        onNewProductSelected(
            product,
            false
        )
        onNewVariantSelected(variant)
        screenState.price.value = Field.Loaded(price)
        screenState.quantity.value = Field.Loaded(quantity)
    }
}

/**
 * Data representing [ModifyItemScreenImpl] screen state
 */
data class ModifyItemScreenState(
    val selectedProduct: MutableState<Field<Product>> = mutableStateOf(Field.Loaded()),
    val selectedVariant: MutableState<Field<ProductVariant?>> = mutableStateOf(Field.Loaded()),
    val quantity: MutableState<Field<String>> = mutableStateOf(Field.Loaded()),
    val price: MutableState<Field<String>> = mutableStateOf(Field.Loaded()),

    var isDatePickerDialogExpanded: MutableState<Boolean> = mutableStateOf(false),
    var isProductSearchDialogExpanded: MutableState<Boolean> = mutableStateOf(false),
    var isVariantSearchDialogExpanded: MutableState<Boolean> = mutableStateOf(false),
): ModifyScreenState() {

    /**
     * Sets all fields to Loading status
     */
    fun allToLoading() {
        price.apply { value = value.toLoading() }
        quantity.apply { value = value.toLoading() }
        selectedProduct.apply { value = value.toLoading() }
        selectedVariant.apply { value = value.toLoading() }
    }
}