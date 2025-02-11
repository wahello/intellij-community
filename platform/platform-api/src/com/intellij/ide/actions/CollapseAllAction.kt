// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ide.actions

import com.intellij.ide.TreeExpander
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.IdeActions.ACTION_COLLAPSE_ALL
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys.TOOL_WINDOW
import com.intellij.openapi.actionSystem.PlatformDataKeys.TREE_EXPANDER
import com.intellij.openapi.actionSystem.ex.ActionUtil.copyFrom
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.ExperimentalUI

class CollapseAllAction : DumbAwareAction {
  private val getTreeExpander: (AnActionEvent) -> TreeExpander?

  constructor() : super() {
    getTreeExpander = { TREE_EXPANDER.getData(it.dataContext) ?: findTreeExpander(it) }
  }

  constructor(getExpander: (AnActionEvent) -> TreeExpander?) : super() {
    getTreeExpander = getExpander
    copyFrom(this, ACTION_COLLAPSE_ALL)
  }

  override fun actionPerformed(event: AnActionEvent) {
    val expander = getTreeExpander(event) ?: return
    if (expander.canCollapse()) expander.collapseAll()
  }

  override fun update(event: AnActionEvent) {
    val expander = getTreeExpander(event)
    val hideIfMissing = event.getData(PlatformDataKeys.TREE_EXPANDER_HIDE_ACTIONS_IF_NO_EXPANDER) ?: false
    event.presentation.isVisible = expander == null && !hideIfMissing ||
                                   expander != null && expander.isCollapseAllVisible && expander.isVisible(event)
    event.presentation.isEnabled = expander != null && expander.canCollapse()
    if (ExperimentalUI.isNewUI() && ActionPlaces.isPopupPlace(event.place)) {
      event.presentation.icon = null
    }
  }

  companion object {
    // find tree expander for a toolbar of a tool window
    internal fun findTreeExpander(event: AnActionEvent): TreeExpander? {
      if (!event.isFromActionToolbar) return null
      val window = TOOL_WINDOW.getData(event.dataContext) ?: return null
      val component = window.contentManagerIfCreated?.selectedContent?.component ?: return null
      val provider = component as? DataProvider ?: return null
      return TREE_EXPANDER.getData(provider)
    }
  }
}
