package com.davidmedenjak.indiana.features.projects

import android.view.View
import android.widget.CheckBox
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.davidmedenjak.indiana.R

class FilterController : EpoxyController() {

    override fun buildModels() {

        val projectTypes = listOf(
            ProjectType("android", R.string.project_type_android),
            ProjectType("ios", R.string.project_type_ios),
            ProjectType("cordova", R.string.project_type_cordova),
            ProjectType("other", R.string.project_type_other),
            ProjectType("xamarin", R.string.project_type_xamarin),
            ProjectType("macos", R.string.project_type_macos),
            ProjectType("ionic", R.string.project_type_ionic),
            ProjectType("react-native", R.string.project_type_react_native),
            ProjectType("fastlane", R.string.project_type_fastlane)
        ).forEach { item ->
            CheckboxModel(item.name, false) { isChecked ->

            }
                .id(item.type)
                .addTo(this)
        }
    }
}

data class CheckboxModel(
    val title: Int,
    val checked: Boolean,
    val onCheckedChanged: (Boolean) -> Unit
) : EpoxyModelWithHolder<CheckboxModel.Holder>() {

    class Holder : EpoxyHolder() {

        lateinit var checkbox: CheckBox

        override fun bindView(itemView: View) {
            checkbox = itemView.findViewById(R.id.checkbox)
        }
    }

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = checked
        holder.checkbox.setOnCheckedChangeListener { _, isChecked -> onCheckedChanged(isChecked) }

        holder.checkbox.setText(title)
    }

    override fun getDefaultLayout(): Int = R.layout.item_checkbox

    override fun createNewHolder(): Holder = Holder()
}

class ProjectType(
    val type: String,
    val name: Int
)