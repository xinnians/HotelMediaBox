package com.ufistudio.hotelmediabox

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.ufistudio.hotelmediabox.pages.TemplateViewModel
import com.ufistudio.hotelmediabox.pages.channel.ChannelViewModel
import com.ufistudio.hotelmediabox.pages.facilies.HotelFacilitiesViewModel
import com.ufistudio.hotelmediabox.pages.factory.FactoryViewModel
import com.ufistudio.hotelmediabox.pages.flights.FlightsInfoViewModel
import com.ufistudio.hotelmediabox.pages.fullScreen.FullScreenViewModel
import com.ufistudio.hotelmediabox.pages.home.HomeViewModel
import com.ufistudio.hotelmediabox.pages.nearby.NearbyMeViewModel
import com.ufistudio.hotelmediabox.pages.roomService.RoomServiceViewModel
import com.ufistudio.hotelmediabox.pages.welcome.WelcomeViewModel
import com.ufistudio.hotelmediabox.pages.setting.SettingViewModel
import com.ufistudio.hotelmediabox.pages.setting.content.LanguageViewModel
import com.ufistudio.hotelmediabox.pages.weather.WeatherViewModel
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.repository.provider.resource.ResourceProvider
import io.reactivex.disposables.CompositeDisposable


class ViewModelFactory(private val application: Application,
                       private val repository: Repository,
                       private val preferences: SharedPreferencesProvider,
                       private val resource: ResourceProvider
) : ViewModelProvider.NewInstanceFactory() {

    //ViewModel需要的model再打進去

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return with(modelClass) {
            when {
                isAssignableFrom(TemplateViewModel::class.java) -> TemplateViewModel(application, CompositeDisposable(), repository)
                isAssignableFrom(ChannelViewModel::class.java) -> ChannelViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(HotelFacilitiesViewModel::class.java) -> HotelFacilitiesViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(RoomServiceViewModel::class.java) -> RoomServiceViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(WelcomeViewModel::class.java) -> WelcomeViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(FactoryViewModel::class.java) -> FactoryViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(FullScreenViewModel::class.java) -> FullScreenViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(SettingViewModel::class.java) -> SettingViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(LanguageViewModel::class.java) -> LanguageViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(NearbyMeViewModel::class.java) -> NearbyMeViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(FlightsInfoViewModel::class.java) -> FlightsInfoViewModel(application,CompositeDisposable(),repository)
                isAssignableFrom(WeatherViewModel::class.java) -> WeatherViewModel(application,CompositeDisposable(),repository)
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            } as T
        }
    }
}