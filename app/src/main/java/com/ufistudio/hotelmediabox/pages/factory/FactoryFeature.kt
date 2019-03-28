package com.ufistudio.hotelmediabox.pages.factory

import com.ufistudio.hotelmediabox.R

enum class FactoryFeature(var stringRes: Int) {
    IMPORT_JSON_FILE(R.string.factory_import_json_files),
    EXPORT_JSON_FILE(R.string.factory_export_json_files),
    CHECK_UPGRADE_FROM_URL(R.string.factory_check_upgrade_from_url),
    CHECK_UPGRADE_FROM_USB(R.string.factory_check_upgrade_from_usb),
    OPEN_SETTING(R.string.factory_open_setting),
    OPEN_DEFAULT_LAUNCHER(R.string.factory_open_default_launcher)

}