package com.shivams.mockmate.ui.util

import com.shivams.mockmate.R

object AvatarUtils {
    val AVATAR_MAP: Map<String, Int> = mapOf(
        "student_male" to R.drawable.avatar_student_male,
        "student_female" to R.drawable.avatar_student_female,
        "professional_male" to R.drawable.avatar_professional_male,
        "professional_female" to R.drawable.avatar_professional_female
    )

    fun getAvatarResId(avatarKey: String): Int {
        return AVATAR_MAP[avatarKey] ?: R.drawable.mentor_avatar
    }
}
