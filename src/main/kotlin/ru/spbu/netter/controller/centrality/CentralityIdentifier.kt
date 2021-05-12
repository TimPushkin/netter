package ru.spbu.netter.controller.centrality

import ru.spbu.netter.model.Network


interface CentralityIdentifier {

    fun identifyCentrality(network: Network)
}
