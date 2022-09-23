package com.gonpas.nasaapis.ui.epic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gonpas.nasaapis.databinding.SliderItemBinding
import com.gonpas.nasaapis.domain.DomainEpic

class ViewPagerAdapter(private val imageList: List<DomainEpic>, private val condition: ConditionViewPager) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    class ViewPagerViewHolder(val binding: SliderItemBinding) : RecyclerView.ViewHolder(binding.root){
        companion object{
            fun from(parent: ViewGroup): ViewPagerViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SliderItemBinding.inflate(layoutInflater, parent, false)
                return ViewPagerViewHolder(binding)
            }
        }

        fun bind(item: DomainEpic){
            binding.epic = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        //ViewPagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.slider_item, parent, false))
        return ViewPagerViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        condition.condition(position, imageList.size)
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    interface ConditionViewPager{
        fun condition(position: Int, fullSize: Int)
    }
}