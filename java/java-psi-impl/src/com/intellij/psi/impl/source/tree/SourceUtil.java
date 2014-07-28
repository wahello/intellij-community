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
package com.intellij.psi.impl.source.tree;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/** @deprecated use {@link JavaSourceUtil} (to be removed in IDEA 15) */
@SuppressWarnings("UnusedDeclaration")
public class SourceUtil {
  private SourceUtil() { }

  @NotNull
  public static String getReferenceText(@NotNull PsiJavaCodeReferenceElement ref) {
    return JavaSourceUtil.getReferenceText(ref);
  }

  @NotNull
  public static String getReferenceText(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
    return JavaSourceUtil.getReferenceText(tree, node);
  }

  public static TreeElement addParenthToReplacedChild(@NotNull IElementType parenthType, @NotNull TreeElement newChild, @NotNull PsiManager manager) {
    return JavaSourceUtil.addParenthToReplacedChild(parenthType, newChild, manager);
  }
}
