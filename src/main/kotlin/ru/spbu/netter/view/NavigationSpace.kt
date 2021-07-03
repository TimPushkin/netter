package ru.spbu.netter.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeType
import ru.spbu.netter.controller.*
import ru.spbu.netter.controller.centrality.*
import ru.spbu.netter.controller.clustering.*
import ru.spbu.netter.controller.layout.*
import ru.spbu.netter.model.Network
import tornadofx.*


class NavigationSpace : View() {
    private lateinit var networkView: NetworkView

    var network: Network
        get() = networkView.network
        set(value) {
            networkView = NetworkView(value)
            isNetworkImported = true
            replaceNetworkView(networkView)
        }

    val isNetworkImportedProperty = SimpleBooleanProperty(this, "isNetworkImported", false)
    var isNetworkImported by isNetworkImportedProperty

    private val simpleLayout: SimpleLayoutMethod by inject<CircularLayout>()
    private val smartLayout: SmartLayoutMethod by inject<ForceAtlas2Layout>()
    private val communityDetector: CommunityDetector by inject<LeidenCommunityDetector>()
    private val centralityIdentifier: CentralityIdentifier by inject<HarmonicCentralityIdentifier>()

    private val navigator: NetworkEventHandler = find<NetworkNavigator>()

    override val root = pane {
        setOnMousePressed { it?.let { navigator.handleMousePressed(it) } }
        setOnMouseDragged { it?.let { navigator.handleMouseDragged(it) } }
        setOnScroll { it?.let { navigator.handleScroll(it) } }
    }

    init {
        root += label(messages["Label_ImportNetwork"]) {
            translateXProperty().bind((root.widthProperty() - widthProperty()) / 2)
            translateYProperty().bind((root.heightProperty() - heightProperty()) / 2)
        }

        listOf(simpleLayout, smartLayout, communityDetector, centralityIdentifier).forEachIndexed { i, statusable ->
            root += with(statusable.status) {
                vbox {
                    visibleWhen { running }

                    alignment = Pos.CENTER

                    translateXProperty().bind(root.widthProperty() - widthProperty())
                    translateYProperty().bind(heightProperty() * (i + 1))

                    text(message).apply {
                        stroke = Color.WHITESMOKE
                        strokeType = StrokeType.OUTSIDE
                        strokeWidth = 2.0
                    }
                    progressbar(progress)
                }
            }
        }
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
        communityDetector.detectCommunities(networkView.network, resolution) { networkView.updateColorsNum() }
    }

    fun inspectForCentrality() {
        centralityIdentifier.identifyCentrality(networkView.network) { networkView.updateNodeOrder() }
    }

    private fun replaceNetworkView(newNetworkView: NetworkView) {
        root.children.retainAll { it is VBox }
        root.children.add(0, newNetworkView.apply {
            // When NavigationSpace gets reloaded, its root's sizes are not yet updated, so 'onChangeOnce' is used

            if (root.width != 0.0) translateX = root.width / 2
            else root.widthProperty().onChangeOnce { translateX = root.width / 2 }

            if (root.height != 0.0) translateY = root.height / 2
            else root.heightProperty().onChangeOnce { translateY = root.height / 2 }
        })
    }
}
