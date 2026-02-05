<?php

$hostname = "127.0.0.1";
$dbname = "CarAppDB";
$table_name = "health_records";
$user = "root";
$pass = "pas";


$pdo = new PDO("mysql:host=$hostname;dbname=$dbname", $user, $pass);
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
$stm = $pdo->exec("CREATE TABLE IF NOT EXISTS $table_name (
                        deviceID varchar(20),
                        date varchar(30),
                        bpm float,
                        o2InBlood float,
                        accelerationX float,
                        accelerationY float,
                        accelerationZ float
)");

//http://localhost/carapp.php?action=insert&trackingID=123&deviceID=123456&date=25_09_22&bpm=44&o2InBlood=60&accelerationX=2&accelerationY=2&accelerationZ=2
if($_SERVER["REQUEST_METHOD"]=="GET") {
    
    if ($_GET['action'] == "insert" && isset($_GET['deviceID']) && isset($_GET['date'])) {

        $device = $_GET['deviceID'];
        $date = $_GET['date'];

        if(isset($_GET['bpm'])) {
            $bpm = $_GET['bpm'];
        }
        if(isset($_GET['o2InBlood'])){
            $o2InBlood = $_GET['o2InBlood'];
        }
        if(isset($_GET['accelerationX'])){
            $accelerationX = $_GET['accelerationX'];
            $accelerationY = $_GET['accelerationY'];
            $accelerationZ = $_GET['accelerationZ'];
        }

        $insert = "
            INSERT INTO $table_name VALUES (
            \"$device\", 
            \"$date\", 
            $bpm,
            $o2InBlood,
            $accelerationX ,
            $accelerationY, 
            $accelerationZ)";
        $stm = $pdo->prepare($insert);
        $stm->execute();
        echo $insert;
    }
    elseif ($_GET['action'] == "query") {
        if (isset($_GET['all']) && $_GET['all']==true){

            $query = "SELECT * FROM $table_name";
            $stm = $pdo->query($query);
            $results = $stm->fetchAll(PDO::FETCH_ASSOC);
            header("Content-Type: application/json;");
            echo json_encode($results);
        } //http://localhost/carapp.php?action=query&only_date=2023-03-09  ->  2023-03-09T00:53:13.513
        elseif (isset($_GET['only_date'])){
            $only_date = $_GET['only_date'];
            $query = "SELECT * FROM $table_name";
            $stm = $pdo->query($query);
            $rows = $stm->fetchAll(PDO::FETCH_ASSOC);
            $results = array();
            foreach ($rows as $value){
                $only_date_from_DB = explode("T", $value["date"]);
                if ($only_date_from_DB[0] == $only_date){
                    array_push($results, $value);
                }
            }
            header("Content-Type: application/json;");
            echo json_encode($results);
        }
        elseif (isset($_GET['days']) && $_GET['days']==true){
            $query = "SELECT date FROM $table_name";
            $stm = $pdo->query($query);
            $rows = $stm->fetchAll(PDO::FETCH_ASSOC);
            $results = array();
            foreach ($rows as $value){
                $s = explode("T", implode($value))[0];
                array_push($results, strval($s));
            }
            header("Content-Type: application/json;");
            echo json_encode(array_values(array_unique($results)));
        }
    }
}
?>