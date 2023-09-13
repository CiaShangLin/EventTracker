import org.junit.Assert
import org.junit.Test

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