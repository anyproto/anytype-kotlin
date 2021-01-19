package com.anytypeio.anytype.providers

import com.anytypeio.anytype.domain.common.Hash
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.presentation.page.cover.CoverImageHashProvider

class DefaultCoverImageHashProvider : CoverImageHashProvider {

    override fun provide(id: Id): Hash? = data[id]

    companion object {
        val data = mapOf(
            "crystal-pallace" to "bafybeicab5x3i4zo74rnoqd7oipzb2r63ejoidsp2rrxh2xulzisee2fum",
            "the-little-pond" to "bafybeifuxo5r3c5cariqjl4v374gjgrojqj4yihfsd6ihonu3zytmzgns4",
            "walk-at-pourville" to "bafybeihocrbpdshvajpixukpketuplex4ckzt5l3pcksc6iqespoaugde4",
            "poppy-field" to "bafybeiaq2li2tl6lgvap5wysj5r7vozfc25c4t54dkcyjwazpucwny72yq",
            "ballet" to "bafybeifea6alfcpjkcytgzvkzv3vyhjarcoopzihwzojzycohgpboovwee",
            "flower-girl" to "bafybeicfhibstzlvpifekhldyp7bsdma6dgwy5mrku7ledjoswocuyl3xa",
            "fruits-midi" to "bafybeidl6xzxsjhgamb7t4qgjjjif7bkvtpthhewprgjoiedouqj2vgjwi",
            "autumn" to "bafybeiabhvkc26qafmora5hxvxwb34feyfc6btoe3ybwlxxkyu2ymk22sm",
            "big-electric-chair" to "bafybeify6z44fpduwdwgsdhf4pk5d5s5d4qguelip3jd7okgqme7tnpysy",
            "flowers" to "bafybeiafdf7nbgjwxbmqwedhcmn35gcd4ejfv3yaeka6mw7vy7m2zteboa",
            "sunday-morning" to "bafybeigsl5dfoj23o4r2rtw43sl4kvhijpkq77qjmgvc2io6qg7nrjjbgu",
            "japan" to "bafybeifowedv7fme7ugjnj6vnqrmf4uz4wudt5l55gee5igqxemhg4xrh4",
            "grass" to "bafybeiginsg5gc7qosdrhkpeobtosbkpmyg6j7mdgomcy567mfpbnbpbpa",
            "butter" to "bafybeihbzr7knybfztkxhpbwezv3lx44h5issibjz77m5fbuy3tku5rkby",
            "medication" to "bafybeiauvmphta2ujtjll3vv7uc2aunzir2vkrlb4d3ais5bmcnzi4w25u",
            "landscape3" to "bafybeidf2fwr4pmoldksbify2n36ufd4e6po4nt664rnidw5jl2k3nuzqi",
            "third-sleep" to "bafybeiaq23nrbztctw36xtayerd6qheoi2ae2x2nods3ksosgfizbrzxqq",
            "banquet" to "bafybeiamsdjmdbdrwdswmkhy4fuavoi7agvdelp2wrafq3q23gn3lnrnoi",
            "chemist" to "bafybeihqthprdduwgxmeyejstjhzoem3u257msinqbeqt3jgbjwylpoese"
        )
    }
}