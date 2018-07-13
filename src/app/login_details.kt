package app

sealed class LoginDetails(val loggedIn: Boolean)

class LoggedIn(val username: String, val token: String) : LoginDetails(true)
class NotLoggedIn : LoginDetails(false)