package com.hellomydoc

object Reg {
    val email: Regex
        get() = Constants.EMAIL_REGEX.toRegex()
    val password: Regex
        get() = Constants.PASSWORD_REGEX.toRegex()
    val mobile: Regex
        get() = Constants.MOBILE_REGEX.toRegex()
    val person: Regex
        get() = Constants.PERSON_REGEX.toRegex()
}

val String.isEmail: Boolean
    get() = matches(Reg.email)

val String.isMobile: Boolean
    get() = matches(Reg.mobile)

val String.isNotMobile: Boolean
    get() = !matches(Reg.mobile)

val String.isEmpty: Boolean
    get() = isEmpty()

val String.isNotEmpty: Boolean
    get() = isNotEmpty()

val String.isNotEmail: Boolean
    get() = !matches(Reg.email)

val String.isPassword: Boolean
    get() = matches(Reg.password)

val String.isNotPassword: Boolean
    get() = !matches(Reg.password)


val String.isPerson: Boolean
    get() = matches(Reg.person)
val String.isNotPerson: Boolean
    get() = !matches(Reg.person)