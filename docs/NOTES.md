# Hafif TV — LuminaOS yerine hafif HOME

2026-11-05 · OpenCode / GPT-6.1 Sol · Paket: `org.mehmet.litetv`, Activity: `.MainActivity`.

## Sonuç ve önemli sınır

Saf Android framework Java ile **tek Activity**, animasyonsuz, Türkçe, koyu temalı, D-pad odaklı launcher. AndroidX, RecyclerView, Gradle, üçüncü taraf bağımlılık, native `.so`, servis, reklam/analitik SDK, internet izni veya sistem ayarı yazma kodu yoktur. Uygulamalar LEANBACK_LAUNCHER + LAUNCHER sorgularından birleştirilir, TV girişi öncelenir, paket bazında tekilleştirilir. HOME/DEFAULT ve ayrı LEANBACK_LAUNCHER intent filtreleri vardır.

**Bu uygulama LuminaOS'un sistem yetkisini devralmaz.** 21 private Activity'nin işlevleri, orijinal uygulama kurulu kaldığı için OEM üst menüsü üzerinden erişilebilir kalır; bunları yeni UI içinde bire bir yeniden uyguladık demiyoruz. Doğrudan projeksiyon setter'ı, ayrıcalıklı güç API'si, kalibrasyon reset'i veya private ekran bypass'ı yoktur. Özel durum simgeleri ve Lumina SQLite/verileri kopyalanmamıştır. Bağımsız, %100 OEM'den arındırılmış bir replacement değildir; hafif HOME + güvenli üretici köprüsüdür.

## Dosyalar

- `src/org/mehmet/litetv/MainActivity.java`: bütün uygulama; dört sütun GridView, uygulama/favori/yönetici modları, yerel menüler, güvenli köprüler.
- `AndroidManifest.xml`, `res/values`, `res/drawable`: manifest, framework tema/TV banner. `res/drawable-nodpi`: cihazdaki gerçek uygulamalardan bir kez rasterlenmiş 18 adet 256×256 PNG; `res/raw/bundled_apps.tsv`: Türkçe etiket/ikon/boyut kataloğu.
- `build.ps1`: çevrimdışı aapt2 → javac → d8 → APK → zipalign → apksigner. Kalıcı geliştirme anahtarı `debug.keystore`; üretim sertifikası değildir.
- **İmzalı APK: `out/LiteTV-signed.apk`**. minSdk 23, targetSdk 34. V1/V2/V3 imza ve zipalign doğrulaması derleme içinde zorunlu.
- Final APK **387.350 bayt (378,27 KiB)**; SHA-256 `cb6d49e0d13fba47edff5ca2fc5c0863233b04549ec1de51b67a9b1bc84f5ef3`. Kaynak Lumina APK'sından boyutça %98,78 küçük. Bu boyut karşılaştırması işlevlerin OEM'e köprülenmesi nedeniyle bağımsız işlev eşdeğerliği anlamına gelmez.
- `test-device.ps1`: geçici HOME, D-pad gezinme, güvenli ekran testi; koşulsuz `finally` ile LuminaOS HOME restore.
- `measure.ps1`: paket başına üç iç içe process-cold ölçüm; ham `am start -W` ve `dumpsys meminfo`, JSON/CSV; koşulsuz restore.
- `re/INVENTORY.md`: kapsamlı RE raporu; tam özellik tablosu, vendor property/API'leri, 30 Activity, diğer component'ler, izin ve riskler.
- `re/decompiled/`: JADX kaynak ve resource çıktıları. `re/components.csv`, `re/manifest-summary.json`, `re/inspect_manifest.ps1`: makinece doğrulanmış envanter.
- `re/device-config-oem.ini`: orkestratörün cihaz `/oem/config.ini` kopyası; menü bu cihaz konfigürasyonuna göre daraltılmıştır.
- `out/tests/`, `out/metrics/`: çalışma zamanı kanıtları. Yalnız en son başarılı çalıştırma son durum kanıtıdır; önceki smoke denemelerindeki XML'ler de dizinde kalabilir.

## Cihaza özgü menü

ADT-3, Android 14 / SDK34, 1920×1080 @320dpi, armeabi-v7a, yaklaşık 951 MB RAM. USB seri çevrimdışıydı; orkestratörün verdiği canlı ADB hedefi `192.168.1.100:5555` kullanıldı.

Gerçek OEM config: autoKeystone/manualKeystone/screenZoom/projectMode/initAngleCorrect/displayPictureMode/network/wifi/bluetooth açık; autoFourCorner/autoFocus/deviceMode/powerMode/account/bootSource/brightAndColor kapalı, layout_select=3. Bu yüzden **otomatik odak, dört-köşe ve hesap kartları yoktur**. Projeksiyon menüsü yalnız Keystone (oto/manuel), Görüntü Modu, Zoom/En-boy, Projeksiyon Yönü, uyarılı Açı Kalibrasyonu sunar. Config okunup sistem ayarı değiştirilmez; bu derleme belirtilen cihaz profiline göre tasarlanmıştır, dinamik OEM config yöneticisi değildir.

## RE bulguları

Kaynak APK: 31.631.110 bayt, SHA-256 `afd3fd183a986e9b7a0e3fcd8381f1a7e372a65a0d41e037412c809e978927cc`, LuminaOS 1.0.48. JADX 3.599 sınıf çıkardı, 25 yöntem için decompile hatasıyla exit1 verdi; manifest ve çevre çağrı zincirleri bağımsız doğrulandı. Eksik yöntemler `re/INVENTORY.md` §9'dadır. Dolayısıyla hatalı yöntemlerde tam davranış kanıtı iddiası yoktur.

30 Activity: **9 exported +21 private**, 2 exported service, 6 exported receiver, 0 provider. `android.uid.system` shared UID, REBOOT/WRITE_SECURE_SETTINGS vb. izinler yalnız platform imzalı üretici uygulamasına aittir. `com.softwinner.awmanager`/AwTvDisplayManager, PQControl, PrjScreen, AudioManagerEx, property/hidden API katmanları bulundu; normal UID için kararlı ve izinli bir kontrol API'si kanıtlanamadı. `zoom_value` tek başına doğru zoom uygulaması değildir; koordinat, property ve geometri zinciriyle birlikte yürütülür. Bu nedenle doğrudan Global/property/Binder yazımı yapılmadı.

### Dışa açık Activity'lerin eksiksiz listesi

Ortak prefix `com.htc.luminaos/`; sınıflar tablodaki tam sınıf adıyla kullanılır.

| Ad | Ne yapıyor | Bizim menüde karşılığı |
|---|---|---|
| `com.htc.luminaos.activity.MainActivity` | Orijinal HOME, içerik/giriş/destek kısayolları | Diğer → LuminaOS; destek/OEM duvar kağıdı/eksik harici uygulama fallback'i |
| `com.htc.luminaos.activity.settings.MainSettingActivity` | Ağ, BT, Projeksiyon, Uygulama, Dil, Tarih, Diğer, Hakkında | Ayarlar → Cihaz ayarları; private ekranlar için ana güvenli köprü |
| `com.htc.luminaos.activity.settings.WifiActivity` | Wi-Fi aç/kapat, tarama, bağlantı, IP | Ayarlar → Wi-Fi/Ağ → OEM Ağ menüsü içinden Wi-Fi; ağ değişimi test edilmedi |
| `com.htc.luminaos.activity.settings.BluetoothActivity` | BT enable/discovery/eşleştirme/profiller | Ayarlar → Bluetooth → önce uyarı/onay; otomatik smoke test yalnız onaya kadar |
| `com.htc.luminaos.activity.settings.NetworkActivity` | Wi-Fi/Ethernet/hotspot üst menüsü | Ayarlar → Wi-Fi/Ağ; güvenli üst menü |
| `com.htc.luminaos.activity.settings.WiredActivity` | Ethernet DHCP/statik IP bilgisi/ayarları; bu cihazda arayüz/config yoksa onResume NPE ile OEM süreci çöker | Doğrudan kullanılmaz. Ayarlar → Ethernet, uyarı ile güvenli NetworkActivity üst menüsünü açar |
| `com.htc.luminaos.activity.settings.DisplaySettingsActivity` | Saydam vendor PQ/EQ paneli, overlay/property/audio yan etkileri | Doğrudan kullanılmaz. Projeksiyon → Görüntü Modu → OEM ana ayarları; PQ paneli keystone/zoom eşdeğeri değildir |
| `com.htc.luminaos.activity.settings.PowerActivity` | Uyku/kapat/reboot/timer/BT speaker; açılışta 15 sn kapatma sayacı | Güç → yerel uyarılı sayfa → ikinci onay; varsayılan odak VAZGEÇ; otomatik test üretici ekranına geçmez |
| `com.htc.luminaos.activity.settings.BluetoothIncomingFileConfirmActivity` | Gerçek Bluetooth dosya aktarımı kabul akışı; Parcelable cihaz bağlamı gerekir | Yapay kısayol yok; gerçek sistem transfer akışı korunur, sahte istek üretilmez |

### İşlev → LuminaOS → Hafif TV eşlemesi

`Yerel` bizim UI/veri alanımız; `OEM menü` bir ek kullanıcı seçimi gerektirir. Private ekranların hiçbiri exported yapılmadı, uygulama verileri okunmadı/değiştirilmedi.

| İşlev | LuminaOS'ta nasıl | Bizim uygulamada nasıl / sınır |
|---|---|---|
| HOME, saat/tarih | Ana Activity + durum/marka görünümü | Yerel büyük TextClock, tarih, koyu büyük kartlar; sistem dili değiştirilmez |
| Uygulama çekmecesi | Private AppsActivity, Launcher/Leanback sorgusu | Yerel GridView, iki kategori, TV öncelik, Türkçe sıralama, paket tekilleştirme |
| Uygulama açma | Paket/component | Yerel OK veya uzun OK → Aç; sistem uygulaması da filtre şartıyla görünür |
| Favori ekle/çıkar | Private AppFavoritesActivity + SQLite | Yerel Favori düzenle / uzun OK, ayrı SharedPreferences; Lumina favorileri aktarılmaz |
| Uygulama bilgisi/kaldır/cache | Private AppsManagerActivity | Yerel seçim → sistem Uygulama Bilgisi; kaldır/veri/cache işlemi kullanıcıya bırakılır |
| OEM yanlış cache etiketi | clearApplicationUserData çağrısı, veri silici | Kopyalanmadı. Sessiz veri/cache silme veya sistem uygulaması kaldırma yok |
| Dahili/USB duvar kağıdı | Private WallPaperActivity ve özel dosyalar | Yerel 3 renk + ACTION_OPEN_DOCUMENT resim/USB seçici; RGB565 ≤960×540 örneklenir; kalıcı URI izni. OEM seçiciye ayrıca orijinal HOME köprüsü |
| HDMI/giriş | awlivetv MainActivity + input_source | Yerel onaylı seçim: HDMI1/HDMI2/CVBS1/VGA; runtime component erişim kontrolü. Mevcut portlar OEM donanımına bağlı, listedeki her fiziksel port doğrulanmış değildir |
| Video/stream/store uygulamaları | Netflix/YouTube/Disney/store/GTV paketleri | Kurulu ve görünür uygulamalar gridde; mağaza için ayrıca kısayol. Uygulama indirme/kurma yok |
| Ekran yansıtma | EShare `com.ecloud.eshare.server`, OEM override mümkün | Diğer → Ekran yansıtma; paket yoksa Lumina HOME. Ağ/döküm başlatma otomatik test edilmez |
| Dosya/USB | `com.hisilicon.explorer` | Diğer → Dosyalar/USB veya grid; paket yoksa Lumina HOME |
| Bildirimler | `com.htc.notification` | Diğer → üretici Bildirimler; özel bildirim dinleyicisi yok |
| Bellek temizleme | `com.htc.clearmemory` | Diğer → önce onay; aracı kullanıcı açar. Otomatik kill/temizleme yok |
| Destek/kılavuz/QR | Private SupportActivity, OEM config içerikleri | Diğer/Ayarlar → Lumina HOME, oradaki destek simgesi; bu bölüme MainSetting'in eşdeğer olduğu iddia edilmez |
| Wi-Fi/bağlantı/statik IP | WifiActivity + private WifiIpSetActivity | OEM Network → Wi-Fi → IP; yeni launcher Wi-Fi'yi sessiz açıp kapatmaz |
| Ethernet DHCP/statik | Exported WiredActivity; mevcut cihazda null IP config nedeniyle çöküyor | Ayarlar → Ethernet → uyarılı OEM Ağ üst menüsü. Fiziksel arayüz yok; bu OEM bug'ı yeni UI'dan tetiklenmez |
| Hotspot | Private HotspotActivity | OEM Cihaz ayarları → Ağ → Hotspot; yeni uygulama tethering başlatmaz |
| Bluetooth ve transfer | Exported BT UI ve gerçek transfer intent'i | BT kartında önce yan etki uyarısı; sonra OEM UI. Gerçek transfer sistem akışında kalır |
| Bluetooth speaker/sink | Private BluetoothSpeakerActivity; Wi-Fi/BT/LED yan etkili | Üst OEM ayarları üzerinden kullanıcı yolu; private ekran doğrudan açılmaz; otomatik testte yok |
| Keystone oto/manuel | Private Project/Correction, vendor geometri | Projeksiyon → Keystone → OEM Cihaz ayarları → Projeksiyon; mevcut kalibrasyon korunur |
| Zoom/en-boy | Project, Global + property + geometri | Projeksiyon → Zoom/En-boy → aynı OEM Projeksiyon yolu |
| Projeksiyon yönü | Project + panel/keystone dönüşümü | Projeksiyon → Projeksiyon Yönü → OEM Projeksiyon yolu |
| Açı kalibrasyonu | InitAngleActivity açılışında reset | Yerel uyarı/VAZGEÇ → yalnız OEM Project üst menüsü; InitAngle veya INITANGLE broadcast çağrılmaz |
| Görüntü/ses/EQ/RGB/ARC | Private Picture/Audio; vendor overlay ve manager'lar; config'e bağlı | Görüntü/Ses kartları OEM üst menüsü; gizli/devre dışı satırların varlığı vaat edilmez; sahte slider yok |
| Otomatik odak/dört-köşe/cihaz modu | Kod var; bu cihaz OEM config'inde kapalı | Kart yok, donanım yeteneği iddiası yok. Genel orijinal OEM UI korunur |
| Dil/klavye | Private LanguageAndKeyboardActivity | OEM Cihaz ayarları → Dil/Klavye. Yalnız bizim app locale'i Türkçe, sistem locale/IME değiştirilmez |
| Tarih/saat/saat dilimi | Private DateTimeActivity | OEM Cihaz ayarları → Tarih/Saat; sessiz SET_TIME yok |
| Hesap/parola | Private Account/PasswordVerify1/2; hesap bu cihazda kapalı | Hesap kartı yok. Üretici parolası geçerliyse OEM doğrulaması atlanmaz; Lumina parola verisine erişilmez |
| Ekran koruyucu/tuş sesi/erişilebilirlik | Private OtherSettings | Ayarlar → Diğer veya Zamanlayıcı → OEM Diğer; ayar yazılmaz |
| Başlangıç kaynağı/power mode | Private OtherSettings, config kapalı | Özel kart yok; OEM menüsündeki mevcut görünürlük aynen korunur |
| Uyku/kapat/yeniden başlat | Ayrıcalıklı PowerActivity | Güç ekranı ve ikinci açık onay; açılışın 15 sn sayacı konusunda uyarı. Gerçek güç eylemi test edilmez |
| Kapanma zamanlayıcısı | Private OtherSettings + TimeOffService | Zamanlayıcı → OEM Diğer; exported servise sahte süre gönderilmez |
| Hakkında/cihaz bilgisi | Private About | Ayarlar → Hakkında → OEM ana menü. RAM ölçümü pazarlama/config ölçeği yerine dumpsys ile yapılır |
| OTA/USB güncelleme/fabrika reset | About/Other + vendor update ve FACTORY_RESET | OEM Hakkında/Diğer içinde kalır. Yeni launcher'da tek tık tehlikeli işlem yok; test edilmez |
| Developer/fabrika/test/gizli tuşlar | Private DeveloperMode/About | Orijinal OEM UI'da kalır; ADB açma/reboot testi/güvenlik ayarı değişimi yok |
| OEM durum simgeleri | Wi-Fi/BT/USB/batarya/notification vendor state | Yeni HOME'da bire bir statusbar replikası yok; ilgili OEM ekranları/notification kısayolu korunur. Bu görsel eksik açıkça kabul edilmelidir |

21 private Activity kapsamı: Apps/AppFavorites yerel karşılık; AppsManager sistem ayrıntıları; Wallpaper yerel resim ve OEM HOME; Support OEM HOME; PasswordVerify1/2 üretici içinde; Project/Correction/Picture/Audio/InitAngle OEM Projeksiyon; WifiIpSet/Hotspot OEM Ağ; BluetoothSpeaker OEM güç/üst menü; Language/Date/About/Other/Developer OEM ayarlar; Account cihazda kapalı. **Erişim korunması, doğrudan tek tık aynı alt ekrana ulaşma veya yeni UID'den setter uygulama demek değildir.**

### Her kartın final APK'daki gerçek hedefi

`S` = `com.htc.luminaos/.activity.settings.MainSettingActivity`, `N` = `com.htc.luminaos/.activity.settings.NetworkActivity`, `H` = `com.htc.luminaos/.activity.MainActivity`. Private alt sınıf çağrılmaz; runtime exported/enabled/permission kontrolünden sonra bu gerçek hedefe düşülür. S de kullanılamazsa ilgili `android.settings.*`, son olarak `com.android.tv.settings/.MainSettings` denenir.

| Kart / alt menü | Bu cihazda gerçek hedef |
|---|---|
| Ana: Giriş/HDMI | Yerel 4 kaynak seçimi; yalnız seçimde `com.softwinner.awlivetv/.MainActivity`, `input_source=HDMI1/HDMI2/CVBS1/VGA`; erişilemezse H |
| Ana: Ayarlar / Projeksiyon / Güç / Diğer | Yerel ilgili sayfa/dialog; ilk OK'de donanım eylemi yok |
| Ana: Uygulamalar; Tüm uygulamalar/Favoriler/Favori düzenle/Uygulama yöneticisi | Aynı Activity içinde GridView modları |
| Ayarlar: Cihaz ayarları | S |
| Ayarlar: Wi-Fi/Ağ; Ethernet | N; Ethernet hatası hakkında toast |
| Ayarlar: Bluetooth | Yerel VAZGEÇ odaklı onay; kabulde `com.htc.luminaos/.activity.settings.BluetoothActivity` |
| Ayarlar: Erişim noktası / Ses modu / Görüntü modu / Dil-Klavye | S; kullanıcı ilgili bölümü seçer |
| Ayarlar: Tarih-Saat / Zamanlayıcı / Diğer ayarlar / Hakkında | S; kullanıcı ilgili bölümü seçer |
| Ayarlar: Duvar kağıdı | Yerel tema/resim dialog'u |
| Ayarlar: Destek | H; kullanıcı üretici destek simgesini seçer |
| Ayarlar: Uygulamalar | Yerel yönetici grid'i → uzun OK/app seçenekleri |
| Ayarlar: Bluetooth hoparlör | S; private sink ekranı çağrılmaz |
| Projeksiyon: Keystone / Görüntü Modu / Zoom-Enboy / Projeksiyon Yönü | S; kullanıcı Projeksiyon bölümünü seçer |
| Projeksiyon: Açı Kalibrasyonu | Yerel VAZGEÇ odaklı uyarı, kabulde S; InitAngle asla doğrudan çağrılmaz |
| Güç: Güç seçenekleri | Yerel ikinci onay, kabulde `com.htc.luminaos/.activity.settings.PowerActivity` (15 sn risk!) |
| Güç: Diğer-Zamanlayıcı; Ana ekran | S; yerel HOME |
| Diğer: Duvar kağıdı-Tema / Destek / Zamanlayıcı | Yerel dialog / H / S |
| Diğer: Ekran yansıtma / Dosyalar-USB / Uygulama mağazası | PM Leanback/Launcher intent'i: `com.ecloud.eshare.server` / `com.hisilicon.explorer` / `com.htc.storeos`; bulunamazsa H |
| Diğer: LuminaOS ana ekran / Bildirimler | H / PM paket intent'i `com.htc.notification`, yoksa H |
| Diğer: Bellek temizleme | Yerel onay, kabulde PM paket intent'i `com.htc.clearmemory`, yoksa H |
| Tema: Gece / Okyanus / Orman | Yalnız uygulamanın SharedPreferences arka planı |
| Tema: Dosya-USB'den resim / Üretici duvar kağıtları | `ACTION_OPEN_DOCUMENT image/*` / H |
| Uygulama OK / uzun OK | Kaydedilen gerçek `ResolveInfo` component'i / yerel favori-bilgi-aç dialog'u |
| Uygulama bilgisi-kaldır-önbellek | `android.settings.APPLICATION_DETAILS_SETTINGS`, `package:<seçilen paket>`; otomatik işlem yok |

## Test ve güvenlik kaydı

Güvenli test kapsamı: geçici HOME atanması ve HOME tuşu; 6 ana kart, 16 ayar kartı, 5 projeksiyon kartı; yalnız kendi UI + OEM MainSetting/Network/Home fallback'leri; favori/liste/düzenleme/yönetici/uzun OK; güç ikinci onayında VAZGEÇ varsayılan odağı. Her vaka sonrası `dumpsys window` ile Hafif TV'ye dönüş denetlenir. Hiçbir ayar toggle'ı, kalibrasyon, reset, uninstall/data clear, güç işlemi seçilmez.

**Final imzalı APK ile tekrar test: 38/38 güvenli vaka geçti.** HOME varsayılanlığı + 6 ana kart + güç çift kapısı + 16 ayar + 5 projeksiyon + 5 güvenli Diğer seçimi + 4 uygulama/favori/yönetici/uzun OK vakası. `out/tests/results.json` her vakanın açılan top Activity'sini, açılan focus'u ve Hafif TV'ye dönen focus'u içerir. Bu sayı bütün donanım işlemlerinin uçtan uca geçtiği anlamına gelmez; aşağıdaki kapsam dışı işlemler aynen kapsam dışıdır.

Bluetooth enable/discovery, DisplaySettings PQ overlay, InitAngle reset, Bluetooth sink, HDMI gerçek kaynak seçimi, mağaza/döküm/güncelleme/temizleme test kapsamı dışında tutuldu; sadece yerel uyarı veya OEM güvenli üst menü test edildi. Kullanıcı donanım eylemlerini daha sonra bilinçli doğrulamalıdır. Yerel fotoğraf seçici/URI kalıcılığı kullanıcı resmi seçmeden uçtan uca doğrulanmış sayılmaz.

Önceki başarısız denemeler saklanmamalı diye dosya silinmedi: ilk PowerShell sürümünde başarılı `adb pull` stderr çıktısı hata sanıldı (düzeltildi). Bir erken D-pad denemesinde HOME sonrası eski HDMI dialog'u kapanmamıştı; yanlışlıkla awlivetv açıldı. Bu olay orkestratöre hemen bildirildi. `show/onNewIntent` artık bütün aktif dialog'ları kapatır. **Hiç giriş değişmedi diye bir iddia yoktur**; önceki fiziksel girişin ne olduğu kanıtlanamadığından tahmini geri değişim yapılmadı. Daha sonraki güç assertion'ı VAZGEÇ'in büyük harfli görünümünden false-negative verdi; kıyas case-insensitive yapıldı. Üretici PowerActivity hiçbir testte açılmadı.

Test/ölçüm sonrasında hem HOME resolver hem görünür focus'un `com.htc.luminaos/.activity.MainActivity` olması gereklidir. En son kanıt `out/tests/final-window.txt`, `restored-resolver.txt` ve `out/metrics/` karşılıklarındadır. Orijinal launcher kaldırılmadı/devre dışı bırakılmadı; kalıcı cihaz ayarı değiştirme komutu kullanılmadı. Geçici varsayılan HOME işlemi test sonunda geri alınır.

Son bağımsız kapanış doğrulaması: `out/final-home-resolver.txt` ve `out/final-home-window.txt` LuminaOS HOME'u gösterir; `out/tests/restored-home.png` görsel olarak da orijinal ana ekranı doğrular. Kurulu `pm path org.mehmet.litetv` APK'sı geri çekilip `out/installed-final.apk` SHA-256'sı imzalı çıktıyla bire bir eşlendi (`out/installed-proof.txt`); doğru son APK'nın cihazda olduğu böylece ayrıca kanıtlandı.

**Canlı OEM bug'ı:** İki güvenli aç/geri denemesinde `WiredActivity.onResume → updateEthernetStatus` null `mIpConfiguration.getIpAssignment()` NPE verdi ve LuminaOS süreci çöktü. `dumpsys ethernet`: IP Configurations/Tracking interfaces boş; yani fiziksel Ethernet arayüzü yok. `out/tests/crash-evidence.txt` kanıtı korunur. Bu sırada DHCP/static seçilmedi, ağ değeri yazılmadı; finally Lumina HOME'u görünür geri getirdi. Final APK Ethernet'i NetworkActivity'ye yönlendirir, bu buggy alt ekranı doğrudan açmaz. OEM'in kendi menüsünden aynı alt ekranı kullanıcı seçerse kendi hatası devam edebilir; üretici APK'sı değiştirilmemiştir.

## Ölçümler

Final APK ile 3× process-cold, menü açık ve 3 sn yerleşmiş durum; ham değerler `out/metrics/results.json`, `results.csv`, `summary.json` ve paket/örnek bazlı `*-start.txt`, `*-meminfo.txt` dosyalarındadır. Birimler dumpsys KB (pratikte KiB), süre ms. Medyan:

| Uygulama / ölçüm turu | PSS KB | RSS KB | Cold ms |
|---|---:|---:|---:|
| LuminaOS ilk referans (istenen 60.072) | 60.072 | 134.100 | 1.212 |
| LuminaOS final ile aynı tur | 60.187 | 134.168 | 1.207 |
| **Hafif TV final, gerçek gömülü ikonlar** | **30.791** | **98.656** | **720** |

İlk Lumina referansına göre **PSS %48,74 az, RSS %26,43 az, cold süre %40,59 kısa**. Final ile eşzamanlı Lumina turuna göre sırasıyla yaklaşık %48,84 / %26,47 / %40,35. Final örnekleri: Lumina PSS `[60499,60073,60187]`, RSS `[134444,134060,134168]`, süre `[1272,1207,1192]`; Hafif TV PSS `[30766,30809,30791]`, RSS `[98656,98628,98672]`, süre `[720,675,724]`.

Örnek 2 App Summary: Hafif TV Java heap PSS 9.096 KB, native 8.164 KB, `.apk mmap` 1.348 KB; ilk gerçek-dinamik-ikon denemesi `.apk mmap` 32.285 KB idi. Önemli fark framework'ün her paketin ikonunu **ve etiketini** yüklerken kaynak APK resource tablolarını launcher sürecine map etmesidir. Bu yüzden kaynağı 2 MiB'tan büyük 18 uygulamanın gerçek ikonları **bir kez cihazdan** 256×256 rasterlenip gömüldü, etiketleri de snapshot alındı; monogram veya sahte simge kullanılmadı. Grid ikonunun gerçek basılan boyutu **40dp × density2 = 80 ekran pikseli** (karo 400+ piksel genişliğinde olsa da ikon o boyutta değildir); 256 px kaynak 3,2× pay sağlar. Son `out/tests/home.png` görselinde kenar/yazı bulanıklığı görülmedi. Yerel UI ve özellik yolları değiştirilmedi.

**Snapshot sınırı:** katalog bu cihazın tr-TR uygulama listesi içindir. APK boyutu değişirse snapshot kullanılmaz, normal label/icon yolu denenir; ikon LRU cache'i süreç yaşamı boyunca kalabilir. Boyut aynı kalırken uygulama etiketi/ikonu değişirse eski snapshot sürebilir. Tam sürüm takibi yoktur; kalıcı çözüm yeni ikon kataloğu üretmek veya `ACTION_PACKAGE_REPLACED` / package version değişiminde ilgili snapshot+LRU girdisini geçersiz kılmaktır. Bu refresh mekanizması final APK'da uygulanmış gibi sunulmaz. Yeni/2 MiB altı uygulamalar normal gerçek ikon/etiket yolunu kullanır; gelecekte büyük yeni uygulama eklenmesi PSS'yi yükseltebilir. İkonlar ilgili uygulamaların markalı varlıklarıdır, bağımsız mülkiyet iddiası yoktur.

### Map kanıtı ve paylaşılmış sayfa uyarısı

`out/maps-original-icons.txt` → `out/maps-final.txt`: APK yolu 48→15; launcher'ın kurulum yolu hariç **33 APK/overlay yolu artık map edilmez**. Kaybolan ilk kaynak segmentleri toplamı 60.788 KiB **sanal adres aralığıdır, RAM/PSS tasarrufu değildir**. Tam liste/range/path: `out/maps-removed.csv`. Örnek kalkmış aralıklar:

| Kaynak | Önceki segment | Sanal KiB |
|---|---|---:|
| OfficeSuite | `8530c000–8667c000` | 19.904 |
| TvSettings | `87ee9000–88ca4000` | 14.060 |
| BrowseHere | `87ad8000–87ee9000` | 4.164 |
| SmartTube | `87755000–87ad8000` | 3.596 |
| LuminaOS | `86adc000–86c76000` | 1.640 |

Lumina'nın 31,6 MB APK'sı bütünüyle map edilmiş değildi; yalnız görülen kaynak segmenti 1,6 MiB idi. TvSettings 13,73 MiB sanal segmentinin önceki smaps resident değeri 4.648 KB idi; sanal boyut resident değildir. Son map'te kalan uygulama kaynakları ClearMemory (PSS/RSS 464/464 KB), com.tvlauncher (344/344), AwSource (284/284), kendi APK (26/32); ayrıca framework-res (235/1344) ve küçük framework overlay/idmap'ler. Diagnostic kendi `/proc/self/maps` ve `/proc/self/smaps` okur; ADB'den diğer süreç `/proc/<pid>/maps` SELinux nedeniyle okunamadı. Yalnız `am start ... --ez memory_diagnostics true` ile açılır; normal HOME'da thread/proc I/O yoktur. Diagnostic/export turu benchmark'a dahil değildir; export kodu final APK'dan çıkarıldı.

`out/metrics-original-icons`, `out/metrics-monogram-experiment`, `out/metrics-lumina-only` önceki deneylerdir, final sonuç değildir. İlk dinamik gerçek ikon sürümü medyan PSS 62.299 KB ile Lumina'dan daha yüksekti; bu başarısız optimizasyon gizlenmedi. Monogram denemesi UX nedeniyle reddedildi. Son değerler yalnız final gömülü **gerçek** ikonlu APK'ya aittir.

Yöntem: 3'er örnek, Lumina/Hafif TV iç içe; önce diğer uygulama HOME yapılır ve HOME'a gidilir, hedef `am force-stop`, sonra `am start -W -n ...`; **LaunchState:COLD zorunlu**. 3 sn yerleşme sonrası `dumpsys meminfo <paket>` TOTAL PSS/RSS (KB). Root/drop-caches yok; bunlar process-cold, reboot/disk-cold değildir. PSS paylaşılan sayfaların bölüşümüne ve RAM basıncına bağlıdır. Yalnız ilgili süreç karşılaştırılır, bütün sistem/OEM servis tüketimi ortadan kalkmış sayılmaz. Varsayılan düz renk, aynı cihaz/uygulama listesi. İlk kurulumdaki tek seferlik 2238 ms ölçümü bu benchmark'a dahil edilmez.

## Derleme, kurulum ve geri dönüş

PowerShell, proje kökünden:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 install -r .\out\LiteTV-signed.apk

# İsteğe bağlı geçici HOME; cihaz ayarlarını değiştiren hiçbir başka komut yok:
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell cmd package set-home-activity org.mehmet.litetv/.MainActivity
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell input keyevent HOME

# Geri dönüş — önce yerel overlay/dialog'u kapat, sadece bizim süreci durdur:
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell input keyevent BACK
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell am force-stop org.mehmet.litetv
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell cmd package set-home-activity com.htc.luminaos/.activity.MainActivity
& 'C:\Android\sdk\platform-tools\adb.exe' -s 192.168.1.100:5555 shell input keyevent HOME
```

JDK17: `C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot`; SDK `C:\Android\sdk`, android-34/build-tools34. Javac'da Android jar **classpath** kullanılır; bootclasspath yapmak JDK17/LambdaMetafactory derlemesini bozduğu için seçilmedi. D8 android.jar ile public Android API hedefi sağlar. Bilinen uyarılar source8 bootstrap classpath ve deprecated framework API'leridir; derleme/imza doğrulaması başarısızlığı maskelenmez. Güncel kaynak değiştiğinde APK yeniden derlenmelidir; yeniden üretim harici indirmeye ihtiyaç duymaz.

## 2026-11-05 - Ayarlar ekrani sadeleştirildi (v2)
- settings(): 16 olu buton -> 6 gercek kisayol (Cihaz ayarlari, TV Ayarlari, Ag/Wi-Fi, Bluetooth, Uygulamalar, Duvar kagidi).
- Kaldirilanlar: Erisim noktasi, Ses modu, Goruntu modu, Dil/Klavye, Tarih/Saat, Zamanlayici, Diger ayarlar, Hakkindа, Destek, Bluetooth hoparlor, Ethernet.
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
## 2026-11-07 - Odak testi deseni (v4)
- Sorun: projeksiyonun odagi manuel halka ile ayarlaniyor; kullanici kenar/orta netligini ve yazi puntolarini ayni anda degerlendirecek yerlesik referans istedi.
- Cozum: Ayarlar > Projeksiyon ekranina "Odak testi (desen)" girdisi; tam ekran FocusView (uc desen), OK/sag-sol ile desen degisir, Geri projeksiyon ayarlarina doner.
- Desen 1 odak yildizi: 36 kollu Siemens yildizi, dis halkalar, dort kose + dort kenar artisi, iki yanda 1-3 dp kalinlikta ince cizgi demetleri, kose etiketleri.
- Desen 2 punto merdiveni: 30/24/19/15/12/9/7 punto Turkce satirlar; sag ustte 16/13/11/9/8 punto kenar ornekleri, sag altta 9/7 punto tekrar, solda ince yatay cizgi demeti.
- Desen 3 izgara: 24 dp izgara, dama kenar seridi, dort kosede 1/2/3/5/8/12 dp aralikli dikey cizgi gruplari.
- Hic gorsel varlik eklenmedi; hepsi Canvas ile vektorel. APK 395.544 bayt (onceki 391.446).
- Surum: versionCode 2 / versionName 1.1. Imza ayni debug anahtari (SHA-256 D0:14:D7:D8:...68A55), bu yuzden adb install -r sorunsuz.
- Dogrulama (adb 192.168.3.31:5555): Ayarlar > Projeksiyon > Odak testi acildi; uc desen ekran goruntusu alindi; Geri projeksiyon ekranina, tekrar Geri ana ekrana donuyor; logcat FATAL/ANR yok; varsayilan HOME hala org.mehmet.litetv.
- sha256 135BAE87FA0CBCA3A0025B39F0E522C193EB09A14ADC9C720ADDF1A97BDA47ED.
- Not: kaynak/repo hala gecici dizinde (C:\Users\mehme\AppData\Local\Temp\opencode\hafif-tv-repo); kalici bir yere tasinmali.