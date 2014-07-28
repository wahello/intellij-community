/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInsight.template.postfix.templates;

import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public abstract class SurroundPostfixTemplateBase extends StatementWrapPostfixTemplate {

  protected SurroundPostfixTemplateBase(@NotNull String name,
                                        @NotNull String descr,
                                        @NotNull PostfixTemplatePsiInfo psiInfo,
                                        @NotNull Condition<PsiElement> typeChecker) {
    super(name, descr, psiInfo, typeChecker);
  }

  @SuppressWarnings("unchecked")
  protected SurroundPostfixTemplateBase(@NotNull String name,
                                        @NotNull String descr,
                                        @NotNull PostfixTemplatePsiInfo psiInfo) {
    super(name, descr, psiInfo, Condition.TRUE);
  }


  @Override
  public final void expandForChooseExpression(@NotNull PsiElement context, @NotNull final Editor editor) {
    PsiElement topmostExpression = myPsiInfo.getTopmostExpression(context);
    PsiElement expression = getWrappedExpression(topmostExpression);
    assert topmostExpression != null;
    PsiElement replace = topmostExpression.replace(expression);
    TextRange range = PostfixTemplatesUtils.surround(getSurrounder(), editor, replace);

    if (range != null) {
      editor.getCaretModel().moveToOffset(range.getStartOffset());
    }
  }

  public boolean isStatement() {
    return false;
  }

  @NotNull
  protected abstract Surrounder getSurrounder();
}

