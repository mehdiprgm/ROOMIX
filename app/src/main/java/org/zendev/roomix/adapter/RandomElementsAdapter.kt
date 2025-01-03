package org.zendev.roomix.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.zendev.roomix.R

class RandomElementsAdapter(
    var context: Context,
    var elements: ArrayList<String>
) : RecyclerView.Adapter<RandomElementsAdapter.RandomElementsViewHolder> () {

    class RandomElementsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvRandomElement : TextView = itemView.findViewById(R.id.tvRandomElement)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandomElementsViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.random_layout, parent, false)
        return RandomElementsViewHolder(view)
    }

    override fun getItemCount(): Int {
       return elements.size
    }

    override fun onBindViewHolder(holder: RandomElementsViewHolder, position: Int) {
        holder.tvRandomElement.text = elements[position]

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Random Element", holder.tvRandomElement.text)
        clipboardManager.setPrimaryClip(clipData)
    }


}