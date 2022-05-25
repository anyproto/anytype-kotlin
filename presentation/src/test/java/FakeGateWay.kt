import com.anytypeio.anytype.domain.config.Gateway

object FakeGateWay : Gateway {
    override fun obtain(): String = "anytype.io"
}