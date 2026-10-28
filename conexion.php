<?php
$host = "127.0.0.1";
$puerto = 3306;
$dbname = "denuncias_db";
$usuario = "root";
$contrasena = "";

try {
    $pdo = new PDO(
        "mysql:host=$host;port=$puerto;dbname=$dbname;charset=utf8mb4",
        $usuario,
        $contrasena,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
        ]
    );
} catch (PDOException $e) {
    http_response_code(500);
    header("Content-Type: application/json; charset=UTF-8");

    // Mostrar el error real temporalmente para diagnosticar
    echo json_encode([
        "error" => "Error al conectar con la base de datos",
        "detalle" => $e->getMessage()
    ]);

    exit;
}