// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.util

import com.intellij.openapi.progress.timeoutRunBlocking
import com.intellij.openapi.progress.timeoutWaitUp
import com.intellij.testFramework.ApplicationExtension
import com.intellij.testFramework.UncaughtExceptionsExtension
import com.intellij.util.concurrency.Semaphore
import com.intellij.util.ui.EDT
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import javax.swing.SwingUtilities

class EventDispatchThreadTest {

  companion object {

    @RegisterExtension
    @JvmField
    val applicationExtension = ApplicationExtension()

    @RegisterExtension
    @JvmField
    val uncaughtExceptionsExtension = UncaughtExceptionsExtension()
  }

  @Test
  fun `EDT thread interrupted flag is cleared`(): Unit = timeoutRunBlocking {
    val started = Semaphore(1)
    val neverEndingTask = launch(start = CoroutineStart.UNDISPATCHED) {
      suspendCancellableCoroutine { continuation ->
        SwingUtilities.invokeLater {
          started.up()
          while (continuation.isActive) {
            // Here we emulate cancellation by other means, e.g. explicitly passed progress (continuation in this case).
            // Don't block here, otherwise it will throw InterruptedException and clear the flag.
          }
        }
      }
    }
    started.timeoutWaitUp()

    val edt = EDT.getEventDispatchThread()
    edt.interrupt()
    neverEndingTask.cancel() // allow the never-ending EDT event to exit the loop and end

    suspendCancellableCoroutine { continuation ->
      SwingUtilities.invokeLater {
        continuation.resumeWith(kotlin.runCatching {
          Assertions.assertSame(edt, Thread.currentThread())
        })
      }
    }
  }
}
