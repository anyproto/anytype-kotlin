package com.anytypeio.anytype.ui.date

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModelFactory
import com.anytypeio.anytype.feature_date.ui.DateLayoutScreen
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class DateLayoutFragment : BaseComposeFragment() {
    @Inject
    lateinit var factory: AllContentViewModelFactory

    private val vm by viewModels<AllContentViewModel> { factory }

    private val space get() = argString(ARG_SPACE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme(
            typography = typography
        ) {
            DateLayoutScreenWrapper()
        }
    }

    @Composable
    fun DateLayoutScreenWrapper() {
        NavHost(
            navController = rememberNavController(),
            startDestination = DATE_MAIN
        ) {
            composable(route = DATE_MAIN) {
                DateLayoutScreen()
            }
        }
    }

    override fun injectDependencies() {
        TODO("Not yet implemented")
    }

    override fun releaseDependencies() {
        TODO("Not yet implemented")
    }

    companion object DateLayoutNavigation {
        private const val DATE_MAIN = "date_main"
        private const val DATE_CALENDAR = "date_calendar"
        private const val DATE_ALL_RELATIONS = "date_all_relations"
        const val ARG_SPACE = "space"
    }
}