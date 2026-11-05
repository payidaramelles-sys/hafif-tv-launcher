# Hafif TV â€” LuminaOS yerine hafif HOME

2026-11-05 Â· OpenCode / GPT-6.1 Sol Â· Paket: `org.mehmet.litetv`, Activity: `.MainActivity`.

## SonuÃ§ ve Ã¶nemli sÄ±nÄ±r

Saf Android framework Java ile **tek Activity**, animasyonsuz, TÃ¼rkÃ§e, koyu temalÄ±, D-pad odaklÄ± launcher. AndroidX, RecyclerView, Gradle, Ã¼Ã§Ã¼ncÃ¼ taraf baÄŸÄ±mlÄ±lÄ±k, native `.so`, servis, reklam/analitik SDK, internet izni veya sistem ayarÄ± yazma kodu yoktur. Uygulamalar LEANBACK_LAUNCHER + LAUNCHER sorgularÄ±ndan birleÅŸtirilir, TV giriÅŸi Ã¶ncelenir, paket bazÄ±nda tekilleÅŸtirilir. HOME/DEFAULT ve ayrÄ± LEANBACK_LAUNCHER intent filtreleri vardÄ±r.

**Bu uygulama LuminaOS'un sistem yetkisini devralmaz.** 21 private Activity'nin iÅŸlevleri, orijinal uygulama kurulu kaldÄ±ÄŸÄ± iÃ§in OEM Ã¼st menÃ¼sÃ¼ Ã¼zerinden eriÅŸilebilir kalÄ±r; bunlarÄ± yeni UI iÃ§inde bire bir yeniden uyguladÄ±k demiyoruz. DoÄŸrudan projeksiyon setter'Ä±, ayrÄ±calÄ±klÄ± gÃ¼Ã§ API'si, kalibrasyon reset'i veya private ekran bypass'Ä± yoktur. Ã–zel durum simgeleri ve Lumina SQLite/verileri kopyalanmamÄ±ÅŸtÄ±r. BaÄŸÄ±msÄ±z, %100 OEM'den arÄ±ndÄ±rÄ±lmÄ±ÅŸ bir replacement deÄŸildir; hafif HOME + gÃ¼venli Ã¼retici kÃ¶prÃ¼sÃ¼dÃ¼r.

## Dosyalar

- `src/org/mehmet/litetv/MainActivity.java`: bÃ¼tÃ¼n uygulama; dÃ¶rt sÃ¼tun GridView, uygulama/favori/yÃ¶netici modlarÄ±, yerel menÃ¼ler, gÃ¼venli kÃ¶prÃ¼ler.
- `AndroidManifest.xml`, `res/values`, `res/drawable`: manifest, framework tema/TV banner. `res/drawable-nodpi`: cihazdaki gerÃ§ek uygulamalardan bir kez rasterlenmiÅŸ 18 adet 256Ã—256 PNG; `res/raw/bundled_apps.tsv`: TÃ¼rkÃ§e etiket/ikon/boyut kataloÄŸu.
- `build.ps1`: Ã§evrimdÄ±ÅŸÄ± aapt2 â†’ javac â†’ d8 â†’ APK â†’ zipalign â†’ apksigner. KalÄ±cÄ± geliÅŸtirme anahtarÄ± `debug.keystore`; Ã¼retim sertifikasÄ± deÄŸildir.
- **Ä°mzalÄ± APK: `out/LiteTV-signed.apk`**. minSdk 23, targetSdk 34. V1/V2/V3 imza ve zipalign doÄŸrulamasÄ± derleme iÃ§inde zorunlu.
- Final APK **387.350 bayt (378,27 KiB)**; SHA-256 `cb6d49e0d13fba47edff5ca2fc5c0863233b04549ec1de51b67a9b1bc84f5ef3`. Kaynak Lumina APK'sÄ±ndan boyutÃ§a %98,78 kÃ¼Ã§Ã¼k. Bu boyut karÅŸÄ±laÅŸtÄ±rmasÄ± iÅŸlevlerin OEM'e kÃ¶prÃ¼lenmesi nedeniyle baÄŸÄ±msÄ±z iÅŸlev eÅŸdeÄŸerliÄŸi anlamÄ±na gelmez.
- `test-device.ps1`: geÃ§ici HOME, D-pad gezinme, gÃ¼venli ekran testi; koÅŸulsuz `finally` ile LuminaOS HOME restore.
- `measure.ps1`: paket baÅŸÄ±na Ã¼Ã§ iÃ§ iÃ§e process-cold Ã¶lÃ§Ã¼m; ham `am start -W` ve `dumpsys meminfo`, JSON/CSV; koÅŸulsuz restore.
- `re/INVENTORY.md`: kapsamlÄ± RE raporu; tam Ã¶zellik tablosu, vendor property/API'leri, 30 Activity, diÄŸer component'ler, izin ve riskler.
- `re/decompiled/`: JADX kaynak ve resource Ã§Ä±ktÄ±larÄ±. `re/components.csv`, `re/manifest-summary.json`, `re/inspect_manifest.ps1`: makinece doÄŸrulanmÄ±ÅŸ envanter.
- `re/device-config-oem.ini`: orkestratÃ¶rÃ¼n cihaz `/oem/config.ini` kopyasÄ±; menÃ¼ bu cihaz konfigÃ¼rasyonuna gÃ¶re daraltÄ±lmÄ±ÅŸtÄ±r.
- `out/tests/`, `out/metrics/`: Ã§alÄ±ÅŸma zamanÄ± kanÄ±tlarÄ±. YalnÄ±z en son baÅŸarÄ±lÄ± Ã§alÄ±ÅŸtÄ±rma son durum kanÄ±tÄ±dÄ±r; Ã¶nceki smoke denemelerindeki XML'ler de dizinde kalabilir.

## Cihaza Ã¶zgÃ¼ menÃ¼

ADT-3, Android 14 / SDK34, 1920Ã—1080 @320dpi, armeabi-v7a, yaklaÅŸÄ±k 951 MB RAM. USB seri Ã§evrimdÄ±ÅŸÄ±ydÄ±; orkestratÃ¶rÃ¼n verdiÄŸi canlÄ± ADB hedefi `192.168.1.100:5555` kullanÄ±ldÄ±.

GerÃ§ek OEM config: autoKeystone/manualKeystone/screenZoom/projectMode/initAngleCorrect/displayPictureMode/network/wifi/bluetooth aÃ§Ä±k; autoFourCorner/autoFocus/deviceMode/powerMode/account/bootSource/brightAndColor kapalÄ±, layout_select=3. Bu yÃ¼zden **otomatik odak, dÃ¶rt-kÃ¶ÅŸe ve hesap kartlarÄ± yoktur**. Projeksiyon menÃ¼sÃ¼ yalnÄ±z Keystone (oto/manuel), GÃ¶rÃ¼ntÃ¼ Modu, Zoom/En-boy, Projeksiyon YÃ¶nÃ¼, uyarÄ±lÄ± AÃ§Ä± Kalibrasyonu sunar. Config okunup sistem ayarÄ± deÄŸiÅŸtirilmez; bu derleme belirtilen cihaz profiline gÃ¶re tasarlanmÄ±ÅŸtÄ±r, dinamik OEM config yÃ¶neticisi deÄŸildir.

## RE bulgularÄ±

Kaynak APK: 31.631.110 bayt, SHA-256 `afd3fd183a986e9b7a0e3fcd8381f1a7e372a65a0d41e037412c809e978927cc`, LuminaOS 1.0.48. JADX 3.599 sÄ±nÄ±f Ã§Ä±kardÄ±, 25 yÃ¶ntem iÃ§in decompile hatasÄ±yla exit1 verdi; manifest ve Ã§evre Ã§aÄŸrÄ± zincirleri baÄŸÄ±msÄ±z doÄŸrulandÄ±. Eksik yÃ¶ntemler `re/INVENTORY.md` Â§9'dadÄ±r. DolayÄ±sÄ±yla hatalÄ± yÃ¶ntemlerde tam davranÄ±ÅŸ kanÄ±tÄ± iddiasÄ± yoktur.

30 Activity: **9 exported +21 private**, 2 exported service, 6 exported receiver, 0 provider. `android.uid.system` shared UID, REBOOT/WRITE_SECURE_SETTINGS vb. izinler yalnÄ±z platform imzalÄ± Ã¼retici uygulamasÄ±na aittir. `com.softwinner.awmanager`/AwTvDisplayManager, PQControl, PrjScreen, AudioManagerEx, property/hidden API katmanlarÄ± bulundu; normal UID iÃ§in kararlÄ± ve izinli bir kontrol API'si kanÄ±tlanamadÄ±. `zoom_value` tek baÅŸÄ±na doÄŸru zoom uygulamasÄ± deÄŸildir; koordinat, property ve geometri zinciriyle birlikte yÃ¼rÃ¼tÃ¼lÃ¼r. Bu nedenle doÄŸrudan Global/property/Binder yazÄ±mÄ± yapÄ±lmadÄ±.

### DÄ±ÅŸa aÃ§Ä±k Activity'lerin eksiksiz listesi

Ortak prefix `com.htc.luminaos/`; sÄ±nÄ±flar tablodaki tam sÄ±nÄ±f adÄ±yla kullanÄ±lÄ±r.

| Ad | Ne yapÄ±yor | Bizim menÃ¼de karÅŸÄ±lÄ±ÄŸÄ± |
|---|---|---|
| `com.htc.luminaos.activity.MainActivity` | Orijinal HOME, iÃ§erik/giriÅŸ/destek kÄ±sayollarÄ± | DiÄŸer â†’ LuminaOS; destek/OEM duvar kaÄŸÄ±dÄ±/eksik harici uygulama fallback'i |
| `com.htc.luminaos.activity.settings.MainSettingActivity` | AÄŸ, BT, Projeksiyon, Uygulama, Dil, Tarih, DiÄŸer, HakkÄ±nda | Ayarlar â†’ Cihaz ayarlarÄ±; private ekranlar iÃ§in ana gÃ¼venli kÃ¶prÃ¼ |
| `com.htc.luminaos.activity.settings.WifiActivity` | Wi-Fi aÃ§/kapat, tarama, baÄŸlantÄ±, IP | Ayarlar â†’ Wi-Fi/AÄŸ â†’ OEM AÄŸ menÃ¼sÃ¼ iÃ§inden Wi-Fi; aÄŸ deÄŸiÅŸimi test edilmedi |
| `com.htc.luminaos.activity.settings.BluetoothActivity` | BT enable/discovery/eÅŸleÅŸtirme/profiller | Ayarlar â†’ Bluetooth â†’ Ã¶nce uyarÄ±/onay; otomatik smoke test yalnÄ±z onaya kadar |
| `com.htc.luminaos.activity.settings.NetworkActivity` | Wi-Fi/Ethernet/hotspot Ã¼st menÃ¼sÃ¼ | Ayarlar â†’ Wi-Fi/AÄŸ; gÃ¼venli Ã¼st menÃ¼ |
| `com.htc.luminaos.activity.settings.WiredActivity` | Ethernet DHCP/statik IP bilgisi/ayarlarÄ±; bu cihazda arayÃ¼z/config yoksa onResume NPE ile OEM sÃ¼reci Ã§Ã¶ker | DoÄŸrudan kullanÄ±lmaz. Ayarlar â†’ Ethernet, uyarÄ± ile gÃ¼venli NetworkActivity Ã¼st menÃ¼sÃ¼nÃ¼ aÃ§ar |
| `com.htc.luminaos.activity.settings.DisplaySettingsActivity` | Saydam vendor PQ/EQ paneli, overlay/property/audio yan etkileri | DoÄŸrudan kullanÄ±lmaz. Projeksiyon â†’ GÃ¶rÃ¼ntÃ¼ Modu â†’ OEM ana ayarlarÄ±; PQ paneli keystone/zoom eÅŸdeÄŸeri deÄŸildir |
| `com.htc.luminaos.activity.settings.PowerActivity` | Uyku/kapat/reboot/timer/BT speaker; aÃ§Ä±lÄ±ÅŸta 15 sn kapatma sayacÄ± | GÃ¼Ã§ â†’ yerel uyarÄ±lÄ± sayfa â†’ ikinci onay; varsayÄ±lan odak VAZGEÃ‡; otomatik test Ã¼retici ekranÄ±na geÃ§mez |
| `com.htc.luminaos.activity.settings.BluetoothIncomingFileConfirmActivity` | GerÃ§ek Bluetooth dosya aktarÄ±mÄ± kabul akÄ±ÅŸÄ±; Parcelable cihaz baÄŸlamÄ± gerekir | Yapay kÄ±sayol yok; gerÃ§ek sistem transfer akÄ±ÅŸÄ± korunur, sahte istek Ã¼retilmez |

### Ä°ÅŸlev â†’ LuminaOS â†’ Hafif TV eÅŸlemesi

`Yerel` bizim UI/veri alanÄ±mÄ±z; `OEM menÃ¼` bir ek kullanÄ±cÄ± seÃ§imi gerektirir. Private ekranlarÄ±n hiÃ§biri exported yapÄ±lmadÄ±, uygulama verileri okunmadÄ±/deÄŸiÅŸtirilmedi.

| Ä°ÅŸlev | LuminaOS'ta nasÄ±l | Bizim uygulamada nasÄ±l / sÄ±nÄ±r |
|---|---|---|
| HOME, saat/tarih | Ana Activity + durum/marka gÃ¶rÃ¼nÃ¼mÃ¼ | Yerel bÃ¼yÃ¼k TextClock, tarih, koyu bÃ¼yÃ¼k kartlar; sistem dili deÄŸiÅŸtirilmez |
| Uygulama Ã§ekmecesi | Private AppsActivity, Launcher/Leanback sorgusu | Yerel GridView, iki kategori, TV Ã¶ncelik, TÃ¼rkÃ§e sÄ±ralama, paket tekilleÅŸtirme |
| Uygulama aÃ§ma | Paket/component | Yerel OK veya uzun OK â†’ AÃ§; sistem uygulamasÄ± da filtre ÅŸartÄ±yla gÃ¶rÃ¼nÃ¼r |
| Favori ekle/Ã§Ä±kar | Private AppFavoritesActivity + SQLite | Yerel Favori dÃ¼zenle / uzun OK, ayrÄ± SharedPreferences; Lumina favorileri aktarÄ±lmaz |
| Uygulama bilgisi/kaldÄ±r/cache | Private AppsManagerActivity | Yerel seÃ§im â†’ sistem Uygulama Bilgisi; kaldÄ±r/veri/cache iÅŸlemi kullanÄ±cÄ±ya bÄ±rakÄ±lÄ±r |
| OEM yanlÄ±ÅŸ cache etiketi | clearApplicationUserData Ã§aÄŸrÄ±sÄ±, veri silici | KopyalanmadÄ±. Sessiz veri/cache silme veya sistem uygulamasÄ± kaldÄ±rma yok |
| Dahili/USB duvar kaÄŸÄ±dÄ± | Private WallPaperActivity ve Ã¶zel dosyalar | Yerel 3 renk + ACTION_OPEN_DOCUMENT resim/USB seÃ§ici; RGB565 â‰¤960Ã—540 Ã¶rneklenir; kalÄ±cÄ± URI izni. OEM seÃ§iciye ayrÄ±ca orijinal HOME kÃ¶prÃ¼sÃ¼ |
| HDMI/giriÅŸ | awlivetv MainActivity + input_source | Yerel onaylÄ± seÃ§im: HDMI1/HDMI2/CVBS1/VGA; runtime component eriÅŸim kontrolÃ¼. Mevcut portlar OEM donanÄ±mÄ±na baÄŸlÄ±, listedeki her fiziksel port doÄŸrulanmÄ±ÅŸ deÄŸildir |
| Video/stream/store uygulamalarÄ± | Netflix/YouTube/Disney/store/GTV paketleri | Kurulu ve gÃ¶rÃ¼nÃ¼r uygulamalar gridde; maÄŸaza iÃ§in ayrÄ±ca kÄ±sayol. Uygulama indirme/kurma yok |
| Ekran yansÄ±tma | EShare `com.ecloud.eshare.server`, OEM override mÃ¼mkÃ¼n | DiÄŸer â†’ Ekran yansÄ±tma; paket yoksa Lumina HOME. AÄŸ/dÃ¶kÃ¼m baÅŸlatma otomatik test edilmez |
| Dosya/USB | `com.hisilicon.explorer` | DiÄŸer â†’ Dosyalar/USB veya grid; paket yoksa Lumina HOME |
| Bildirimler | `com.htc.notification` | DiÄŸer â†’ Ã¼retici Bildirimler; Ã¶zel bildirim dinleyicisi yok |
| Bellek temizleme | `com.htc.clearmemory` | DiÄŸer â†’ Ã¶nce onay; aracÄ± kullanÄ±cÄ± aÃ§ar. Otomatik kill/temizleme yok |
| Destek/kÄ±lavuz/QR | Private SupportActivity, OEM config iÃ§erikleri | DiÄŸer/Ayarlar â†’ Lumina HOME, oradaki destek simgesi; bu bÃ¶lÃ¼me MainSetting'in eÅŸdeÄŸer olduÄŸu iddia edilmez |
| Wi-Fi/baÄŸlantÄ±/statik IP | WifiActivity + private WifiIpSetActivity | OEM Network â†’ Wi-Fi â†’ IP; yeni launcher Wi-Fi'yi sessiz aÃ§Ä±p kapatmaz |
| Ethernet DHCP/statik | Exported WiredActivity; mevcut cihazda null IP config nedeniyle Ã§Ã¶kÃ¼yor | Ayarlar â†’ Ethernet â†’ uyarÄ±lÄ± OEM AÄŸ Ã¼st menÃ¼sÃ¼. Fiziksel arayÃ¼z yok; bu OEM bug'Ä± yeni UI'dan tetiklenmez |
| Hotspot | Private HotspotActivity | OEM Cihaz ayarlarÄ± â†’ AÄŸ â†’ Hotspot; yeni uygulama tethering baÅŸlatmaz |
| Bluetooth ve transfer | Exported BT UI ve gerÃ§ek transfer intent'i | BT kartÄ±nda Ã¶nce yan etki uyarÄ±sÄ±; sonra OEM UI. GerÃ§ek transfer sistem akÄ±ÅŸÄ±nda kalÄ±r |
| Bluetooth speaker/sink | Private BluetoothSpeakerActivity; Wi-Fi/BT/LED yan etkili | Ãœst OEM ayarlarÄ± Ã¼zerinden kullanÄ±cÄ± yolu; private ekran doÄŸrudan aÃ§Ä±lmaz; otomatik testte yok |
| Keystone oto/manuel | Private Project/Correction, vendor geometri | Projeksiyon â†’ Keystone â†’ OEM Cihaz ayarlarÄ± â†’ Projeksiyon; mevcut kalibrasyon korunur |
| Zoom/en-boy | Project, Global + property + geometri | Projeksiyon â†’ Zoom/En-boy â†’ aynÄ± OEM Projeksiyon yolu |
| Projeksiyon yÃ¶nÃ¼ | Project + panel/keystone dÃ¶nÃ¼ÅŸÃ¼mÃ¼ | Projeksiyon â†’ Projeksiyon YÃ¶nÃ¼ â†’ OEM Projeksiyon yolu |
| AÃ§Ä± kalibrasyonu | InitAngleActivity aÃ§Ä±lÄ±ÅŸÄ±nda reset | Yerel uyarÄ±/VAZGEÃ‡ â†’ yalnÄ±z OEM Project Ã¼st menÃ¼sÃ¼; InitAngle veya INITANGLE broadcast Ã§aÄŸrÄ±lmaz |
| GÃ¶rÃ¼ntÃ¼/ses/EQ/RGB/ARC | Private Picture/Audio; vendor overlay ve manager'lar; config'e baÄŸlÄ± | GÃ¶rÃ¼ntÃ¼/Ses kartlarÄ± OEM Ã¼st menÃ¼sÃ¼; gizli/devre dÄ±ÅŸÄ± satÄ±rlarÄ±n varlÄ±ÄŸÄ± vaat edilmez; sahte slider yok |
| Otomatik odak/dÃ¶rt-kÃ¶ÅŸe/cihaz modu | Kod var; bu cihaz OEM config'inde kapalÄ± | Kart yok, donanÄ±m yeteneÄŸi iddiasÄ± yok. Genel orijinal OEM UI korunur |
| Dil/klavye | Private LanguageAndKeyboardActivity | OEM Cihaz ayarlarÄ± â†’ Dil/Klavye. YalnÄ±z bizim app locale'i TÃ¼rkÃ§e, sistem locale/IME deÄŸiÅŸtirilmez |
| Tarih/saat/saat dilimi | Private DateTimeActivity | OEM Cihaz ayarlarÄ± â†’ Tarih/Saat; sessiz SET_TIME yok |
| Hesap/parola | Private Account/PasswordVerify1/2; hesap bu cihazda kapalÄ± | Hesap kartÄ± yok. Ãœretici parolasÄ± geÃ§erliyse OEM doÄŸrulamasÄ± atlanmaz; Lumina parola verisine eriÅŸilmez |
| Ekran koruyucu/tuÅŸ sesi/eriÅŸilebilirlik | Private OtherSettings | Ayarlar â†’ DiÄŸer veya ZamanlayÄ±cÄ± â†’ OEM DiÄŸer; ayar yazÄ±lmaz |
| BaÅŸlangÄ±Ã§ kaynaÄŸÄ±/power mode | Private OtherSettings, config kapalÄ± | Ã–zel kart yok; OEM menÃ¼sÃ¼ndeki mevcut gÃ¶rÃ¼nÃ¼rlÃ¼k aynen korunur |
| Uyku/kapat/yeniden baÅŸlat | AyrÄ±calÄ±klÄ± PowerActivity | GÃ¼Ã§ ekranÄ± ve ikinci aÃ§Ä±k onay; aÃ§Ä±lÄ±ÅŸÄ±n 15 sn sayacÄ± konusunda uyarÄ±. GerÃ§ek gÃ¼Ã§ eylemi test edilmez |
| Kapanma zamanlayÄ±cÄ±sÄ± | Private OtherSettings + TimeOffService | ZamanlayÄ±cÄ± â†’ OEM DiÄŸer; exported servise sahte sÃ¼re gÃ¶nderilmez |
| HakkÄ±nda/cihaz bilgisi | Private About | Ayarlar â†’ HakkÄ±nda â†’ OEM ana menÃ¼. RAM Ã¶lÃ§Ã¼mÃ¼ pazarlama/config Ã¶lÃ§eÄŸi yerine dumpsys ile yapÄ±lÄ±r |
| OTA/USB gÃ¼ncelleme/fabrika reset | About/Other + vendor update ve FACTORY_RESET | OEM HakkÄ±nda/DiÄŸer iÃ§inde kalÄ±r. Yeni launcher'da tek tÄ±k tehlikeli iÅŸlem yok; test edilmez |
| Developer/fabrika/test/gizli tuÅŸlar | Private DeveloperMode/About | Orijinal OEM UI'da kalÄ±r; ADB aÃ§ma/reboot testi/gÃ¼venlik ayarÄ± deÄŸiÅŸimi yok |
| OEM durum simgeleri | Wi-Fi/BT/USB/batarya/notification vendor state | Yeni HOME'da bire bir statusbar replikasÄ± yok; ilgili OEM ekranlarÄ±/notification kÄ±sayolu korunur. Bu gÃ¶rsel eksik aÃ§Ä±kÃ§a kabul edilmelidir |

21 private Activity kapsamÄ±: Apps/AppFavorites yerel karÅŸÄ±lÄ±k; AppsManager sistem ayrÄ±ntÄ±larÄ±; Wallpaper yerel resim ve OEM HOME; Support OEM HOME; PasswordVerify1/2 Ã¼retici iÃ§inde; Project/Correction/Picture/Audio/InitAngle OEM Projeksiyon; WifiIpSet/Hotspot OEM AÄŸ; BluetoothSpeaker OEM gÃ¼Ã§/Ã¼st menÃ¼; Language/Date/About/Other/Developer OEM ayarlar; Account cihazda kapalÄ±. **EriÅŸim korunmasÄ±, doÄŸrudan tek tÄ±k aynÄ± alt ekrana ulaÅŸma veya yeni UID'den setter uygulama demek deÄŸildir.**

### Her kartÄ±n final APK'daki gerÃ§ek hedefi

`S` = `com.htc.luminaos/.activity.settings.MainSettingActivity`, `N` = `com.htc.luminaos/.activity.settings.NetworkActivity`, `H` = `com.htc.luminaos/.activity.MainActivity`. Private alt sÄ±nÄ±f Ã§aÄŸrÄ±lmaz; runtime exported/enabled/permission kontrolÃ¼nden sonra bu gerÃ§ek hedefe dÃ¼ÅŸÃ¼lÃ¼r. S de kullanÄ±lamazsa ilgili `android.settings.*`, son olarak `com.android.tv.settings/.MainSettings` denenir.

| Kart / alt menÃ¼ | Bu cihazda gerÃ§ek hedef |
|---|---|
| Ana: GiriÅŸ/HDMI | Yerel 4 kaynak seÃ§imi; yalnÄ±z seÃ§imde `com.softwinner.awlivetv/.MainActivity`, `input_source=HDMI1/HDMI2/CVBS1/VGA`; eriÅŸilemezse H |
| Ana: Ayarlar / Projeksiyon / GÃ¼Ã§ / DiÄŸer | Yerel ilgili sayfa/dialog; ilk OK'de donanÄ±m eylemi yok |
| Ana: Uygulamalar; TÃ¼m uygulamalar/Favoriler/Favori dÃ¼zenle/Uygulama yÃ¶neticisi | AynÄ± Activity iÃ§inde GridView modlarÄ± |
| Ayarlar: Cihaz ayarlarÄ± | S |
| Ayarlar: Wi-Fi/AÄŸ; Ethernet | N; Ethernet hatasÄ± hakkÄ±nda toast |
| Ayarlar: Bluetooth | Yerel VAZGEÃ‡ odaklÄ± onay; kabulde `com.htc.luminaos/.activity.settings.BluetoothActivity` |
| Ayarlar: EriÅŸim noktasÄ± / Ses modu / GÃ¶rÃ¼ntÃ¼ modu / Dil-Klavye | S; kullanÄ±cÄ± ilgili bÃ¶lÃ¼mÃ¼ seÃ§er |
| Ayarlar: Tarih-Saat / ZamanlayÄ±cÄ± / DiÄŸer ayarlar / HakkÄ±nda | S; kullanÄ±cÄ± ilgili bÃ¶lÃ¼mÃ¼ seÃ§er |
| Ayarlar: Duvar kaÄŸÄ±dÄ± | Yerel tema/resim dialog'u |
| Ayarlar: Destek | H; kullanÄ±cÄ± Ã¼retici destek simgesini seÃ§er |
| Ayarlar: Uygulamalar | Yerel yÃ¶netici grid'i â†’ uzun OK/app seÃ§enekleri |
| Ayarlar: Bluetooth hoparlÃ¶r | S; private sink ekranÄ± Ã§aÄŸrÄ±lmaz |
| Projeksiyon: Keystone / GÃ¶rÃ¼ntÃ¼ Modu / Zoom-Enboy / Projeksiyon YÃ¶nÃ¼ | S; kullanÄ±cÄ± Projeksiyon bÃ¶lÃ¼mÃ¼nÃ¼ seÃ§er |
| Projeksiyon: AÃ§Ä± Kalibrasyonu | Yerel VAZGEÃ‡ odaklÄ± uyarÄ±, kabulde S; InitAngle asla doÄŸrudan Ã§aÄŸrÄ±lmaz |
| GÃ¼Ã§: GÃ¼Ã§ seÃ§enekleri | Yerel ikinci onay, kabulde `com.htc.luminaos/.activity.settings.PowerActivity` (15 sn risk!) |
| GÃ¼Ã§: DiÄŸer-ZamanlayÄ±cÄ±; Ana ekran | S; yerel HOME |
| DiÄŸer: Duvar kaÄŸÄ±dÄ±-Tema / Destek / ZamanlayÄ±cÄ± | Yerel dialog / H / S |
| DiÄŸer: Ekran yansÄ±tma / Dosyalar-USB / Uygulama maÄŸazasÄ± | PM Leanback/Launcher intent'i: `com.ecloud.eshare.server` / `com.hisilicon.explorer` / `com.htc.storeos`; bulunamazsa H |
| DiÄŸer: LuminaOS ana ekran / Bildirimler | H / PM paket intent'i `com.htc.notification`, yoksa H |
| DiÄŸer: Bellek temizleme | Yerel onay, kabulde PM paket intent'i `com.htc.clearmemory`, yoksa H |
| Tema: Gece / Okyanus / Orman | YalnÄ±z uygulamanÄ±n SharedPreferences arka planÄ± |
| Tema: Dosya-USB'den resim / Ãœretici duvar kaÄŸÄ±tlarÄ± | `ACTION_OPEN_DOCUMENT image/*` / H |
| Uygulama OK / uzun OK | Kaydedilen gerÃ§ek `ResolveInfo` component'i / yerel favori-bilgi-aÃ§ dialog'u |
| Uygulama bilgisi-kaldÄ±r-Ã¶nbellek | `android.settings.APPLICATION_DETAILS_SETTINGS`, `package:<seÃ§ilen paket>`; otomatik iÅŸlem yok |

## Test ve gÃ¼venlik kaydÄ±

GÃ¼venli test kapsamÄ±: geÃ§ici HOME atanmasÄ± ve HOME tuÅŸu; 6 ana kart, 16 ayar kartÄ±, 5 projeksiyon kartÄ±; yalnÄ±z kendi UI + OEM MainSetting/Network/Home fallback'leri; favori/liste/dÃ¼zenleme/yÃ¶netici/uzun OK; gÃ¼Ã§ ikinci onayÄ±nda VAZGEÃ‡ varsayÄ±lan odaÄŸÄ±. Her vaka sonrasÄ± `dumpsys window` ile Hafif TV'ye dÃ¶nÃ¼ÅŸ denetlenir. HiÃ§bir ayar toggle'Ä±, kalibrasyon, reset, uninstall/data clear, gÃ¼Ã§ iÅŸlemi seÃ§ilmez.

**Final imzalÄ± APK ile tekrar test: 38/38 gÃ¼venli vaka geÃ§ti.** HOME varsayÄ±lanlÄ±ÄŸÄ± + 6 ana kart + gÃ¼Ã§ Ã§ift kapÄ±sÄ± + 16 ayar + 5 projeksiyon + 5 gÃ¼venli DiÄŸer seÃ§imi + 4 uygulama/favori/yÃ¶netici/uzun OK vakasÄ±. `out/tests/results.json` her vakanÄ±n aÃ§Ä±lan top Activity'sini, aÃ§Ä±lan focus'u ve Hafif TV'ye dÃ¶nen focus'u iÃ§erir. Bu sayÄ± bÃ¼tÃ¼n donanÄ±m iÅŸlemlerinin uÃ§tan uca geÃ§tiÄŸi anlamÄ±na gelmez; aÅŸaÄŸÄ±daki kapsam dÄ±ÅŸÄ± iÅŸlemler aynen kapsam dÄ±ÅŸÄ±dÄ±r.

Bluetooth enable/discovery, DisplaySettings PQ overlay, InitAngle reset, Bluetooth sink, HDMI gerÃ§ek kaynak seÃ§imi, maÄŸaza/dÃ¶kÃ¼m/gÃ¼ncelleme/temizleme test kapsamÄ± dÄ±ÅŸÄ±nda tutuldu; sadece yerel uyarÄ± veya OEM gÃ¼venli Ã¼st menÃ¼ test edildi. KullanÄ±cÄ± donanÄ±m eylemlerini daha sonra bilinÃ§li doÄŸrulamalÄ±dÄ±r. Yerel fotoÄŸraf seÃ§ici/URI kalÄ±cÄ±lÄ±ÄŸÄ± kullanÄ±cÄ± resmi seÃ§meden uÃ§tan uca doÄŸrulanmÄ±ÅŸ sayÄ±lmaz.

Ã–nceki baÅŸarÄ±sÄ±z denemeler saklanmamalÄ± diye dosya silinmedi: ilk PowerShell sÃ¼rÃ¼mÃ¼nde baÅŸarÄ±lÄ± `adb pull` stderr Ã§Ä±ktÄ±sÄ± hata sanÄ±ldÄ± (dÃ¼zeltildi). Bir erken D-pad denemesinde HOME sonrasÄ± eski HDMI dialog'u kapanmamÄ±ÅŸtÄ±; yanlÄ±ÅŸlÄ±kla awlivetv aÃ§Ä±ldÄ±. Bu olay orkestratÃ¶re hemen bildirildi. `show/onNewIntent` artÄ±k bÃ¼tÃ¼n aktif dialog'larÄ± kapatÄ±r. **HiÃ§ giriÅŸ deÄŸiÅŸmedi diye bir iddia yoktur**; Ã¶nceki fiziksel giriÅŸin ne olduÄŸu kanÄ±tlanamadÄ±ÄŸÄ±ndan tahmini geri deÄŸiÅŸim yapÄ±lmadÄ±. Daha sonraki gÃ¼Ã§ assertion'Ä± VAZGEÃ‡'in bÃ¼yÃ¼k harfli gÃ¶rÃ¼nÃ¼mÃ¼nden false-negative verdi; kÄ±yas case-insensitive yapÄ±ldÄ±. Ãœretici PowerActivity hiÃ§bir testte aÃ§Ä±lmadÄ±.

Test/Ã¶lÃ§Ã¼m sonrasÄ±nda hem HOME resolver hem gÃ¶rÃ¼nÃ¼r focus'un `com.htc.luminaos/.activity.MainActivity` olmasÄ± gereklidir. En son kanÄ±t `out/tests/final-window.txt`, `restored-resolver.txt` ve `out/metrics/` karÅŸÄ±lÄ±klarÄ±ndadÄ±r. Orijinal launcher kaldÄ±rÄ±lmadÄ±/devre dÄ±ÅŸÄ± bÄ±rakÄ±lmadÄ±; kalÄ±cÄ± cihaz ayarÄ± deÄŸiÅŸtirme komutu kullanÄ±lmadÄ±. GeÃ§ici varsayÄ±lan HOME iÅŸlemi test sonunda geri alÄ±nÄ±r.

Son baÄŸÄ±msÄ±z kapanÄ±ÅŸ doÄŸrulamasÄ±: `out/final-home-resolver.txt` ve `out/final-home-window.txt` LuminaOS HOME'u gÃ¶sterir; `out/tests/restored-home.png` gÃ¶rsel olarak da orijinal ana ekranÄ± doÄŸrular. Kurulu `pm path org.mehmet.litetv` APK'sÄ± geri Ã§ekilip `out/installed-final.apk` SHA-256'sÄ± imzalÄ± Ã§Ä±ktÄ±yla bire bir eÅŸlendi (`out/installed-proof.txt`); doÄŸru son APK'nÄ±n cihazda olduÄŸu bÃ¶ylece ayrÄ±ca kanÄ±tlandÄ±.

**CanlÄ± OEM bug'Ä±:** Ä°ki gÃ¼venli aÃ§/geri denemesinde `WiredActivity.onResume â†’ updateEthernetStatus` null `mIpConfiguration.getIpAssignment()` NPE verdi ve LuminaOS sÃ¼reci Ã§Ã¶ktÃ¼. `dumpsys ethernet`: IP Configurations/Tracking interfaces boÅŸ; yani fiziksel Ethernet arayÃ¼zÃ¼ yok. `out/tests/crash-evidence.txt` kanÄ±tÄ± korunur. Bu sÄ±rada DHCP/static seÃ§ilmedi, aÄŸ deÄŸeri yazÄ±lmadÄ±; finally Lumina HOME'u gÃ¶rÃ¼nÃ¼r geri getirdi. Final APK Ethernet'i NetworkActivity'ye yÃ¶nlendirir, bu buggy alt ekranÄ± doÄŸrudan aÃ§maz. OEM'in kendi menÃ¼sÃ¼nden aynÄ± alt ekranÄ± kullanÄ±cÄ± seÃ§erse kendi hatasÄ± devam edebilir; Ã¼retici APK'sÄ± deÄŸiÅŸtirilmemiÅŸtir.

## Ã–lÃ§Ã¼mler

Final APK ile 3Ã— process-cold, menÃ¼ aÃ§Ä±k ve 3 sn yerleÅŸmiÅŸ durum; ham deÄŸerler `out/metrics/results.json`, `results.csv`, `summary.json` ve paket/Ã¶rnek bazlÄ± `*-start.txt`, `*-meminfo.txt` dosyalarÄ±ndadÄ±r. Birimler dumpsys KB (pratikte KiB), sÃ¼re ms. Medyan:

| Uygulama / Ã¶lÃ§Ã¼m turu | PSS KB | RSS KB | Cold ms |
|---|---:|---:|---:|
| LuminaOS ilk referans (istenen 60.072) | 60.072 | 134.100 | 1.212 |
| LuminaOS final ile aynÄ± tur | 60.187 | 134.168 | 1.207 |
| **Hafif TV final, gerÃ§ek gÃ¶mÃ¼lÃ¼ ikonlar** | **30.791** | **98.656** | **720** |

Ä°lk Lumina referansÄ±na gÃ¶re **PSS %48,74 az, RSS %26,43 az, cold sÃ¼re %40,59 kÄ±sa**. Final ile eÅŸzamanlÄ± Lumina turuna gÃ¶re sÄ±rasÄ±yla yaklaÅŸÄ±k %48,84 / %26,47 / %40,35. Final Ã¶rnekleri: Lumina PSS `[60499,60073,60187]`, RSS `[134444,134060,134168]`, sÃ¼re `[1272,1207,1192]`; Hafif TV PSS `[30766,30809,30791]`, RSS `[98656,98628,98672]`, sÃ¼re `[720,675,724]`.

Ã–rnek 2 App Summary: Hafif TV Java heap PSS 9.096 KB, native 8.164 KB, `.apk mmap` 1.348 KB; ilk gerÃ§ek-dinamik-ikon denemesi `.apk mmap` 32.285 KB idi. Ã–nemli fark framework'Ã¼n her paketin ikonunu **ve etiketini** yÃ¼klerken kaynak APK resource tablolarÄ±nÄ± launcher sÃ¼recine map etmesidir. Bu yÃ¼zden kaynaÄŸÄ± 2 MiB'tan bÃ¼yÃ¼k 18 uygulamanÄ±n gerÃ§ek ikonlarÄ± **bir kez cihazdan** 256Ã—256 rasterlenip gÃ¶mÃ¼ldÃ¼, etiketleri de snapshot alÄ±ndÄ±; monogram veya sahte simge kullanÄ±lmadÄ±. Grid ikonunun gerÃ§ek basÄ±lan boyutu **40dp Ã— density2 = 80 ekran pikseli** (karo 400+ piksel geniÅŸliÄŸinde olsa da ikon o boyutta deÄŸildir); 256 px kaynak 3,2Ã— pay saÄŸlar. Son `out/tests/home.png` gÃ¶rselinde kenar/yazÄ± bulanÄ±klÄ±ÄŸÄ± gÃ¶rÃ¼lmedi. Yerel UI ve Ã¶zellik yollarÄ± deÄŸiÅŸtirilmedi.

**Snapshot sÄ±nÄ±rÄ±:** katalog bu cihazÄ±n tr-TR uygulama listesi iÃ§indir. APK boyutu deÄŸiÅŸirse snapshot kullanÄ±lmaz, normal label/icon yolu denenir; ikon LRU cache'i sÃ¼reÃ§ yaÅŸamÄ± boyunca kalabilir. Boyut aynÄ± kalÄ±rken uygulama etiketi/ikonu deÄŸiÅŸirse eski snapshot sÃ¼rebilir. Tam sÃ¼rÃ¼m takibi yoktur; kalÄ±cÄ± Ã§Ã¶zÃ¼m yeni ikon kataloÄŸu Ã¼retmek veya `ACTION_PACKAGE_REPLACED` / package version deÄŸiÅŸiminde ilgili snapshot+LRU girdisini geÃ§ersiz kÄ±lmaktÄ±r. Bu refresh mekanizmasÄ± final APK'da uygulanmÄ±ÅŸ gibi sunulmaz. Yeni/2 MiB altÄ± uygulamalar normal gerÃ§ek ikon/etiket yolunu kullanÄ±r; gelecekte bÃ¼yÃ¼k yeni uygulama eklenmesi PSS'yi yÃ¼kseltebilir. Ä°konlar ilgili uygulamalarÄ±n markalÄ± varlÄ±klarÄ±dÄ±r, baÄŸÄ±msÄ±z mÃ¼lkiyet iddiasÄ± yoktur.

### Map kanÄ±tÄ± ve paylaÅŸÄ±lmÄ±ÅŸ sayfa uyarÄ±sÄ±

`out/maps-original-icons.txt` â†’ `out/maps-final.txt`: APK yolu 48â†’15; launcher'Ä±n kurulum yolu hariÃ§ **33 APK/overlay yolu artÄ±k map edilmez**. Kaybolan ilk kaynak segmentleri toplamÄ± 60.788 KiB **sanal adres aralÄ±ÄŸÄ±dÄ±r, RAM/PSS tasarrufu deÄŸildir**. Tam liste/range/path: `out/maps-removed.csv`. Ã–rnek kalkmÄ±ÅŸ aralÄ±klar:

| Kaynak | Ã–nceki segment | Sanal KiB |
|---|---|---:|
| OfficeSuite | `8530c000â€“8667c000` | 19.904 |
| TvSettings | `87ee9000â€“88ca4000` | 14.060 |
| BrowseHere | `87ad8000â€“87ee9000` | 4.164 |
| SmartTube | `87755000â€“87ad8000` | 3.596 |
| LuminaOS | `86adc000â€“86c76000` | 1.640 |

Lumina'nÄ±n 31,6 MB APK'sÄ± bÃ¼tÃ¼nÃ¼yle map edilmiÅŸ deÄŸildi; yalnÄ±z gÃ¶rÃ¼len kaynak segmenti 1,6 MiB idi. TvSettings 13,73 MiB sanal segmentinin Ã¶nceki smaps resident deÄŸeri 4.648 KB idi; sanal boyut resident deÄŸildir. Son map'te kalan uygulama kaynaklarÄ± ClearMemory (PSS/RSS 464/464 KB), com.tvlauncher (344/344), AwSource (284/284), kendi APK (26/32); ayrÄ±ca framework-res (235/1344) ve kÃ¼Ã§Ã¼k framework overlay/idmap'ler. Diagnostic kendi `/proc/self/maps` ve `/proc/self/smaps` okur; ADB'den diÄŸer sÃ¼reÃ§ `/proc/<pid>/maps` SELinux nedeniyle okunamadÄ±. YalnÄ±z `am start ... --ez memory_diagnostics true` ile aÃ§Ä±lÄ±r; normal HOME'da thread/proc I/O yoktur. Diagnostic/export turu benchmark'a dahil deÄŸildir; export kodu final APK'dan Ã§Ä±karÄ±ldÄ±.

`out/metrics-original-icons`, `out/metrics-monogram-experiment`, `out/metrics-lumina-only` Ã¶nceki deneylerdir, final sonuÃ§ deÄŸildir. Ä°lk dinamik gerÃ§ek ikon sÃ¼rÃ¼mÃ¼ medyan PSS 62.299 KB ile Lumina'dan daha yÃ¼ksekti; bu baÅŸarÄ±sÄ±z optimizasyon gizlenmedi. Monogram denemesi UX nedeniyle reddedildi. Son deÄŸerler yalnÄ±z final gÃ¶mÃ¼lÃ¼ **gerÃ§ek** ikonlu APK'ya aittir.

YÃ¶ntem: 3'er Ã¶rnek, Lumina/Hafif TV iÃ§ iÃ§e; Ã¶nce diÄŸer uygulama HOME yapÄ±lÄ±r ve HOME'a gidilir, hedef `am force-stop`, sonra `am start -W -n ...`; **LaunchState:COLD zorunlu**. 3 sn yerleÅŸme sonrasÄ± `dumpsys meminfo <paket>` TOTAL PSS/RSS (KB). Root/drop-caches yok; bunlar process-cold, reboot/disk-cold deÄŸildir. PSS paylaÅŸÄ±lan sayfalarÄ±n bÃ¶lÃ¼ÅŸÃ¼mÃ¼ne ve RAM basÄ±ncÄ±na baÄŸlÄ±dÄ±r. YalnÄ±z ilgili sÃ¼reÃ§ karÅŸÄ±laÅŸtÄ±rÄ±lÄ±r, bÃ¼tÃ¼n sistem/OEM servis tÃ¼ketimi ortadan kalkmÄ±ÅŸ sayÄ±lmaz. VarsayÄ±lan dÃ¼z renk, aynÄ± cihaz/uygulama listesi. Ä°lk kurulumdaki tek seferlik 2238 ms Ã¶lÃ§Ã¼mÃ¼ bu benchmark'a dahil edilmez.

## Derleme, kurulum ve geri dÃ¶nÃ¼ÅŸ

PowerShell, proje kÃ¶kÃ¼nden:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 install -r .\out\LiteTV-signed.apk

# Ä°steÄŸe baÄŸlÄ± geÃ§ici HOME; cihaz ayarlarÄ±nÄ± deÄŸiÅŸtiren hiÃ§bir baÅŸka komut yok:
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell cmd package set-home-activity org.mehmet.litetv/.MainActivity
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell input keyevent HOME

# Geri dÃ¶nÃ¼ÅŸ â€” Ã¶nce yerel overlay/dialog'u kapat, sadece bizim sÃ¼reci durdur:
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell input keyevent BACK
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell am force-stop org.mehmet.litetv
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell cmd package set-home-activity com.htc.luminaos/.activity.MainActivity
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell input keyevent HOME
```

JDK17: `C:\Program Files\Eclipse Adoptium\jdk-17`; SDK `C:\Android\sdk`, android-34/build-tools34. Javac'da Android jar **classpath** kullanÄ±lÄ±r; bootclasspath yapmak JDK17/LambdaMetafactory derlemesini bozduÄŸu iÃ§in seÃ§ilmedi. D8 android.jar ile public Android API hedefi saÄŸlar. Bilinen uyarÄ±lar source8 bootstrap classpath ve deprecated framework API'leridir; derleme/imza doÄŸrulamasÄ± baÅŸarÄ±sÄ±zlÄ±ÄŸÄ± maskelenmez. GÃ¼ncel kaynak deÄŸiÅŸtiÄŸinde APK yeniden derlenmelidir; yeniden Ã¼retim harici indirmeye ihtiyaÃ§ duymaz.

## 2026-11-05 - Ayarlar ekrani sadeleÅŸtirildi (v2)
- settings(): 16 olu buton -> 6 gercek kisayol (Cihaz ayarlari, TV Ayarlari, Ag/Wi-Fi, Bluetooth, Uygulamalar, Duvar kagidi).
- Kaldirilanlar: Erisim noktasi, Ses modu, Goruntu modu, Dil/Klavye, Tarih/Saat, Zamanlayici, Diger ayarlar, HakkindĞ°, Destek, Bluetooth hoparlor, Ethernet.
- projection(): 5 olu kart -> 2 giris: "Ekran ve Ses (TV)" -> com.android.tv.settings DisplaySoundActivity; "LuminaOS Projeksiyon" -> MainSettingActivity.
- power(): 3 -> 2 (Guc secenekleri, Ana ekran).
- more(): 9 -> 4 (Duvar kagidi, Ekran yansitma, Dosyalar/USB, Uygulama magazasi). openPackage -> openAny(pkg...).
- Dogrulama (adb 192.168.1.100:5555): TV Ayarlari -> MainSettings; Ekran ve Ses (TV) -> DisplaySoundActivity; Cihaz ayarlari -> MainSettingActivity; BACK her ikisinden launcher'a donuyor; logcat FATAL/ANR yok.
- APK 387.350 bayt, sha256 8E2A027DDFF2BB03E5379FF16BAA1C29521DA9ACCF02BEDF371F9DF4A2492EA1.
## 2026-11-05 - Ana ekran 8 buyuk kare (v3)
- home(): 2x4 buyuk kutu izgarasi. Uygulama GridView ana ekrandan kaldirildi; yalniz "Uygulamalar" ekraninda.
- Sira: Giris/HDMI, Kablosuz Ekran, YouTube, BrowseHere | SmartTube, Dosyalar, Uygulamalar, Ayarlar.
- Projeksiyon ve Guc ana ekrandan Ayarlar ekranina tasindi (3. satir: Projeksiyon, Guc, Kisayollar).
- Buyuk kutu: 68dp ikon + 21sp etiket, odakta turkuaz cerceve.
- Yardim metni kisaltildi. Gozlem: MENU tusu sistem tarafindan yutuluyor ve OEM MainSettingActivity aciliyor (uygulamanin onKeyUp'ina ulasmiyor).
- APK 387.350 bayt, sha256 727F9E63573024F6C3A189CAD578590E23226BF7A1A812D20A0661524A4ECEE8.
- Sicak olcum: PSS 24.723 KB, RSS 96.056 KB, Java heap 8.708 KB.