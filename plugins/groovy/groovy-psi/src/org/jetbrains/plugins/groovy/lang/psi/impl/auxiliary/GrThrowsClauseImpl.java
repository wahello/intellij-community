// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.plugins.groovy.lang.psi.impl.auxiliary;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.light.LightClassReference;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrThrowsClause;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef.GrReferenceListImpl;
import org.jetbrains.plugins.groovy.lang.psi.stubs.GrReferenceListStub;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

import java.util.List;

/**
 * @author: Dmitry.Krasilschikov
 * @date: 03.04.2007
 */
public class GrThrowsClauseImpl extends GrReferenceListImpl implements GrThrowsClause {
  public GrThrowsClauseImpl(GrReferenceListStub stub) {
    super(stub, GroovyElementTypes.THROW_CLAUSE);
  }

  @Override
  protected IElementType getKeywordType() {
    return GroovyTokenTypes.kTHROWS;
  }

  public GrThrowsClauseImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull GroovyElementVisitor visitor) {
    visitor.visitThrowsClause(this);
  }

  public String toString() {
    return "Throw clause";
  }

  @Override
  @NotNull
  public PsiJavaCodeReferenceElement[] getReferenceElements() {
    PsiClassType[] types = getReferencedTypes();
    if (types.length == 0) return PsiJavaCodeReferenceElement.EMPTY_ARRAY;

    PsiManagerEx manager = getManager();

    List<PsiJavaCodeReferenceElement> result = ContainerUtil.newArrayList();
    for (PsiClassType type : types) {
      PsiClassType.ClassResolveResult resolveResult = type.resolveGenerics();
      PsiClass resolved = resolveResult.getElement();
      if (resolved != null) {
        result.add(new LightClassReference(manager, type.getCanonicalText(), resolved, resolveResult.getSubstitutor()));
      }
    }
    return result.toArray(PsiJavaCodeReferenceElement.EMPTY_ARRAY);
  }

  @Override
  public Role getRole() {
    return Role.THROWS_LIST;
  }

  @Override
  public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
    if (element instanceof GrCodeReferenceElement || element instanceof PsiJavaCodeReferenceElement) {
      if (findChildByClass(GrCodeReferenceElement.class) == null) {
        getNode().addLeaf(GroovyTokenTypes.kTHROWS, "throws", null);
      }
      else {
        PsiElement lastChild = getLastChild();
        lastChild = PsiUtil.skipWhitespacesAndComments(lastChild, false);
        if (!lastChild.getNode().getElementType().equals(GroovyTokenTypes.mCOMMA)) {
          getNode().addLeaf(GroovyTokenTypes.mCOMMA, ",", null);
        }
      }

      if (element instanceof PsiJavaCodeReferenceElement) {
        element = GroovyPsiElementFactory.getInstance(getProject()).createCodeReferenceElementFromText(element.getText());
      }
    }
    return super.add(element);
  }

}
