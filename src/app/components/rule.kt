package app.components

import model.Rule
import model.mutabilityString
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.h2
import react.dom.p
import react.dom.pre

interface RuleProps : RProps {
    var rule: Rule
}

class RuleComponent : RComponent<RuleProps, RState>() {
    override fun RBuilder.render() {
        div("RuleContainer") {
            val rule = props.rule

            h2("RuleTitle") {
                +"Rule ${rule.number} (${rule.mutability.mutabilityString()})"
            }

            pre("RuleText") {
                val text = rule.text

                for (line in text.split("\n")) {
                    p { +line }
                }
            }
        }
    }
}

fun RBuilder.ruleComponent(rule: Rule) = child(RuleComponent::class) {
    attrs.rule = rule
}