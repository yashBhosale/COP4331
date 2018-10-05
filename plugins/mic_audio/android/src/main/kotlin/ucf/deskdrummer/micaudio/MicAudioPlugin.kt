package ucf.deskdrummer.micaudio

import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.PluginRegistry.Registrar

import android.media.AudioRecord
import android.media.AudioRecord.OnRecordPositionUpdateListener
import android.media.AudioFormat
import android.media.MediaRecorder

import kotlin.concurrent.thread

class MicAudioPlugin(mRegistrar: Registrar): MethodCallHandler, EventChannel.StreamHandler {
private lateinit var mRecorder: AudioRecord
private lateinit var mAudioData: ByteArray
private var mReadThreadRunning: Boolean = false
private var mEventSink: EventChannel.EventSink? = null

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar): Unit {
      val plugin = MicAudioPlugin(registrar)
      val channel = MethodChannel(registrar.messenger(), "micAudio")
      channel.setMethodCallHandler(plugin)

      val eventChannel = EventChannel(registrar.messenger(), "micAudioStream")
      eventChannel.setStreamHandler(plugin)
    }
  }



  private fun initializeMicAudio(): Boolean {
    val SAMPLE_RATE = 44100
    val CHANNEL = AudioFormat.CHANNEL_IN_MONO
    val SOURCE = MediaRecorder.AudioSource.MIC
    val FORMAT = AudioFormat.ENCODING_PCM_8BIT
    val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT)
    mAudioData = ByteArray(BUFFER_SIZE / 2)
    val posUpdateListener = object : OnRecordPositionUpdateListener {
      override fun onPeriodicNotification(recorder: AudioRecord?) {
//        val numRead = recorder?.read(mAudioData, 0, mAudioData.size)
//        if (numRead != null) {
//          for (i in mAudioData.indices) {
//            mAudioData[i] = if (i >= numRead) -1 else mAudioData[i]
//          }
//        }
//        mEventSink?.success(mAudioData)
      }

      override fun onMarkerReached(recorder: AudioRecord?) {}
    }
    mRecorder = AudioRecord(SOURCE, SAMPLE_RATE, CHANNEL, FORMAT, BUFFER_SIZE)
    mRecorder.setRecordPositionUpdateListener(posUpdateListener)
    mRecorder.setPositionNotificationPeriod((SAMPLE_RATE / 5))
    return mRecorder != null
  }

  override fun onListen(args: Any?, eventSink: EventChannel.EventSink) {
    mEventSink = eventSink
    mRecorder?.startRecording()
    thread(start = true) {
      mReadThreadRunning = true
      while (mReadThreadRunning) {
        val numRead = mRecorder?.read(mAudioData, 0, mAudioData.size)
        if (numRead != null) {
          for (i in mAudioData.indices) {
            mAudioData[i] = if (i >= numRead) -1 else mAudioData[i]
          }
        }
        mEventSink?.success(mAudioData)
      }
    }
  }

  override fun onCancel(args: Any?) {
    mReadThreadRunning = false
    mRecorder?.stop()
    mEventSink = null
  }

  override fun onMethodCall(call: MethodCall, result: Result): Unit {
    when (call.method) {
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "initializeMicAudio" -> result.success(initializeMicAudio())
      else -> result.notImplemented()
    }
  }
}

