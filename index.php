
<?php
header("Content-Type: application/json; charset=UTF-8");

require_once __DIR__ . "/conexion.php";

$metodo = $_SERVER["REQUEST_METHOD"];
$uri = parse_url($_SERVER["REQUEST_URI"], PHP_URL_PATH);

// Eliminar el prefijo de la carpeta y el archivo index.php
$prefijo = "/denuncias_api";

if (strpos($uri, $prefijo) === 0) {
    $uri = substr($uri, strlen($prefijo));
}

if (strpos($uri, "/index.php") === 0) {
    $uri = substr($uri, strlen("/index.php"));
}

$uri = "/" . trim($uri, "/");
$partes = array_values(array_filter(explode("/", $uri)));

function responder($datos, $codigo = 200) {
    http_response_code($codigo);
    echo json_encode($datos, JSON_UNESCAPED_UNICODE);
    exit;
}

function leerJson() {
    $datos = json_decode(file_get_contents("php://input"), true);

    if (!is_array($datos)) {
        responder(["error" => "JSON inválido"], 400);
    }

    return $datos;
}

try {
    // POST: registrar una denuncia
    if (
        $metodo === "POST" &&
        count($partes) === 1 &&
        $partes[0] === "denuncias"
    ) {
        $datos = leerJson();

        $dni = trim($datos["dni"] ?? "");
        $fecha = $datos["fecha"] ?? "";
        $tipo = trim($datos["tipo"] ?? "");
        $direccion = trim($datos["direccion"] ?? "");
        $descripcion = trim($datos["descripcion"] ?? "");

        if (!preg_match('/^\d{8}$/', $dni)) {
            responder(["error" => "El DNI debe tener 8 dígitos"], 400);
        }

        if (!$fecha || !$tipo || !$direccion || !$descripcion) {
            responder(["error" => "Todos los campos son obligatorios"], 400);
        }

        if (
            strlen($tipo) > 30 ||
            strlen($direccion) > 200 ||
            strlen($descripcion) > 255
        ) {
            responder(["error" => "Uno o más campos exceden la longitud permitida"], 400);
        }

        $sql = "INSERT INTO denuncias
                (dni, fecha, tipo, direccion, descripcion, estado)
                VALUES (?, ?, ?, ?, ?, 1)";

        $stmt = $pdo->prepare($sql);
        $stmt->execute([
            $dni,
            $fecha,
            $tipo,
            $direccion,
            $descripcion
        ]);

        $id = (int) $pdo->lastInsertId();

        $stmt = $pdo->prepare("SELECT * FROM denuncias WHERE id = ?");
        $stmt->execute([$id]);

        responder($stmt->fetch(), 201);
    }

    // GET: listar denuncias con paginación
    if (
        $metodo === "GET" &&
        count($partes) === 1 &&
        $partes[0] === "denuncias"
    ) {
        $offset = filter_var($_GET["offset"] ?? 0, FILTER_VALIDATE_INT);
        $limit = filter_var($_GET["limit"] ?? 5, FILTER_VALIDATE_INT);

        if (
            $offset === false ||
            $offset < 0 ||
            $limit === false ||
            $limit < 1 ||
            $limit > 100
        ) {
            responder(["error" => "Offset o limit inválido"], 400);
        }

        $stmt = $pdo->prepare(
            "SELECT * FROM denuncias ORDER BY id DESC LIMIT ? OFFSET ?"
        );

        $stmt->bindValue(1, $limit, PDO::PARAM_INT);
        $stmt->bindValue(2, $offset, PDO::PARAM_INT);
        $stmt->execute();

        responder($stmt->fetchAll());
    }

    // GET: consultar denuncias de un ciudadano por DNI
    if (
        $metodo === "GET" &&
        count($partes) === 3 &&
        $partes[0] === "denuncias" &&
        $partes[1] === "ciudadano"
    ) {
        $dni = $partes[2];

        if (!preg_match('/^\d{8}$/', $dni)) {
            responder(["error" => "El DNI debe tener 8 dígitos"], 400);
        }

        $stmt = $pdo->prepare(
            "SELECT * FROM denuncias WHERE dni = ? ORDER BY id DESC"
        );

        $stmt->execute([$dni]);

        responder($stmt->fetchAll());
    }

    // GET: consultar una denuncia por ID
    if (
        $metodo === "GET" &&
        count($partes) === 2 &&
        $partes[0] === "denuncias" &&
        ctype_digit($partes[1])
    ) {
        $id = (int) $partes[1];

        $stmt = $pdo->prepare("SELECT * FROM denuncias WHERE id = ?");
        $stmt->execute([$id]);

        $denuncia = $stmt->fetch();

        if (!$denuncia) {
            responder(["error" => "Denuncia no encontrada"], 404);
        }

        responder($denuncia);
    }

    // PUT: marcar una denuncia como atendida
    if (
        $metodo === "PUT" &&
        count($partes) === 2 &&
        $partes[0] === "denuncias" &&
        ctype_digit($partes[1])
    ) {
        $id = (int) $partes[1];

        $stmt = $pdo->prepare("SELECT * FROM denuncias WHERE id = ?");
        $stmt->execute([$id]);

        if (!$stmt->fetch()) {
            responder(["error" => "Denuncia no encontrada"], 404);
        }

        $stmt = $pdo->prepare(
            "UPDATE denuncias SET estado = 0 WHERE id = ?"
        );

        $stmt->execute([$id]);

        $stmt = $pdo->prepare("SELECT * FROM denuncias WHERE id = ?");
        $stmt->execute([$id]);

        responder($stmt->fetch());
    }

    // Si ninguna ruta coincide
    responder([
        "error" => "Ruta no encontrada",
        "metodo_recibido" => $metodo,
        "ruta_recibida" => $uri,
        "partes_recibidas" => $partes
    ], 404);

} catch (PDOException $e) {
    responder(["error" => "Error interno de base de datos"], 500);
}
