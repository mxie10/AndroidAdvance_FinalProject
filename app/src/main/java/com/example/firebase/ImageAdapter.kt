import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebase.FullImageActivity
import com.example.firebase.R

class ImagesAdapter(
    private val imagesList: List<String>,
    private val onDeleteClickListener: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(imageUrl: String, onDeleteClickListener: ((Int) -> Unit)?) {
            Glide.with(itemView.context)
                .load(imageUrl)
                .into(image)

            // Set click listener for delete button
            deleteButton.setOnClickListener {
                onDeleteClickListener?.invoke(adapterPosition)
            }

            // Set click listener
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, FullImageActivity::class.java).apply {
                    putExtra("image_url", imageUrl)
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imagesList[position]
        holder.bind(imageUrl, onDeleteClickListener)
    }

    override fun getItemCount() = imagesList.size
}
