import com.anytypeio.anytype.domain.config.Gateway

object FakeGateWay : Gateway {
    override fun provide(): String = "anytype.io"
}