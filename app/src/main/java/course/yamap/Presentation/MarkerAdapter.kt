import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import course.yamap.Data.DataBase.MarkerEntity
import course.yamap.databinding.MarkerItemBinding

class MarkerAdapter(private val context: Context, private val markerList: MutableList<MarkerEntity>, private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder>() {

    class MarkerViewHolder(private val binding: MarkerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgView = binding.img
        val textTitleView = binding.textTitle
        val card = binding.cardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = MarkerItemBinding.inflate(inflater, parent, false)
        return MarkerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        val markerItem = markerList[position]

        holder.textTitleView.text = markerItem.description
        holder.imgView.setImageBitmap(markerItem.image)

        // Обработка клика на элемент
        holder.card.setOnClickListener {
            onItemClick.invoke(markerItem.id ?: -1)
        }
    }

    override fun getItemCount(): Int {
        return markerList.size
    }

    fun setData(newList: List<MarkerEntity>) {
        markerList.clear()
        markerList.addAll(newList)
        notifyDataSetChanged()
    }
}