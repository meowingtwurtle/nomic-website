package app.components

import app.Page
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.nav
import react.dom.p

interface NavbarProps : RProps {
    var username: String?
    var selectPage: (newPage: Page) -> Unit
    var logoutFunction: () -> Unit
}

class Navbar : RComponent<NavbarProps, RState>() {
    override fun RBuilder.render() {
        div("Navbar") {
            loginBar()

            nav("NavbarNavigation") {
                navigationItem("Home", Page.HOME)
                navigationItem("Proposals", Page.PROPOSALS)
                navigationItem("Standing Rules", Page.STANDING_RULES)
            }
        }
    }

    private fun RBuilder.navigationItem(name: String, newPage: Page) {
        div(classes = "NavbarLink click-cursor") {
            div {
                attrs.onClickFunction = {
                    props.selectPage(newPage)
                }

                p { +name }
            }
        }
    }

    private fun RBuilder.loginBar() {
        div("NavbarLoginBar") {
            val userText: String
            val buttonText: String
            val buttonFunction: (Event) -> Unit

            if (props.username != null) {
                userText = "Logged in as " + props.username
                buttonText = "Logout"
                buttonFunction = { props.logoutFunction() }
            } else {
                userText = "Not logged in"
                buttonText = "Login"
                buttonFunction = { props.selectPage(Page.LOGIN) }
            }

            p("NavbarLoginText") { +userText }
            div("NavbarLoginButton click-cursor") {
                attrs.onClickFunction = buttonFunction

                p {
                    +buttonText
                }
            }
        }
    }

}

fun RBuilder.navbar(username: String?, selectPageFunction: (newPage: Page) -> Unit, logoutFunction: () -> Unit) = child(Navbar::class) {
    attrs.username = username
    attrs.selectPage = selectPageFunction
    attrs.logoutFunction = logoutFunction
}
