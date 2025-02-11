// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.externalSystem.util

import com.intellij.configurationStore.StoreUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.testFramework.runInEdtAndWait

fun Project.use(save: Boolean = false, action: (Project) -> Unit) {
  try {
    action(this)
  }
  finally {
    forceCloseProject(save)
  }
}

fun Project.forceCloseProject(save: Boolean = false) {
  runInEdtAndWait {
    val projectManager = ProjectManagerEx.getInstanceEx()
    if (save) {
      StoreUtil.saveSettings(this, forceSavingAllSettings = true)
    }
    projectManager.forceCloseProject(this)
  }
}
