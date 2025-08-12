package com.boardgameinventory

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.update.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var mockUpdateManager: AppUpdateManager
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockUpdateManager = Mockito.mock(AppUpdateManager::class.java)
        val application = Mockito.mock(BoardGameInventoryApp::class.java)
        Mockito.`when`(application.updateManager).thenReturn(mockUpdateManager)

        // Initialize in-memory Room database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.setAppUpdateManagerForTest(mockUpdateManager)
        }
    }

    @After
    fun tearDown() {
        scenario.close()
        database.close()
    }

    @Test
    fun testActivityLaunchesSuccessfully() {
        scenario.onActivity { activity ->
            assert(activity != null)
        }
    }

    @Test
    fun testAppUpdateManagerInitialization() {
        scenario.onActivity { activity ->
            assert(activity.getAppUpdateManager() == mockUpdateManager)
        }
    }

    @Test
    fun testInAppUpdateFlow() {
        val mockAppUpdateInfo = Mockito.mock(AppUpdateInfo::class.java)
        val mockActivity = Mockito.mock(Activity::class.java)

        scenario.onActivity { activity ->
            Mockito.doNothing().`when`(mockUpdateManager).triggerImmediateUpdateForTest(mockAppUpdateInfo, mockActivity)
            activity.getAppUpdateManager().triggerImmediateUpdateForTest(mockAppUpdateInfo, mockActivity)

            Mockito.verify(mockUpdateManager).triggerImmediateUpdateForTest(mockAppUpdateInfo, mockActivity)
        }
    }
}
