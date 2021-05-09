package ru.spbu.netter.view

import javafx.beans.property.SimpleBooleanProperty
import ru.spbu.netter.controller.NetworkEventHandler
import ru.spbu.netter.controller.NetworkNavigator
import ru.spbu.netter.controller.centrality.CentralityIdentifier
import ru.spbu.netter.controller.centrality.DegreeCentralityIdentifier
import ru.spbu.netter.controller.clustering.CommunityDetector
import ru.spbu.netter.controller.clustering.LeidenCommunityDetector
import ru.spbu.netter.controller.layout.CircularLayout
import ru.spbu.netter.controller.layout.LayoutMethod
import ru.spbu.netter.controller.layout.SmartLayout
import ru.spbu.netter.model.Network
import tornadofx.*


class NavigationSpace : View() {
    lateinit var networkView: NetworkView
    val isNetworkImportedProperty = SimpleBooleanProperty(this, "isNetworkImported", false)
    private var isNetworkImported by isNetworkImportedProperty

    private val defaultLayout: LayoutMethod by inject<CircularLayout>()
    private val smartLayout: LayoutMethod by inject<SmartLayout>()
    private val communityDetector: CommunityDetector by inject<LeidenCommunityDetector>()
    private val centralityIdentifier: CentralityIdentifier by inject<DegreeCentralityIdentifier>()

    private val navigator: NetworkEventHandler = find<NetworkNavigator>()

    override val root = pane().apply {
        setOnMousePressed { it?.let { navigator.handleMousePressed(it) } }
        setOnMouseDragged { it?.let { navigator.handleMouseDragged(it) } }
        setOnScroll { it?.let { navigator.handleScroll(it) } }
    }

    init {
        root += label("Import a network to be displayed here").apply {
            translateXProperty().bind((root.widthProperty() - widthProperty()) / 2)
            translateYProperty().bind((root.heightProperty() - heightProperty()) / 2)
        }
    }

    fun initNetworkView(network: Network, repulsion: Double) {
        networkView = NetworkView(network)
        isNetworkImported = true
        applyDefaultLayout(repulsion)
        replaceNetworkView(networkView)
    }

    fun applyDefaultLayout(repulsion: Double) {
        networkView.applyLayout(defaultLayout.layOut(networkView.network, repulsion = repulsion))
    }

    fun applySmartLayout(repulsion: Double) {
        networkView.applyLayout(smartLayout.layOut(networkView.network, repulsion = repulsion))
    }

    fun inspectForCommunities(resolution: Double) {
        communityDetector.detectCommunities(networkView.network, resolution)
    }

    fun inspectForCentrality() {
        centralityIdentifier.identifyCentrality(networkView.network)
        networkView.placeNodesInCentralityOrder()
    }

    private fun replaceNetworkView(newNetworkView: NetworkView) {
        root.children.clear()
        root += newNetworkView.apply {
            translateX = root.width / 2
            translateY = root.height / 2
        }
    }
}
