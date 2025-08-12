import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import com.boardgameinventory.viewmodel.BulkUploadViewModel.UploadResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.boardgameinventory.viewmodel.BulkUploadViewModel
import com.boardgameinventory.repository.GameRepository
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.data.GameDao
import com.boardgameinventory.data.Game
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(org.robolectric.RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BulkUploadViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: BulkUploadViewModel
    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockRepository: GameRepository

    @MockK
    private lateinit var mockDatabase: AppDatabase

    @MockK
    private lateinit var mockGameDao: GameDao

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        Dispatchers.setMain(testDispatcher)
        val application = ApplicationProvider.getApplicationContext<Application>()
        mockGameDao = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true) {
            every { gameDao() } returns mockGameDao
        }
        mockRepository = GameRepository(mockGameDao, application)
        viewModel = BulkUploadViewModel(mockRepository, application)

        runBlocking {
            coEvery { mockRepository.getGameByBarcode(any()) } returns Game(
                id = 1L,
                name = "Test Game",
                barcode = "123",
                bookcase = "A",
                shelf = "1"
            )
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addGameBarcode adds unique barcode`() {
        viewModel.addGameBarcode("123")
        viewModel.addGameBarcode("456")
        viewModel.addGameBarcode("123") // duplicate
        Assert.assertEquals(listOf("123", "456"), viewModel.scannedBarcodes.value)
    }

    @Test
    fun `removeGameBarcode removes barcode`() {
        viewModel.addGameBarcode("123")
        viewModel.addGameBarcode("456")
        viewModel.removeGameBarcode("123")
        Assert.assertEquals(listOf("456"), viewModel.scannedBarcodes.value)
    }

    @Test
    fun `clearUploadResult resets uploadResult`() {
        viewModel.clearUploadResult()
        Assert.assertNull(viewModel.uploadResult.value)
    }

    @Test
    fun `getGameByBarcode handles suspend function correctly`() = runBlocking {
        coEvery { mockRepository.getGameByBarcode("123") } returns Game(
            id = 1L,
            name = "Test Game",
            barcode = "123",
            bookcase = "A",
            shelf = "1"
        )

        val result = mockRepository.getGameByBarcode("123")

        Assert.assertNotNull(result)
        Assert.assertEquals("123", result?.barcode)
        Assert.assertEquals("Test Game", result?.name)
    }
}
