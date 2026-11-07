package vegabobo.dsusideloader.ui.screen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import vegabobo.dsusideloader.model.Session

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val session: Session,
) : ViewModel()
