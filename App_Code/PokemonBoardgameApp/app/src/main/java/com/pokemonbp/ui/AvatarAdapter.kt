package com.pokemonbp.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.pokemonbp.R
import com.pokemonbp.data.ThemeColors
import com.pokemonbp.model.TrainerAvatar
import com.pokemonbp.model.TrainerGender

class AvatarAdapter(
    private var avatars: List<TrainerAvatar>,
    private var selectedId: Int,
    private val c: ThemeColors,
    private val onSelected: (TrainerAvatar) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v.findViewById(R.id.card_avatar)
        val ivImg: ImageView = v.findViewById(R.id.iv_avatar_img)
        val tvLabel: TextView = v.findViewById(R.id.tv_avatar_label)
        val tvName: TextView = v.findViewById(R.id.tv_avatar_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_avatar, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val avatar = avatars[position]
        val isSelected = avatar.id == selectedId

        // Try to load trainer image from drawable
        val resName = if (avatar.gender == TrainerGender.MALE)
            "trainer_male_${avatar.id}"
        else
            "trainer_female_${avatar.id - 100}"
        val resId = holder.itemView.context.resources.getIdentifier(
            resName, "drawable", holder.itemView.context.packageName)

        if (resId != 0) {
            holder.ivImg.setImageResource(resId)
            holder.ivImg.visibility = View.VISIBLE
            holder.tvLabel.visibility = View.GONE
        } else {
            holder.ivImg.visibility = View.GONE
            holder.tvLabel.text = if (avatar.gender == TrainerGender.MALE) "♂" else "♀"
            holder.tvLabel.visibility = View.VISIBLE
        }

        holder.tvName.text = avatar.displayName
        holder.tvName.setTextColor(if (isSelected) Color.WHITE else c.textPrimary)

        holder.card.setCardBackgroundColor(if (isSelected) c.accent else c.surface)
        holder.card.strokeColor = if (isSelected) c.accent else c.cardStroke

        holder.card.setOnClickListener {
            selectedId = avatar.id
            notifyDataSetChanged()
            onSelected(avatar)
        }
    }

    override fun getItemCount() = avatars.size

    fun updateList(newAvatars: List<TrainerAvatar>) {
        avatars = newAvatars
        selectedId = newAvatars.firstOrNull()?.id ?: selectedId
        notifyDataSetChanged()
    }
}
