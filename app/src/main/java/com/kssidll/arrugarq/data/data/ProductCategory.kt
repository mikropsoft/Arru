package com.kssidll.arrugarq.data.data

import androidx.room.*

@Entity(
    indices = [
        Index(
            value = ["name"],
            unique = true
        )
    ]
)
data class ProductCategory(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
) {
    constructor(
        name: String,
    ): this(
        0,
        name
    )
}

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ProductCategory::class,
            parentColumns = ["id"],
            childColumns = ["productCategoryId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.RESTRICT,
        )
    ],
    indices = [
        Index(
            value = ["name"],
            unique = true
        )
    ]
)
data class ProductCategoryAltName(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(index = true) val productCategoryId: Long,
    val name: String,
) {
    constructor(
        productCategoryId: Long,
        name: String,
    ): this(
        0,
        productCategoryId,
        name
    )
}

data class ProductCategoryWithAltNames(
    @Embedded val productCategory: ProductCategory,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    ) val alternativeNames: List<ProductAltName>
)