package com.example.travelsharingapp.utils

import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.ProposalStatus
import com.example.travelsharingapp.data.model.Typology

fun String.toTypologyOrNull(): Typology? {
    return Typology.entries.find { it.name.equals(this, ignoreCase = true) }
}

fun String.toProposalStatusOrNull(): ProposalStatus? =
    ProposalStatus.entries.find { it.name.equals(this, ignoreCase = true) }

fun String.toApplicationStatusOrNull(): ApplicationStatus? =
    ApplicationStatus.entries.find { it.name.equals(this, ignoreCase = true) }