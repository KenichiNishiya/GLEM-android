import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.glem.ListItem
import com.example.glem.R

class CustomAdapter(
    private val context: Context,
    private val dataSource: List<ListItem>
) : ArrayAdapter<ListItem>(context, R.layout.list_item, dataSource) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val rowView: View

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.list_item, parent, false)
            viewHolder = ViewHolder(
                rowView.findViewById(R.id.imageView),
                rowView.findViewById(R.id.textInfo),
                rowView.findViewById(R.id.textPreco)
            )
            rowView.tag = viewHolder
        } else {
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }

        val item = getItem(position)
        item?.let {
            Glide.with(context)
                .load(it.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(viewHolder.imageView)

            viewHolder.textMarcaModelo.text = "${it.marca} ${it.modelo}"
            viewHolder.textPreco.text = "R$${it.preco}"
        }

        return rowView
    }

    private class ViewHolder(
        val imageView: ImageView,
        val textMarcaModelo: TextView,
        val textPreco: TextView
    )
}
