package app.components

import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import model.Proposal
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import kotlin.js.Promise

interface ProposalProps : RProps {
    var proposal: Proposal
    var voteDetails: ProposalComponent.VoteDetails?
}

interface ProposalState : RState {
    var currentVote: Proposal.Vote?
}

private fun Int.toDayString(): String {
    return when(this) {
        0 -> "Sunday"
        1 -> "Tuesday"
        2 -> "Wednesday"
        3 -> "Thursday"
        4 -> "Friday"
        5 -> "Saturday"
        6 -> "Sunday"
        else -> throw IllegalArgumentException()
    }
}

class ProposalComponent : RComponent<ProposalProps, ProposalState>() {
    data class VoteDetails(val currentVote: Proposal.Vote?, val setVoteFunction: (vote: Proposal.Vote) -> Promise<Unit>)

    override fun componentWillReceiveProps(nextProps: ProposalProps) {
        setState {
            currentVote = nextProps.voteDetails?.currentVote
        }
    }

    override fun RBuilder.render() {
        div("ProposalContainer") {
            val proposal = props.proposal

            h2("ProposalTitle") {
                +"Proposal ${proposal.number}"
            }

            div("ProposalFieldList") {
                proposalField("Type", { type.readableName })
                proposalField("State", { state.readableName }, true)
                proposalField("Supersedes", { parent })
                proposalField("Proposed by", Proposal::proposer)
                if (proposal.state == Proposal.State.VOTING_OPEN) {
                    proposalField("Vote closes", { "${voteClosing!!.toDateString().replace(" " + voteClosing.getFullYear(), "")} ${voteClosing.getHours()}:${voteClosing.getMinutes().toString().padStart(2, '0')}" })
                    proposalField("Vote threshold", { voteThreshold.humanReadableName() })
                }
            }

            pre("ProposalText") {
                p("ProposalTypeHeader") {
                    b {
                        when (proposal.type) {
                            Proposal.Type.ENACTMENT -> +"Add a new rule:"
                            Proposal.Type.REPEAL -> +"Repeal Rule ${proposal.parent!!}."
                            Proposal.Type.AMENDMENT -> +"Amend Rule ${proposal.parent!!} to:"
                            Proposal.Type.MAKE_MUTABLE -> +"Make Rule ${proposal.parent!!} mutable."
                            Proposal.Type.MAKE_IMMUTABLE -> +"Make Rule ${proposal.parent!!} immutable."
                            else -> TODO("unimplemented proposal type")
                        }
                    }
                }

                val text = proposal.text

                for (line in text.split("\n")) {
                    p { +line }
                }
            }

            val voteDetails = props.voteDetails

            if (proposal.state == Proposal.State.VOTING_OPEN && voteDetails != null) {

                div("ProposalVoteContainer") {
                    div("ProposalVoteVoteText") { p { +"Vote" } }

                    div("ProposalVoteButton ProposalVoteAyeButton") {
                        attrs.onClickFunction = voteFunction(Proposal.Vote.IN_FAVOR)

                        if (state.currentVote == Proposal.Vote.IN_FAVOR) {
                            attrs.classes += "ProposalVoteCurrent"
                        }

                        p { +"Aye" }
                    }
                    div("ProposalVoteButton ProposalVoteNoButton") {
                        attrs.onClickFunction = voteFunction(Proposal.Vote.OPPOSE)

                        if (state.currentVote == Proposal.Vote.OPPOSE) {
                            attrs.classes += "ProposalVoteCurrent"
                        }

                        p { +"No" }
                    }
                }
            }
        }
    }

    private fun RBuilder.proposalField(name: String, getter: Proposal.() -> Any?, showOnNull: Boolean = false) {
        val value = props.proposal.getter()
        if (value != null || showOnNull) {
            p("ProposalFieldContainer") {
                b("ProposalFieldName") { +"$name: " }
                +"$value"
            }
        }
    }

    private fun voteFunction(vote: Proposal.Vote): (Event) -> Unit {
        return {
            val voteDetails = props.voteDetails!!
            voteDetails.setVoteFunction(vote).then {
                setState {
                    currentVote = vote
                }
            }
        }
    }
}

fun RBuilder.proposalComponent(proposal: Proposal, voteDetails: ProposalComponent.VoteDetails? = null) = child(ProposalComponent::class) {
    attrs.proposal = proposal
    attrs.voteDetails = voteDetails
}