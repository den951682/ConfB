package com.force.confbb.data.test

import com.force.confbb.data.UserDataRepository
import com.force.model.DarkThemeConfig
import com.force.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserDataRepository @Inject constructor() : UserDataRepository {
    override val userData: Flow<UserData>
        get() = flow {
            emit(UserData(darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM))
        }
}
