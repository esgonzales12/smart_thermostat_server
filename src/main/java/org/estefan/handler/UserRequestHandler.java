package org.estefan.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.estefan.dao.TempProgrammingDao;
import org.estefan.dao.TempProgrammingRecord;
import org.estefan.domain.ErrorResponse;
import org.estefan.domain.GetResponse;
import org.estefan.domain.TempProgRequest;
import org.estefan.domain.TempProgResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Optional;

public class UserRequestHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET" -> handleGetRequest(exchange);
            case "POST" -> handleTempProgrammingRequest(exchange);
            case "DELETE" -> handleDeleteRequest(exchange);
            default -> sendErrorResponse(400, "Unsupported method", exchange);
        }
    }

    public void handleGetRequest(HttpExchange exchange) throws IOException {
        Integer param = parseParam(exchange.getRequestURI().toString());
        if (param == null) {
            sendErrorResponse(400, "Invalid URI or ID", exchange);
            return;
        }

        Optional<TempProgrammingRecord> recordOptional = TempProgrammingDao.getInstance().findByRecordId(param);

        if(recordOptional.isPresent()) {
            TempProgrammingRecord record = recordOptional.get();
            GetResponse response = new GetResponse();
            response.setDatetime(record.getDatetime());
            response.setDeviceId(record.getDeviceId());
            response.setTemp(record.getTemp());

            String json = new Gson().toJson(response);

            OutputStream responseBody = exchange.getResponseBody();
            exchange.sendResponseHeaders(200, json.length());
            responseBody.write(json.getBytes());
            exchange.getRequestBody().close();
            responseBody.close();
            return;
        }

        sendErrorResponse(404, "Unable to locate record", exchange);
    }

    public void handleDeleteRequest(HttpExchange exchange) throws IOException {
        Integer param = parseParam(exchange.getRequestURI().toString());
        if (param == null) {
            sendErrorResponse(400, "Invalid URI or ID", exchange);
            return;
        }

        boolean deleted = TempProgrammingDao.getInstance().deleteRecord(param);

        if (deleted) {
            exchange.sendResponseHeaders(200, -1);
            exchange.getRequestBody().close();
            exchange.getResponseBody().close();
            return;
        }

        sendErrorResponse(404, "Unable to locate or delete record", exchange);
    }

    public void handleTempProgrammingRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        TempProgRequest req = new Gson().fromJson(
                new InputStreamReader(requestBody),
                TempProgRequest.class);
        requestBody.close();

        Integer param = parseParam(exchange.getRequestURI().toString());

        if (param == null
                || req == null
                || req.getTemp() == null
                || req.getDatetime() == null) {
            sendErrorResponse(400, "Invalid URI or request body", exchange);
            return;
        }

        TempProgrammingRecord record = new TempProgrammingRecord();
        record.setTemp(req.getTemp());
        record.setDeviceId(param);
        record.setDatetime(req.getDatetime());

        TempProgrammingRecord savedRecord = TempProgrammingDao
                .getInstance()
                .saveRecord(record);

        if(savedRecord == null) {
            sendErrorResponse(500, "Error processing your request", exchange);
            return;
        }

        String json = new Gson().toJson(new TempProgResponse(savedRecord.getId()));
        exchange.sendResponseHeaders(200, json.length());
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(json.getBytes());
        responseBody.close();
    }

    public Integer parseParam(String uri) {
        String [] uriContents = uri.split("/");
        if (uriContents.length != 3) return null;
        String token = uriContents[2];
        if (!token.matches("^\\d+$")) return null;

        Integer deviceId = null;

        try {
            deviceId = Integer.parseInt(token);
        } catch (Exception ignored) {}

        return deviceId;
    }

    public void sendErrorResponse(int code, String message, HttpExchange exchange) throws IOException {
        String json = new Gson().toJson(new ErrorResponse(message));
        exchange.sendResponseHeaders(code, json.length());
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(json.getBytes());
        responseBody.close();
    }

    public void serverError(HttpExchange exchange) {
        try {
            sendErrorResponse(500, "Unknown server error", exchange);
        } catch (IOException ex) {
            System.out.println("Unable to send server error response");
            System.out.println(ex.getMessage());
        }
    }

}
