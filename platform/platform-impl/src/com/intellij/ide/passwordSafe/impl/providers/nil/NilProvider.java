/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
package com.intellij.ide.passwordSafe.impl.providers.nil;

import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.ide.passwordSafe.impl.PasswordSafeProvider;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The most secure provider that does not store anything, so it cannot be cracked
 */
public final class NilProvider extends PasswordSafeProvider {

  @Override
  public boolean isSupported() {
    return true;
  }

  @Override
  public String getDescription() {
    return "The provider that does not remembers password.";
  }

  @Override
  public String getName() {
    return "Do not Store";
  }

  @Nullable
  public String getPassword(@Nullable Project project, @NotNull Class requester, String key,
                            @Nullable ModalityState modalityState) throws PasswordSafeException {
    // nothing is stored
    return null;
  }

  @Override
  public void removePassword(@Nullable Project project, @NotNull Class requester, String key,
                             @Nullable ModalityState modalityState) throws PasswordSafeException {
    // do nothing
  }

  @Override
  public void storePassword(@Nullable Project project, @NotNull Class requester, String key, String value,
                            @Nullable ModalityState modalityState) throws PasswordSafeException {
    // just forget about password
  }
}
