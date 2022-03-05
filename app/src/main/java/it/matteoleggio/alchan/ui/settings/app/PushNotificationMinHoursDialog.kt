package it.matteoleggio.alchan.ui.settings.app

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import it.matteoleggio.alchan.R
import kotlinx.android.synthetic.main.dialog_number_picker.view.*

class PushNotificationMinHoursDialog : DialogFragment() {

    interface PushNotificationMinHoursListener {
        fun passHour(hour: Int)
    }

    private lateinit var listener: PushNotificationMinHoursListener
    private val hourArray = Array(48) { it + 1.0 }
    private var currentHour = 1.0

    companion object {
        const val CURRENT_HOUR = "currentHour"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_number_picker, null)

        if (!this::listener.isInitialized) {
            dismiss()
        }

        currentHour = arguments?.getDouble(CURRENT_HOUR, 1.0) ?: 1.0

        view.numberPicker.apply {
            minValue = 1
            maxValue = 48
            displayedValues = hourArray.map { ((it * 3).toDouble() / 6).toString() }.toTypedArray()
            wrapSelectorWheel = false
            value = (currentHour * 2).toInt()
        }

        builder.setView(view)
        builder.setPositiveButton(R.string.set) { _, _ ->
            listener.passHour(view.numberPicker.value / 2)
        }
        builder.setNegativeButton(R.string.cancel, null)
        return builder.create()
    }

    fun setListener(pushNotificationMinHoursListener: PushNotificationMinHoursListener) {
        listener = pushNotificationMinHoursListener
    }
}