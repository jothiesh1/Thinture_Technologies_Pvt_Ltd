package com.example.gpsapp.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DateTimePicker(
    label: String,
    dateTime: String,
    onDateTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column {
        OutlinedTextField(
            value = dateTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("$label Timestamp") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Button(
            onClick = {
                DatePickerDialog(context, { _, year, month, day ->
                    TimePickerDialog(context, { _, hour, minute ->
                        calendar.set(year, month, day, hour, minute)
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        onDateTimeSelected(sdf.format(calendar.time))
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text("Select $label Time")
        }
    }
}
