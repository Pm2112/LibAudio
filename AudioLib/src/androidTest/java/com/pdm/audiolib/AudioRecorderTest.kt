package com.pdm.audiolib

import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AudioRecorderTest {

    private lateinit var audioRecorder: AudioRecorder
    private lateinit var activity: ComponentActivity

    @Before
    fun setUp() {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activityInstance ->
            activity = activityInstance
            audioRecorder = AudioRecorder(activity)
        }
    }

    @After
    fun tearDown() {
        audioRecorder.discardRecording()
    }

    @Test
    fun testStartRecording() {
        audioRecorder.startRecording()
        Assert.assertTrue(audioRecorder.isRecording())
    }

    @Test
    fun testStopRecording() {
        audioRecorder.startRecording()
        audioRecorder.stopRecording()
        Assert.assertFalse(audioRecorder.isRecording())
    }



    @Test
    fun testSavePCMRecording() {
        audioRecorder.startRecording()
        audioRecorder.stopRecording()

        val file = File(activity.filesDir, "test_audio.pcm")
        audioRecorder.saveRecording(file, AudioFormatType.PCM)

        Assert.assertTrue(file.exists())
        Assert.assertTrue(file.length() > 0)
    }

    @Test
    fun testDiscardRecording() {
        audioRecorder.startRecording()
        audioRecorder.stopRecording()

        audioRecorder.discardRecording()

        val file = File(activity.filesDir, "discard_test.pcm")
        audioRecorder.saveRecording(file, AudioFormatType.PCM)

        Assert.assertFalse(file.exists() && file.length() > 0)
    }
}