// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.uast.test.common.kotlin

import org.jetbrains.uast.UFile
import org.jetbrains.uast.test.env.kotlin.assertEqualsToFile
import java.io.File

interface IdentifiersTestBase {
    fun getIdentifiersFile(testName: String): File

    fun check(testName: String, file: UFile) {
        val valuesFile = getIdentifiersFile(testName)

        assertEqualsToFile("Identifiers", valuesFile, file.asIdentifiersWithParents())
        file.testIdentifiersParents()
    }

}
