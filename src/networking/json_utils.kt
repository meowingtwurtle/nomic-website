package networking

import model.Proposal
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.json

internal fun Proposal.toJSON(): Json {
    return json(
            "number" to number,
            "text" to text,
            "parent" to parent,
            "type" to type.externalName,
            "state" to state.externalName,
            "proposer" to proposer,
            "vote_threshold" to voteThreshold.specifier(),
            "vote_closing" to voteClosing
    )
}

internal fun Json.toProposal(
        forceNumber: Int = get("number") as Int,
        forceState: Proposal.State = Proposal.State.fromExternalName(get("state") as String?),
        forceProposer: String? = get("proposer") as String?,
        forceVoteThreshold: Proposal.VoteThreshold = Proposal.VoteThreshold.parseBySpec(get("vote_threshold") as String),
        forceVoteClosing: Date? = Date(get("vote_closing") as String),
        forceMutability: Boolean = get("mutable") as Boolean
): Proposal {
    return Proposal(
            forceNumber,
            get("text") as String,
            get("parent") as Int?,
            Proposal.Type.fromExternalName(get("type") as String?),
            forceState,
            forceProposer,
            forceVoteThreshold,
            forceVoteClosing,
            forceMutability
    )
}