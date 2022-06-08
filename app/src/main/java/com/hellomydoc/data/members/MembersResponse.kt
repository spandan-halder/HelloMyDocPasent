package com.hellomydoc.data.members

data class MembersResponse(
    val members: List<Member>,
    val message: String,
    val success: Boolean
)