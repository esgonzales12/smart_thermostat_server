package org.estefan.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.estefan.dao.TempProgrammingDao;
import org.estefan.dao.TempProgrammingRecord;
import org.estefan.domain.DeviceRequest;
import org.estefan.domain.DeviceResponse;
import org.estefan.domain.ErrorResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DeviceRequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        OutputStream responseBody = exchange.getResponseBody();

        if (!exchange.getRequestMethod().equals("POST")) {
            throw new UnsupportedOperationException();
        }

        DeviceRequest req = new Gson().fromJson(
                new InputStreamReader(requestBody),
                DeviceRequest.class);

        Integer deviceId = req.getDeviceId();
        Double currentTemp = Double.valueOf(req.getTemp());
        Instant current = Instant.now();

        List<TempProgrammingRecord> deviceRecords = TempProgrammingDao.getInstance().findByDeviceId(deviceId);

        deviceRecords.sort((o1, o2) -> {
            Instant i1 = Instant.parse(o1.getDatetime());
            Instant i2 = Instant.parse(o2.getDatetime());
            return i1.compareTo(i2);
        });

        int i = 0;
        Integer on;

        while (i < deviceRecords.size() &&
                current.isBefore(Instant.parse(deviceRecords.get(i).getDatetime()))) {
            i++;
        }

        // no current temp program pending
        if (i >= deviceRecords.size()) {
            on = currentTemp > 20 ? 0 : 1;
        } else {
            TempProgrammingRecord soonest = deviceRecords.get(i);
            Instant progTime = Instant.parse(soonest.getDatetime());
            Double desiredTemp = Double.valueOf(soonest.getTemp());

            // program is an hour out, just default to room temp
            if (current.plus(1, ChronoUnit.HOURS).isBefore(progTime)) {
                on = currentTemp > 20 ? 0 : 1;
            } else {
                on = currentTemp > desiredTemp ? 0 : 1;
            }
        }

        String json = new Gson().toJson(new DeviceResponse(on));
        exchange.sendResponseHeaders(200, json.length());
        responseBody.write(json.getBytes());
        requestBody.close();
        responseBody.close();
    }
}
