package io.github.lumyuan.turingbox.common.data

interface IEventReceiver {
    fun eventFilter(eventType: EventType): Boolean
    fun onReceive(eventType: EventType, data: HashMap<String, Any>? = null)
    val isAsync: Boolean;
    fun onSubscribe()
    fun onUnsubscribe()
}