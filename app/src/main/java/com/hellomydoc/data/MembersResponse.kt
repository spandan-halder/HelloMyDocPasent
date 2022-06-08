package com.hellomydoc.data
import com.hellomydoc.User

data class MembersResponse(
    val success: Boolean,
    val message: String,
    val members: List<User>
)
