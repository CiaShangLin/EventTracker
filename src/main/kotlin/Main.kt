fun main(args: Array<String>) {
    EventTracker.appOpen()
}

/**
 * Builder介面
 * @property getParmaMap 參數map主要測試用
 * @property getLogEvent 參數key主要測試用
 * @property putMap 放置參數
 * @property logEvent 傳送
 */
interface IBuilder {
    fun getParmaMap(): Map<String, String>
    fun getLogEvent(): String
    fun putMap(key: String, value: String): IBuilder
    fun logEvent(logEvent: String)
}

/**
 * IGetBuilder
 * @property getBuilder 提供介面,實作的不寫死是因為測試會注入假的Builder
 */
interface IGetBuilder<out T : IBuilder> {
    fun getBuilder(): T
}

/**
 * IEventTracker開放介面
 * 繼承出來的介面可以做一些額外的事件,如果有的話
 */
sealed interface IEventTracker {
    interface Flurry : IEventTracker
    interface Analytics : IEventTracker

    fun appOpen()
}

class FlurryEvent(private val mGetBuilder: IGetBuilder<IBuilder>) : IEventTracker.Flurry {
    override fun appOpen() {
        mGetBuilder.getBuilder().putMap("HOME_PAGE","APP_OPEN").logEvent("System")
    }

}

class AnalyticsEvent(private val mGetBuilder: IGetBuilder<IBuilder>) : IEventTracker.Analytics {
    override fun appOpen() {
        mGetBuilder.getBuilder().putMap("HOME_PAGE","APP_OPEN").logEvent("System")
    }
}

/**
 * 埋點追蹤
 */
object EventTracker : IEventTracker.Flurry, IEventTracker.Analytics {
    private val events = arrayOf(FlurryEvent(Flurry()), AnalyticsEvent(Analytics()))
    override fun appOpen() {
        events.forEach {
            it.appOpen()
        }
    }
}

class Flurry : IGetBuilder<Flurry.Builder> {

    override fun getBuilder(): Builder = Builder()

    class Builder : IBuilder {
        private val mParmaMap = mutableMapOf<String, String>()
        private var mLogEvent = ""

        override fun getParmaMap(): Map<String, String> = mParmaMap

        override fun getLogEvent(): String = mLogEvent

        override fun putMap(key: String, value: String): IBuilder {
            mParmaMap[key] = value
            println("Flurry map = $key $value")
            return this
        }

        override fun logEvent(logEvent: String) {
            mLogEvent = logEvent
            //FlurryAgent.logEvent(logEvent, mParmaMap)
            println("Flurry logEvent = $logEvent")
            println("----------------------------")
        }
    }
}

class Analytics : IGetBuilder<Analytics.Builder> {

    override fun getBuilder(): Builder = Builder()

    class Builder : IBuilder {
        private val mParmaMap = mutableMapOf<String, String>()
        private var mLogEvent = ""

        override fun getParmaMap(): Map<String, String> = mParmaMap

        override fun getLogEvent(): String = mLogEvent

        override fun putMap(key: String, value: String): IBuilder {
            //mBundle.putString(key, value)
            mParmaMap[key] = value
            println("Analytics bundle = $key $value")
            return this
        }

        override fun logEvent(logEvent: String) {
            mLogEvent = logEvent
            //mFirebaseAnalytics.logEvent(logEvent, mBundle)
            println("Analytics logEvent = $logEvent")
            println("----------------------------")
        }
    }
}