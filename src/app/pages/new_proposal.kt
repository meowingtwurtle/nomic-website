package app.pages

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import model.Proposal
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import react.*
import react.dom.*
import kotlin.js.Promise
import kotlin.math.roundToInt

interface NewProposalProps : RProps {
    var submitProposalFunction: (text: String?, type: Proposal.Type, parent: Int?) -> Promise<Unit>
    var showProposalPageFunction: () -> Unit
}

interface NewProposalPageState : RState {
    var selectedType: Proposal.Type
    var selectedParent: Int?
    var text: String?
    var errorMessage: String?
}

class NewProposalPagePage : RComponent<NewProposalProps, NewProposalPageState>() {
    override fun RBuilder.render() {
        div("NewProposalContainer") {
            div("NewProposalInlineFieldContainer") {
                p("NewProposalLabel NewProposalTypeLabel") { +"Type: " }
                select("NewProposalTypeSelect") {
                    attrs.id = "NewProposalType"
                    attrs.onChangeFunction = {
                        val target = it.target!! as HTMLSelectElement
                        val type = Proposal.Type.fromExternalName(target.value)

                        setState {
                            selectedType = type
                            if (!type.hasParent) selectedParent = null
                        }
                    }

                    typeOption(Proposal.Type.ENACTMENT)
                    typeOption(Proposal.Type.AMENDMENT)
                    typeOption(Proposal.Type.REPEAL)
                    typeOption(Proposal.Type.MAKE_MUTABLE)
                    typeOption(Proposal.Type.MAKE_IMMUTABLE)
                }
            }

            if (state.selectedType.hasParent) {
                div("NewProposalInlineFieldContainer") {
                    p("NewProposalLabel NewProposalTargetLabel") { +"Target: " }
                    input(type = InputType.number, classes = "NewProposalTargetSelect") {
                        attrs.id = "NewProposalTarget"

                        attrs.onChangeFunction = {
                            val target = it.target!! as HTMLInputElement

                            val numberValue = target.valueAsNumber

                            if (numberValue % 1 != 0.0) {
                                setState {
                                    errorMessage = "Parent must be an integer"
                                }
                            } else {
                                setState {
                                    selectedParent = numberValue.roundToInt()
                                }
                            }
                        }
                    }
                }
            }

            if (state.selectedType.hasText) {
                p("NewProposalLabel NewProposalTextLabel") { +"Text: " }
                textArea("12", classes = "NewProposalTextEntry") {
                    attrs.onChangeFunction = {
                        val target = it.target!! as HTMLTextAreaElement
                        setState {
                            text = target.value
                        }
                    }
                }
            }

            div("NewProposalSubmitButton") {
                attrs.onClickFunction = { _ ->
                    props.submitProposalFunction(state.text, state.selectedType, state.selectedParent).then {
                        props.showProposalPageFunction()
                    }
                }

                p { +"Submit" }
            }
        }
    }

    override fun NewProposalPageState.init() {
        selectedType = Proposal.Type.ENACTMENT
        selectedParent = null
        text = ""
        errorMessage = null
    }

    private fun RBuilder.typeOption(type: Proposal.Type) {
        option {
            attrs.text(type.readableName)
            attrs.value = type.externalName
        }
    }
}

fun RBuilder.newProposalPage(submitProposalFunction: (text: String?, type: Proposal.Type, parent: Int?) -> Promise<Unit>, showProposalPageFunction: () -> Unit) = child(NewProposalPagePage::class) {
    attrs.submitProposalFunction = submitProposalFunction
    attrs.showProposalPageFunction = showProposalPageFunction
}