package it.matteoleggio.alchan.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.response.User
import it.matteoleggio.alchan.helper.genericType
import kotlinx.android.synthetic.main.dialog_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LikesDialog : DialogFragment() {

    private val viewModel by viewModel<LikesViewModel>()

    interface LikesDialogListener {
        fun passSelectedUser(userId: Int)
    }

    private lateinit var listener: LikesDialogListener
    private var userList = ArrayList<User>()

    companion object {
        const val USER_LIST = "userList"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(R.string.likes)

        if (!this::listener.isInitialized) {
            dismiss()
        }

        userList = viewModel.gson.fromJson(arguments?.getString(USER_LIST), genericType<List<User>>())

        val view = activity?.layoutInflater?.inflate(R.layout.dialog_list, null)!!

        view.listRecyclerView.adapter = LikesRvAdapter(requireActivity(), userList, object : LikesRvAdapter.LikesListener {
            override fun passSelectedUser(userId: Int) {
                listener.passSelectedUser(userId)
                dismiss()
            }
        })

        builder.setView(view)
        return builder.create()
    }

    fun setListener(likesDialogListener: LikesDialogListener) {
        listener = likesDialogListener
    }
}