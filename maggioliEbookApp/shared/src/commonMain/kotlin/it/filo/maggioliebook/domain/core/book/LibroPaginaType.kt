package it.filo.maggioliebook.domain.core.book

enum class LibroPaginaType {
    XLIGO {
        override fun toString(): String {
            return "xligo"
        }
    },
    REDA {
        override fun toString(): String {
            return "reda"
        }
    },
    REDA_ALL {
        override fun toString(): String {
            return "reda-all"
        }
    }
}