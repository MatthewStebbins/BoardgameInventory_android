import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.boardgameinventory.viewmodel.BulkUploadViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BulkUploadViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: BulkUploadViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = BulkUploadViewModel(application)
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
}
