package com.force.model

/*
#define ERROR_CIFFERTEXT_TO_SHORT       0x01
#define ERROR_DECRYPT                   0x02
#define ERROR_NOT_SUPPORT               0x03
#define ERROR_PROTO_DECODE              0x04
#define ERROR_PROTO_ENCODE              0x05
#define ERROR_ENCODE                    0x06
 */

//todo chek what error is critical (when can exchange with messages)
sealed class ConfException(message: String, val isCritical: Boolean = true) : Exception(message) {
    class CifferTextTooShortException : ConfException("Шифртекст закоротокий")
    class DecryptException : ConfException("Помилка розшифровки, можливо, не сходяться пассфрази")
    class NotSupportedException(message: String) : ConfException(message, false)
    class ProtoDecodeException : ConfException("Помилка декодування протоколу, можливо, не сходяться версії")
    class ProtoEncodeException : ConfException("Помилка кодування протоколу")
    class EncodeException : ConfException("Помилка зашифрування")
    class SocketException : ConfException("З'єднання розірвано, можливо, пристрій вимкнено", true)
    class DisconnectException : ConfException("З'єднання закрито", true)
    class UnknownException(message: String) : ConfException("Невідома помилка: $message", true)

    companion object {
        fun fromCode(code: Int): ConfException {
            return when (code) {
                0x01 -> CifferTextTooShortException()
                0x02 -> DecryptException()
                0x03 -> NotSupportedException("Операція не підтримується")
                0x04 -> ProtoDecodeException()
                0x05 -> ProtoEncodeException()
                0x06 -> EncodeException()
                0x07 -> SocketException()
                0x08 -> DisconnectException()
                else -> UnknownException("code: $code")
            }
        }

        fun ConfException.toCode() = when (this) {
            is CifferTextTooShortException -> 0x01
            is DecryptException -> 0x02
            is NotSupportedException -> 0x03
            is ProtoDecodeException -> 0x04
            is ProtoEncodeException -> 0x05
            is EncodeException -> 0x06
            is SocketException -> 0x07
            is DisconnectException -> 0x08
            else -> -1 // Unknown error, no code defined
        }
    }
}
