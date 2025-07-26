package com.force.confbb.model

/*
#define ERROR_CIFFERTEXT_TO_SHORT       0x01
#define ERROR_DECRYPT                   0x02
#define ERROR_NOT_SUPPORT               0x03
#define ERROR_PROTO_DECODE              0x04
#define ERROR_PROTO_ENCODE              0x05
#define ERROR_ENCODE                    0x06
 */

//todo chek what error is critical (when can exchange with messages)
sealed class ConfError(message: String, val isCritical: Boolean = true) : Exception(message) {
    class CifferTextTooShortError : ConfError("Шифртекст закоротокий")
    class DecryptError : ConfError("Помилка розшифровки, можливо, не сходяться пассфрази")
    class NotSupportedError : ConfError("Операція не підтримується", false)
    class ProtoDecodeError : ConfError("Помилка декодування протоколу, можливо, не сходяться версії")
    class ProtoEncodeError : ConfError("Помилка кодування протоколу")
    class EncodeError : ConfError("Помилка зашифрування")
    class SocketError : ConfError("З'єднання розірвано, можливо, пристрій вимкнено", true)
    class UnknownError(message: String) : ConfError("Невідома помилка: $message", true)

    companion object {
        fun fromCode(code: Int): ConfError {
            return when (code) {
                0x01 -> CifferTextTooShortError()
                0x02 -> DecryptError()
                0x03 -> NotSupportedError()
                0x04 -> ProtoDecodeError()
                0x05 -> ProtoEncodeError()
                0x06 -> EncodeError()
                else -> UnknownError("")
            }
        }
    }
}
