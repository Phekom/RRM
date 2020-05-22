package za.co.xisystems.itis_rrm.ui.auth


import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import za.co.xisystems.itis_rrm.R

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(LoginActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_SMS",
            "android.permission.CAMERA",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_PHONE_STATE"
        )

    @Test
    fun loginActivityTest() {
        val appCompatEditText = onView(
            allOf(
                withId(R.id.register_username_editText),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.usernameWrapper),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("rrmcontractor"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.register_password_editText),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.passwordWrapper),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText("123456"), closeSoftKeyboard())

        val appCompatButton = onView(
            allOf(
                withId(R.id.register_button), withText("Login"),
                childAtPosition(
                    allOf(
                        withId(R.id.layout),
                        childAtPosition(
                            withId(R.id.reg_Things),
                            0
                        )
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        appCompatButton.perform(click())

        val appCompatButton2 = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.buttonPanel),
                        0
                    ),
                    3
                )
            )
        )
        appCompatButton2.perform(scrollTo(), click())

        val appCompatEditText3 = onView(
            allOf(
                withId(R.id.enterPinEditText),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("com.google.android.material.textfield.TextInputLayout")),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText3.perform(replaceText("1234"), closeSoftKeyboard())

        val appCompatEditText4 = onView(
            allOf(
                withId(R.id.confirmPinEditText),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("com.google.android.material.textfield.TextInputLayout")),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText4.perform(replaceText("1234"), closeSoftKeyboard())

        val appCompatButton3 = onView(
            allOf(
                withId(R.id.register_Pin_button), withText("Register"),
                childAtPosition(
                    allOf(
                        withId(R.id.layout),
                        childAtPosition(
                            withId(R.id.reg_Things),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatButton3.perform(click())

        val appCompatImageButton = onView(
            allOf(
                withContentDescription("Open navigation drawer"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton.perform(click())

        val navigationMenuItemView = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        navigationMenuItemView.perform(click())

        val appCompatSpinner = onView(
            allOf(
                withId(R.id.contractSpinner),
                childAtPosition(
                    allOf(
                        withId(R.id.selectProjectLayout),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatSpinner.perform(click())

        val appCompatSpinner2 = onView(
            allOf(
                withId(R.id.contractSpinner),
                childAtPosition(
                    allOf(
                        withId(R.id.selectProjectLayout),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatSpinner2.perform(click())

        val checkedTextView = onData(anything())
            .inAdapterView(
                childAtPosition(
                    withClassName(`is`("android.widget.PopupWindow$PopupBackgroundView")),
                    0
                )
            )
            .atPosition(2)
        checkedTextView.perform(click())

        val appCompatSpinner3 = onView(
            allOf(
                withId(R.id.contractSpinner),
                childAtPosition(
                    allOf(
                        withId(R.id.selectProjectLayout),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatSpinner3.perform(click())

        val appCompatSpinner4 = onView(
            allOf(
                withId(R.id.contractSpinner),
                childAtPosition(
                    allOf(
                        withId(R.id.selectProjectLayout),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatSpinner4.perform(click())

        val checkedTextView2 = onData(anything())
            .inAdapterView(
                childAtPosition(
                    withClassName(`is`("android.widget.PopupWindow$PopupBackgroundView")),
                    0
                )
            )
            .atPosition(2)
        checkedTextView2.perform(click())

        val appCompatEditText5 = onView(
            allOf(
                withId(R.id.descriptionEditText),
                childAtPosition(
                    allOf(
                        withId(R.id.selectProjectLayout),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        appCompatEditText5.perform(replaceText("Smoke test 999"), closeSoftKeyboard())

        val materialButton = onView(
            allOf(
                withId(R.id.selectContractProjectContinueButton),
                withText("Confirm Project Selection"),
                childAtPosition(
                    allOf(
                        withId(R.id.selectProjectLayout),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    7
                ),
                isDisplayed()
            )
        )
        materialButton.perform(click())

        val appCompatImageButton2 = onView(
            allOf(
                withId(R.id.addItemButton),
                childAtPosition(
                    allOf(
                        withId(R.id.addItemLayout),
                        childAtPosition(
                            withId(R.id.mid_lin),
                            1
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton2.perform(click())

        val appCompatSpinner5 = onView(
            allOf(
                withId(R.id.sectionItemSpinner),
                childAtPosition(
                    allOf(
                        withId(R.id.linearLayout),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatSpinner5.perform(click())

        val checkedTextView3 = onData(anything())
            .inAdapterView(
                childAtPosition(
                    withClassName(`is`("android.widget.PopupWindow$PopupBackgroundView")),
                    0
                )
            )
            .atPosition(7)
        checkedTextView3.perform(click())

        val recyclerView = onView(
            allOf(
                withId(R.id.item_recyclerView),
                childAtPosition(
                    withId(R.id.selectProjectItemdrop),
                    0
                )
            )
        )
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(1, click()))

        val appCompatImageButton3 = onView(
            allOf(
                withId(R.id.addItemButton),
                childAtPosition(
                    allOf(
                        withId(R.id.addItemLayout),
                        childAtPosition(
                            withId(R.id.mid_lin),
                            1
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton3.perform(click())

        val appCompatSpinner6 = onView(
            allOf(
                withId(R.id.sectionItemSpinner),
                childAtPosition(
                    allOf(
                        withId(R.id.linearLayout),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatSpinner6.perform(click())

        val checkedTextView4 = onData(anything())
            .inAdapterView(
                childAtPosition(
                    withClassName(`is`("android.widget.PopupWindow$PopupBackgroundView")),
                    0
                )
            )
            .atPosition(2)
        checkedTextView4.perform(click())

        val recyclerView2 = onView(
            allOf(
                withId(R.id.item_recyclerView),
                childAtPosition(
                    withId(R.id.selectProjectItemdrop),
                    0
                )
            )
        )
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(1, click()))

        val recyclerView3 = onView(
            allOf(
                withId(R.id.project_recyclerView),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    1
                )
            )
        )
        recyclerView3.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        val materialButton2 = onView(
            allOf(
                withId(R.id.startPhotoButton), withText("Start"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton2.perform(click())

        val materialButton3 = onView(
            allOf(
                withId(R.id.startPhotoButton), withText("Start"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton3.perform(click())

        val materialButton4 = onView(
            allOf(
                withId(R.id.endPhotoButton), withText("End"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton4.perform(click())

        val appCompatEditText6 = onView(
            allOf(
                withId(R.id.valueEditText), withText("1"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.costCard),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatEditText6.perform(replaceText("10"))

        val appCompatEditText7 = onView(
            allOf(
                withId(R.id.valueEditText), withText("10"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.costCard),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatEditText7.perform(closeSoftKeyboard())

        val materialButton5 = onView(
            allOf(
                withId(R.id.updateButton), withText("UPDATE"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.photoLin),
                        4
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        materialButton5.perform(click())

        val recyclerView4 = onView(
            allOf(
                withId(R.id.project_recyclerView),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    1
                )
            )
        )
        recyclerView4.perform(actionOnItemAtPosition<ViewHolder>(1, click()))

        val materialButton6 = onView(
            allOf(
                withId(R.id.startPhotoButton), withText("Start"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton6.perform(click())

        val materialButton7 = onView(
            allOf(
                withId(R.id.endPhotoButton), withText("End"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton7.perform(click())

        val appCompatEditText8 = onView(
            allOf(
                withId(R.id.valueEditText), withText("1"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.costCard),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatEditText8.perform(replaceText(""))

        val appCompatEditText9 = onView(
            allOf(
                withId(R.id.valueEditText),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.costCard),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatEditText9.perform(closeSoftKeyboard())

        val appCompatEditText10 = onView(
            allOf(
                withId(R.id.valueEditText),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.costCard),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatEditText10.perform(click())

        val appCompatEditText11 = onView(
            allOf(
                withId(R.id.valueEditText),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.costCard),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatEditText11.perform(replaceText("20"), closeSoftKeyboard())

        val materialButton8 = onView(
            allOf(
                withId(R.id.updateButton), withText("UPDATE"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.photoLin),
                        4
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        materialButton8.perform(click())

        val cardView = onView(
            allOf(
                withId(R.id.dueDateCardView),
                childAtPosition(
                    allOf(
                        withId(R.id.last_lin),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            2
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        cardView.perform(click())

        val materialButton9 = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.ScrollView")),
                        0
                    ),
                    3
                )
            )
        )
        materialButton9.perform(scrollTo(), click())

        val materialButton10 = onView(
            allOf(
                withId(R.id.submitButton), withText("submit"),
                childAtPosition(
                    allOf(
                        withId(R.id.last_lin),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            2
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        materialButton10.perform(click())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
