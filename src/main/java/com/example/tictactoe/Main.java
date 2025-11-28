package com.example.tictactoe;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Tiny HTTP server using JDK HttpServer.
 * Serves files from ./static and provides a small JSON API for the Game.
 *
 * Updated to support PORT environment variable (required by Render).
 */
public class Main {

    // NEW: Render supplies PORT as an environment variable.
    private static int getPort() {
        String p = System.getenv("PORT");
        if (p != null && !p.isBlank()) {
            try {
                return Integer.parseInt(p);
            } catch (NumberFormatException ignored) {
            }
        }
        return 8000; // fallback default for local development
    }

    private static final File STATIC_DIR = new File("static");
    private static final Game game = new Game();

    public static void main(String[] args) throws Exception {

        int port = getPort(); // NEW
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API endpoints
        server.createContext("/api/state", Main::handleState);
        server.createContext("/api/move", Main::handleMove);
        server.createContext("/api/reset", Main::handleReset);

        // static frontend files
        server.createContext("/", Main::handleStatic);

        server.setExecutor(null);
        System.out.println("Server running at http://localhost:" + port);
        server.start();
    }

    private static void handleState(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "Method Not Allowed");
            return;
        }
        sendJson(exchange, 200, stateJson());
    }

    private static void handleMove(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "Method Not Allowed");
            return;
        }
        String body = readAll(exchange.getRequestBody());
        int pos = parsePos(body);
        if (pos < 0) {
            sendJson(exchange, 400,
                    "{\"ok\":false,\"message\":\"Invalid JSON payload (expected { \\\"pos\\\": 1..9 })\"}");
            return;
        }

        Game.MoveResult res = game.makeMove(pos);
        String json = "{ \"ok\": " + res.ok + ", \"message\": \"" + escapeJson(res.message)
                + "\", \"state\": " + stateJson() + " }";

        sendJson(exchange, res.ok ? 200 : 400, json);
    }

    private static void handleReset(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "Method Not Allowed");
            return;
        }
        game.reset();
        sendJson(exchange, 200, "{ \"ok\": true, \"message\": \"Reset\", \"state\": " + stateJson() + " }");
    }

    private static void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path))
            path = "/index.html";

        File file = new File(STATIC_DIR, path);
        if (!file.exists() || !file.getCanonicalPath().startsWith(STATIC_DIR.getCanonicalPath())) {
            send(exchange, 404, "Not Found");
            return;
        }

        String contentType = contentTypeFor(file.getName());
        byte[] data = java.nio.file.Files.readAllBytes(file.toPath());

        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(200, data.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    // -------------------------------------------------------
    // Utility / helper routines
    // -------------------------------------------------------

    private static String readAll(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            return sb.toString();
        }
    }

    private static int parsePos(String body) {
        if (body == null)
            return -1;
        body = body.trim();
        int idx = body.indexOf("pos");
        if (idx < 0)
            return -1;
        int colon = body.indexOf(":", idx);
        if (colon < 0)
            return -1;

        String rest = body.substring(colon + 1).trim();
        int i = 0;
        while (i < rest.length() && !Character.isDigit(rest.charAt(i)))
            i++;
        if (i >= rest.length())
            return -1;

        int j = i;
        while (j < rest.length() && Character.isDigit(rest.charAt(j)))
            j++;

        try {
            return Integer.parseInt(rest.substring(i, j));
        } catch (Exception e) {
            return -1;
        }
    }

    private static void send(HttpExchange exchange, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String contentTypeFor(String name) {
        if (name.endsWith(".html"))
            return "text/html";
        if (name.endsWith(".css"))
            return "text/css";
        if (name.endsWith(".js"))
            return "application/javascript";
        return "application/octet-stream";
    }

    private static String stateJson() {
        char[] board = game.getBoardSnapshot();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"board\":[");
        for (int i = 0; i < board.length; i++) {
            sb.append("\"").append(board[i]).append("\"");
            if (i < board.length - 1)
                sb.append(",");
        }
        sb.append("],");
        sb.append("\"turn\":\"").append(game.getTurn()).append("\",");
        sb.append("\"winner\":").append(game.getWinner() == null ? "null" : "\"" + game.getWinner() + "\"");
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }
}
