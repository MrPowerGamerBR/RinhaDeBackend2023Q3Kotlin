package com.mrpowergamerbr.rinhadebackend2023q3.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date

object Pessoas : UUIDTable() {
    val apelido = text("apelido").index()
    val nome = text("nome").index()
    val nascimento = date("nascimento")
}