
object VpnUtils {
    fun isTailscaleConnected(): Boolean {
        return try {
            val output = Runtime.getRuntime().exec("ip a").inputStream.bufferedReader().readText()
            output.contains("tailscale0")
        } catch (e: Exception) {
            false
        }
    }
}
