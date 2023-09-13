# EventTracker 埋點追蹤整合器

### 開頭
通常App都會需要做埋點的功能，PM可以看這個頁面的點擊之類的，原先只使用Flurry這個第三方的埋點Library，現在多了一個需求是新增Google Analytics，變成要一次打兩個。
原先Flurry可能是這樣打:
```java
button.onClickListener{
        Flurry.agent().put(key,value).logEvent(log)
        多了GA
        Analytics.agent().put(key,value).logEvent(log)
}
```
這樣有幾個問題
- 散落在各個地方，現在我多了一個GA就要找到每個地方去一增一行，如果以後又多一個要再一次會發瘋。
- 沒辦法做單元測試，因為本身是靜態函數。

### 功能
EventTracker可以透過新增event就可去呼叫到每一個埋點Library，或是只做Flurry不做GA也是可以的。
```java
object EventTracker : IEventTracker.Flurry, IEventTracker.Analytics {
private val events = arrayOf(FlurryEvent(Flurry()), AnalyticsEvent(Analytics()))
        override fun appOpen() {
            events.forEach {
            it.appOpen()
        }
    }
}
```
### 介紹

```java
/**
 * Builder介面，將第三方埋點Library操作抽成介面
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
```

```java
/**
 * IGetBuilder
 * @property getBuilder 提供介面,實作的不寫死是因為測試會注入假的Builder
 */
interface IGetBuilder<out T : IBuilder> {
    fun getBuilder(): T
}
```

```java
/**
 * IEventTracker開放介面
 * 繼承出來的介面可以做一些額外的事件,如果有的話
 */
sealed interface IEventTracker {
    interface Flurry : IEventTracker
    interface Analytics : IEventTracker

    fun appOpen()
}
```

```java
/**
 * Flurry事件實作，注入IGetBuilder，這裡<>不寫死為Flurry.Builder是因為測試要注入假的Builder
 */
class FlurryEvent(private val mGetBuilder: IGetBuilder<IBuilder>) : IEventTracker.Flurry {
        override fun appOpen() {
            mGetBuilder.getBuilder().putMap("HOME_PAGE","APP_OPEN").logEvent("System")
        }
}
```

```java
/**
 * GA事件實作，注入IGetBuilder，這裡<>不寫死為Analytics.Builder是因為測試要注入假的Builder
 */
class AnalyticsEvent(private val mGetBuilder: IGetBuilder<IBuilder>) : IEventTracker.Analytics {
        override fun appOpen() {
            mGetBuilder.getBuilder().putMap("HOME_PAGE","APP_OPEN").logEvent("System")
        }
}
```
```java
/**
 * Flurry的操作封裝
 */
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
```

```java
/**
 * GA的操作封裝
 */
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
```

```java
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
```

### 測試
```java
/**
 * 假的Flurry
 * 如果用原先的Flurry來提供Builder會有幾個問題
 * 第一個是在Android環境下通常會有Log而在junit test時會報錯
 * 第二個是當要做Assert比對時呼叫getBuilder()他會給一個新的而不是當前使用的
 * 第三個你不會想再測試的時候把數據真的打出去
 */
class FakeFlurry : IGetBuilder<IBuilder> {

    private val mBuilder = Builder()

    override fun getBuilder(): IBuilder = mBuilder

    class Builder : IBuilder {
        private val mParmaMap = mutableMapOf<String, String>()
        private var mLogEvent = ""

        override fun getParmaMap(): Map<String, String> = mParmaMap

        override fun getLogEvent(): String = mLogEvent

        override fun putMap(key: String, value: String): IBuilder {
            mParmaMap[key] = value
            return this
        }

        override fun logEvent(logEvent: String) {
            mLogEvent = logEvent
        }
    }
}
```
```java
class UnitTest {
    @Test
    fun test() {
        val fakeFlurry = FakeFlurry()
        val flurryEvent = FlurryEvent(fakeFlurry)
        flurryEvent.appOpen()
        Assert.assertEquals(fakeFlurry.getBuilder().getParmaMap()["HOME_PAGE"],"APP_OPEN")
        Assert.assertEquals(fakeFlurry.getBuilder().getLogEvent(),"System")
    }
}
```

### UML
很久沒畫UML了也不知道有沒有話對。
[![UML](https://github.com/CiaShangLin/EventTracker/blob/main/Main.png "UML")](https://github.com/CiaShangLin/EventTracker/blob/main/Main.png "UML")