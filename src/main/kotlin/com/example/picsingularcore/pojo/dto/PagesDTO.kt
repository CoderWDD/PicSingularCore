package com.example.picsingularcore.pojo.dto

data class PagesDTO<T> (
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int,
    val currentElements: Int,
    val pageSize: Int,
    val hasMore: Boolean,
    val isFirst: Boolean,
    val isLast: Boolean,
    val hasPrevious: Boolean,
    val singulars: List<T>
)