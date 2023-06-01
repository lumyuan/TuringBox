package io.github.lumyuan.turingbox.common.store

import android.content.Context
import io.github.lumyuan.turingbox.common.model.TriggerInfo
import io.github.lumyuan.turingbox.common.shared.ObjectStorage

class TriggerStorage(private val context: Context) : ObjectStorage<TriggerInfo>(context) {
    fun save(obj: TriggerInfo): Boolean {
        return super.save(obj, obj.id)
    }
}
