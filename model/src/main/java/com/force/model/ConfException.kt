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
    class DataToLarge(source: String) : ConfException("$source: дані занадто великі для передачі", false)
    class ToManyErrorsExceptions() : ConfException("Помилка синхронізації, перезапусти з'єднання", false)
    class BindPhraseException: ConfException("Пассфрази не сходяться")
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
                0x09 -> DataToLarge("Unknown source")
                0x10 -> ToManyErrorsExceptions()
                0x11 -> BindPhraseException()
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
            is DataToLarge -> 0x09
            is ToManyErrorsExceptions -> 0x10
            is BindPhraseException -> 0x11
            else -> -1 // Unknown error, no code defined
        }
    }
}
