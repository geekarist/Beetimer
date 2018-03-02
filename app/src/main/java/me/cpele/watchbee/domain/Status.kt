package me.cpele.watchbee.domain

enum class Status {
    SUCCESS, LOADING, FAILURE, AUTH_ERROR;

    private lateinit var msg: String
    private var cause: Throwable? = null

    companion object {
        fun failure(msg: String, throwable: Throwable?): Status {
            val status = FAILURE
            status.msg = msg
            status.cause = throwable
            return status
        }

        fun authError(msg: String): Status {
            val status = AUTH_ERROR
            status.msg = msg
            return status
        }
    }
}