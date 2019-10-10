package com.yoavgibri.miniweather.views

import android.content.Context
import android.preference.DialogPreference
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.view.Gravity
import android.view.View
import android.view.ViewGroup


class NumberPickerPreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {

    init {
        val f = "g"
    }
    // allowed range
    val MAX_VALUE = 60
    val MIN_VALUE = 1
    // enable or disable the 'circular behavior'
    val WRAP_SELECTOR_WHEEL = true

    private var picker: NumberPicker? = null
    private var value: Int = 0


    override fun onCreateDialogView(): View {
        val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER

        picker = NumberPicker(context)
        picker!!.layoutParams = layoutParams

        val dialogView = FrameLayout(context)
        dialogView.addView(picker)

        return dialogView
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        picker!!.minValue = MIN_VALUE
        picker!!.maxValue = MAX_VALUE
        picker!!.wrapSelectorWheel = WRAP_SELECTOR_WHEEL
        picker!!.value = getValue()
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            picker!!.clearFocus()
            val newValue = picker!!.value
            if (callChangeListener(newValue)) {
                setValue(newValue)
            }
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, MIN_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        setValue(if (restorePersistedValue) getPersistedInt(MIN_VALUE) else defaultValue as Int)
    }

    fun setValue(value: Int) {
        this.value = value
        persistInt(this.value)
    }

    fun getValue(): Int {
        return this.value
    }
}