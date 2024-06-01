package su.afk.googlesignappfirebase.signIn



data class SignInState(
    val isSignInSuccess: Boolean = false,
    val signInError: String? = null
)