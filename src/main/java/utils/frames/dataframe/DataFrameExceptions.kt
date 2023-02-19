package utils.frames.dataframe

class SymmetryException(a: Int, b: Int): IllegalStateException("Different symmetry values ${a}:${b}")

class BadDataKeyException(key: String): IllegalStateException("No such key in dataframe $key")

class BadDataException(value: Any?, key: String): IllegalStateException("Value $value not acceptable for $key")