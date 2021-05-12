package ru.spbu.netter

import javafx.scene.image.Image
import ru.spbu.netter.view.MainWindow
import tornadofx.*


class MainApp : App(Image("file:src/main/resources/icon.png"), MainWindow::class)
