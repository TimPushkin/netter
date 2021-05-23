package ru.spbu.netter.controller.centrality

import ru.spbu.netter.controller.Statusable
import ru.spbu.netter.model.Network


interface CentralityIdentifier : Statusable {

    fun identifyCentrality(network: Network)
}
