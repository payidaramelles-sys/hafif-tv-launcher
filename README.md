# Hafif TV

950 MB RAM'li bir Android TV projeksiyon cihazÄ± iÃ§in yazÄ±lmÄ±ÅŸ, **tek Activity'li, sÄ±fÄ±r baÄŸÄ±mlÄ±lÄ±klÄ±** ana ekran (launcher).

CihazÄ±n stok arayÃ¼zÃ¼ (LuminaOS) 60 MB PSS kullanÄ±p 1,2 saniyede aÃ§Ä±lÄ±yordu. Bu launcher aynÄ± iÅŸi **30 MB PSS** ve **0,63 saniyede** yapÄ±yor â€” Ã¼stelik daha az yer kaplÄ±yor: 31,6 MB yerine 382 KB.

![Ana ekran](docs/home.png)

## Neden yazÄ±ldÄ±

Cihaz: ADT-3 (Askey), Allwinner `sun50iw15p1`, Android 14 (SDK 34), **951 MB RAM**, 1920x1080 @320dpi, yalnÄ±z `armeabi-v7a`, root yok (`ro.secure=1`).

Bu sÄ±nÄ±f cihazlarda OEM launcher'Ä±:

- onlarca servisi ve reklam/telemetri kÃ¼tÃ¼phanesini birlikte baÅŸlatÄ±yor,
- uygulama ikonlarÄ±nÄ± her aÃ§Ä±lÄ±ÅŸta paket yÃ¶neticisinden yeniden Ã§Ã¶zÃ¼yor,
- kullanÄ±cÄ±nÄ±n ihtiyacÄ± olmayan 8 ayrÄ± ayar ekranÄ±nÄ± ana ekrana gÃ¶mÃ¼yor.

Bunun yerine ihtiyaÃ§ duyulan ÅŸey kÃ¼Ã§Ã¼k, Ã¶ngÃ¶rÃ¼lebilir ve kumandayla tam Ã§alÄ±ÅŸan bir menÃ¼.

## Ne yapar

- **3 bÃ¼yÃ¼k kart** â€” GiriÅŸ/HDMI, Kablosuz Ekran, Dosyalar
- **4 hÄ±zlÄ± uygulama kutusu** â€” Netflix (tarayÄ±cÄ±da), Prime Video, HBO Max, YouTube
- **5 dÃ¼zenlenebilir yuvarlak** â€” istediÄŸin uygulamayÄ± ata, uzun OK ile deÄŸiÅŸtir
- **Kalem yuvarlaÄŸÄ±** â€” "Alt sÄ±rayÄ± dÃ¼zenle" ekranÄ±: 5 yuvarlaÄŸÄ± tek tek deÄŸiÅŸtir veya temizle
- **Ayarlar** â€” saÄŸ Ã¼stte yuvarlak; cihaz ayarlarÄ±, TV ayarlarÄ±, aÄŸ, bluetooth, uygulamalar, duvar kaÄŸÄ±dÄ±, projeksiyon, gÃ¼Ã§
- Uygulama Ã§ekmecesi: tÃ¼m LEANBACK/LAUNCHER uygulamalarÄ±, alfabetik (TÃ¼rkÃ§e sÄ±ralama), uzun OK ile favori

## Ã–lÃ§Ã¼mler

AynÄ± cihazda, 3 kez soÄŸuk baÅŸlatma, medyan:

| | LuminaOS (stok) | Hafif TV |
|---|---|---|
| PSS | 60,2 MB | **30,0 MB** |
| RSS | 134,1 MB | **98,6 MB** |
| SoÄŸuk aÃ§Ä±lÄ±ÅŸ | 1207 ms | **629 ms** |
| APK boyutu | 31,6 MB | **382 KB** |
| Java + native | 35,3 MB | **17,8 MB** |

Not: soÄŸuk aÃ§Ä±lÄ±ÅŸ "process cold" â€” uygulama sÃ¼reci Ã¶ldÃ¼rÃ¼lÃ¼p yeniden baÅŸlatÄ±larak Ã¶lÃ§Ã¼ldÃ¼, cihaz reseti deÄŸil.

## Neden bu kadar kÃ¼Ã§Ã¼k

- **Tek Activity**, tek layout, sÄ±fÄ±r kÃ¼tÃ¼phane (AndroidX yok, RecyclerView yok, Compose yok).
- **Simge rasterleme yok**: bÃ¼yÃ¼k APK'lardan (SystemUI, TvSettings, 30 MB'lÄ±k OEM uygulamasÄ±) ikon Ã§ekmek 32 MB kaynak haritalamasÄ±na yol aÃ§Ä±yordu. Bunun yerine bu ikonlar cihazda bir kez 256x256 PNG olarak alÄ±nÄ±p pakete gÃ¶mÃ¼ldÃ¼ â€” toplam ~450 KB.
- **Grafikler kodla Ã§iziliyor** (`Canvas`/`Path`): HDMI, yansÄ±tma, klasÃ¶r, Ä±zgara, kaydÄ±rÄ±cÄ±, artÄ±, kalem ikonlarÄ± tek tek Ã§izildi; hiÃ§bir varlÄ±k dosyasÄ± yok.
- **Animasyon yok**, `shared element` yok, arka planda servis yok, izin yok, telemetri yok.

## Derleme

Gereksinimler:

- JDK 17
- Android SDK: `platforms;android-34` + `build-tools;34.0.0`
- PowerShell

```powershell
# AndroidManifest.xml, res/ ve src/ aynÄ± dizinde
./build.ps1
# Ã§Ä±ktÄ±: out/LiteTV-signed.apk
```

`build.ps1` sÄ±rasÄ±yla `aapt2` â†’ `javac` â†’ `d8` â†’ `zipalign` â†’ `apksigner` Ã§aÄŸÄ±rÄ±r; Gradle kullanmaz.

Ä°lk derlemeden Ã¶nce bir debug keystore Ã¼ret:

```powershell
keytool -genkeypair -keystore keys/debug.keystore -alias androiddebugkey `
  -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 10000 `
  -dname "CN=Android Debug,O=Android,C=US"
```

## Kurulum

```bash
adb install -r out/LiteTV-signed.apk
adb shell cmd package set-home-activity org.mehmet.litetv/.MainActivity
```

Geri dÃ¶nmek iÃ§in:

```bash
adb shell cmd package set-home-activity <stok-launcher>/.<AnaActivity>
```

## Bilinen sÄ±nÄ±rlar

Bu bir **kullanÄ±cÄ± arayÃ¼zÃ¼** projesidir; stok sistem uygulamasÄ±nÄ±n yerini tamamen alamaz:

- **Sistem imzasÄ± gerektiren ekranlar** (projeksiyon keystone, gÃ¶rÃ¼ntÃ¼ modu, gÃ¼Ã§ yÃ¶netimi) OEM uygulamasÄ±nÄ±n iÃ§inde `android.uid.system` ile Ã§alÄ±ÅŸÄ±r. Normal bir APK bunlarÄ± aÃ§amaz; bu yÃ¼zden o kartlar OEM'in kendi ayar Ä±zgarasÄ±na yÃ¶nlendirir. Sahte slider/switch konmadÄ±.
- **Widevine L3**: cihazda L1 keybox yok; Netflix gibi uygulamalar "cihaz desteklenmiyor" diyebilir.
- **HDMI** kaynaÄŸÄ± `awlivetv` bileÅŸeniyle deÄŸiÅŸtirilir; kaynak boÅŸsa siyah ekran gÃ¶rÃ¼lÃ¼r, HOME tuÅŸu geri dÃ¶ndÃ¼rÃ¼r.
- GÃ¶mÃ¼lÃ¼ uygulama ikonlarÄ± cihazdan alÄ±nmÄ±ÅŸ anlÄ±k gÃ¶rÃ¼ntÃ¼lerdir; uygulama gÃ¼ncellenince eski ikon gÃ¶rÃ¼nebilir.

## Sorumluluk reddi

Bu yazÄ±lÄ±m **olduÄŸu gibi** sunulmuÅŸtur. CihazÄ±nÄ±zda oluÅŸabilecek her tÃ¼rlÃ¼ sonuÃ§ â€” veri kaybÄ±, aÃ§Ä±lmayan cihaz, garanti kaybÄ±, ayarlarÄ±n bozulmasÄ±, Ã¼Ã§Ã¼ncÃ¼ taraf uygulamalarÄ±n Ã§alÄ±ÅŸmamasÄ± â€” kullanÄ±cÄ±nÄ±n sorumluluÄŸundadÄ±r. GeliÅŸtirici hiÃ§bir sorumluluk kabul etmez. YÃ¼klemeden Ã¶nce stok launcher'Ä± geri yÃ¼kleme yolunuzu bildiÄŸinizden emin olun.

Bu proje hiÃ§bir Ã¼reticiyle baÄŸlantÄ±lÄ± deÄŸildir; marka adlarÄ± yalnÄ±zca tanÄ±mlama amacÄ±yla geÃ§er.

## Lisans

MIT â€” bkz. [LICENSE](LICENSE).
