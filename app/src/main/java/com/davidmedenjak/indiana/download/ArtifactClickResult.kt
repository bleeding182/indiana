package com.davidmedenjak.indiana.download

sealed class ArtifactClickResult {
    data class DownloadStarted(val downloadId: String) : ArtifactClickResult()
    object DownloadInProgress : ArtifactClickResult()
    object FileOpened : ArtifactClickResult()
    data class Error(val message: String) : ArtifactClickResult()
}