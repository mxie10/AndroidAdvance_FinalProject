import android.bluetooth.BluetoothAdapter
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebase.FullImageActivity
import com.example.firebase.R

class ImagesAdapter(
    private val imagesList: List<String>,
    private val onDeleteClickListener: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.imageView)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val bluetoothButton: ImageButton = itemView.findViewById(R.id.bluetoothButton)

        fun bind(imageUrl: String, onDeleteClickListener: ((Int) -> Unit)?) {
            Glide.with(itemView.context)
                .load(imageUrl)
                .into(image)

            // Set click listener for delete button
            deleteButton.setOnClickListener {
                onDeleteClickListener?.invoke(adapterPosition)
            }

            // Set click listener for full image view
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, FullImageActivity::class.java).apply {
                    putExtra("image_url", imageUrl)
                }
                itemView.context.startActivity(intent)
            }

            // Set click listener for Bluetooth button
            bluetoothButton.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_STREAM, getImageUri(imageUrl))
                itemView.context.startActivity(Intent.createChooser(intent, "Share image via"))
            }
        }

        private fun getImageUri(imageUrl: String): Uri? {
            val mediaUri: Uri? = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media.DATA}=?"
            val selectionArgs = arrayOf(imageUrl)
            val cursor = itemView.context.contentResolver.query(
                mediaUri!!,
                projection,
                selection,
                selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return ContentUris.withAppendedId(mediaUri, id)
            }
            return null
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
