val jlibtorrentVersion = "1.2.0.17"

dependencies {
    compile(project(":downloader-api"))
}

dependencies {
    implementation("com.frostwire:jlibtorrent:$jlibtorrentVersion")
}

