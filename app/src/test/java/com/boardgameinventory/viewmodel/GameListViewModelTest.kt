package com.boardgameinventory.viewmodel

import com.boardgameinventory.data.SearchAndFilterCriteria
import org.junit.Assert.*
import org.junit.Test

class GameListViewModelTest {
    @Test
    fun `SearchAndFilterCriteria should have default values`() {
        val criteria = SearchAndFilterCriteria()
        assertEquals("", criteria.searchQuery)
        assertNull(criteria.bookcaseFilter)
        assertNull(criteria.locationFilter)
    }

    @Test
    fun `GameListViewModel class should exist`() {
        assertNotNull("GameListViewModel class should exist", GameListViewModel::class.java)
    }
}
