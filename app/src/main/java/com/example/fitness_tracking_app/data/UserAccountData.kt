package com.example.fitness_tracking_app.data

data class UserAccountData (
    val activeAccount : Boolean? = null,
    val userData: UserData? = null
) {
    companion object {
        fun createFromLoginData(
            userLoginData: UserLoginData,
            uid: String,
            profilePhoto: String,
            backgroundPhoto: String,
            email: String
        ) : UserAccountData {
            return UserAccountData(
                activeAccount = true,
                userData = UserData(
                    uid = uid,
                    username = userLoginData.username,
                    name = userLoginData.name,
                    personalData = PersonalData(
                        gender = userLoginData.gender,
                        phoneNumber = userLoginData.phoneNumber,
                        height = userLoginData.height,
                        weightList = if (userLoginData.weight == null) null else {
                            listOf(
                                Weights(
                                    date = userLoginData.date,
                                    weight = userLoginData.weight
                                )
                            )
                        }
                    ),
                    profilePhoto = profilePhoto,
                    backgroundPhoto = backgroundPhoto,
                    email = email
                )
            )
        }
    }
}

class UserLoginData(
    val username: String,
    val name: String,
    val gender: String,
    val phoneNumber: Long,
    val weight: Double?,
    val height: Int?,
    val date: Long
)