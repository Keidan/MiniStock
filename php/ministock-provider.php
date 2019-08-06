<?php
/**
 * Template Name: MiniStockProvider
 * Description: Build the MiniStock provider page for android device
 */
//ini_set('display_errors',1);

function isValidJSON($str) {
	json_decode($str);
	return json_last_error() == JSON_ERROR_NONE;
}

function response($code, $result, $desc) {
	http_response_code($code);
	return "{\"code\": " . $code . ", \"result\": \"" . $result . "\", \"description\": \"" . $desc . "\"}";
}

function endsWith($haystack, $needle){
    $length = strlen($needle);
    if ($length == 0) {
        return true;
    }
    return (substr($haystack, -$length) === $needle);
}

$json_params = file_get_contents("php://input");
$json_params = preg_replace('/[[:cntrl:]]/', '', $json_params);
if (strlen($json_params) > 0 && isValidJSON($json_params)) {
	$json_data = json_decode($json_params, true); /* When TRUE, returned objects will be converted into associative arrays. */
	$user = $json_data["user"];
	$pwd = $json_data["pwd"];
	$action = $json_data["action"];
	$table = $json_data["table"];
	$colId = DATABASE_MINISTOCK_COL_ID;
	$colTitle = DATABASE_MINISTOCK_COL_TITLE;
	$colImage = DATABASE_MINISTOCK_COL_IMAGE;
	$colCount = DATABASE_MINISTOCK_COL_COUNT;
	$colQrCode = DATABASE_MINISTOCK_COL_QRCODEID;
	if(empty($user) || empty($pwd) || empty($action) || empty($table)) {
		echo response(403, "error", "You have attempted to access a resource for which you do not have the proper authorization or which is not available from your location.");
	} else {
		if(strcmp($user, DATABASE_ANDROID_PROVIDER_USERNAME) == 0 && strcmp($pwd, md5(DATABASE_ANDROID_PROVIDER_PASSWORD)) == 0) {
			$mysqli = @new mysqli(DATABASE_HOST, DATABASE_USER, DATABASE_PASSWORD, DATABASE_NAME);
			if($mysqli->connect_errno) {
				echo response(501, "error", "Failed to connect to the server: " . $mysqli->connect_errno);
			} else {
				if(strcmp($table, DATABASE_MINISTOCK_TABLE) == 0) {
					if(strcmp($action, "INSERT") == 0) {
						$valId = $json_data[DATABASE_MINISTOCK_COL_ID];
						$valTitle = addslashes($json_data[DATABASE_MINISTOCK_COL_TITLE]);
						$valImage = $json_data[DATABASE_MINISTOCK_COL_IMAGE];
						$valCount = $json_data[DATABASE_MINISTOCK_COL_COUNT];
						$valQrCode = $json_data[DATABASE_MINISTOCK_COL_QRCODEID];
						$cursor = $mysqli->query("INSERT INTO `$table` (`$colId`, `$colTitle`, `$colImage`, `$colCount`, `$colQrCode`) VALUES ('$valId', '$valTitle', '$valImage', $valCount, '$valQrCode')");
						if(!$cursor) {
							echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
						} else {
							echo response(200, "success", "success");
						}
					} else if(strcmp($action, "UPDATE_WITH_TITLE") == 0) {
						$valTitle = addslashes($json_data[DATABASE_MINISTOCK_COL_TITLE]);
						$valImage = $json_data[DATABASE_MINISTOCK_COL_IMAGE];
						$valCount = $json_data[DATABASE_MINISTOCK_COL_COUNT];
						$valQrCode = $json_data[DATABASE_MINISTOCK_COL_QRCODEID];
						$cursor = $mysqli->query("UPDATE `$table` SET `$colTitle` = '$valTitle', `$colImage` = '$valImage', `$colCount` = $valCount, `$colQrCode` = '$valQrCode' WHERE `$colTitle` = '$valTitle'");
						if(!$cursor) {
							echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
						} else {
							echo response(200, "success", "success");
						}
					} else if(strcmp($action, "UPDATE_WITH_ID") == 0) {
						$valId = $json_data[DATABASE_MINISTOCK_COL_ID];
						$valTitle = addslashes($json_data[DATABASE_MINISTOCK_COL_TITLE]);
						$valImage = $json_data[DATABASE_MINISTOCK_COL_IMAGE];
						$valCount = $json_data[DATABASE_MINISTOCK_COL_COUNT];
						$valQrCode = $json_data[DATABASE_MINISTOCK_COL_QRCODEID];
						$cursor = $mysqli->query("UPDATE `$table` SET `$colTitle` = '$valTitle', `$colImage` = '$valImage', `$colCount` = $valCount, `$colQrCode` = '$valQrCode' WHERE `$colId` = '$valId'");
						if(!$cursor) {
							echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
						} else {
							echo response(200, "success", "success");
						}
					} else if(strcmp($action, "DELETE") == 0) {
						$cursor = $mysqli->query("DELETE FROM `$table` WHERE `$colId` = " . "'" . $json_data[DATABASE_MINISTOCK_COL_ID] . "'");
						if(!$cursor) {
							echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
						} else {
							echo response(200, "success", "success");
						}
					} else if(strcmp($action, "FIND") == 0) {
						$title = addslashes($json_data[DATABASE_MINISTOCK_COL_TITLE]);
						$cursor = $mysqli->query("SELECT * FROM `$table` WHERE `$colTitle` = '$title' LIMIT 1");
						if(!$cursor) {
							echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
						} else {
							$index = 0;
							$items = "";
							while($data = $cursor->fetch_assoc()) {
								$items .= "\"item" . $index . "\": {\"$colId\": \"".$data[DATABASE_MINISTOCK_COL_ID]. 
								"\", \"$colTitle\": \"".$data[DATABASE_MINISTOCK_COL_TITLE] . 
								"\", \"$colImage\": \"".$data[DATABASE_MINISTOCK_COL_IMAGE].
								"\", \"$colCount\": ".$data[DATABASE_MINISTOCK_COL_COUNT] . 
								", \"$colQrCode\": \"".$data[DATABASE_MINISTOCK_COL_QRCODEID] ."\"},";
								$index++;
							}
							mysqli_free_result($cursor);
							if(endsWith($items, ","))
								$items = substr($items, 0, strlen($items) - 1);
							$ret = '{"code": 200, "result": "success",' . $items;
							if(endsWith($ret, ","))
								$ret = substr($ret, 0, strlen($ret) - 1);
							$ret .= '}';
							http_response_code(200);
							echo $ret;
						}
					} else if(strcmp($action, "LIST") == 0) {
						$cursor = $mysqli->query("SELECT * FROM `$table`");
						if(!$cursor) {
							echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
						} else {
							$index = 0;
							$items = "";
							
							while($data = $cursor->fetch_assoc()) {
								$items .= "\"item" . $index . "\": {\"$colId\": \"".$data[DATABASE_MINISTOCK_COL_ID]. 
								"\", \"$colTitle\": \"".$data[DATABASE_MINISTOCK_COL_TITLE] . 
								"\", \"$colImage\": \"".$data[DATABASE_MINISTOCK_COL_IMAGE].
								"\", \"$colCount\": ".$data[DATABASE_MINISTOCK_COL_COUNT] . 
								", \"$colQrCode\": \"".$data[DATABASE_MINISTOCK_COL_QRCODEID] ."\"},";
								$index++;
							}
							mysqli_free_result($cursor);
							if(endsWith($items, ","))
								$items = substr($items, 0, strlen($items) - 1);
							$ret = '{"code": 200, "result": "success",' . $items;
							if(endsWith($ret, ","))
								$ret = substr($ret, 0, strlen($ret) - 1);
							$ret .= '}';
							http_response_code(200);
							echo $ret;
						}
					} else {
						echo response(405, "error", "The request method is not supported for the requested.");
					}	
				} else {
					echo response(403, "error", "You have attempted to access a resource for which you do not have the proper authorization or which is not available from your location." . "user:'$user', pwd:'$pwd', action: '$action', table:'$table'");
				}	
				$mysqli->close();
			}
		} else {
			echo response(403, "error", "You have attempted to access a resource for which you do not have the proper authorization or which is not available from your location.");
		}
	}
} else {
	$s = "JSON error: ";
	switch(json_last_error()) {
		case JSON_ERROR_DEPTH: $s .= "The maximum stack depth has been exceeded"; break;
		case JSON_ERROR_STATE_MISMATCH: $s .= "Invalid or malformed JSON"; break; 
		case JSON_ERROR_CTRL_CHAR: $s .= "Control character error, possibly incorrectly encoded"; break;
		case JSON_ERROR_SYNTAX: $s .= "Syntax error"; break;
		case JSON_ERROR_UTF8: $s .= "Malformed UTF-8 characters, possibly incorrectly encoded"; break;
		case JSON_ERROR_RECURSION: $s .= "One or more recursive references in the value to be encoded"; break;
		case JSON_ERROR_INF_OR_NAN: $s .= "One or more NAN or INF values in the value to be encoded"; break;
		case JSON_ERROR_UNSUPPORTED_TYPE: $s .= "A value of a type that cannot be encoded was given"; break;
	}
	echo response(500, "error", $s);
}
?>
