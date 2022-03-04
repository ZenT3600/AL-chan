package it.matteoleggio.alchan.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.pojo.AdvancedScoresItem
import it.matteoleggio.alchan.helper.roundToOneDecimal
import it.matteoleggio.alchan.helper.trimTrailingZero
import kotlinx.android.synthetic.main.list_advanced_scoring_input.view.*
import java.math.BigDecimal

class AdvancedScoringRvAdapter(private val list: List<AdvancedScoresItem>,
                               private val listener: AdvancedScoringListener?,
                               private val isClickable: Boolean? = true
) : RecyclerView.Adapter<AdvancedScoringRvAdapter.ViewHolder>() {

    interface AdvancedScoringListener {
        fun passScores(index: Int, newScore: Double)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_advanced_scoring_input, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.criteriaLabel.text = item.criteria
        holder.criteriaScoreField.setText(item.score.trimTrailingZero())
        if (isClickable == true) {
            holder.criteriaScoreField.addTextChangedListener {
                try {
                    if (it.isNullOrBlank()) {
                        listener?.passScores(position, 0.0)
                    } else {
                        var newScore = it.toString().toDouble()
                        listener?.passScores(position, newScore)
                    }
                } catch (e: Exception) {
                    // do nothing
                }
            }
        } else {
            holder.criteriaScoreField.isEnabled = false
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val criteriaLabel = view.criteriaLabel!!
        val criteriaScoreField = view.criteriaScoreField!!
    }
}