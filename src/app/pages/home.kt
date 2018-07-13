package app.pages

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.p

class HomePage : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        p {
            +"Welcome to [name TBD] Nomic! Nomic is a game where the game is changing the rules."
        }
    }
}

fun RBuilder.homePage() = child(HomePage::class) {}
