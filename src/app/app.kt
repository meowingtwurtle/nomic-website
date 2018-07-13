@file:Suppress("MoveLambdaOutsideParentheses")

package app

import Persistence
import app.Page.*
import app.components.navbar
import app.pages.*
import model.Proposal
import networking.Networking
import react.*
import react.dom.div
import kotlin.js.Promise

enum class Page {
    HOME, PROPOSALS, NEW_PROPOSAL, STANDING_RULES, LOGIN;
}

class AppState : RState {
    var currentPage: Page = HOME
    var loginDetails: LoginDetails = NotLoggedIn()
}

class App : RComponent<RProps, AppState>() {

    override fun RBuilder.render() {
        div ("App") {
            val loginDetails = state.loginDetails
            val username: String?

            when (loginDetails) {
                is LoggedIn -> username = loginDetails.username
                is NotLoggedIn -> username = null
                else -> TODO()
            }

            navbar(username, ::setPage, ::doLogout)

            when (state.currentPage) {
                HOME -> homePage()
                PROPOSALS -> proposalsPage(state.loginDetails.loggedIn, Networking::getProposals, ::getUserVotes, ::setVote, { setPage(NEW_PROPOSAL) })
                NEW_PROPOSAL -> newProposalPage(::submitProposal, { setPage(Page.PROPOSALS) })
                STANDING_RULES -> standingRulesPage(Networking::getStandingRules)
                LOGIN -> loginPage(::doLogin)
                else -> TODO()
            }
        }
    }

    override fun AppState.init() {
        currentPage = HOME
        loginDetails = NotLoggedIn()

        val storedToken = Persistence.getToken()
        if (storedToken != null) {
            Networking.getTokenUsername(storedToken).then { username ->
                if (username != null) {
                    setLoginDetails(LoggedIn(username, storedToken))
                } else {
                    Persistence.clearToken()
                }
            }
        }
    }

    private fun getUserVotes(): Promise<Map<Int, Proposal.Vote>> {
        val loginDetails = state.loginDetails

        return when (loginDetails) {
            is LoggedIn -> Networking.getUserVotes(loginDetails.username)
            is NotLoggedIn -> Promise.reject(IllegalStateException())
            else -> TODO()
        }
    }

    private fun submitProposal(text: String?, type: Proposal.Type, parent: Int?): Promise<Unit> {
        val loginDetails = state.loginDetails
        val token: String

        when (loginDetails) {
            is LoggedIn -> token = loginDetails.token
            is NotLoggedIn -> throw IllegalStateException("cannot submit proposal while not logged in")
            else -> TODO()
        }

        return Networking.createProposal(token, text, type, parent).then {}
    }

    private fun setPage(newPage: Page) {
        setState { currentPage = newPage }
    }

    private fun setLoginDetails(newLoginDetails: LoginDetails) {
        setState { loginDetails = newLoginDetails }
    }

    private fun setVote(proposal: Int, vote: Proposal.Vote): Promise<Proposal.State> {
        val loginDetails = state.loginDetails
        return when (loginDetails) {
            is LoggedIn -> {
                Networking.setVote(loginDetails.token, proposal, vote)
            }
            is NotLoggedIn -> Promise.reject(IllegalStateException())
            else -> TODO()
        }
    }

    private fun doLogin(username: String, password: String) {
        Networking.login(username, password).then { token ->
            token ?: return@then

            setPage(Page.HOME)
            setState {
                loginDetails = LoggedIn(username, token)
            }

            Persistence.storeToken(token)
        }
    }

    private fun doLogout() {
        val loginDetails = state.loginDetails
        when (loginDetails) {
            is LoggedIn -> {
                Networking.logout(loginDetails.token).then {
                    setLoginDetails(NotLoggedIn())
                }

                Persistence.clearToken()
            }
            is NotLoggedIn -> {}
            else -> {}
        }
    }
}

fun RBuilder.app() = child(App::class) {}
