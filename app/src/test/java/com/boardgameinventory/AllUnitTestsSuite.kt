package com.boardgameinventory

import com.boardgameinventory.data.GameTest
import com.boardgameinventory.data.ValidatedGameTest
import com.boardgameinventory.pagination.PaginationSetupTest
import com.boardgameinventory.repository.GameRepositoryTest
import com.boardgameinventory.utils.ScopedStorageUtilsTest
import com.boardgameinventory.utils.UtilsTest
import com.boardgameinventory.viewmodel.MainViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite that runs all unit tests for the BoardGame Inventory app
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    GameTest::class,
    ValidatedGameTest::class,
    UtilsTest::class,
    ScopedStorageUtilsTest::class,
    GameRepositoryTest::class,
    MainViewModelTest::class,
    PaginationSetupTest::class
)
class AllUnitTestsSuite
