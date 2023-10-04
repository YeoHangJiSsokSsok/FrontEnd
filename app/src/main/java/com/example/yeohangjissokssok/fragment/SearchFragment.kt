package com.example.yeohangjissokssok.fragment

import PlaceAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.yeohangjissokssok.activity.*
import com.example.yeohangjissokssok.api.RetrofitBuilder
import com.example.yeohangjissokssok.databinding.FragmentSearchBinding
import com.example.yeohangjissokssok.databinding.SearchRecyclerBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {
    lateinit var searchAdapter: SearchAdapter
    lateinit var placeAdapter: PlaceAdapter

    // 바인딩 객체 선언
    private lateinit var binding: FragmentSearchBinding
    private lateinit var recyclerViewBinding: SearchRecyclerBinding

    val placeDatas = ArrayList<PlaceResponse>()
    val datas = ArrayList<String>()

    var name = "name"

    private lateinit var searchEditText: EditText
    private lateinit var searchBtnInplacelist: ImageView

//    private var visibilityListener: FragmentVisibilityListener? = null
//
//    fun setVisibilityListener(listener: FragmentVisibilityListener) {
//        visibilityListener = listener
//    }

    lateinit var dividerItemDecoration:DividerItemDecoration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        recyclerViewBinding = SearchRecyclerBinding.inflate(inflater, container, false)
        val view = binding.root

        // 데이터 추가
        datas.add("서울")
        datas.add("부산")

        searchAdapter = SearchAdapter(this.datas)
        binding.recyclerView.adapter = searchAdapter

        // RecyclerView 어댑터에 데이터 설정
        searchAdapter.datas = datas

        // RecyclerView 갱신
        searchAdapter.notifyDataSetChanged()

        initClickEvent()

        return view
    }

    private fun initClickEvent() {

        searchAdapter.itemClicklistener =
            object : SearchAdapter.OnItemClickListener {
                // 최근 검색 리사이클러뷰 data 클릭 리스너
                override fun OnItemClick(position: Int) {
                    placeAdapter = PlaceAdapter(placeDatas)
                    binding.recyclerView.adapter = placeAdapter

                    // RecyclerView를 처음에는 숨김(GONE) 상태로 설정
                    binding.recyclerView.visibility = View.GONE

                    // 리사이클러뷰 구분선 지정
                    dividerItemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                    binding.recyclerView.addItemDecoration(dividerItemDecoration)

                    initPlaceRecycler()

                    var searchText = searchAdapter.datas[position]
                    Log.d("data", searchText)
                    if (searchText.isNotEmpty()) {
                        fetchPlacesByName(searchText)

                        binding.searchText.setText(searchText)
                        binding.searchText.setSelection(0, searchText.length)

                        // datas에 해당 검색어가 이미 존재하면 삭제한 후 추가
                        if(datas.contains(searchText))
                            datas.remove(searchText)
                        datas.add(0, searchText)
                        searchAdapter.notifyDataSetChanged()

                        // 리사이클러뷰를 보이도록 변경
                        binding.recyclerView.visibility = View.VISIBLE
                    }

                    // "최근 검색" 글씨 제거
                    binding.recentRecord.visibility = View.GONE
                }

                // 최근 검색 리사이클러뷰 data 삭제 버튼 클릭 리스너
                override fun DeleteClick(pos: Int) {
                    searchAdapter.removeData(pos)

                    // 마지막 data까지 삭제하면 마지막 구분선 및 "최근 검색" 제거
                    if(datas.isEmpty()) {
                        binding.lastDivider.visibility = View.GONE
                        binding.recentRecord.visibility = View.GONE
                    }
                }
            }

        // 뒤로가기 버튼 클릭 리스너
        binding.apply {
            goBackBtn.setOnClickListener {
                // 기본 검색 화면으로 설정
                binding.searchText.setText("")
                initSearchRecycler()

                // "최근 검색" 글씨 생성
                binding.recentRecord.visibility = View.VISIBLE

                // 리사이클러뷰 구분선 제거
                binding.recyclerView.removeItemDecoration(dividerItemDecoration)
            }

            // 검색 버튼 클릭 리스너
            searchBtn.setOnClickListener {
                // 첫 data 검색 시 지막 구분선 및 "최근 검색" 추가
                if(datas.isEmpty()) {
                    binding.lastDivider.visibility = View.VISIBLE
                    binding.recentRecord.visibility = View.VISIBLE
                }

                searchEditText = binding.searchText
                searchBtnInplacelist = binding.searchBtn

                placeAdapter = PlaceAdapter(placeDatas)
                binding.recyclerView.adapter = placeAdapter

                // RecyclerView를 처음에는 숨김(GONE) 상태로 설정
                binding.recyclerView.visibility = View.GONE

                // 리사이클러뷰 구분선 지정
                dividerItemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                binding.recyclerView.addItemDecoration(dividerItemDecoration)

                initPlaceRecycler()

                val searchText = binding.searchText.text.toString()
                if (searchText.isNotEmpty()) {
                    fetchPlacesByName(searchText)

                    // datas에 해당 검색어가 이미 존재하면 삭제한 후 추가
                    if(datas.contains(searchText))
                        datas.remove(searchText)
                    datas.add(0, searchText)
                    searchAdapter.notifyDataSetChanged()

                    // 검색 버튼 클릭 시 Fragment의 가시성 변경을 알림
                    //visibilityListener?.onFragmentVisibilityChanged(true)

                    // 검색 버튼 클릭 시 리사이클러뷰를 보이도록 변경
                    binding.recyclerView.visibility = View.VISIBLE
                }

                // "최근 검색" 글씨 제거
                recentRecord.visibility = View.GONE
            }
        }
    }

    private fun initSearchRecycler() {
        searchAdapter = SearchAdapter(datas)
        binding.recyclerView.adapter = searchAdapter

        // RecyclerView 어댑터에 데이터 설정
        searchAdapter.datas = datas

        // RecyclerView 갱신
        searchAdapter.notifyDataSetChanged()
    }

    private fun initPlaceRecycler() {
        // 어댑터에 데이터 설정
        placeAdapter.datas = placeDatas

        // RecyclerView를 갱신
        placeAdapter.notifyDataSetChanged()
    }

    private fun fetchPlacesByName(searchText: String) {
        getPlaceByName(searchText) { result ->
            // 데이터를 가져온 후 어댑터에 데이터 설정
            placeDatas.clear() // 기존 데이터를 지우고 새로운 데이터로 대체
            placeDatas.addAll(result)
            placeAdapter.notifyDataSetChanged()

            // 리사이클러뷰를 보이도록 변경
            binding.recyclerView.visibility = View.VISIBLE

            // 로그를 추가하여 데이터가 잘 가져와지는지 확인
            Log.d("SearchResultFragment", "Fetched ${result.size} places by name")
        }
    }

    private fun getPlaceByName(input: String, callback: (List<PlaceResponse>) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            RetrofitBuilder.api.getPlaceByName(input).enqueue(object : Callback<APIResponseData> {
                override fun onResponse(
                    call: Call<APIResponseData>, response: Response<APIResponseData>
                ) {
                    if (response.isSuccessful) {
                        val temp = response.body() as APIResponseData
                        val jsonResult = Gson().toJson(temp.data)
                        val result: List<PlaceResponse> = GsonBuilder()
                            .create()
                            .fromJson(jsonResult, Array<PlaceResponse>::class.java)
                            .toList()
                        callback(result) // 데이터를 가져온 후 콜백으로 전달
                    }
                }

                override fun onFailure(call: Call<APIResponseData>, t: Throwable) {
                    Log.d("test", "연결실패")
                }
            })
        }
    }
}




