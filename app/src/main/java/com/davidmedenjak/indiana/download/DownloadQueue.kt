package com.davidmedenjak.indiana.download

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList
import java.util.Queue

class DownloadQueue {
    private val queue: Queue<DownloadState.Pending> = LinkedList()
    private val mutex = Mutex()
    private val maxConcurrentDownloads = 3
    private var currentDownloads = 0

    suspend fun enqueue(download: DownloadState.Pending) {
        mutex.withLock {
            queue.offer(download)
        }
    }

    suspend fun dequeue(): DownloadState.Pending? {
        return mutex.withLock {
            if (currentDownloads >= maxConcurrentDownloads) {
                null
            } else {
                queue.poll()?.also {
                    currentDownloads++
                }
            }
        }
    }

    suspend fun releaseSlot() {
        mutex.withLock {
            if (currentDownloads > 0) {
                currentDownloads--
            }
        }
    }

    suspend fun size(): Int {
        return mutex.withLock {
            queue.size
        }
    }

    suspend fun isEmpty(): Boolean {
        return mutex.withLock {
            queue.isEmpty()
        }
    }

    suspend fun clear() {
        mutex.withLock {
            queue.clear()
            currentDownloads = 0
        }
    }

    suspend fun getQueuedDownloads(): List<DownloadState.Pending> {
        return mutex.withLock {
            queue.toList()
        }
    }
}