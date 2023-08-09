package com.mrpowergamerbr.rinhadebackend2023q3.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Stacks : LongIdTable() {
    val name = text("name").index()
    val person = reference("person", Pessoas).index()
}