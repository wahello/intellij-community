// ACTION_CLASS: org.jetbrains.kotlin.idea.actions.generate.KotlinGenerateTestSupportActionBase$SetUp
// CONFIGURE_LIBRARY: JUnit
// TEST_FRAMEWORK: JUnit4
open class A {
    open fun setUp() {

    }
}

class B : A() {<caret>

}