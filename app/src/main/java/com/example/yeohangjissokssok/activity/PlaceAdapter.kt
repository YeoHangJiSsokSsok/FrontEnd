import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yeohangjissokssok.activity.PlaceAdapterRecommend
import com.example.yeohangjissokssok.activity.PlaceResponse
import com.example.yeohangjissokssok.databinding.PlaceRecyclerBinding

class PlaceAdapter(var datas: ArrayList<PlaceResponse>)
    : RecyclerView.Adapter<PlaceAdapter.ViewHolder>(){

    interface OnItemClickListener{
        fun OnItemClick(position:Int)
    }

    var itemClicklistener:OnItemClickListener?=null

    inner class ViewHolder(val binding: PlaceRecyclerBinding) :  RecyclerView.ViewHolder(binding.root) {
        init{
            binding.tvRvPlacename.setOnClickListener {
                itemClicklistener?.OnItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceAdapter.ViewHolder {
        val view = PlaceRecyclerBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.binding.tvRvPlace.text = datas[position].region + " " + datas[position].name
        holder.binding.tvRvPlacename.text = datas[position].name
        holder.binding.tvRvPlacelocation.text = datas[position].region
    }

}

//class PlaceAdapter(private val context: Context) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {
//
//    var datas = mutableListOf<MainData>()
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val binding = PlaceRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ViewHolder(binding.root)
//    }
//
//    override fun getItemCount(): Int = datas.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(datas[position])
//    }
//
//    // 리사이클러뷰 클릭 리스너 추가
//    interface OnItemClickListener{
//        fun OnItemClick(position: Int):OnItemClickListener?=null
//    }
//
//    var itemClickListener:OnItemClickListener?=null
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        private val binding = PlaceRecyclerBinding.bind(view)
//
//        init {
//            // 리사이클러뷰 클릭 리스너 연결결
//            binding.tvRvPlace.setOnClickListener {
//                itemClickListener?.OnItemClick(adapterPosition)
//        }}
//
//        fun bind(item: MainData) {
//            binding.tvRvPlace.text=item.tv_rv_place
//            Glide.with(context).load(item.img).into(binding.imgRvPlus)
//            //binding.txtName.text = item.tv_rv_place
//            //Glide.with(context).load(item.img).into(binding.imgPlus)
//        }
//    }
//}