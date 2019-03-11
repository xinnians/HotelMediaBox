package com.ufistudio.hotelmediabox.pages

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.roomService.template.TemplateType1PagerAdapter
import kotlinx.android.synthetic.main.fragment_room_service_content.*
import kotlinx.android.synthetic.main.framgnet_room_service.*

class TemplateType1Fragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: TemplateType1ViewModel
    private val fakeData: ArrayList<Array<String>> = ArrayList<Array<String>>()


    companion object {
        fun newInstance(): TemplateType1Fragment = TemplateType1Fragment()
        private val TAG = TemplateType1Fragment::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_room_service_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arrayOf("a", "b", "c")

        fakeData.add(arrayOf("audi", "奧迪集團是一間德國的汽車公司，是福斯集團的成員。奧迪汽車主要從事汽車製造業，其產品線非常豐富，從摩托車、小型車至運動型多用途車均有涉足，它的產品在華主要針對豪華型汽車市場，在歐美則是科技與外型個性導向。"))
        fakeData.add(arrayOf("bmw", "BMW台灣總代理汎德官方網站提供BMW德國豪華進口汽車最新車款與活動資訊, 在「先有服務、後有銷售」的原則下,提供最熱忱用心的服務。"))
        fakeData.add(arrayOf("benz", "梅賽德斯-賓士，是一家以豪華和高性能著稱的德國汽車品牌，總部設於德國斯圖加特。旗下產品有各式乘用車、中大型商用車輛。目前梅賽德斯-賓士是戴姆勒公司旗下的成員之一"))
        fakeData.add(arrayOf("lexus", "凌志，是豐田集團旗下的豪華汽車品牌，於全球市場均有銷售。凌志車廠與豐田集團其他旗下車廠不同，成立之初即是為製造豪華汽車的車廠。 凌志創立於1989年9月1日，其商標是一個斜體的拉丁字母「L」圍以一個橢圓形。凌志為北美歷史上一個暢銷的豪華汽車品牌；在2012年，凌志單在美國就銷售了273,847輛汽車"))
        fakeData.add(arrayOf("infinity", "Infiniti是日本車廠日產的高級汽車品牌。1989年11月8日在美洲首先開始銷售，現已經遍及15個國家。和謳歌、凌志並列為日本三大豪華汽車品牌"))
        fakeData.add(arrayOf("mazda", "馬自達株式會社，簡稱馬自達，是日本第五大汽車製造廠，總部位於廣島縣安藝郡府中町，且一度成為全球唯一生產轉子引擎市售車的車廠。2015年馬自達在全球的年產量為137萬5千輛，在全球汽車製造廠中排名第16名，主要銷售市場包括亞洲、歐洲、北美洲、大洋洲等地。 "))
        fakeData.add(arrayOf("nissan", "日產汽車是源自日本的跨國汽車製造商，總部位於橫濱港未來開發區，旗下擁有「日產」、「英菲尼迪」、「達特桑」等多個品牌；為日本歷史上第一家專做汽車為主的企業，於1912年成立，與勞斯萊斯和通用汽車同期，現在是日本第二或三大汽車製造商，現時銷量和營業額與新興的本田汽車相持中，年產量僅次於豐田汽車，也是世界第六大汽車製造商。"))

    }

    override fun onStart() {
        super.onStart()
        view_pager_content.adapter = TemplateType1PagerAdapter(context!!, fakeData)
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (sideView.visibility == View.VISIBLE) {
                }
                return false
            }
            KeyEvent.KEYCODE_BACK -> {
                if (sideView.visibility == View.GONE) {
                    return true
                }
            }
        }
        return super.onFragmentKeyDown(keyCode, event)
    }
}