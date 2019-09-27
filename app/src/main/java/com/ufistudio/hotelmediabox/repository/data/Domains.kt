package com.ufistudio.hotelmediabox.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Logo(
    var normalIconName: String = "",
    var bigIconName: String = ""
) : Parcelable

@Parcelize
data class ConnectDetail(
    var ip: String = "",
    var port: String = "",
    var uri: String = "",
    var frequency: String = "",
    var bandwidth: String = "",
    var dvbParameter: String = "",
    var frequencyParameter: String = ""
) : Parcelable

abstract class BaseChannel {
    abstract var chNum: String
    abstract var chName: String
    abstract var chType: String
    abstract var chGenre: String
    abstract var chIp: ConnectDetail
    abstract var chLogo: Logo
}

enum class TVType {
    IPTV, DVBT
}

@Parcelize
data class TVChannel(
    override var chNum: String = "cNumber",
    override var chName: String = "CName",
    override var chType: String = "",
    override var chGenre: String = "",
    override var chIp: ConnectDetail = ConnectDetail(),
    override var chLogo: Logo = Logo()
) : BaseChannel(), Parcelable

@Parcelize
data class Channels(
    var version: String = "",
    var channels: Array<TVChannel>
) : Parcelable


@Parcelize
data class DVBInfo(
    var Frequency: String = "",
    var Bandwidth: String = ""
) : Parcelable

/*
Home
 */
@Parcelize
data class Home(
    val home: HomeContent = HomeContent()
) : Parcelable

@Parcelize
data class HomeContent(
    val stage_type: HomeStageType = HomeStageType(1,"test"),
    val icons: ArrayList<HomeIcons> = arrayListOf(HomeIcons(0,"LIVE TV\\nChannels","LIVE TV",1),
        HomeIcons(1,"VOD\nMovies","VOD",1),
        HomeIcons(2,"Smart\nApps","APP",1),
        HomeIcons(3,"Room\nService","Room Services",1),
        HomeIcons(4,"Hotel\nFacilities","Facilities",1),
        HomeIcons(5,"Nearby\nMe","Nearby Me",1),
        HomeIcons(6,"Tourist\nInfo","Tourist Info",1),
        HomeIcons(7,"Flight\nInfo","Flights",1),
        HomeIcons(8,"Weather\nForecast","Weather",1),
        HomeIcons(9,"Guest\nServices","Guest",1)
    ),
    val promo_banner: ArrayList<HomePromoBanner> = arrayListOf(),
    val weather: HomeWeather = HomeWeather("WIFI ID","aaaaaaaa","WIFI PASSWORD","bbbbbb","TODAY WEATHER","kuala lumpur","-- c")
) : Parcelable

@Parcelize
data class HomeStageType(
    val type: Int,
    val description: String
) : Parcelable

@Parcelize
data class HomeIcons(
    val id: Int,
    val name: String,
    val backTitle: String,
    val enable: Int
) : Parcelable

@Parcelize
data class HomePromoBanner(
    val image: String
) : Parcelable

@Parcelize
data class HomeWeather(
    val wifi_id_title: String,
    val wifi_id: String,
    val wifi_password_title: String,
    val wifi_password: String,
    val weather_title: String,
    val weather_city: String,
    val temp_none: String
) : Parcelable

@Parcelize
data class HomeVodMovie(
    val server_ip: String,
    val port: Int
) : Parcelable

@Parcelize
data class HotelFacilitiesContentList(
    var items: ArrayList<HotelFacilitiesContent>?
) : Parcelable

/*
Hotel Facilities
 */
@Parcelize
data class HotelFacilitiesContent(
    var title: String = "",
    var file_type: String,
    var content: String = "",
    var file_name: String,
    var wait_time: Int = 5
) : Parcelable

@Parcelize
data class HotelFacilitiesCategories(
    val title: String,
    val content_type: Int,
    val description: String,
    val total: Int,
    val contents: ArrayList<HotelFacilitiesContent>
) : Parcelable


@Parcelize
data class HotelFacilities(
    val categories: ArrayList<HotelFacilitiesCategories>
) : Parcelable

/*
Room Service
 */
@Parcelize
data class RoomServices(
    val categories: ArrayList<RoomServiceCategories>,
    val note: NoteButton
) : Parcelable

@Parcelize
data class RoomServiceCategories(
    val title: String,
    val content_type: Int,
    val description: String,
    val total: Int,
    val bottomNote: String,
    val contents: ArrayList<RoomServiceContent>
) : Parcelable

@Parcelize
data class RoomServiceContent(
    var title: String,
    var price: String,
    var type: String,
    var file_type: String,
    var file_name: String,
    var content: String,
    var image: String
) : Parcelable

/*
    Welcome
 */
@Parcelize
data class Welcome(
    var welcome: WelcomeContent
) : Parcelable

@Parcelize
data class WelcomeContent(
    var room: String,
    var description: String,
    var title: String,
    var name: String,
    var time: String,
    var titleImage: String,
    var background: String,
    var music: String,
    var entryButton: String,
    var slideshow:Int = 0
) : Parcelable

/*
    Config
 */
@Parcelize
data class Config(
    var config: ConfigContent = ConfigContent()
) : Parcelable

@Parcelize
data class ConfigContent(
    var language: String = "en",
    var upgradeUrl: String = "",
    var room: String = "",
    var timeFormat: String = "",
    var file_version: String = "",
    var defaultIp: String = "",
    var defaultServerIp: String = "13.113.3.99",
    var j_version: String = "",
    var tar_version: String = "",
    var apk_version: String = ""
) : Parcelable

/*
    Nearby me
 */
@Parcelize
data class NearbyMe(
    val categories: ArrayList<NearbyMeCategories>
) : Parcelable

@Parcelize
data class NearbyMeCategories(
    val title: String,
    val category_id: String,
    val total: Int,
    val contents: ArrayList<NearbyMeContent>
) : Parcelable

@Parcelize
data class NearbyMeContent(
    var title: String,
    var file_type: String,
    var content: String,
    var file_name: String
) : Parcelable

/*
    VOD
 */
@Parcelize
data class Vod(
    val categories: ArrayList<VodCategories>
) : Parcelable

@Parcelize
data class VodCategories(
    val title: String,
    val category_id: String,
    val total: Int,
    val contents: ArrayList<VodContent>
) : Parcelable

@Parcelize
data class VodContent(
    var title: String,
    var uri: String,
    var ip: String,
    var port: String,
    var description: String,
    var info: String,
    var image: String,
    var label: List<String>,
    var trailer: VodTrailer
) : Parcelable

@Parcelize
data class VodTrailer(
    var ip: String,
    var port: String,
    var uri: String
) : Parcelable

/*
    Setting
 */
@Parcelize
data class Setting(
    var categories: ArrayList<SettingCategories>
) : Parcelable

@Parcelize
data class SettingCategories(
    var title: String,
    var type: String,
    var contents: SettingContent
) : Parcelable

@Parcelize
data class SettingContent(
    var content_title: String,
    var image: String,
    var content: List<SettingSubContent>? = ArrayList()
) : Parcelable

@Parcelize
data class SettingSubContent(
    var title: String,
    var code: String
) : Parcelable

/*
   Flights Info
 */
@Parcelize
data class FlightsInfo(
    var categories: ArrayList<FlightsInfoCategories>
) : Parcelable

@Parcelize
data class FlightsInfoCategories(
    var title: String,
    var contents: ArrayList<FlightsInfoContent>
) : Parcelable

@Parcelize
data class FlightsInfoContent(
    var iptv: String
) : Parcelable

/*
   Weather
 */
@Parcelize
data class Weather(
    var categories: ArrayList<WeatherCategories>,
    var title: String,
    var subtitle: String,
    var update: String,
    var temp_none: String,
    var note: NoteButton
) : Parcelable

@Parcelize
data class WeatherCategories(
    var title: String,
    var contents: WeatherContent
) : Parcelable

@Parcelize
data class WeatherContent(
    var title: String,
    var subtitle: String
) : Parcelable

/*
   WeatherInfo
 */
@Parcelize
data class WeatherInfo(
    var forecasts: ArrayList<WeatherForecasts>? = ArrayList(),
    var location: WeatherLocation,
    var current_observation: CurrentObservation?
) : Parcelable

@Parcelize
data class CurrentObservation(
    var pubDate: String? = ""
) : Parcelable

@Parcelize
data class WeatherLocation(
    var city: String? = ""
) : Parcelable

@Parcelize
data class WeatherForecasts(
    var day: String? = "",
    var date: Long = 0,
    var low: String? = "",
    var high: String? = "",
    var text: String? = ""
) : Parcelable


/*
    Note button on bottom
 */
@Parcelize
data class NoteButton(
    var note: Note? = Note()
) : Parcelable

@Parcelize
data class Note(
    var home: String = "Home",
    var back: String = "Back",
    var fullScreen: String = "Full Screen",
    var toScroll: String = "To Scroll",
    var select: String = "Select",
    var watch_movie: String = "Watch Trailer",
    var rewind: String = "Rewind",
    var play_pause: String = "Play/Pause",
    var fast_forward: String = "Fast Forward",
    var stop: String = "Stop",
    var navigation: String = "Navigation",
    var channels: String = "Channels",
    var next: String = "Next",
    var select_language: String = "Select Language",
    var get_message:String = "You got a message. Please press"
) : Parcelable

/*
    Broadcast
 */
@Parcelize
data class Broadcast(
    var ip: String,
    var command: String,
    var url: String,
    var port: String,
    var type: String,
    var needUpdate: Int = 0,
    var force: String = "0",
    var md5: String
) : Parcelable


@Parcelize
data class TimeInfo(
    var time: String
) : Parcelable

/*
tourist
*/
data class TouristAttractions(
    var title: String = "",
    var imageName: String = "",
    var description: String = ""
)

data class TouristLocation(
    var placeName: String = "",
    var focus: Any = "",
    var background: Any = "",
    var attractionsList: ArrayList<TouristAttractions>? = ArrayList()
)

data class TouristInfo(
    var locationList: ArrayList<TouristLocation>? = ArrayList()
)

@Parcelize
data class InitialData(
    var time: String,
    var timestamp: Long,
    var guestName: String = "",
    var roomNum: String = "",
    var timezone: String = "Asia/Taipei",
    var wifiId: String = "",
    var wifiPassword: String = "",
    var memos: ArrayList<Memo>? = ArrayList()

) : Parcelable

@Parcelize
data class Memo(
    var title: String = "",
    var content: String = ""
) : Parcelable

@Parcelize
data class StaticIpData(
    var defaultIp: String?,
    var defaultMask: String?,
    var gateway: String? = "0.0.0.0"
) : Parcelable

data class JVersion(
    var process: String = "",
    var ver: String = "",
    var build: String = "",
    var chanlistver: String = ""
)

data class GenreList(
    var GenreType: ArrayList<GenreType> = ArrayList()
)

data class GenreType(
    var key: String = "",
    var display: String = ""
)

data class KDownloadVersion(
    var process: String = "",
    var ver: String = "",
    var build: String = ""
)

data class KDownloadProgress(
    var dlinfo: DLInfo = DLInfo(),
    var status: DLStatus = DLStatus(),
    var md5result: DLmd5result = DLmd5result()
)

data class DLInfo(
    var flag: String = "",
    var fullURL: String = "",
    var outFile: String = "",
    var buexpMD5ild: String = "",
    var expFileSZ: String = ""
)

data class DLStatus(
    var download_exec_valid: String = "",
    var download_exec_rtn: String = "",
    var msg_valid: String = "",
    var msg: String = ""
)

data class DLmd5result(
    var valid: String = "",
    var calMD5: String = "",
    var cmp: String = ""
)

@Parcelize
data class GuestMessage(
    var title: String,
    var content: String,
    var guest: String
) : Parcelable

@Parcelize
data class GuestCatagories(
    var title: String = "",
    var content_type: String = "",
    var total: Int = 0,
    var contents: ArrayList<GuestMessage> = arrayListOf()
) : Parcelable

@Parcelize
data class PMS(
    var status: String = "",
    var catagories: ArrayList<GuestCatagories> = arrayListOf()
) : Parcelable

data class slideItem(
    var file_type: String = "",
    var file_name: String = "",
    var wait_time: Int = 0
)