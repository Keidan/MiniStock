<?php

/**
 * Template Name: MiniStockProvider
 * Description: Build the MiniStock provider page for android device
 */
//ini_set('display_errors',1);
include_once($_SERVER["DOCUMENT_ROOT"] . "/wp-includes/ralala-class/__web_access_configuration__.php");

function isValidJSON($str)
{
  json_decode($str);
  return json_last_error() == JSON_ERROR_NONE;
}

function response($code, $result, $desc)
{
  http_response_code($code);
  return "{\"code\": " . $code . ", \"result\": \"" . $result . "\", \"description\": \"" . $desc . "\"}";
}

function endsWith($haystack, $needle)
{
  $length = strlen($needle);
  if ($length == 0)
  {
    return true;
  }
  return (substr($haystack, -$length) === $needle);
}

function extractItems($valItems)
{
  $items = "";
  foreach ($valItems as $item)
  {
    $items .= "{\"version\": \"" . $item['version'] . "\", \"date\": \"" . $item['date'] . "\"},";
  }
  if (endsWith($items, ","))
  {
    $items = substr($items, 0, strlen($items) - 1);
  }
  return $items;
}

$json_params = file_get_contents("php://input");
$json_params = preg_replace('/[[:cntrl:]]/', '', $json_params);
if (strlen($json_params) > 0 && isValidJSON($json_params))
{
  $json_data = json_decode($json_params, true); /* When TRUE, returned objects will be converted into associative arrays. */
  $user = $json_data["user"];
  $pwd = $json_data["pwd"];
  $action = $json_data["action"];
  $table = $json_data["table"];
  $rangeMin = 0;
  $rangeCount = 0;
  if (isset($json_data['rangeMin']))
  {
    $rangeMin = intval($json_data['rangeMin']);
  }
  if (isset($json_data['rangeCount']))
  {
    $rangeCount = intval($json_data['rangeCount']);
  }

  $colId = DATABASE_MINISTOCK_COL_ID;
  $colVersion = DATABASE_MINISTOCK_COL_VERSION;
  $colTitle = DATABASE_MINISTOCK_COL_TITLE;
  $colImage = DATABASE_MINISTOCK_COL_IMAGE;
  $colItems = DATABASE_MINISTOCK_COL_ITEMS;

  if (empty($user) || empty($pwd) || empty($action) || empty($table))
  {
    echo response(403, "error", "You have attempted to access a resource for which you do not have the proper authorization or which is not available from your location (1).");
  }
  else
  {
    if (strcmp($user, DATABASE_ANDROID_PROVIDER_USERNAME) == 0 && strcmp($pwd, md5(DATABASE_ANDROID_PROVIDER_PASSWORD)) == 0)
    {
      $mysqli = @new mysqli(DATABASE_HOST, DATABASE_USER, DATABASE_PASSWORD, DATABASE_NAME);
      if ($mysqli->connect_errno)
      {
        echo response(501, "error", "Failed to connect to the server: " . $mysqli->connect_errno);
      }
      else
      {
        if (strcmp($table, DATABASE_MINISTOCK_TABLE) == 0)
        {
          if (strcmp($action, "INSERT") == 0)
          {
            $valId = $json_data[$colId];
            $valVersion = $json_data[$colVersion];
            $valTitle = addslashes($json_data[$colTitle]);
            $valImage = $json_data[$colImage];
            $items = extractItems($json_data[$colItems]);
            $cursor = $mysqli->query("INSERT INTO `$table` (`$colId`, `$colVersion`, `$colTitle`, `$colImage`, `$colItems`) VALUES ('$valId', '$valVersion', '$valTitle', '$valImage', '$items')");
            if (!$cursor)
            {
              echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
            }
            else
            {
              echo response(200, "success", "success");
            }
          }
          else if (strcmp($action, "UPDATE_WITH_TITLE") == 0)
          {
            $valTitle = addslashes($json_data[$colTitle]);
            $valVersion = $json_data[$colVersion];
            $valImage = $json_data[$colImage];
            $items = extractItems($json_data[$colItems]);
            $cursor = $mysqli->query("UPDATE `$table` SET `$colVersion` = '$valVersion', `$colTitle` = '$valTitle', `$colImage` = '$valImage', `$colItems` = '$items' WHERE `$colTitle` = '$valTitle'");
            if (!$cursor)
            {
              echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
            }
            else
            {
              echo response(200, "success", "success");
            }
          }
          else if (strcmp($action, "UPDATE_WITH_ID") == 0)
          {
            $valId = $json_data[$colId];
            $valVersion = $json_data[$colVersion];
            $valTitle = addslashes($json_data[$colTitle]);
            $valImage = $json_data[$colImage];
            $items = extractItems($json_data[$colItems]);
            $cursor = $mysqli->query("UPDATE `$table` SET `$colVersion` = '$valVersion', `$colTitle` = '$valTitle', `$colImage` = '$valImage', `$colItems` = '$items' WHERE `$colId` = '$valId'");
            if (!$cursor)
            {
              echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
            }
            else
            {
              echo response(200, "success", "success");
            }
          }
          else if (strcmp($action, "DELETE") == 0)
          {
            $valId = $json_data[$colId];
            $cursor = $mysqli->query("DELETE FROM `$table` WHERE `$colId` = " . "'$valId'");
            if (!$cursor)
            {
              echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
            }
            else
            {
              echo response(200, "success", "success");
            }
          }
          else if (strcmp($action, "FIND") == 0)
          {
            $title = addslashes($json_data[$colTitle]);
            $cursor = $mysqli->query("SELECT * FROM `$table` WHERE `$colTitle` = '$title' LIMIT 1");
            if (!$cursor)
            {
              echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
            }
            else
            {
              $index = 0;
              $items = "";
              while ($data = $cursor->fetch_assoc())
              {
                $items .= "\"item" . $index . "\": {\"$colId\": \"" . $data[$colId] .
                  "\", \"$colVersion\": \"" . $data[$colVersion] .
                  "\", \"$colTitle\": \"" . $data[$colTitle] .
                  "\", \"$colImage\": \"" . $data[$colImage] .
                  "\", \"$colItems\": [" . $data[$colItems] . "]},";
                $index++;
              }
              mysqli_free_result($cursor);
              if (endsWith($items, ","))
              {
                $items = substr($items, 0, strlen($items) - 1);
              }
              $ret = '{"code": 200, "result": "success",' . $items;
              if (endsWith($ret, ","))
              {
                $ret = substr($ret, 0, strlen($ret) - 1);
              }
              $ret .= '}';
              http_response_code(200);
              echo $ret;
            }
          }
          else if (strcmp($action, "LIST") == 0)
          {
            if ($rangeMin > 0 && $rangeCount > 0)
            {
              $cursor = $mysqli->query("SELECT * FROM `$table` LIMIT $rangeMin, $rangeCount");
            }
            else
            {
              $cursor = $mysqli->query("SELECT * FROM `$table`");
            }
            if (!$cursor)
            {
              echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
            }
            else
            {
              $index = $rangeMin;
              $items = "";
              while ($data = $cursor->fetch_assoc())
              {
                $items .= "\"item" . $index . "\": {\"$colId\": \"" . $data[$colId] .
                  "\", \"$colVersion\": \"" . $data[$colVersion] .
                  "\", \"$colTitle\": \"" . $data[$colTitle] .
                  "\", \"$colImage\": \"" . $data[$colImage] .
                  "\", \"$colItems\": [" . $data[$colItems] . "]},";
                $index++;
              }
              mysqli_free_result($cursor);
              if (endsWith($items, ","))
              {
                $items = substr($items, 0, strlen($items) - 1);
              }
              $ret = '{"code": 200, "result": "success",' . $items;
              if (endsWith($ret, ","))
              {
                $ret = substr($ret, 0, strlen($ret) - 1);
              }
              $ret .= '}';
              http_response_code(200);
              echo $ret;
            }
          }
          else if (strcmp($action, "COUNT") == 0)
          {
            $cursor = $mysqli->query("SELECT COUNT(*) as total FROM `$table`");
            if (!$cursor)
            {
              echo response(501, "error", "Unable to execute the SQL request: " . $mysqli->error);
            }
            else
            {
              $data = $cursor->fetch_assoc();
              $total = $data["total"];
              mysqli_free_result($cursor);
              $ret = '{"code": 200, "result": "success", "count": ' . $total . "}";
              http_response_code(200);
              echo $ret;
            }
          }
          else
          {
            echo response(405, "error", "The request method is not supported for the requested.");
          }
        }
        else
        {
          echo response(403, "error", "You have attempted to access a resource for which you do not have the proper authorization or which is not available from your location (2).");
        }
        $mysqli->close();
      }
    }
    else
    {
      echo response(403, "error", "You have attempted to access a resource for which you do not have the proper authorization or which is not available from your location (3).");
    }
  }
}
else
{
  $s = "JSON error: ";
  switch (json_last_error())
  {
    case JSON_ERROR_DEPTH:
      $s .= "The maximum stack depth has been exceeded";
      break;
    case JSON_ERROR_STATE_MISMATCH:
      $s .= "Invalid or malformed JSON";
      break;
    case JSON_ERROR_CTRL_CHAR:
      $s .= "Control character error, possibly incorrectly encoded";
      break;
    case JSON_ERROR_SYNTAX:
      $s .= "Syntax error";
      break;
    case JSON_ERROR_UTF8:
      $s .= "Malformed UTF-8 characters, possibly incorrectly encoded";
      break;
    case JSON_ERROR_RECURSION:
      $s .= "One or more recursive references in the value to be encoded";
      break;
    case JSON_ERROR_INF_OR_NAN:
      $s .= "One or more NAN or INF values in the value to be encoded";
      break;
    case JSON_ERROR_UNSUPPORTED_TYPE:
      $s .= "A value of a type that cannot be encoded was given";
      break;
  }
  echo response(500, "error", $s);
}
