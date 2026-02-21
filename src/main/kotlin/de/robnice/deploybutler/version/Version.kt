package de.robnice.deploybutler.version

data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version>  {

    override fun compareTo(other: Version): Int =
        compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

    override fun toString(): String = "$major.$minor.$patch"

    fun bump(type: ReleaseType): Version =
        when (type) {
            ReleaseType.NONE,
            ReleaseType.FROM_BUILD_GRADLE ->
                this

            ReleaseType.REVISION ->
                copy(patch = patch + 1)

            ReleaseType.FEATURE ->
                copy(minor = minor + 1, patch = 0)

            ReleaseType.MAJOR ->
                Version(major + 1, 0, 0)
        }

    companion object {
        fun parse(raw: String): Version {
            val parts = raw.split(".")
            require(parts.size == 3) { "Invalid version format: $raw" }
            return Version(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
    }
}
