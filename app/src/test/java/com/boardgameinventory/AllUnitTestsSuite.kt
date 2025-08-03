package com.boardgameinventory

import com.boardgameinventory.data.GameTest
import com.boardgameinventory.data.ValidatedGameTest
import com.boardgameinventory.pagination.PaginationSetupTest
import com.boardgameinventory.pagination.SimplePaginationTest
import com.boardgameinventory.pagination.PaginationValidationTest
import com.boardgameinventory.repository.GameRepositoryTest
import com.boardgameinventory.utils.ScopedStorageUtilsTest
import com.boardgameinventory.utils.UtilsTest
import com.boardgameinventory.validation.ValidationUtilsTest
import com.boardgameinventory.validation.InputValidationExtensionsTest
import com.boardgameinventory.validation.SecurityValidationTest
import com.boardgameinventory.viewmodel.MainViewModelTest
import com.boardgameinventory.viewmodel.GameListViewModelTest
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
    ValidationUtilsTest::class,
    InputValidationExtensionsTest::class,
    SecurityValidationTest::class,
    GameRepositoryTest::class,
    MainViewModelTest::class,
    GameListViewModelTest::class,
    PaginationSetupTest::class,
    SimplePaginationTest::class,
    PaginationValidationTest::class
)
class AllUnitTestsSuite
