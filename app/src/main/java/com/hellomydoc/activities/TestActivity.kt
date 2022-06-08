package com.hellomydoc.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hellomydoc.R
import com.hellomydoc.data.slots.DayPartSlot
import com.hellomydoc.data.slots.Slot
import com.hellomydoc.views.SlotsView

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        findViewById<SlotsView>(R.id.sv_slots).apply {
            onChangeCallback = {pos,slotPos->
                Log.d("checked_debug","$pos.$slotPos");
            }
        }.setData(
            listOf(
                DayPartSlot(
                    "1",
                    "Afternoon",
                    listOf(
                        Slot(
                            "1",
                            "12:50 PM"
                        ),
                        Slot(
                            "1",
                            "12:50 PM"
                        ),
                        Slot(
                            "1",
                            "12:50 PM"
                        ),

                        Slot(
                            "1",
                            "12:50 PM"
                        ),
                        Slot(
                            "1",
                            "12:50 PM"
                        ),
                        Slot(
                            "1",
                            "12:50 PM"
                        ),
                    )
                ),
                DayPartSlot(
                    "1",
                    "Afternoon",
                    listOf(
                        Slot(
                            "1",
                            "12:50 PM"
                        ),
                        Slot(
                            "1",
                            "12:50 PM"
                        ),
                        Slot(
                            "1",
                            "12:50 PM"
                        ),
                    )
                )
            )
        )
    }
}