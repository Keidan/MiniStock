# MiniStock
[![Build Status](https://img.shields.io/travis/Keidan/MiniStock/master.svg?style=plastic)](https://travis-ci.org/Keidan/MiniStock)
[![GitHub license](https://img.shields.io/github/license/Keidan/MiniStock.svg?style=plastic)](https://github.com/Keidan/MiniStock/blob/master/license.txt)


(GPL) Android MiniStock is a FREE software.

This application allows you to manage a small stock by saving the data on an external database (via php).


## Instructions


download the software :

	mkdir devel
	cd devel
	git clone git@github.com:Keidan/MiniStock
	cd MiniStock
 	Use with android studio

	
## Dropbox

:warning: For the application to work properly, please make the following changes:
* Create the SQL table (see [table.sql](https://github.com/Keidan/MiniStock/blob/master/php/table.sql))
* Don't forget to create the php fields:
	* `define('DATABASE_ANDROID_PROVIDER_USERNAME', 'your username');`
	* `define('DATABASE_ANDROID_PROVIDER_PASSWORD', 'your user password');`
	* `define('DATABASE_MINISTOCK_TABLE', 'ministock');`
	* `define('DATABASE_MINISTOCK_COL_ID', 'id');`
	* `define('DATABASE_MINISTOCK_COL_TITLE', 'title');`
	* `define('DATABASE_MINISTOCK_COL_IMAGE', 'image');`
	* `define('DATABASE_MINISTOCK_COL_COUNT', 'count');`
	* `define('DATABASE_MINISTOCK_COL_QRCODEID', 'qrCodeId');`
* Edit the File: [strings.xml](https://github.com/Keidan/MiniStock/blob/master/app/src/main/res/values/strings.xml) and replace `YOUR_DEFAULT_USER_XXXX` by your settings
	* `YOUR_DEFAULT_USER_NAME` -> Must match with `DATABASE_ANDROID_PROVIDER_USERNAME` of your php configuration
	* `YOUR_DEFAULT_USER_PASSWORD` -> Must match with `DATABASE_ANDROID_PROVIDER_PASSWORD` of your php configuration
* Also remember to modify the other default fields of the [strings.xml](https://github.com/Keidan/MiniStock/blob/master/app/src/main/res/values/strings.xml) file:
	* default_protocol -> HTTP or HTTPS
	* default_host -> The web address, eg: www.your_site.com
	* default_port -> The web port, eg: 80, 443, etc...
	* default_page -> The page to use, eg: /mini-stock-provider/

## License

[GPLv3](https://github.com/Keidan/MiniStock/blob/master/license.txt)
