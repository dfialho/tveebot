package dfialho.tveebot.app.api.models

/**
 * Representation for the quality of video files.
 *
 * @property identifier An identifier specific to a video quality
 * @author David Fialho (dfialho@protonmail.com)
 */
enum class VideoQuality(private val identifier: String) {
    FHD("1080p"),
    HD("720p"),
    SD("480p");

    companion object {

        /**
         * Returns the default video quality.
         */
        fun default(): VideoQuality = SD
    }

    override fun toString(): String {
        return identifier
    }
}

/**
 * Map to convert an identifier to a video quality.
 */
private val qualities: Map<String, VideoQuality> = VideoQuality.values().associateBy({ it.toString() }, { it })

/**
 * Returns the [VideoQuality] corresponding to this string identifier.
 *
 * @throws IllegalArgumentException if the string is not a valid identifier of a [VideoQuality]
 */
fun String.toVideoQuality(): VideoQuality =
    qualities[this] ?: throw IllegalArgumentException("No video quality found for identifier '$this'")


/**
 * Returns the [VideoQuality] corresponding to this string identifier, or null if the string does not a valid
 * identifier of a [VideoQuality].
 */
fun String.toVideoQualityOrNull(): VideoQuality? = qualities[this]
