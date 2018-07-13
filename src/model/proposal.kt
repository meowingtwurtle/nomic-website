package model

import kotlin.js.Date

data class Proposal(val number: Int, val text: String, val parent: Int?, val type: Type, val state: State, val proposer: String?, val voteThreshold: VoteThreshold, val voteClosing: Date?, val mutability: Boolean) {
    enum class Type(val externalName: String, val readableName: String = externalName, val hasParent: Boolean = true, val hasText: Boolean = true) {
        ENACTMENT("enactment", hasParent = false),
        AMENDMENT("amendment"),
        REPEAL("repeal", hasText = false),
        MAKE_IMMUTABLE("make_immutable", "make immutable", hasText = false),
        MAKE_MUTABLE("make_mutable", "make mutable", hasText = false);

        companion object {
            fun fromExternalName(externalName: String?): Type {
                externalName ?: throw IllegalArgumentException("null proposal type")

                val matching = values().filter { it.externalName == externalName }
                if (matching.isEmpty()) throw IllegalArgumentException("invalid proposal type \"$externalName\"")
                return matching[0]
            }
        }
    }

    enum class State(val externalName: String, val readableName: String = externalName.capitalize()) {
        VOTING_OPEN("open"), ADOPTED("adopted"), REJECTED("rejected");

        companion object {
            fun fromExternalName(externalName: String?): State {
                externalName ?: throw IllegalArgumentException("null state name")

                val matching = values().filter { it.externalName == externalName }
                if (matching.isEmpty()) throw IllegalArgumentException("invalid state name \"$externalName\"")
                return matching[0]
            }
        }
    }

    enum class Vote(val externalName: String, val readableName: String = externalName.capitalize()) {
        IN_FAVOR("aye"), OPPOSE("no"), ABSTAIN("abstain");


        companion object {
            fun fromExternalName(externalName: String?): Vote {
                externalName ?: throw IllegalArgumentException("null vote type")

                val matching = values().filter { it.externalName == externalName }
                if (matching.isEmpty()) throw IllegalArgumentException("invalid vote type \"$externalName\"")
                return matching[0]
            }
        }
    }

    interface VoteThreshold {
        fun passesPreemptively(ayes: Int, noes: Int, abstentions: Int): Boolean
        fun passesOnClosing(ayes: Int, noes: Int, abstentions: Int): Boolean
        fun failsPreemptively(ayes: Int, noes: Int, abstentions: Int): Boolean
        fun failsOnClosing(ayes: Int, noes: Int, abstentions: Int): Boolean = !passesOnClosing(ayes, noes, abstentions)
        fun specifier(): String
        fun humanReadableName(): String

        companion object Implementations {
            fun unanimousConsent(): VoteThreshold {
                return object : VoteThreshold {
                    override fun passesPreemptively(ayes: Int, noes: Int, abstentions: Int): Boolean {
                        return noes == 0
                    }

                    override fun passesOnClosing(ayes: Int, noes: Int, abstentions: Int): Boolean {
                        return noes == 0 && abstentions == 0
                    }

                    override fun failsPreemptively(ayes: Int, noes: Int, abstentions: Int): Boolean {
                        return noes > 0
                    }

                    override fun specifier(): String = "unanimousConsent"
                    override fun humanReadableName(): String = "unanimous consent"
                }
            }

            fun fullUnanimous(): VoteThreshold {
                return object : VoteThreshold {
                    override fun passesPreemptively(ayes: Int, noes: Int, abstentions: Int): Boolean {
                        return noes == 0 && abstentions == 0
                    }

                    override fun passesOnClosing(ayes: Int, noes: Int, abstentions: Int): Boolean {
                        return noes == 0 && abstentions == 0
                    }

                    override fun failsPreemptively(ayes: Int, noes: Int, abstentions: Int): Boolean {
                        return noes > 0
                    }

                    override fun specifier(): String = "fullUnanimous"
                    override fun humanReadableName(): String = "full unanimity"
                }
            }

            fun majority(): VoteThreshold = FractionalPassing(1, 2, false, "majority", "majority")

            fun twoThirds(): VoteThreshold = FractionalPassing(2, 3, true, "two-thirds")

            fun parseBySpec(spec: String): VoteThreshold {
                return when(spec) {
                    "unanimousConsent" -> unanimousConsent()
                    "fullUnanimous" -> fullUnanimous()
                    "majority" -> majority()
                    else -> parseDefaultSpec(spec)
                }
            }

            private fun defaultSpec(numerator: Int, denominator: Int, allowEqual: Boolean): String {
                return "$numerator-$denominator-$allowEqual"
            }

            private fun parseDefaultSpec(spec: String): VoteThreshold {
                val parts = spec.split("-")
                return FractionalPassing(parts[0].toInt(), parts[1].toInt(), parts[2].toBoolean())
            }

            private fun createReadableName(numerator: Int, denominator: Int, allowEqual: Boolean): String {
                val suffix = when (denominator % 10) {
                    1 -> if (denominator != 11) "sts" else "ths"
                    2 -> if (denominator == 2) "" else if (denominator == 12) "ths" else "nds"
                    else -> "ths"
                }
                return (if (allowEqual) "" else "more than " ) + "$numerator/$denominator" + suffix
            }

            data class FractionalPassing(val numerator: Int, val denominator: Int, val allowEqual: Boolean, val spec: String = defaultSpec(numerator, denominator, allowEqual), val readableName: String = createReadableName(numerator, denominator, allowEqual)) : VoteThreshold {
                override fun passesPreemptively(ayes: Int, noes: Int, abstentions: Int): Boolean {
                    // ayes/total > num/den -> ayes * den > total * num
                    val total = ayes + noes + abstentions
                    return (ayes * denominator).possiblyStrictCompare(total * numerator)
                }

                override fun passesOnClosing(ayes: Int, noes: Int, abstentions: Int): Boolean {
                    val total = ayes + noes
                    return (ayes * denominator).possiblyStrictCompare(total * numerator)
                }

                override fun failsPreemptively(ayes: Int, noes: Int, abstentions: Int): Boolean {
                    val total = ayes + noes + abstentions
                    return !((((ayes + abstentions)) * denominator).possiblyStrictCompare(total * numerator))
                }

                override fun specifier(): String {
                    return spec
                }

                override fun humanReadableName(): String {
                    return readableName
                }

                /// Compares > if equal not allowed, >= otherwise
                private fun Int.possiblyStrictCompare(that: Int): Boolean {
                    return if (this@FractionalPassing.allowEqual) this >= that else this > that
                }
            }
        }

    }
}

fun Boolean.mutabilityString(): String {
    return if (this) "mutable" else "immutable"
}
