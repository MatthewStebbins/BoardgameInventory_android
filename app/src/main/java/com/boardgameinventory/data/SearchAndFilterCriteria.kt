package com.boardgameinventory.data

data class SearchAndFilterCriteria(
    val searchQuery: String = "",
    val bookcaseFilter: String? = null,
    val locationFilter: String? = null,
    val dateFromFilter: Long? = null,
    val dateToFilter: Long? = null,
    val sortBy: SortCriteria = SortCriteria.NAME_ASC
)

enum class SortCriteria {
    NAME_ASC,
    NAME_DESC,
    DATE_ADDED_ASC,
    DATE_ADDED_DESC,
    LOCATION_ASC,
    LOCATION_DESC
}
