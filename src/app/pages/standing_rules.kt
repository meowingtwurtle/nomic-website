package app.pages

import app.components.ruleComponent
import model.Proposal
import model.Rule
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.h1
import kotlin.js.Promise

interface StandingRulesProps: RProps {
    var getStandingRulesFunction: () -> Promise<List<Rule>>
}

interface StandingRulesState : RState {
    var standingRules: List<Rule>
}

class StandingRulesPage : RComponent<StandingRulesProps, StandingRulesState>() {
    override fun componentWillMount() {
        props.getStandingRulesFunction().then { rules ->
            setState(transformState = { state ->
                state.standingRules = rules

                return@setState state
            })
        }
    }

    override fun RBuilder.render() {
        div {
            h1 { +"Standing Rules" }

            div("StandingRulesList") {
                for (rule in state.standingRules) {
                    ruleComponent(rule)
                }
            }
        }
    }

    override fun StandingRulesState.init() {
        standingRules = emptyList()
    }
}

fun RBuilder.standingRulesPage(getStandingRulesFunction: () -> Promise<List<Rule>>) = child(StandingRulesPage::class) {
    attrs.getStandingRulesFunction = getStandingRulesFunction
}
