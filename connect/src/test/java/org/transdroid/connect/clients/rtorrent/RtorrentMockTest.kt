package org.transdroid.connect.clients.rtorrent

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.transdroid.connect.Configuration
import org.transdroid.connect.clients.Client

class RtorrentMockTest {

    private lateinit var server: MockWebServer
    private lateinit var rtorrent: Rtorrent

    @Before
    fun setUp() {
        server = MockWebServer()
        rtorrent = Rtorrent(Configuration(Client.RTORRENT, server.url("/").toString(), "/RPC2"))
    }

    @Test
    fun clientVersion() {
        server.enqueue(mock("<param><value><string>0.9.6</string></value></param>"))
        rtorrent.clientVersion()
                .test()
                .assertValue("0.9.6")
        server.takeRequest()
    }

    @Test
    fun torrents() {
        server.enqueue(mock("<param><value><array><data><value><array><data><value><string>59066769B9AD42DA2E508611C33D7C4480B3857B</string></value><value><string>ubuntu-17.04-desktop-amd64.iso</string></value><value><i8>0</i8></value><value><i8>0</i8></value><value><i8>0</i8></value><value><i8>0</i8></value><value><i8>0</i8></value><value><i8>0</i8></value><value><i8>0</i8></value><value><i8>1609039872</i8></value><value><i8>1609039872</i8></value><value><i8>1492077159</i8></value><value><i8>0</i8></value><value><i8>0</i8></value><value><i8>0</i8></value><value><string></string></value><value><string></string></value><value><string></string></value><value><string></string></value><value><string></string></value><value><string></string></value><value><i8>0</i8></value><value><i8>0</i8></value></data></array></value></data></array></value></param>"))
        rtorrent.torrents()
                .test()
                .assertValue { it.hash == "59066769B9AD42DA2E508611C33D7C4480B3857B" }
        server.takeRequest()
    }

    @Test
    fun addByUrl() {
        server.enqueue(mock("<param><value><string>0.9.6</string></value></param>"))
        server.enqueue(mock("<param><value><i4>0</i4></value></param>"))
        rtorrent.addByUrl("http://releases.ubuntu.com/17.04/ubuntu-17.04-desktop-amd64.iso.torrent")
                .test()
                .assertNoErrors()
        server.takeRequest()
        server.takeRequest()
    }

    @Test
    fun addByMagnet() {
        server.enqueue(mock("<param><value><string>0.9.6</string></value></param>"))
        server.enqueue(mock("<param><value><i4>0</i4></value></param>"))
        rtorrent.addByMagnet("http://torrent.ubuntu.com:6969/file?info_hash=%04%03%FBG%28%BDx%8F%BC%B6%7E%87%D6%FE%B2A%EF8%C7Z")
                .test()
                .assertNoErrors()
        server.takeRequest()
        server.takeRequest()
    }

    @Test
    fun start() {
        server.enqueue(mock("<param><value><i4>0</i4></value></param>"))
        rtorrent.start(MockTorrent.downloading)
                .test()
                .assertValue { it.canStop }
        server.takeRequest()
    }

    @Test
    fun stop() {
        server.enqueue(mock("<param><value><i4>0</i4></value></param>"))
        rtorrent.stop(MockTorrent.seeding)
                .test()
                .assertValue { it.canStart }
        server.takeRequest()
    }

    private fun mock(params: String): MockResponse? {
        return MockResponse()
                .addHeader("Content-Type", "application/xml; charset=UTF-8")
                .setBody("<?xml version=\"1.0\"?>\n" +
                        "<methodResponse>\n" +
                        "  <params>\n" +
                        "    {$params}\n" +
                        "  </params>\n" +
                        "</methodResponse>")
    }

}