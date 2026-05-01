package com.shaeed.fcmclient.network

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RetrofitClientHandlerTest {

    @Test
    fun handleRetrofitCall_success_returnsBodyMessage() = runTest {
        val result = RetrofitClient.handleRetrofitCall<RegisterDevice, PostResponse>(
            url = "http://host/register",
            data = RegisterDevice("d", "u", "t"),
            apiServiceFun = { _, _ -> Response.success(PostResponse("registered ok")) }
        )
        assertEquals("registered ok", result)
    }

    @Test
    fun handleRetrofitCall_httpError_returnsCodeAndBody() = runTest {
        val errorBody = "bad request".toResponseBody("text/plain".toMediaTypeOrNull())
        val result = RetrofitClient.handleRetrofitCall<RegisterDevice, PostResponse>(
            url = "http://host/register",
            data = RegisterDevice("d", "u", "t"),
            apiServiceFun = { _, _ -> Response.error(400, errorBody) }
        )
        assertTrue(result.contains("400"))
        assertTrue(result.contains("bad request"))
    }

    @Test
    fun handleRetrofitCall_httpErrorEmptyBody_returnsCodeOnly() = runTest {
        val errorBody = "".toResponseBody("text/plain".toMediaTypeOrNull())
        val result = RetrofitClient.handleRetrofitCall<RegisterDevice, PostResponse>(
            url = "http://host/register",
            data = RegisterDevice("d", "u", "t"),
            apiServiceFun = { _, _ -> Response.error(500, errorBody) }
        )
        assertTrue(result.contains("500"))
    }

    @Test
    fun handleRetrofitCall_ioException_returnsFailureMessage() = runTest {
        val result = RetrofitClient.handleRetrofitCall<RegisterDevice, PostResponse>(
            url = "http://host/register",
            data = RegisterDevice("d", "u", "t"),
            apiServiceFun = { _, _ -> throw java.io.IOException("timeout") }
        )
        assertEquals("Failure: timeout", result)
    }

    @Test
    fun handleRetrofitCall_genericException_returnsFailureMessage() = runTest {
        val result = RetrofitClient.handleRetrofitCall<RegisterDevice, PostResponse>(
            url = "http://host/register",
            data = RegisterDevice("d", "u", "t"),
            apiServiceFun = { _, _ -> throw RuntimeException("crash") }
        )
        assertTrue(result.startsWith("Failure:"))
    }

    @Test
    fun handleRetrofitCall_urlAndDataPassedThrough() = runTest {
        var capturedUrl = ""
        var capturedData: RegisterDevice? = null
        val expectedRequest = RegisterDevice("device1", "user1", "token1")

        RetrofitClient.handleRetrofitCall<RegisterDevice, PostResponse>(
            url = "http://host/path",
            data = expectedRequest,
            apiServiceFun = { url, data ->
                capturedUrl = url
                capturedData = data
                Response.success(PostResponse("ok"))
            }
        )

        assertEquals("http://host/path", capturedUrl)
        assertEquals(expectedRequest, capturedData)
    }
}
