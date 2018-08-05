package dfialho.tveebot.tracker.api

/**
 * Representation for the quality of video files.
 *
 * @property identifier An identifier specific to a video quality
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
enum class VideoQuality(val identifier: String) {
    FULL_HD("1080p"),
    HD("720p"),
    SD("480p"),
}

/**
 * Map to convert an identifier to a video quality.
 */
private val qualities: Map<String, VideoQuality> = VideoQuality.values().associateBy({it.identifier}, {it})

/**
 * Returns the [VideoQuality] corresponding to the [identifier], or null if the [identifier] does not match with any
 * video quality.
 */
fun videoQualityFromIdentifier(identifier: String): VideoQuality? = qualities[identifier]