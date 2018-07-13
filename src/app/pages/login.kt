package app.pages

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyDownFunction
import kotlinx.html.js.onKeyPressFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.*
import react.dom.div
import react.dom.input
import react.dom.pre

interface LoginPageProps : RProps {
    var loginFunction: LoginFunction
}

interface LoginPageState : RState {
    var username: String
    var password: String
}

private typealias LoginFunction = (username: String, password: String) -> Unit

class LoginPage : RComponent<LoginPageProps, LoginPageState>() {
    override fun RBuilder.render() {
        div("LoginContainer") {
            val submitFunction: () -> Unit = {
                props.loginFunction(state.username, state.password)
            }

            val enterFunction: (Event) -> Unit = { event ->
                val keyboardEvent = event.unsafeCast<KeyboardEvent>()

                if (keyboardEvent.key.equals("enter", true) || keyboardEvent.keyCode == 13) {
                    submitFunction()
                }
            }

            div("LoginSection") {

                pre("LoginLabel") { +"Username: " }
                input(type = InputType.text) {
                    attrs.onKeyPressFunction = enterFunction
                    attrs.onChangeFunction = {
                        val target = it.target as HTMLInputElement

                        setState {
                            username = target.value
                        }
                    }

                    attrs.id = "LoginPageUsernameInput"
                }
            }

            div("LoginSection") {

                pre("LoginLabel") { +"Password: " }
                input(type = InputType.password) {
                    attrs.onKeyDownFunction = enterFunction

                    attrs.onChangeFunction = {
                        val target = it.target as HTMLInputElement

                        setState {
                            password = target.value
                        }
                    }

                    attrs.id = "LoginPagePasswordInput"
                }
            }


            div(classes = "LoginButton") {
                attrs.onClickFunction = { submitFunction() }

                +"Login"
            }
        }
    }

    override fun LoginPageState.init() {
        username = ""
        password = ""
    }
}

fun RBuilder.loginPage(loginFunction: LoginFunction) = child(LoginPage::class) {
    attrs.loginFunction = loginFunction
}
