package ru.spbu.netter.controller

import tornadofx.TaskStatus


interface Statusable {
    val status: TaskStatus
}
