package com.zen.alchan.ui.base

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class DefaultDialogManager(private val context: Context) : DialogManager {

    override fun showToast(message: Int) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun showMessageDialog(title: Int, message: Int, positiveButton: Int) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton, null)
            .setCancelable(false)
            .show()
    }

    override fun showConfirmationDialog(
        title: Int,
        message: Int,
        positiveButton: Int,
        positiveAction: () -> Unit,
        negativeButton: Int,
        negativeAction: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { _, _ -> positiveAction() }
            .setNegativeButton(negativeButton) { _, _ -> negativeAction() }
            .setCancelable(false)
            .show()
    }
}