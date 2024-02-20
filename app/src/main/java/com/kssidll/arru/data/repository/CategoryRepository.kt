package com.kssidll.arru.data.repository

import androidx.paging.*
import com.kssidll.arru.data.dao.*
import com.kssidll.arru.data.data.*
import com.kssidll.arru.data.paging.*
import com.kssidll.arru.data.repository.CategoryRepositorySource.Companion.AltInsertResult
import com.kssidll.arru.data.repository.CategoryRepositorySource.Companion.AltUpdateResult
import com.kssidll.arru.data.repository.CategoryRepositorySource.Companion.DeleteResult
import com.kssidll.arru.data.repository.CategoryRepositorySource.Companion.InsertResult
import com.kssidll.arru.data.repository.CategoryRepositorySource.Companion.MergeResult
import com.kssidll.arru.data.repository.CategoryRepositorySource.Companion.UpdateResult
import kotlinx.coroutines.flow.*

class CategoryRepository(private val dao: CategoryDao): CategoryRepositorySource {
    // Create

    override suspend fun insert(name: String): InsertResult {
        val category = ProductCategory(name)

        if (category.validName()
                .not()
        ) {
            return InsertResult.Error(InsertResult.InvalidName)
        }

        val other = dao.byName(category.name)

        if (other != null) {
            return InsertResult.Error(InsertResult.DuplicateName)
        }

        return InsertResult.Success(dao.insert(category))
    }

    override suspend fun insertAltName(
        category: ProductCategory,
        alternativeName: String
    ): AltInsertResult {
        if (dao.get(category.id) != category) {
            return AltInsertResult.Error(AltInsertResult.InvalidId)
        }

        val categoryAltName = ProductCategoryAltName(
            category = category,
            name = alternativeName,
        )

        if (categoryAltName.validName()
                .not()
        ) {
            return AltInsertResult.Error(AltInsertResult.InvalidName)
        }

        val others = dao.altNames(category.id)

        if (categoryAltName.name in others.map { it.name }) {
            return AltInsertResult.Error(AltInsertResult.DuplicateName)
        }

        return AltInsertResult.Success(dao.insertAltName(categoryAltName))
    }

    // Update

    override suspend fun update(
        categoryId: Long,
        name: String
    ): UpdateResult {
        if (dao.get(categoryId) == null) {
            return UpdateResult.Error(UpdateResult.InvalidId)
        }

        val category = ProductCategory(
            id = categoryId,
            name = name.trim(),
        )

        if (category.validName()
                .not()
        ) {
            return UpdateResult.Error(UpdateResult.InvalidName)
        }

        val other = dao.byName(category.name)

        if (other != null) {
            if (other.id == category.id) {
                return UpdateResult.Success
            }

            return UpdateResult.Error(UpdateResult.DuplicateName)
        }

        dao.update(category)

        return UpdateResult.Success
    }

    override suspend fun updateAltName(
        alternativeNameId: Long,
        categoryId: Long,
        name: String
    ): AltUpdateResult {
        if (dao.getAltName(alternativeNameId) == null) {
            return AltUpdateResult.Error(AltUpdateResult.InvalidId)
        }

        if (dao.get(categoryId) == null) {
            return AltUpdateResult.Error(AltUpdateResult.InvalidCategoryId)
        }

        val alternativeName = ProductCategoryAltName(
            id = alternativeNameId,
            productCategoryId = categoryId,
            name = name.trim(),
        )

        if (alternativeName.validName()
                .not()
        ) {
            return AltUpdateResult.Error(AltUpdateResult.InvalidName)
        }

        val others = dao.altNames(categoryId)

        if (alternativeName.name in others.map { it.name }) {
            if (alternativeName.id in others.map { it.id }) {
                return AltUpdateResult.Success
            }

            return AltUpdateResult.Error(AltUpdateResult.DuplicateName)
        }

        dao.updateAltName(alternativeName)

        return AltUpdateResult.Success
    }

    override suspend fun merge(
        category: ProductCategory,
        mergingInto: ProductCategory
    ): MergeResult {
        if (dao.get(category.id) == null) {
            return MergeResult.Error(MergeResult.InvalidCategory)
        }

        if (dao.get(mergingInto.id) == null) {
            return MergeResult.Error(MergeResult.InvalidMergingInto)
        }

        val products = dao.getProducts(category.id)
        products.forEach { it.categoryId = mergingInto.id }
        dao.updateProducts(products)

        dao.deleteAltName(dao.altNames(category.id))
        dao.delete(category)

        return MergeResult.Success
    }

    // Delete

    override suspend fun delete(
        productCategoryId: Long,
        force: Boolean
    ): DeleteResult {
        val category =
            dao.get(productCategoryId) ?: return DeleteResult.Error(DeleteResult.InvalidId)

        val altNames = dao.altNames(productCategoryId)
        val products = dao.getProducts(productCategoryId)
        val productVariants = dao.getProductsVariants(productCategoryId)
        val productAltNames = dao.getProductsAltNames(productCategoryId)
        val items = dao.getItems(productCategoryId)
        val transactionBasketItems = dao.getTransactionBasketItems(productCategoryId)

        if (!force && (altNames.isNotEmpty() || products.isNotEmpty() || productAltNames.isNotEmpty() || items.isNotEmpty())) {
            return DeleteResult.Error(DeleteResult.DangerousDelete)
        } else {
            dao.deleteTransactionBasketItems(transactionBasketItems)
            dao.deleteItems(items)
            dao.deleteProductAltNames(productAltNames)
            dao.deleteProductVariants(productVariants)
            dao.deleteProducts(products)
            dao.deleteAltName(altNames)
            dao.delete(category)
        }

        return DeleteResult.Success
    }

    override suspend fun deleteAltName(alternativeNameId: Long): DeleteResult {
        val altName = dao.getAltName(alternativeNameId)
            ?: return DeleteResult.Error(DeleteResult.InvalidId)

        dao.deleteAltName(altName)

        return DeleteResult.Success
    }

    // Read

    override suspend fun get(categoryId: Long): ProductCategory? {
        return dao.get(categoryId)
    }

    override fun getFlow(categoryId: Long): Flow<ProductCategory?> {
        return dao.getFlow(categoryId)
            .cancellable()
            .distinctUntilChanged()
    }

    override fun totalSpentFlow(category: ProductCategory): Flow<Long> {
        return dao.totalSpentFlow(category.id)
            .cancellable()
            .distinctUntilChanged()
    }

    override fun totalSpentByDayFlow(category: ProductCategory): Flow<List<ItemSpentByTime>> {
        return dao.totalSpentByDayFlow(category.id)
            .cancellable()
            .distinctUntilChanged()
    }

    override fun totalSpentByWeekFlow(category: ProductCategory): Flow<List<ItemSpentByTime>> {
        return dao.totalSpentByWeekFlow(category.id)
            .cancellable()
            .distinctUntilChanged()
    }

    override fun totalSpentByMonthFlow(category: ProductCategory): Flow<List<ItemSpentByTime>> {
        return dao.totalSpentByMonthFlow(category.id)
            .cancellable()
            .distinctUntilChanged()
    }

    override fun totalSpentByYearFlow(category: ProductCategory): Flow<List<ItemSpentByTime>> {
        return dao.totalSpentByYearFlow(category.id)
            .cancellable()
            .distinctUntilChanged()
    }

    override fun fullItemsPagedFlow(category: ProductCategory): Flow<PagingData<FullItem>> {
        return Pager(
            config = PagingConfig(pageSize = 3),
            initialKey = 0,
            pagingSourceFactory = {
                FullItemPagingSource(
                    query = { start, loadSize ->
                        dao.fullItems(
                            category.id,
                            loadSize,
                            start
                        )
                    },
                    itemsBefore = {
                        dao.countItemsBefore(
                            it,
                            category.id
                        )
                    },
                    itemsAfter = {
                        dao.countItemsAfter(
                            it,
                            category.id
                        )
                    },
                )
            }
        )
            .flow
    }

    override fun totalSpentByCategoryFlow(): Flow<List<ItemSpentByCategory>> {
        return dao.totalSpentByCategoryFlow()
            .cancellable()
            .distinctUntilChanged()
    }

    override fun totalSpentByCategoryByMonthFlow(
        year: Int,
        month: Int
    ): Flow<List<ItemSpentByCategory>> {
        val date: String = buildString {
            append(year)
            append("-")

            val monthStr: String = if (month < 10) {
                "0$month"
            } else {
                month.toString()
            }
            append(monthStr)
        }

        return dao.totalSpentByCategoryByMonthFlow(date)
            .cancellable()
            .distinctUntilChanged()
    }

    override fun allFlow(): Flow<List<ProductCategory>> {
        return dao.allFlow()
            .cancellable()
            .distinctUntilChanged()
    }

    override fun allWithAltNamesFlow(): Flow<List<ProductCategoryWithAltNames>> {
        return dao.allWithAltNamesFlow()
            .cancellable()
            .distinctUntilChanged()
    }
}