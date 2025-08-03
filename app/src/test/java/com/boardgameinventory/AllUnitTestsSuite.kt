package com.boardgameinventory

import com.boardgameinventory.data.GameTest
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
    UtilsTest::class,
    ScopedStorageUtilsTest::class,
    GameRepositoryTest::class,
    MainViewModelTest::class
)
class AllUnitTestsSuite
