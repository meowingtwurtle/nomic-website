package networking

import model.Proposal
import model.Rule
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Json
import kotlin.js.Promise

object Networking {
    private const val serverUrl = "http://localhost:4453"

    private fun url(subPath: String) = serverUrl + subPath

    private fun <T> post(url: String, value: String = "", mapFunction: (response: Any?) -> T): Promise<T> {
        return Promise { resolve: (T) -> Unit, reject: (Throwable) -> Unit ->
            val xhr = XMLHttpRequest()
            xhr.open("POST", url(url))
            xhr.send(value)
            xhr.onload = {
                resolve(mapFunction(xhr.response))
            }
            xhr.onerror = {
                reject(IllegalStateException(JSON.stringify(xhr.response)))
            }
        }
    }

    private fun <T> get(url: String, mapFunction: (response: Any?) -> T): Promise<T> {
        return Promise { resolve: (T) -> Unit, reject: (Throwable) -> Unit ->
            val xhr = XMLHttpRequest()
            xhr.open("GET", url(url))
            xhr.send("")
            xhr.onload = {
                resolve(mapFunction(xhr.response))
            }
            xhr.onerror = {
                reject(IllegalStateException(JSON.stringify(xhr.response)))
            }
        }
    }

    fun getProposal(number: Int): Promise<Proposal> {
        return get("/proposal/$number") {
            JSON.parse<Json>(it as String).toProposal()
        }
    }

    fun getTokenUsername(token: String): Promise<String?> {
        val json = """ {"token": "$token"} """

        return post("/user/check_token", json) { response ->
            val jsonResponse = JSON.parse<Json>(response as String)
            if (jsonResponse["valid"] == true) {
                return@post jsonResponse["username"] as String?
            } else {
                return@post null
            }
        }
    }

    fun login(username: String, password: String): Promise<String?> {
        val json = """ {"username": "$username", "password": "$password"} """

        return post("/user/login", json) { response ->
            val jsonResponse = JSON.parse<Json>(response as String)
            if (jsonResponse["valid"] == true) {
                return@post jsonResponse["token"] as String?
            } else {
                return@post null
            }
        }
    }

    fun logout(token: String): Promise<Unit> {
        val json = """ {"token": "$token"} """

        return post("/user/logout", json) {}
    }

    fun getStandingRules(): Promise<List<Rule>> {
        return get("/standing") { response ->
            val responseJson = JSON.parse<Json>(response as String)
            val standingRules = responseJson["standing_rules"] as Array<Json>
            return@get standingRules.map { Rule(it["number"] as Int, it["mutable"] as Boolean, it["text"] as String) }
        }
    }

    fun getProposals(): Promise<List<Proposal>> {
        return get("/proposals") { response ->
            val responseJson = JSON.parse<Json>(response as String)
            val proposals = responseJson["proposals"] as Array<Json>
            return@get proposals.map { it.toProposal() }
        }
    }

    fun getUserVotes(username: String): Promise<Map<Int, Proposal.Vote>> {
        return get("/vote/user/$username") { response ->
            val responseJson = JSON.parse<Json>(response as String)
            val votedOn = responseJson["voted_on"] as Array<Int>
            val votesJson = responseJson["votes"] as Json

            val map = mutableMapOf<Int, Proposal.Vote>()

            for (proposal in votedOn) {
                map[proposal] = Proposal.Vote.fromExternalName(votesJson[proposal.toString()] as String)
            }

            return@get map
        }
    }

    fun setVote(token: String, proposal: Int, vote: Proposal.Vote): Promise<Proposal.State> {
        val json = """{"token": "$token", "vote": "${vote.externalName}"}"""

        return post("/vote/$proposal", json) { response ->
            val responseJson = JSON.parse<Json>(response as String)

            if ((responseJson["success"] as Boolean) != true) {
                throw IllegalStateException()
            }

            return@post Proposal.State.fromExternalName(responseJson["state"] as String)
        }
    }

    fun createProposal(token: String, text: String?, type: Proposal.Type, parent: Int?): Promise<Proposal> {
        val json = """ {"token": "$token", "text": ${JSON.stringify(text)}, "type": "${type.externalName}", "parent": ${parent ?: "null"}} """

        return post("/proposal", json) { response ->
            val responseJSON = JSON.parse<Json>(response as String)
            val success = (responseJSON["success"] as Boolean)

            if (!success) {
                val failureReason = responseJSON["reason"] as String

                throw IllegalStateException("could not create proposal: $failureReason")
            }

            val createdProposal = (responseJSON["proposal"] as Json).toProposal()
            return@post createdProposal
        }
    }
}
