package io.github.lumyuan.turingbox.common.store

object SpfConfig {
    const val POWER_CONFIG_SPF = "powercfg"
    const val HOME_QUICK_SWITCH = "home_quick_switch"

    const val CHARGE_SPF = "charge" //spf

    const val CHARGE_SPF_QC_BOOSTER = "qc_booster" //bool

    const val CHARGE_SPF_QC_LIMIT = "charge_limit_ma" //int

    const val CHARGE_SPF_QC_LIMIT_DEFAULT = 3000 //int

    const val CHARGE_SPF_BP = "bp" //bool

    const val CHARGE_SPF_BP_LEVEL = "bp_level" //int

    const val CHARGE_SPF_BP_LEVEL_DEFAULT = 90 //int

    // 是否开启睡眠时间充电速度调整
    const val CHARGE_SPF_NIGHT_MODE = "sleep_time" //bool

    // 起床时间
    const val CHARGE_SPF_TIME_GET_UP = "time_get_up" //int（hours*60 + minutes）

    // 起床时间（默认为7:00）
    const val CHARGE_SPF_TIME_GET_UP_DEFAULT = 7 * 60 //

    // 睡觉时间
    const val CHARGE_SPF_TIME_SLEEP = "time_slepp" //int（hours*60 + minutes）

    // 睡觉时间（默认为22:30点）
    const val CHARGE_SPF_TIME_SLEEP_DEFAULT = 22 * 60 + 30

    // 执行模式
    const val CHARGE_SPF_EXEC_MODE = ""
    const val CHARGE_SPF_EXEC_MODE_SPEED_UP = 0 // 目标 加快充电

    const val CHARGE_SPF_EXEC_MODE_SPEED_DOWN = 1 // 目标 降低速度保护电池

    const val CHARGE_SPF_EXEC_MODE_SPEED_FORCE = 2 // 目标 强制加速

    const val CHARGE_SPF_EXEC_MODE_DEFAULT = CHARGE_SPF_EXEC_MODE_SPEED_UP // 目标（默认设置）


    const val BOOSTER_SPF_CFG_SPF = "boostercfg2"
    const val DATA = "data"
    const val WIFI = "wifi"
    const val NFC = "nfc"
    const val GPS = "gps"
    const val FORCEDOZE = "doze"
    const val POWERSAVE = "powersave"

    const val ON = "_on"
    const val OFF = "_off"

    const val GLOBAL_SPF = "global" //spf

    const val GLOBAL_SPF_AUTO_INSTALL = "is_auto_install"
    const val GLOBAL_SPF_HELP_ICON = "show_help_icon"
    const val GLOBAL_SPF_SKIP_AD = "is_skip_ad"
    const val GLOBAL_SPF_SKIP_AD_PRECISE = "is_skip_ad_precise2"
    const val GLOBAL_SPF_DISABLE_ENFORCE = "enforce_0"
    const val GLOBAL_SPF_START_DELAY = "start_delay"
    const val GLOBAL_SPF_SCENE_LOG = "scene_logview"
    const val GLOBAL_SPF_AUTO_EXIT = "auto_exit"
    const val GLOBAL_SPF_NIGHT_MODE = "app_night_mode"
    const val GLOBAL_SPF_THEME = "app_theme2"
    const val GLOBAL_SPF_MAC = "wifi_mac"
    const val GLOBAL_SPF_MAC_AUTOCHANGE_MODE = "wifi_mac_autochange_mode"
    const val GLOBAL_SPF_MAC_AUTOCHANGE_MODE_1 = 1
    const val GLOBAL_SPF_MAC_AUTOCHANGE_MODE_2 = 2
    const val GLOBAL_SPF_POWERCFG_FIRST_MODE = "powercfg_first_mode"
    const val GLOBAL_SPF_POWERCFG_SLEEP_MODE = "powercfg_sleep_mode"
    const val GLOBAL_SPF_DYNAMIC_CONTROL = "dynamic_control"
    const val GLOBAL_SPF_DYNAMIC_CONTROL_DEFAULT = false
    const val GLOBAL_SPF_DYNAMIC_CONTROL_STRICT = "dynamic_control_strict"
    const val GLOBAL_SPF_DYNAMIC_CONTROL_DELAY = "dynamic_control_delay"
    const val GLOBAL_SPF_PROFILE_SOURCE = "scene4_profile_source"
    const val GLOBAL_SPF_POWERCFG = "global_powercfg"
    const val GLOBAL_SPF_CONTRACT = "global_contract_scene4"
    const val GLOBAL_SPF_POWERCFG_FRIST_NOTIFY = "global_powercfg_notifyed"
    const val GLOBAL_SPF_LAST_UPDATE = "global_last_update"
    const val GLOBAL_SPF_CURRENT_NOW_UNIT = "global_current_now_unit"
    const val GLOBAL_SPF_CURRENT_NOW_UNIT_DEFAULT = -1000
    const val GLOBAL_SPF_FREEZE_ICON_NOTIFY = "freeze_icon_notify"
    const val GLOBAL_SPF_FREEZE_SUSPEND = "freeze_suspend"
    const val GLOBAL_SPF_FREEZE_DELAY = "freeze_screen_off_delay" // 息屏后处理延迟

    const val GLOBAL_SPF_FREEZE_TIME_LIMIT = "freeze_suspend_time_limit"
    const val GLOBAL_SPF_FREEZE_ITEM_LIMIT = "freeze_suspend_item_limit"
    const val GLOBAL_SPF_FREEZE_XPOSED_OPEN = "freeze_xposed_open"
    const val GLOBAL_SPF_FREEZE_CLICK_OPEN = "freeze_click_open"
    const val GLOBAL_NIGHT_BLACK_NOTIFICATION = "night_black_notification"

    const val SWAP_SPF = "swap" //spf

    const val SWAP_SPF_SWAP = "swap"
    const val SWAP_SPF_SWAP_SWAPSIZE = "swap_size"
    const val SWAP_SPF_SWAP_PRIORITY = "swap_priority"
    const val SWAP_SPF_SWAP_USE_LOOP = "swap_use_loop"
    const val SWAP_SPF_ZRAM = "zram"
    const val SWAP_SPF_ZRAM_SIZE = "zram_size"
    const val SWAP_SPF_SWAPPINESS = "swappiness"
    const val SWAP_SPF_EXTRA_FREE_KBYTES = "extra_free_kbytes"
    const val SWAP_SPF_WATERMARK_SCALE = "watermark_scale"
    const val SWAP_SPF_AUTO_LMK = "auto_lmk"
    const val SWAP_SPF_ALGORITHM = "comp_algorithm" // zram 压缩算法


    const val SCENE_BLACK_LIST = "scene_black_list_spf"
    const val AUTO_SKIP_BLACKLIST = "AUTO_SKIP_BLACKLIST"
}