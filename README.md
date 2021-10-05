# NJTAI
An unofficial nhentai.net client for J2ME devices with MIDP 2.0 support.

## Features
- "Popular" list from site
- "Recently uploaded" list from site
- Search via title
- Search via ID
- Online view with zoom an optional preloading
- Downloading titles to your phone memory
- Both touchscreen/keys support
- Working via bultin proxy (for blocks bypassing and avoiding HTTPS use) (configurable)

## System requirements
Basically, it will work on S40v5, S40v6, Symbian 9.1+, most Sony Erricsons with 176x220+ screens, and may be some other 2007-2013 phones. J2MELoader and KEmulator 1.0.3+ are supported.
### Memory
- At least 2048 kb of heap to download titles (with disabled covers loading)
- At least 16 mb to view online without preloading
- ~100mb to preload ~50 pages on 240p screen
- ~100mb to preload ~10 pages on 640p screen
### JVM capabilities
- MIDP 2.0/2.1
- CLDC 1.1
- Full LCDUI support on midp2 level
- JSR-75 to download titles
- Nokia/SE keyboard layout (it's different from very old motorolla/siemens devices)

## Setting your own proxy up
You need an http server. Create a script that will take URL from request params, query it via curl or something else and return it's content.

Example on PHP (url to set in application settngs will be `http://yourserver.com/proxy.php?`) (CURL is required):
```
<?php
$url = urldecode($_SERVER['QUERY_STRING']);
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
if(isset($_SERVER['HTTP_USER_AGENT']))
	curl_setopt($ch, CURLOPT_USERAGENT, $_SERVER['HTTP_USER_AGENT']);
$res = curl_exec($ch);
curl_close($ch);
?>
```

Make sure it's accessible via pure http without cloudflare/etc. checks!

## Building
Use Eclipse IDE with MTJ and S40v5 sdk or NetBeans with j2me sdk 3.0. 
To build from command line, you need to compile all files with 1.3 java compability using 1.8 jdk, preverify them using `preverify.exe` from SDK and pack in JAR.

## Settings explaining (rus)
### Поведение кэширования
Если отключено полностью - при каждом переключении страницы она будет скачиваться заново, даже если была только что закрыта. Если включено кэширование - несколько предыдущих страниц будут держаться в памяти (быстрее перелистывание). При включенной предзагрузке приложение будет пытаться скачать все страницы как можно раньше.
### Загрузка обложек
На телефонах с малым количеством памяти её может не хватить даже на декодирование превьюшки - отключив их загрузку этими 2 пунктами, вы сможете "вслепую" добраться до кнопки скачивания.
### Запоминание списков
Если отключить, то нажатие кнопки "назад" в манге вернёт вас в главное меню, а не список новых/популярных/поиска. Тоже экономит память, если отключить
### Декодирование JPEG единожды
Если отключить, картинка будет при каждом ремасштабировании заново декодироваться. Это немного уменьшит потребление памяти при сворачивании приложения.
### Предзагрузка URL
Если включено, ускорит перелистывание почти на 50% - URL изображений запросится заранее.

### Оптимальные
Настройка|Экономия памяти|Быстродействие и удобство
---|---|---
Поведение кэширования|`Отключено`|`Предзагружать`
Загружать обложку на странице|`Нет`|`Да`
Загружать обложку в списках|`Нет`|`Да`
Запоминать списки при открытии страницы|`Нет`|`Да`
Декодировать JPEG единожды|Не влияет|`Да`
Предзагружать URL страниц|`Нет`|`Да`
