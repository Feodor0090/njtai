# NJTAI
An unofficial nhentai.net client for J2ME devices with MIDP 2.0 support.

![image](res/njtai.svg)

## Project status

This application works as parser (it downloads full page and extracts data from it). 

## Releases
[Latest releases on nnchan](https://nnp.nnchan.ru/nj)

Check [nnproject chat in TG](https://t.me/nnmidletschat) if you want to ask us something.

## Features
- "Popular" list from site
- "Recently uploaded" list from site
- Search via title
- Search via ID
- Online view with zoom an optional preloading
- Downloading titles to your phone memory
- Both touchscreen/keys support
- Working via bultin proxy (for bans bypassing and avoiding HTTPS use) (configurable)

## System requirements
Basically, it will work on S40v5, S40v6, Symbian 9.1+, most Sony Erricsons with 176x220+ screens, and may be some other advanced 2007-2013 phones. J2MELoader and KEmulator 1.0.3+ are supported.

### Memory
- At least 2048 kb of heap to download titles (with disabled covers loading)
- At least 16 mb to view online
- 16+ mb of VRAM to use HWA view

### Processor units
We don't use any special units. Everything is done on CPU, HWA view can use your GPU if device's M3G implementation supports it. Most Symbian 9.1/9.2 devices with ~300mHz CPUs are too weak to normally work with large images, so you should be ready to face huge stutters.

### JVM capabilities
- MIDP 2.0/2.1
- CLDC 1.1
- Full LCDUI support on MIDP2 level
- JSR-75 to download titles
- Nokia/SE keyboard layout (it's different from very old motorolla/siemens devices)

## Setting your own proxy up
You need an http server. Create a script that will take URL from request params, query it via curl or something else and return it's content.

Example on PHP (url to set in application settngs will be `http://yourserver.com/proxy.php?` if this file is in the server's directory root) (CURL is required):
```
<?php
$url = urldecode($_SERVER['QUERY_STRING']);
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
if(isset($_SERVER['HTTP_USER_AGENT']))
	curl_setopt($ch, CURLOPT_USERAGENT, $_SERVER['HTTP_USER_AGENT']);
$res = curl_exec($ch);
curl_close($ch);
?>
```

Make sure it's accessible via pure http without cloudflare/etc. checks!

## Building
Use Eclipse IDE with MTJ and any j2me sdk (we use S40v5) or NetBeans with j2me sdk 3.0. 
To build from command line, you need to compile all classes using 1.8 jdk with 1.3 target, preverify them using `preverify.exe` from SDK and pack in JAR package.

I recommend you to find a j2me SDK for your own device and follow instructions in it's documentation (you want to build a midlet suite and export it as a package).

## Settings explanation (rus)
### Поведение кэширования
Если отключено полностью - при каждом переключении страницы она будет скачиваться заново, даже если была только что закрыта. Если включено кэширование - несколько предыдущих страниц будут держаться в памяти (быстрее перелистывание). При включенной предзагрузке приложение будет пытаться скачать все страницы как можно раньше.
### Загрузка обложек
На телефонах с малым количеством памяти её может не хватить даже на декодирование превьюшки - отключив их загрузку этими 2 пунктами, вы сможете "вслепую" добраться до кнопки скачивания.
### Запоминание списков
Если отключить, то нажатие кнопки "назад" в манге вернёт вас в главное меню, а не список новых/популярных/поиска. Тоже экономит память, если отключить.
### Декодирование JPEG единожды
Если отключить, картинка будет при каждом ремасштабировании заново декодироваться. Это немного уменьшит потребление памяти при сворачивании приложения.
### Предзагрузка URL
Если включено, ускорит перелистывание почти на 50% - URL изображений запросятся заранее.
