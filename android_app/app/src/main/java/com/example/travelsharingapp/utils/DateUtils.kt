package com.example.travelsharingapp.utils

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun Timestamp.toLocalDate(): LocalDate =
    toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

fun LocalDate.toTimestamp(): Timestamp =
    Timestamp(Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant()))
