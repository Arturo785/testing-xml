package com.example.testinginxml

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import kotlinx.coroutines.ExperimentalCoroutinesApi


// all this shit is to help dagger hilt to create the activity that will hold the fragments for our UI tests


//T : Fragment =  any argument passed but has to be an instance of fragment

// the Type T of type fragment could be any kind of fragment we want the
//  activity to make the transaction into
@ExperimentalCoroutinesApi
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    themeResId: Int = androidx.fragment.testing.manifest.R.style.FragmentScenarioEmptyFragmentActivityTheme,
    fragmentFactory: FragmentFactory? = null,
    @IdRes navGraphId: Int? = null,
    crossinline action: T.() -> Unit = {}
) {
    // creates intent to display activity
    // in test there is no such thing as main activity so we create it
    val mainActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
    ).putExtra(
        "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
        themeResId
    )

    // this is the activity to be launched in this case the HiltTestActivity with our intent
    ActivityScenario.launch<HiltTestActivity>(mainActivityIntent).onActivity { activity ->
        // we retrieve our factory in case we need it to create a custom fragment with injection
        fragmentFactory?.let {
            activity.supportFragmentManager.fragmentFactory = it
        }
        // we create a T fragment which could be any fragment and instantiate it with our factory
        var fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            Preconditions.checkNotNull(T::class.java.classLoader),
            T::class.java.name
        ).also { fragmentRef ->
            if (navGraphId != null) {
                // This allows fragments to use by navGraphViewModels()
                addNavControllerToFragment(navGraphId, fragmentRef)
            }
        }

        // we attach the arguments given
        fragment.arguments = fragmentArgs

        // we put the fragment retrieved into the activity
        // and launch it into UI
        activity.supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()

        (fragment as T).action() // invokes the function we gave into the T fragment passed
    }

}

fun addNavControllerToFragment(@IdRes navGraphId: Int, fragmentRef: Fragment) {
    // This allows fragments to use by navGraphViewModels()
    //https://stackoverflow.com/questions/67521121/ui-testing-viewpager-with-navgraphviewmodel-using-espresso

    // we create a testNavController, uses this dependency
    //   implementation 'androidx.navigation:navigation-testing:2.5.3'
    val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    navController.setViewModelStore(ViewModelStore()) // a default viewModel store
    // we set the navGraph that we need,
    UiThreadStatement.runOnUiThread {
        navController.setGraph(navGraphId)
    }

    // we apply the navController to the fragment because we are inside the not null check
    fragmentRef.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
        if (viewLifecycleOwner != null) {
            Navigation.setViewNavController(fragmentRef.requireView(), navController)
        }
    }
}

