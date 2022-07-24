package com.example.picsingularcore.dao

import com.example.picsingularcore.pojo.SingularCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: JpaRepository<SingularCategory,Long> {
    fun findByCategoryName(categoryName: String): SingularCategory?
}