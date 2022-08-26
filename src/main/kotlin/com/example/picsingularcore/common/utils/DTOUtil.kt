package com.example.picsingularcore.common.utils

import com.example.picsingularcore.pojo.dto.PagesDTO
import org.springframework.data.domain.Page
import kotlin.math.ceil

object DTOUtil {

    fun <T> pagesToPagesDTO(singularPages: Page<T>): PagesDTO<T> {
        return PagesDTO(
            hasMore = singularPages.hasNext(),
            totalPages = singularPages.totalPages,
            totalElements = singularPages.totalElements,
            currentPage = singularPages.number + 1,
            currentElements = singularPages.numberOfElements,
            pageSize = singularPages.size,
            isFirst = singularPages.isFirst,
            isLast = singularPages.isLast,
            hasPrevious = singularPages.hasPrevious(),
            dataList = singularPages.content
        )
    }

    fun <T> listToPageDTO(userList: List<T>, page: Int, size: Int): PagesDTO<T> {
        val currentElements = if (page * size <= userList.size) size else if (page == 1 && userList.size < size) userList.size else userList.size % size
        val totalPages = ceil(userList.size / size.toDouble()).toInt()
        return PagesDTO(
            hasMore = page * size < userList.size,
            totalPages = totalPages,
            totalElements = userList.size.toLong(),
            currentPage = page,
            currentElements = currentElements,
            pageSize = size,
            isFirst = page == 1,
            isLast = page == totalPages,
            hasPrevious = page != 1,
            dataList = userList
        )
    }
}