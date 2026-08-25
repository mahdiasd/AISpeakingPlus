package ir.speaking.feature.category.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.category.db.CategoryDAO
import ir.speaking.feature.category.db.toModel
import ir.speaking.feature.category.model.Category
import org.koin.core.annotation.Single
import java.util.*


@Single
class CategoryRepositoryImpl : CategoryRepository {
    override suspend fun getCategoryById(id: UUID): Category? =
        suspendTransaction {
            CategoryDAO.findById(id)?.toModel()
        }

    override suspend fun getAllCategories(): List<Category> = 
        suspendTransaction {
            CategoryDAO.all().map { it.toModel() }
        }

    override suspend fun createCategory(category: Category): Category =
        suspendTransaction {
            CategoryDAO.new {
                name = category.name
                imageUrl = category.imageUrl
                createdAt = category.createdAt
            }.toModel()
        }

    override suspend fun updateCategory(category: Category): Category? =
        suspendTransaction {
            CategoryDAO.findById(category.id)?.apply {
                name = category.name
                imageUrl = category.imageUrl
            }?.toModel()
        }

    override suspend fun deleteCategory(id: UUID): Boolean =
        suspendTransaction {
            CategoryDAO.findById(id)?.let {
                it.delete()
                true
            } ?: false
        }
}