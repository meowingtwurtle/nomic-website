import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set

object Persistence {
    private val localStorage: Storage = js("window.localStorage") as Storage

    fun storeToken(token: String) {
        localStorage["token"] = token
    }

    fun clearToken() {
        localStorage.removeItem("token")
    }

    fun getToken(): String? = localStorage["token"]
}