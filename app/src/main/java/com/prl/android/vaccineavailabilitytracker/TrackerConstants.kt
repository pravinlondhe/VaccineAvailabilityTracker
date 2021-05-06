package com.prl.android.vaccineavailabilitytracker

import java.util.*

object TrackerConstants {
    private val cal = Calendar.getInstance()
    var pin_code = "416005"
    val date: String
        get() {
            return with(cal) {
                "${get(Calendar.DAY_OF_MONTH) + 1}-${get(Calendar.MONTH) + 1}-${get(Calendar.YEAR)}"
            }
        }
    const val CENTER_ID = 692532 // vikramnagar center tracker
    const val FREQ =  30 * 1000L // shouldn't not more than 20 per minute
    // If kept more frequency more power consumtion
}