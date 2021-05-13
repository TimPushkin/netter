package ru.spbu.netter.view

import javafx.beans.property.SimpleBooleanProperty
import ru.spbu.netter.controller.*
import ru.spbu.netter.controller.centrality.*
import ru.spbu.netter.controller.clustering.*
import ru.spbu.netter.controller.layout.*
import ru.spbu.netter.model.Network
import tornadofx.*


class NavigationSpace : View() {
    lateinit var networkView: NetworkView
    val isNetworkImportedProperty = SimpleBooleanProperty(this, "isNetworkImported", false)
    private var isNetworkImported by isNetworkImportedProperty

    private val simpleLayout: SimpleLayoutMethod by inject<CircularLayout>()
    private val smartLayout: SmartLayoutMethod by inject<ForceAtlas2Layout>()
    private val communityDetector: CommunityDetector by inject<LeidenCommunityDetector>()
    private val centralityIdentifier: CentralityIdentifier by inject<HarmonicCentralityIdentifier>()

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

    fun initNetworkView(network: Network) {
        networkView = NetworkView(network)
        isNetworkImported = true
        replaceNetworkView(networkView)
    }

    fun applyDefaultLayout(repulsion: Double) {
        simpleLayout.applyLayout(networkView.network, repulsion = repulsion)
    }

    fun applySmartLayout(
        loopsNum: Int,
        applyAdjustSizes: Boolean,
        applyBarnesHut: Boolean,
        applyLinLogMode: Boolean,
        applyStrongGravityMode: Boolean,
        withJitterTolerance: Double,
        withScalingRatio: Double,
        withGravity: Double,
        withBarnesHutTheta: Double,
    ) {
        smartLayout.applyLayout(
            networkView.network,
            loopsNum,
            applyAdjustSizes,
            applyBarnesHut,
            applyLinLogMode,
            applyStrongGravityMode,
            withJitterTolerance,
            withScalingRatio,
            withGravity,
            withBarnesHutTheta,
        )
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
