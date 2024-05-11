import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
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
                rowView.findViewById(R.id.textView1),
                rowView.findViewById(R.id.textView2)
            )
            rowView.tag = viewHolder
        } else {
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }

        val item = getItem(position)
        item?.imageId?.let { viewHolder.imageView.setImageResource(it) }
        viewHolder.textView1.text = item?.numberInDigit
        viewHolder.textView2.text = item?.numbersInText

        return rowView
    }

    private class ViewHolder(
        val imageView: ImageView,
        val textView1: TextView,
        val textView2: TextView
    )
}
