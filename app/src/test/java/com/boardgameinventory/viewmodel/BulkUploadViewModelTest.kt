

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.boardgameinventory.data.Game
import com.boardgameinventory.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

import com.boardgameinventory.viewmodel.BulkUploadViewModel
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BulkUploadViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var application: Application
    @Mock
    private lateinit var repository: GameRepository

    private lateinit var viewModel: BulkUploadViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        // Use reflection to inject the mock repository if not in constructor
        viewModel = BulkUploadViewModel(application)
        val repoField = BulkUploadViewModel::class.java.getDeclaredField("repository")
        repoField.isAccessible = true
        repoField.set(viewModel, repository)
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
    fun `processBulkUpload success scenario`() = runTest(testDispatcher) {
        viewModel.addGameBarcode("123")
        viewModel.addGameBarcode("456")
        // Mock repository to always succeed
        whenever(repository.getGameByBarcode(any())).thenReturn(null)
        whenever(repository.insertGame(any())).thenReturn(1L)
        viewModel.processBulkUpload("A", "1")
        testDispatcher.scheduler.advanceUntilIdle()
        val result = viewModel.uploadResult.value
        Assert.assertNotNull(result)
        Assert.assertEquals(2, result?.successful)
        Assert.assertTrue(result?.failed?.isEmpty() == true)
    }

    @Test
    fun `processBulkUpload with failures`() = runTest(testDispatcher) {
        viewModel.addGameBarcode("123")
        viewModel.addGameBarcode("456")
        // First barcode fails, second succeeds
        whenever(repository.getGameByBarcode("123")).thenThrow(RuntimeException("fail"))
        whenever(repository.getGameByBarcode("456")).thenReturn(null)
        whenever(repository.insertGame(any())).thenReturn(1L)
        viewModel.processBulkUpload("A", "1")
        testDispatcher.scheduler.advanceUntilIdle()
        val result = viewModel.uploadResult.value
        Assert.assertNotNull(result)
        Assert.assertEquals(1, result?.successful)
        Assert.assertEquals(listOf("123"), result?.failed)
    }

    @Test
    fun `clearUploadResult resets uploadResult`() = runTest(testDispatcher) {
        viewModel.addGameBarcode("123")
        whenever(repository.getGameByBarcode(any())).thenReturn(null)
        whenever(repository.insertGame(any())).thenReturn(1L)
        viewModel.processBulkUpload("A", "1")
        testDispatcher.scheduler.advanceUntilIdle()
        Assert.assertNotNull(viewModel.uploadResult.value)
        viewModel.clearUploadResult()
        Assert.assertNull(viewModel.uploadResult.value)
    }
}
