package app.pages

import app.components.ProposalComponent
import app.components.proposalComponent
import kotlinx.html.DIV
import kotlinx.html.js.onClickFunction
import model.Proposal
import react.*
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.h1
import react.dom.p
import kotlin.js.Promise

interface ProposalsPageState : RState {
    var proposals: MutableMap<Int, Proposal>
    var votes: Map<Int, Proposal.Vote>
}

interface ProposalsPageProps : RProps {
    var isLoggedIn: Boolean
    var setVoteFunction: (proposal: Int, vote: Proposal.Vote) -> Promise<Proposal.State>
    var openCreateNewProposalPage: () -> Unit
    var getProposalsFunction: () -> Promise<List<Proposal>>
    var getUserVotesFunction: () -> Promise<Map<Int, Proposal.Vote>>
}

class ProposalsPage : RComponent<ProposalsPageProps, ProposalsPageState>() {
    override fun RBuilder.render() {
        div {
            h1 { +"Proposals" }

            if (props.isLoggedIn) {
                newProposalButton()
            }

            div("ProposalsList") {
                for (proposal in state.proposals.values.reversed()) {
                    val voteDetails: ProposalComponent.VoteDetails?

                    if (props.isLoggedIn && proposal.state == Proposal.State.VOTING_OPEN) {
                        voteDetails = ProposalComponent.VoteDetails(state.votes[proposal.number]) { vote ->
                            props.setVoteFunction(proposal.number, vote).then { newState ->
                                setState {
                                    proposals[proposal.number] = Proposal(
                                            proposal.number,
                                            proposal.text,
                                            proposal.parent,
                                            proposal.type,
                                            newState,
                                            proposal.proposer,
                                            proposal.voteThreshold,
                                            proposal.voteClosing,
                                            proposal.mutability
                                    )
                                }

                                return@then Unit
                            }
                        }
                    } else {
                        voteDetails = null
                    }

                    proposalComponent(proposal, voteDetails)
                }
            }
        }
    }

    private fun RDOMBuilder<DIV>.newProposalButton() {
        div("ProposalsNewProposalButton") {
            attrs.onClickFunction = { props.openCreateNewProposalPage() }

            p { +"Create new" }
        }
    }

    override fun componentWillMount() {
        props.getProposalsFunction().then {
            val map = mutableMapOf<Int, Proposal>()

            for (proposal in it) {
                map[proposal.number] = proposal
            }

            setState {
                proposals = map
            }
        }

        if (props.isLoggedIn) {
            props.getUserVotesFunction().then {
                setState {
                    votes = it
                }
            }
        }
    }

    override fun ProposalsPageState.init() {
        proposals = mutableMapOf()
        votes = emptyMap()
    }
}

fun RBuilder.proposalsPage(isLoggedIn: Boolean, getProposalsFunction: () -> Promise<List<Proposal>>, getUserVotesFunction: () -> Promise<Map<Int, Proposal.Vote>>, setVoteFunction: (proposal: Int, vote: Proposal.Vote) -> Promise<Proposal.State>, openCreateNewProposalPage: () -> Unit) {
    child(ProposalsPage::class) {
        attrs.isLoggedIn = isLoggedIn
        attrs.setVoteFunction = setVoteFunction
        attrs.openCreateNewProposalPage = openCreateNewProposalPage
        attrs.getProposalsFunction = getProposalsFunction
        attrs.getUserVotesFunction = getUserVotesFunction
    }
}