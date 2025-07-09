package com.force.confbb.data

import com.force.confbb.model.DarkThemeConfig
import com.force.confbb.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeUserDataRepository @Inject constructor() : UserDataRepository {
    override val userData: Flow<UserData>
        get() = flow {
            emit(UserData(darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM))
        }
}
