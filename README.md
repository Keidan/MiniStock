# MiniStock
[![Build Status](https://github.com/Keidan/MiniStock/actions/workflows/build.yml/badge.svg)][build]
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)][license][![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=bugs)][sonarcloud]
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=code_smells)][sonarcloud]
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=duplicated_lines_density)][sonarcloud]
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=vulnerabilities)][sonarcloud]
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=sqale_rating)][sonarcloud]
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=reliability_rating)][sonarcloud]
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=security_rating)][sonarcloud]
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=sqale_index)][sonarcloud]
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=ncloc)][sonarcloud]

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
* Create the SQL table (see [table.sql][table_sql])
* Don't forget to create the php fields:
	* `define('DATABASE_ANDROID_PROVIDER_USERNAME', 'your username');`
	* `define('DATABASE_ANDROID_PROVIDER_PASSWORD', 'your user password');`
	* `define('DATABASE_MINISTOCK_TABLE', 'ministock');`
	* `define('DATABASE_MINISTOCK_COL_ID', 'id');`
	* `define('DATABASE_MINISTOCK_COL_VERSION', 'version');`
	* `define('DATABASE_MINISTOCK_COL_TITLE', 'title');`
	* `define('DATABASE_MINISTOCK_COL_IMAGE', 'image');`
	* `define('DATABASE_MINISTOCK_COL_ITEMS', 'items');`
* Edit the File: [strings.xml][strings_xml] and replace `YOUR_DEFAULT_USER_XXXX` by your settings
	* `YOUR_DEFAULT_USER_NAME` -> Must match with `DATABASE_ANDROID_PROVIDER_USERNAME` of your php configuration
	* `YOUR_DEFAULT_USER_PASSWORD` -> Must match with `DATABASE_ANDROID_PROVIDER_PASSWORD` of your php configuration
* Also remember to modify the other default fields of the [strings.xml][strings_xml] file:
	* default_protocol -> HTTP or HTTPS
	* default_host -> The web address, eg: www.your_site.com
	* default_port -> The web port, eg: 80, 443, etc...
	* default_page -> The page to use, eg: /mini-stock-provider/

## License

[GPLv3][license]

[build]: https://github.com/Keidan/MiniStock/actions
[table_sql]: https://github.com/Keidan/MiniStock/blob/master/php/table.sql
[strings_xml]: https://github.com/Keidan/MiniStock/blob/master/app/src/main/res/values/strings.xml
[license]: https://github.com/Keidan/MiniStock/blob/master/license.txt
[sonarcloud]: https://sonarcloud.io/summary/new_code?id=Keidan_MiniStock